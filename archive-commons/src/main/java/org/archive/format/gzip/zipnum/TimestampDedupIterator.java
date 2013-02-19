package org.archive.format.gzip.zipnum;

import java.io.IOException;

import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.CloseableIterator;

public class TimestampDedupIterator extends AbstractPeekableIterator<String> {

	protected int timestampDedupLength = 0;
	
	protected CloseableIterator<String> inner;
	
	protected boolean isFirst = true;
	
	protected String nextLine = null;
	
	protected String nextStamp = null;
	
	public TimestampDedupIterator(CloseableIterator<String> inner, int timestampDedupLength)
	{
		this.inner = inner;
		this.timestampDedupLength = timestampDedupLength;
	}
	
	protected String makeTimestamp(String line)
	{
		if (line == null) {
			return null;
		}
		
		if (timestampDedupLength <= 0) {
			return null;
		}
		
		int space = line.indexOf(' ');
		if (space >= 0) {
			return line.substring(0, space + 1 + timestampDedupLength);
		} else {
			return line;
		}
	}
	
	protected boolean sameTimestamp(String currStamp, String nextStamp)
	{		
		if (currStamp == null || nextStamp == null) {
			return false;
		}
		
		return currStamp.equals(nextStamp);
	}
	
	@Override
	public String getNextInner() {
		
		if (isFirst) {
			if (inner.hasNext()) {
				nextLine = inner.next();
			}
			isFirst = false;
		}
				
		String currLine = null;
		String currStamp = null;
		
		do {
			currLine = nextLine;
			currStamp = nextStamp;
			
			nextLine = (inner.hasNext() ? inner.next() : null);
			nextStamp = makeTimestamp(nextLine);
			
		} while ((nextLine != null) && sameTimestamp(currStamp, nextStamp));
		
				
		return currLine;
	}

	@Override
	public void close() throws IOException {
		if (inner != null) {
			inner.close();
			inner = null;
		}
	}
}
