package org.archive.util.iterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SortedCompositeIteratorTest {

	@Test
	public void testHasNext() throws FileNotFoundException, IOException {
		
		File a = File.createTempFile("filea", null); 
		File b = File.createTempFile("fileb", null);
		
		PrintWriter apw = new PrintWriter(a);
		PrintWriter bpw = new PrintWriter(b);
		apw.println("1");
		apw.println("3");
		bpw.println("2");
		bpw.println("4");
		apw.close();
		bpw.close();
		BufferedReader abr = new BufferedReader(new FileReader(a));
		BufferedReader bbr = new BufferedReader(new FileReader(b));
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
