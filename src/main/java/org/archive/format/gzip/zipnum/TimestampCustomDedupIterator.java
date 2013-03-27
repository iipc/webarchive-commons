package org.archive.format.gzip.zipnum;

import org.archive.util.iterator.CloseableIterator;

public class TimestampCustomDedupIterator extends TimestampDedupIterator {
	
	// The additional field used as status field from the timestamp
	private int additionalFieldNum = 3;
	private int sep = ' ';

	public TimestampCustomDedupIterator(CloseableIterator<String> inner,
			int timestampDedupLength) {
		super(inner, timestampDedupLength);
	}
	
	@Override
	protected boolean isSame(String currStamp, String nextStamp,
			String currLine, String nextLine) {
		
		if (!super.isSame(currStamp, nextStamp, currLine, nextLine)) {
			return false;
		}
		
		//Same only if status code also matches
		String currStatus = getNthField(currLine, currStamp.length(), additionalFieldNum, sep);
		if (currStatus == null) {
			return false;
		}
		
		String nextStatus = getNthField(nextLine, nextStamp.length(), additionalFieldNum, sep);
		if (nextStatus == null) {
			return false;
		}
		
		return currStatus.equals(nextStatus);
	}
}
