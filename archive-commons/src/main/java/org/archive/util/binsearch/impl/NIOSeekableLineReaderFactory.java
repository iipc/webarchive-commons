package org.archive.util.binsearch.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;

/**
 * A SeekableLineReaderFactory that opens a file ONCE at construction.
 * 
 * Should be able to re-use the same FileChannel across threads, and might be
 * more efficient than the RandomAccessFileSeekableLineReaderXXX classes.
 * 
 * @author brad
 *
 */
public class NIOSeekableLineReaderFactory implements SeekableLineReaderFactory {
	private FileChannel fc;
	public NIOSeekableLineReaderFactory(File file) throws IOException {
		fc = new RandomAccessFile(file,"r").getChannel();
	}
	public SeekableLineReader get() throws IOException {
		return new NIOSeekableLineReader(fc);
	}

}
