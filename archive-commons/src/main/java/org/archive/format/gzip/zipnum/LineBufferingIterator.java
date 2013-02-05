package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.archive.util.iterator.CloseableIterator;

public class LineBufferingIterator implements CloseableIterator<String> {
	
	protected CloseableIterator<String> inner;
	protected int maxLines;
	
	protected Iterator<String> currIter;
	
	public LineBufferingIterator(CloseableIterator<String> inner, int maxLines)
	{
		this.inner = inner;
		this.maxLines = maxLines;
	}
	
	protected void bufferInput()
	{
		LinkedList<String> lineBuffer = new LinkedList<String>();
		
		int lines = 0;
		
		while (inner.hasNext() && (lines++ < maxLines)) {
			lineBuffer.add(inner.next());
		}
		
		currIter = lineBuffer.iterator();
		
		try {
			inner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		if (currIter == null) {
			bufferInput();
		}
		
		return currIter.hasNext();
	}
	
	@Override
	public String next() {
		return currIter.next();
	}

	@Override
	public void close() throws IOException {
		//Nothing to close, inner iter already closed!
	}

	@Override
	public void remove() {

	}
}
