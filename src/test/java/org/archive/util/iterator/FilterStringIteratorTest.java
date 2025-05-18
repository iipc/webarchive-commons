package org.archive.util.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FilterStringIteratorTest {

	@Test
	public void testHasNext() {
		String[] blocks = {"a","ab","ba","cc"};
		
		List<String> bl = Arrays.asList(blocks);
		TransformingPrefixStringFilter f = new TransformingPrefixStringFilter(bl);
		assertBlocked(true,"a",f);
		assertBlocked(true,"ab",f);
		assertBlocked(true,"ac",f);
		assertBlocked(true,"acca",f);
		assertBlocked(false,"b",f);
		assertBlocked(true,"ba",f);
		assertBlocked(true,"bac",f);
		assertBlocked(false,"bc",f);
		assertBlocked(false,"ca",f);
		assertBlocked(true,"cc",f);
		assertBlocked(true,"cca",f);
	}

	@Test
	public void testTreeSet() {
		String[] blocks = {"a","ab","ba","cc"};
		TreeSet<String> s = TransformingPrefixStringFilter.makeTreeSet(Arrays.asList(blocks),null);
		assertTrue(s.contains("a"));
		assertFalse(s.contains("ab"));

		String[] blocks2 = {"ab","a","ba","cc"};
		TreeSet<String> s2 = TransformingPrefixStringFilter.makeTreeSet(Arrays.asList(blocks2),null);
		assertTrue(s2.contains("a"));
		assertFalse(s2.contains("ab"));
	}

	private void assertBlocked(boolean blocked, String s, StringFilter f) {
		ArrayList<String> l = new ArrayList<String>();
		l.add(s);
		FilterStringIterator i = new FilterStringIterator(l.iterator(), f);
		if(blocked) {
			assertFalse(i.hasNext());
		} else {
			assertTrue(i.hasNext());
			assertEquals(s,i.next());
		}
	}
}
