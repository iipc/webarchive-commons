package org.archive.hadoop.cdx;

import java.util.ArrayList;

import junit.framework.TestCase;

public class SplitFileTest extends TestCase {

	public void testRead() {
		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add("one");
		tmp.add("two");
		tmp.add("three");
		String[] a = tmp.toArray(new String[]{});
		for(String s : a) {
			System.out.println(s);
		}
	}

}
