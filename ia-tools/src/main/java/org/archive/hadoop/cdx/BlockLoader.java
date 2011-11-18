package org.archive.hadoop.cdx;

import java.io.IOException;

public interface BlockLoader {
	
	public byte[] readBlock(String url, long start, int length) throws IOException;
}
