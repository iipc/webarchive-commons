package org.archive.format.cdx;

import java.io.IOException;

import org.archive.util.iterator.CloseableIterator;

public interface CDXInputSource {
	
	public CloseableIterator<String> getLineIterator(String key, boolean exact) throws IOException;
}
