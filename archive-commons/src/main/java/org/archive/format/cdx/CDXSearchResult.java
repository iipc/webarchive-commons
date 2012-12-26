package org.archive.format.cdx;

import org.archive.util.iterator.CloseableIterator;

public class CDXSearchResult {
	protected CloseableIterator<String> iter;
	protected boolean truncated;
	
	public CDXSearchResult(CloseableIterator<String> iterator, boolean truncated)
	{
		this.iter = iterator;
		this.truncated = truncated;
	}
	
	public CloseableIterator<String> iterator()
	{
		return iter;
	}
	
	public boolean isTruncated()
	{
		return this.truncated;
	}
}
