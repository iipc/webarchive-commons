package org.archive.streamcontext;

import java.io.Closeable;
import java.io.IOException;

/**
 * Alternate simplified interface for accessing data from an underlying source 
 * of bytes.
 *
 * @author brad
 *
 */
public interface Stream extends Closeable {
	public long getOffset();
	public long setOffset(long offset) throws IOException;
	public int read(byte[] bytes, int off, int len) throws IOException;
	public boolean atEof();
}
