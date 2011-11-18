package org.archive.util.binsearch;

import java.io.IOException;
import java.nio.charset.Charset;

public interface SeekableLineReader {
	public final static Charset UTF8 = Charset.forName("UTF-8");
	public void seek(long offset) throws IOException;
	public String readLine() throws IOException;
	public void close() throws IOException;
	public long getSize() throws IOException;
}
