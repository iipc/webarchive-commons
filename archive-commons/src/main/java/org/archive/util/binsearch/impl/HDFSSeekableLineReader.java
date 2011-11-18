package org.archive.util.binsearch.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.fs.FSDataInputStream;
import org.archive.util.binsearch.SeekableLineReader;

public class HDFSSeekableLineReader implements SeekableLineReader {
	private FSDataInputStream fsdis;
	private BufferedReader br;
	private InputStreamReader isr;
	private long length;
	private int blockSize;
	
	public HDFSSeekableLineReader(FSDataInputStream fsdis, long length,
			int blockSize) {
		this.fsdis = fsdis;
		this.length = length;
		this.blockSize = blockSize;
		br = null;
		isr = null;
	}

	public void seek(long offset) throws IOException {
		fsdis.seek(offset);
    	isr = new InputStreamReader(fsdis, UTF8);
    	br = new BufferedReader(isr, blockSize);
	}

	public String readLine() throws IOException {
		if(br == null) {
			seek(0);
		}
		return br.readLine();
	}

	public void close() throws IOException {
		fsdis.close();
		br.close();
	}

	public long getSize() throws IOException {
		return length;
	}
	
}
