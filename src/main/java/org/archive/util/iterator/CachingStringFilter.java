package org.archive.util.iterator;

import java.util.LinkedHashMap;
import java.util.Map;

public class CachingStringFilter implements StringFilter {
	private LRUCache cache;
	private StringFilter inner;
	public CachingStringFilter(StringFilter inner, int max) {
		this.inner = inner;
		cache = new LRUCache(max);
	}

	public boolean isFiltered(String text) {
		Boolean v = cache.remove(text);
		if(v == null) {
			v = inner.isFiltered(text);			
		}
		cache.put(text, v);
		return v;
	}

	public class LRUCache extends LinkedHashMap<String, Boolean> {
	     /**  */
		private static final long serialVersionUID = 1L;
		private int max = 100;

	     public LRUCache(int max) {
	    	 this.max = max;
	     }

	     protected boolean removeEldestEntry(Map.Entry<String,Boolean> eldest) {
	    	 return (size() > max);
	     }
	}
}
