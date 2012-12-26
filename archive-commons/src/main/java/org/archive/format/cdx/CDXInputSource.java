package org.archive.format.cdx;

import java.io.IOException;

public interface CDXInputSource {
	
	public CDXSearchResult getLineIterator(String key, boolean exact) throws IOException;
}
