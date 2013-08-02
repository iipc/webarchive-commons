package org.archive.format.gzip.zipnum;

import org.archive.format.cdx.FieldSplitLine;

class SummaryLine extends FieldSplitLine
{
	String partId;		
	long offset;
	int length;
	
	public SummaryLine(String line)
	{
		super(line, '\t', null);
		partId = super.getField(1);
		if (super.getNumFields() < 3) {
			return;
		}
		offset = Long.parseLong(super.getField(2));
		length = Integer.parseInt(super.getField(3));
		//timestamp = makeTimestamp(parts[0]);
	}
	
//		String makeTimestamp(String key)
//		{
//			if (params.getTimestampDedupLength() <= 0) {
//				return null;
//			}
//			
//			int space = key.indexOf(' ');
//			if (space >= 0) {
//				return key.substring(0, space + 1 + params.getTimestampDedupLength());
//			} else {
//				return null;
//			}
//		}
	
	public boolean isContinuous(SummaryLine next)
	{
		if (next == null || next.fullLine == null) {
			return false;
		}
		
		// Must be same part
		if (!partId.equals(next.partId)) {
			return false;
		}
		
		if ((offset + length) != next.offset) {
			return false;
		}
		
		return true;
	}
	
//		boolean sameTimestamp(SplitLine next)
//		{
//			if (next == null || next.timestamp == null) {
//				return false;
//			}
//			
//			if (timestamp == null) {
//				return false;
//			}
//			
//			return timestamp.equals(next.timestamp);
//		}
}