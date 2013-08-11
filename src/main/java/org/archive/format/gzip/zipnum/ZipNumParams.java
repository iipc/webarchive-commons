package org.archive.format.gzip.zipnum;

public class ZipNumParams
{
	protected int maxAggregateBlocks = 1;
	protected int timestampDedupLength = 0;
	protected int maxBlocks = 0;
	
	public ZipNumParams()
	{
	    
	}
	
	public ZipNumParams(int maxAggregateBlocks, int maxBlocks, int timestampDedupLength)
	{
	    this.maxAggregateBlocks = maxAggregateBlocks;
	    this.maxBlocks = maxBlocks;
	    this.timestampDedupLength = timestampDedupLength;
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
}