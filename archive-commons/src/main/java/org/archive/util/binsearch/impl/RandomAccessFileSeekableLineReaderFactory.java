package org.archive.util.binsearch.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;

public class RandomAccessFileSeekableLineReaderFactory implements SeekableLineReaderFactory {
	private File file;
	public RandomAccessFileSeekableLineReaderFactory(File file) {
		this.file = file;
	}
	public SeekableLineReader get() throws IOException {
		return new RandomAccessFileSeekableLineReader(new RandomAccessFile(file, "r"),
				4096);
	}
}
