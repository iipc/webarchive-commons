package org.archive.util.iterator;

import java.io.IOException;
import java.util.Iterator;

public class StartBoundedStringIterator extends AbstractPeekableIterator<String> {

	private Iterator<String> inner;
	private String boundary;
	private boolean done = false;
	private boolean started = false;
	private int flip = 1;
	
	public StartBoundedStringIterator(Iterator<String> inner, String boundary) {
		this(inner, boundary, false);
	}

	public StartBoundedStringIterator(Iterator<String> inner, String boundary, boolean reverse) {
		this.inner = inner;
		this.boundary = boundary;
		this.done = false;
		this.started = false;
		this.flip = (reverse ? -1 : 1);
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
			
			int cmp = boundary.compareTo(tmp) * flip;
			
			if ((cmp <= 0)) {
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
