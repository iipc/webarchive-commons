package org.archive.util.binsearch.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;

public class RandomAccessFileSeekableLineReaderFactory implements SeekableLineReaderFactory {
	private File file;
	private int blockSize = BINSEARCH_BLOCK_SIZE;
	
	public RandomAccessFileSeekableLineReaderFactory(File file) {
		this.file = file;
	}
	public RandomAccessFileSeekableLineReaderFactory(File file, int blockSize) {
		this.file = file;
		this.blockSize = blockSize;
	}
	public SeekableLineReader get() throws IOException {
		return new RandomAccessFileSeekableLineReader(new RandomAccessFile(file, "r"),
				blockSize);
	}
	public void close() throws IOException {
		this.file = null;
	}
	
	public long getModTime()
	{
		return file.lastModified();
	}
}
