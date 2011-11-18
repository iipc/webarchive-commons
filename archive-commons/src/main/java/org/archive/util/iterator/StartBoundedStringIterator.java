package org.archive.util.iterator;

import java.io.IOException;
import java.util.Iterator;

public class StartBoundedStringIterator extends AbstractPeekableIterator<String> {

	private Iterator<String> inner;
	private String boundary;
	private boolean done = false;
	private boolean started = false;

	public StartBoundedStringIterator(Iterator<String> inner, String boundary) {
		this.inner = inner;
		this.boundary = boundary;
		done = false;
		started = false;
	}

	@Override
	public String getNextInner() {
		if(done) {
			return null;
		}
		if(started) {
			if(inner.hasNext()) {
				String tmp = inner.next();
				if(tmp == null) {
					done = true;
					return null;
				}
				return tmp;
			}
		}
		while(inner.hasNext()) {
			String tmp = inner.next();
			int cmp = boundary.compareTo(tmp);
			if(cmp > 0) {
//				System.out.format("Skipping: %s\n", tmp);
			}
			if(boundary.compareTo(tmp) <= 0) {
				started = true;
				return tmp;
			}
		}
		try {
			close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		done = true;
		return null;
	}

	public void close() throws IOException {
		CloseableIteratorUtil.attemptClose(inner);
	}


}
