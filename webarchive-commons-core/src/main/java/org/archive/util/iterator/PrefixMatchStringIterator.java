package org.archive.util.iterator;

import java.io.IOException;


public class PrefixMatchStringIterator extends AbstractPeekableIterator<String>
{
	private boolean first = true;
	private String key;
	private CloseableIterator<String> inner;
	
	public PrefixMatchStringIterator(CloseableIterator<String> inner, String key, boolean alwaysIncludeFirst)
	{
		this.inner = inner;
		this.key = key;
		this.first = alwaysIncludeFirst;
	}

	@Override
	public String getNextInner() {
		
		if (!inner.hasNext()) {
			return null;
		}
		
		String blockLine = inner.next();
		
		// only compare the correct length:
		String prefCmp = key;
		
		if (first) {
			// always add first:
			first = false;
		} else if (!blockLine.startsWith(prefCmp)) {
			return null;
		}
		
		return blockLine;
	}

	@Override
	public void close() throws IOException {
		inner.close();
	}
}