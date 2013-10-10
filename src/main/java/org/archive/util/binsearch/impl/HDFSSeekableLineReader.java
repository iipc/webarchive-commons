package org.archive.util.binsearch.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.fs.FSDataInputStream;
import org.archive.util.binsearch.AbstractSeekableLineReader;

import com.google.common.io.LimitInputStream;

public class HDFSSeekableLineReader extends AbstractSeekableLineReader {
	private FSDataInputStream fsdis;
	private long length;
	
	public HDFSSeekableLineReader(FSDataInputStream fsdis, long length,
			int blockSize) {
		super(blockSize);
		this.fsdis = fsdis;
		this.length = length;
	}
	
	public InputStream doSeekLoad(long offset, int maxLength) throws IOException {
		fsdis.seek(offset);
		
		if (maxLength >= 0) {
			return new LimitInputStream(fsdis, maxLength);
		} else {
			return fsdis;
		}
    }
	
	public long getOffset() throws IOException {
		return fsdis.getPos();
	}

	public void doClose() throws IOException {
		//Superclass closes the input stream
		fsdis = null;
	}

	public long getSize() throws IOException {
		return length;
	}
	
}
