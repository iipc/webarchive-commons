package org.archive.util.iterator;

import java.util.Collection;
import java.util.TreeSet;

public class TransformingPrefixStringFilter implements StringFilter {
	TreeSet<String> filters;
	StringTransformer transformer;

	public TransformingPrefixStringFilter(Collection<String> blocks) {
		this(blocks,null);
	}
	public TransformingPrefixStringFilter(Collection<String> blocks,
			StringTransformer transformer) {
		filters = makeTreeSet(blocks,transformer);
		this.transformer = transformer;
	}

	public static TreeSet<String> makeTreeSet(Collection<String> blocks, 
			StringTransformer trans) {
		TreeSet<String> tmp = new TreeSet<String>();
		for(String filter : blocks) {
			if(trans != null) {
				filter = trans.transform(filter);
			}
			String possiblePrefix = tmp.floor(filter);
	        if (possiblePrefix != null && filter.startsWith(possiblePrefix)) {
	        	// don't add - a prefix is already in the set:
	        } else {
	        	// is this a prefix of the existing item?
	        	String possibleLonger = tmp.ceiling(filter);
	        	if(possibleLonger == null) {
	        	} else if(possibleLonger.startsWith(filter)) {
	        		tmp.remove(possibleLonger);
	        	}
	        	tmp.add(filter);
	        }
		}
		return tmp;
	}
	
	public boolean isFiltered(String text) {
		if(transformer != null) {
			text = transformer.transform(text);
		}
        String possiblePrefix = filters.floor(text);
        return (possiblePrefix != null && text.startsWith(possiblePrefix));
	}
}
