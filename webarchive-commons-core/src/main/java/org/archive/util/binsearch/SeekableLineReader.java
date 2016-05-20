package org.archive.util.binsearch;

import java.io.IOException;
import java.io.InputStream;

public interface SeekableLineReader {
	public void seek(long offset) throws IOException;
	public void seekWithMaxRead(long offset, boolean gzip, int maxLength) throws IOException;
	public InputStream getInputStream();
	public String readLine() throws IOException;
	public void skipLine() throws IOException;
	public void close() throws IOException;
	public long getSize() throws IOException;
	public void setBufferFully(boolean bufferFully);
	public boolean isClosed();
}
