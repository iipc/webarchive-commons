package org.archive.util.binsearch;

import java.io.IOException;

public interface SeekableLineReaderFactory {
	public final static int BINSEARCH_BLOCK_SIZE = 8192;
	public SeekableLineReader get() throws IOException;
    public void close() throws IOException;
	public long getModTime();
}
