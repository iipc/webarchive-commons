package org.archive.util.iterator;

import java.io.IOException;
import java.util.Iterator;

public class BoundedStringIterator extends AbstractPeekableIterator<String> 
	implements CloseableIterator<String> {

	private Iterator<String> inner;
	private String boundary;

	public BoundedStringIterator(Iterator<String> inner, String boundary) {
		this.inner = inner;
		this.boundary = boundary;
	}

	@Override
	public String getNextInner() {
		String tmp = null;
		if(inner.hasNext()) {
			tmp = inner.next();
			if(tmp.compareTo(boundary) > 0) {
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
