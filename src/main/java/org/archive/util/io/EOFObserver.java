package org.archive.util.io;

import java.io.IOException;

public interface EOFObserver {
	public void notifyEOF() throws IOException;
}
