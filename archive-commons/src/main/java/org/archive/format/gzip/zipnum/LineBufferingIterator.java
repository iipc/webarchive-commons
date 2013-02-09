package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.archive.util.iterator.CloseableIterator;

public class LineBufferingIterator implements CloseableIterator<String> {
	
	protected CloseableIterator<String> inner;
	protected int maxLines;
	protected int timestampDedupLength = 0;
	
	protected Iterator<String> currIter;
	
	public LineBufferingIterator(CloseableIterator<String> inner, int maxLines, int timestampDedupLength)
	{
		this.inner = inner;
		this.maxLines = maxLines;
		this.timestampDedupLength = timestampDedupLength;
	}
	
	protected String makeTimestamp(String line)
	{
		if (timestampDedupLength <= 0) {
			return null;
		}
		
		int space = line.indexOf(' ');
		if (space >= 0) {
			return line.substring(0, space + 1 + timestampDedupLength);
		} else {
			return null;
		}
	}
	
	protected boolean sameTimestamp(String currStamp, String nextStamp)
	{
		
		if (currStamp == null || nextStamp == null) {
			return false;
		}
		
		return currStamp.equals(nextStamp);
	}
	
	protected void bufferInput()
	{
		LinkedList<String> lineBuffer = new LinkedList<String>();
		
		String currLine = null;
		String nextLine = null;
		
		String nextStamp = null;
		String currStamp = null;
		
		while (inner.hasNext() && (lineBuffer.size() < maxLines)) {		
			nextLine = inner.next();
			nextStamp = makeTimestamp(nextLine);
			
			if (!sameTimestamp(currStamp, nextStamp) && (currLine != null)) {			
				lineBuffer.add(currLine);
			}
			
			currLine = nextLine;
			currStamp = nextStamp;
		}
		
		lineBuffer.add(currLine);
		
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
