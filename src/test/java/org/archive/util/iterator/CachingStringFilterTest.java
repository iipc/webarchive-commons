package org.archive.util.iterator;

import org.junit.jupiter.api.Test;

public class CachingStringFilterTest {
	@Test
	public void testCache() {
		StringFilter tf = new StringFilter() {
			public boolean isFiltered(String text) {
				return true;
			}
		};
		CachingStringFilter csf = new CachingStringFilter(tf, 3);
		csf.isFiltered("one");
		csf.isFiltered("one");
		csf.isFiltered("two");
		csf.isFiltered("one");
		csf.isFiltered("three");
		csf.isFiltered("two");
		csf.isFiltered("four");
	}
}
