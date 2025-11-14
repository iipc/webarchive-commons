package org.archive.util.binsearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.archive.util.binsearch.impl.RandomAccessFileSeekableLineReaderFactory;
import org.archive.util.iterator.CloseableIterator;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SortedTextFileTest {

	private static String formatS(int i) {
		return String.format(Locale.ROOT, "%07d", i);
	}

	private void createFile(File target, int max) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter pw = new PrintWriter(target, UTF_8.name());
		for(int i = 0; i < max; i++) {
			pw.println(formatS(i));
		}
		pw.flush();
		pw.close();
	}

	@Test
	public void testGetRecordIteratorStringBoolean() throws IOException {
		File test = File.createTempFile("test", null);
		int max = 1000000;
		createFile(test,max);
		RandomAccessFileSeekableLineReaderFactory factory = 
			new RandomAccessFileSeekableLineReaderFactory(test);
		SortedTextFile ff = new SortedTextFile(factory);

//		String url = "http://home.us.archive.org/~brad/test.tmp";
//		HTTPSeekableLineReaderFactory httpFactory = new HTTPSeekableLineReaderFactory(url);
//		SortedTextFile ff = new SortedTextFile(httpFactory);
		
		checkFirst(ff,true,formatS(0),formatS(0));
		checkFirst(ff,false,formatS(0),formatS(0));
		checkFirst(ff,true,formatS(1),formatS(0));
		checkFirst(ff,true,formatS(2),formatS(1));
		checkFirst(ff,false,formatS(1),formatS(1));
		checkFirst(ff,false,formatS(12355),formatS(12355));
		checkFirst(ff,true,formatS(12355),formatS(12354));

		checkFirst(ff,true,formatS(max-1),formatS(max-2));
		checkFirst(ff,false,formatS(max-1),formatS(max-1));
		
		checkFirst(ff,false,formatS(max),null);
		checkFirst(ff,true,formatS(max),formatS(max-1));

		checkFirst(ff,true,formatS(max+1),formatS(max-1));
		checkFirst(ff,false,formatS(max+1),null);
//		test.delete();
	}

	private void checkFirst(SortedTextFile stf, boolean lt, String key, String want) throws IOException {
		CloseableIterator<String> itr = stf.getRecordIterator(key, lt);
		if(want == null) {
			assertFalse(itr.hasNext());
		} else {
			String got = itr.next();
			assertEquals(want,got);
		}
	}

}
