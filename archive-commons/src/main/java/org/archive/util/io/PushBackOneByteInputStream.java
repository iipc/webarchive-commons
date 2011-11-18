package org.archive.util.io;

import java.io.IOException;

public interface PushBackOneByteInputStream {
	public void pushback() throws IOException;
	public int read() throws IOException;
}
