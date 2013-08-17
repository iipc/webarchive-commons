package org.archive.format.gzip.zipnum;

public class ZipNumParams
{
	protected int maxAggregateBlocks = 1;
	protected int timestampDedupLength = 0;
	protected int maxBlocks = 0;
	private boolean reverse = false;
	
	public ZipNumParams()
	{
	    
	}
	
	public ZipNumParams(int maxAggregateBlocks, int maxBlocks, int timestampDedupLength, boolean reverse)
	{
	    this.maxAggregateBlocks = maxAggregateBlocks;
	    this.maxBlocks = maxBlocks;
	    this.timestampDedupLength = timestampDedupLength;
	    this.reverse = reverse;
	}
	
	public int getMaxAggregateBlocks() {
		return maxAggregateBlocks;
	}

	public void setMaxAggregateBlocks(int maxAggregateBlocks) {
		this.maxAggregateBlocks = maxAggregateBlocks;
	}

	public int getTimestampDedupLength() {
		return timestampDedupLength;
	}

	public void setTimestampDedupLength(int timestampDedupLength) {
		this.timestampDedupLength = timestampDedupLength;
	}

	public int getMaxBlocks() {
		return maxBlocks;
	}

	public void setMaxBlocks(int maxBlocks) {
		this.maxBlocks = maxBlocks;
	}

	public boolean isReverse() {
		return this.reverse;
    }
	
	public void setReverse(boolean reverse) {
		this.reverse  = reverse;
	}
}