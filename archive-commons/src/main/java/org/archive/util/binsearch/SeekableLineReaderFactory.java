package org.archive.util.binsearch;

import java.io.IOException;

public interface SeekableLineReaderFactory {
	public SeekableLineReader get() throws IOException;
}
