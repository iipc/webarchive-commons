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
	
	protected String extractMatchKey(String line)
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
	
	protected boolean isSame(String currStamp, String nextStamp, String currLine, String nextLine)
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
				nextStamp = extractMatchKey(nextLine);
			}
			isFirst = false;
		}
				
		String currLine = null;
		String currStamp = null;
		
		do {
			currLine = nextLine;
			currStamp = nextStamp;
			
			nextLine = (inner.hasNext() ? inner.next() : null);
			nextStamp = extractMatchKey(nextLine);
			
		} while ((nextLine != null) && isSame(currStamp, nextStamp, currLine, nextLine));
		
				
		return currLine;
	}
	
	protected String getNthField(String source, int start, int num, int ch)
	{
		int lastIndex = -1;
		int index = start;
		
		for (int i = 0; i <= num; i++) {
			
			lastIndex = index;
			index = source.indexOf(' ', index) + 1;
			
			if (index < 0) {
				return null;
			}
		}
		
		return source.substring(lastIndex, index - 1);
	}

	@Override
	public void close() throws IOException {
		if (inner != null) {
			inner.close();
			inner = null;
		}
	}
}
