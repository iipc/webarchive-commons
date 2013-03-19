package org.archive.util.binsearch.impl;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;

public class HDFSSeekableLineReaderFactory implements SeekableLineReaderFactory {
	private FileSystem fs;
	private Path path;
	public HDFSSeekableLineReaderFactory(FileSystem fs, Path path) {
		this.fs = fs;
		this.path = path;
	}
	public SeekableLineReader get() throws IOException {
		FileStatus status = fs.getFileStatus(path);
		if(status.isDir()) {
			throw new IOException("Path:" + path.toUri().toASCIIString() + " is a directory!");
		}
		long length = status.getLen();
		FSDataInputStream fsdis = fs.open(path);
		return new HDFSSeekableLineReader(fsdis, length, 4096);
	}
	
	public void close() throws IOException
	{
		if (this.fs != null) {
			fs.close();
		}
	}
	
	public long getModTime()
	{
		try {
			return fs.getFileStatus(path).getModificationTime();
		} catch (IOException e) {
			return 0;
		}
	}
}
