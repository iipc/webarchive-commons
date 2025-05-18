package org.archive.util;

import org.archive.util.StringFieldExtractor.StringTuple;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringFieldExtractorTest {

	@Test
	public void testExtract() {
		StringFieldExtractor ex1 = new StringFieldExtractor(' ', 0);
		StringFieldExtractor ex2 = new StringFieldExtractor(' ', 1);
		StringFieldExtractor ex3 = new StringFieldExtractor(' ', 2);
		StringFieldExtractor ex4 = new StringFieldExtractor(' ', 3);
		StringFieldExtractor ex5 = new StringFieldExtractor(' ', 4);
		assertEquals("1",ex1.extract("1 2 3 4"));
		assertEquals("2",ex2.extract("1 2 3 4"));
		assertEquals("3",ex3.extract("1 2 3 4"));
		assertEquals("4",ex4.extract("1 2 3 4"));
		assertEquals(null,ex5.extract("1 2 3 4"));
		assertEquals("",ex5.extract("1 2 3 4 "));
		assertEquals("",ex1.extract(" 1 2 3 4 "));
		assertEquals("1",ex2.extract(" 1 2 3 4 "));
		assertEquals("2",ex3.extract(" 1 2 3 4 "));
		assertEquals("abc",ex1.extract("abc 1 2 3 4 "));
		assertEquals("1",ex2.extract("abc 1 2 3 4 "));
	}

	private void checkSplit(String f, String s,StringTuple t) {
		assertEquals(f,t.first);
		assertEquals(s,t.second);
	}

	@Test
	public void testSplit() {
		StringFieldExtractor sfx = new StringFieldExtractor(' ',2);
		checkSplit("a b","x y",sfx.split("a b x y"));
		checkSplit("ab ","x y",sfx.split("ab  x y"));
		checkSplit("ab x","y z",sfx.split("ab x y z"));
		checkSplit("ab x","y z",sfx.split("ab x y z"));
		checkSplit("ab",null,sfx.split("ab"));
		checkSplit("ab x",null,sfx.split("ab x"));
	}
}
