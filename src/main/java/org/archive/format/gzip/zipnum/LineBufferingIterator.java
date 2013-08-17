package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.archive.util.iterator.CloseableIterator;

public class LineBufferingIterator implements CloseableIterator<String> {
	
	protected CloseableIterator<String> inner;
	protected int maxLines;
	protected boolean reverse;
	
	protected Iterator<String> currIter;
	
	public LineBufferingIterator(CloseableIterator<String> inner, int maxLines, boolean reverse)
	{
		this.inner = inner;
		this.maxLines = maxLines;
		this.reverse = reverse;
	}
		
	public void bufferInput()
	{
		if (currIter != null) {
			return;
		}
		
		LinkedList<String> lineBuffer = new LinkedList<String>();
		
		while (inner.hasNext() && (lineBuffer.size() < maxLines)) {
			lineBuffer.add(inner.next());
		}
		
		currIter = (reverse ? lineBuffer.descendingIterator() : lineBuffer.iterator());
		
		try {
			inner.close();
			inner = null;
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
