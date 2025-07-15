package org.archive.format.gzip.zipnum;

import org.apache.commons.lang3.math.NumberUtils;
import org.archive.util.iterator.CloseableIterator;

public class TimestampBestPickDedupIterator extends TimestampDedupIterator {

	public TimestampBestPickDedupIterator(CloseableIterator<String> inner,	int timestampDedupLength) {
		super(inner, timestampDedupLength);
	}
	
	private int additionalFieldNum = 3;
	private int sep = ' ';
	
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
		boolean same = false;
		
		do {
			if (!same || pickNew(currStamp, nextStamp, currLine, nextLine)) {
				currLine = nextLine;
				currStamp = nextStamp;
			}
			
			nextLine = (inner.hasNext() ? inner.next() : null);
			nextStamp = extractMatchKey(nextLine);
			
		} while ((nextLine != null) && (same = isSame(currStamp, nextStamp, currLine, nextLine)));
		
				
		return currLine;
	}
	
	protected boolean pickNew(String currStamp, String nextStamp,
			String currLine, String nextLine) {

		String currStatusStr = getNthField(currLine, currStamp.length(), additionalFieldNum, sep);
		String nextStatusStr = getNthField(nextLine, nextStamp.length(), additionalFieldNum, sep);
		
		int currStatusNum = NumberUtils.toInt(currStatusStr, Integer.MAX_VALUE);
		int nextStatusNum = NumberUtils.toInt(nextStatusStr, Integer.MAX_VALUE);
		
		// Pick the next one as long as the status of next line is smaller or at least equal to current
		return (nextStatusNum <= currStatusNum);
	}
}
