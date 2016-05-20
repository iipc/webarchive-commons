package org.archive.util.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.util.StringFieldExtractor;

public class FilterStringIterator implements Iterator<String> {
	private static final Logger LOGGER =
		Logger.getLogger(FilterStringIterator.class.getName());
	private static final int DEFAULT_FIELD = 0;
	private static final char DEFAULT_DELIM = ' ';

	private int field = DEFAULT_FIELD;
	private char delim = DEFAULT_DELIM;
	
	private StringFilter filter;
	private Iterator<String> wrapped;
	private StringFieldExtractor extractor;
	private String cachedNext;

	public FilterStringIterator(Iterator<String> wrapped, StringFilter filter) {
		this.wrapped = wrapped;
		this.filter = filter;
		cachedNext = null;
		extractor = new StringFieldExtractor(delim, field);
	}
	
	/**
	 * @return the field
	 */
	public int getField() {
		return field;
	}
	/**
	 * @param field the field to set
	 */
	public void setField(int field) {
		this.field = field;
		extractor = new StringFieldExtractor(delim, field);
	}
	/**
	 * @return the delim
	 */
	public char getDelim() {
		return delim;
	}
	/**
	 * @param delim the delim to set
	 */
	public void setDelim(char delim) {
		this.delim = delim;
		extractor = new StringFieldExtractor(delim, field);
	}

	public boolean hasNext() {
		if(cachedNext != null) {
			return true;
		}
		while(true) {
			if(!wrapped.hasNext()) {
				return false;
			}
			String tmp = wrapped.next();
			String f = extractor.extract(tmp);
			if(filter.isFiltered(f)) {
				if(LOGGER.isLoggable(Level.FINE)) {
					LOGGER.fine("Filtered:" + f);
				}
			} else {
				cachedNext = tmp;
				return true;
			}
		}
	}

	public String next() {
		if(cachedNext == null) {
			throw new NoSuchElementException("Call hasNext() first");
		}
		String tmp = cachedNext;
		cachedNext = null;
		return tmp;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
