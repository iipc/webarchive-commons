package org.archive.format.gzip.zipnum.blockloader;

import java.io.IOException;

import org.archive.util.binsearch.SeekableLineReader;

public interface BlockLoader {
	public SeekableLineReader createBlockReader(String filename) throws IOException;
}
