package org.archive.util;

import java.util.ArrayList;
import java.util.List;

import org.archive.util.CrossProduct;

import junit.framework.TestCase;

public class CrossProductTest extends TestCase {
	private void dumpC(List<Object> a) {
		StringBuilder sb = new StringBuilder();
		boolean first = false;
		for(Object o : a) {
			if(first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(o.toString());
		}
		System.out.println("Dump:" + sb.toString());
	}
	private void dumpLOL(List<List<Object>> coc) {
		for(List<Object> co : coc) {
			dumpC(co);
		}
	}
	public void testVersion() {
		String version = IAUtils.loadCommonsVersion();
		System.out.format("Loaded version(%s)\n", version);
	}
	public void testCrossProduct() {
		ArrayList<List<Object>> input = new ArrayList<List<Object>>();
		CrossProduct<Object> xp = new CrossProduct<Object>();
		input.add(AtoL("1","2"));
		input.add(AtoL("Charming"));
		input.add(AtoL("Berry","Elvis"));
		input.add(AtoL("a","b","c","d"));
		List<List<Object>> cross = xp.crossProduct(input);
		dumpLOL(cross);
	}
	private List<Object> AtoL(Object... a) {
		ArrayList<Object> al = new ArrayList<Object>(a.length);
		for(Object s : a) {
			al.add(s);
		}
		return al;
	}
}
