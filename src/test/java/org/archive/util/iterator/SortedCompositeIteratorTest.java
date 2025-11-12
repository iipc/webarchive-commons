package org.archive.util.iterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Comparator;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SortedCompositeIteratorTest {

	@Test
	public void testHasNext() throws FileNotFoundException, IOException {
		
		File a = File.createTempFile("filea", null); 
		File b = File.createTempFile("fileb", null);
		
		PrintWriter apw = new PrintWriter(a, UTF_8.name());
		PrintWriter bpw = new PrintWriter(b, UTF_8.name());
		apw.println("1");
		apw.println("3");
		bpw.println("2");
		bpw.println("4");
		apw.close();
		bpw.close();
		BufferedReader abr = new BufferedReader(new InputStreamReader(new FileInputStream(a), UTF_8));
		BufferedReader bbr = new BufferedReader(new InputStreamReader(new FileInputStream(b), UTF_8));
		SortedCompositeIterator<String> sci = new SortedCompositeIterator<String>(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
			
		});
		sci.addIterator(AbstractPeekableIterator.wrapReader(abr));
		sci.addIterator(AbstractPeekableIterator.wrapReader(bbr));
		assertTrue(sci.hasNext());
		assertEquals("1",sci.next());
		assertTrue(sci.hasNext());
		assertEquals("2",sci.next());
		assertTrue(sci.hasNext());
		assertEquals("3",sci.next());
		assertTrue(sci.hasNext());
		assertEquals("4",sci.next());
		a.delete();
		b.delete();
	}

}
