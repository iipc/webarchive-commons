package org.archive.util.iterator;

import java.io.IOException;
import java.util.Iterator;

public class BoundedStringIterator extends AbstractPeekableIterator<String> 
	implements CloseableIterator<String> {

	private Iterator<String> inner;
	private String boundary;
	private boolean inclusive;

	public BoundedStringIterator(Iterator<String> inner, String boundary) {
		this(inner, boundary, false);
	}
	
	public BoundedStringIterator(Iterator<String> inner, String boundary, boolean inclusive) {
		this.inner = inner;
		this.boundary = boundary;
		this.inclusive = inclusive;
	}

	@Override
	public String getNextInner() {
		String tmp = null;
		if(inner.hasNext()) {
			tmp = inner.next();
			if(tmp.compareTo(boundary) > 0 && (!inclusive || !tmp.startsWith(boundary))) {
				tmp = null;
				try {
					close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return tmp;
	}

	public void close() throws IOException {
		CloseableIteratorUtil.attemptClose(inner);
	}

}
