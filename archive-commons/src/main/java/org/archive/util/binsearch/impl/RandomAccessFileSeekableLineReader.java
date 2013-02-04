package org.archive.util.binsearch.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.zip.GZIPMembersInputStream;

import com.google.common.io.LimitInputStream;

public class RandomAccessFileSeekableLineReader implements SeekableLineReader {
	private static final Logger LOGGER =
		Logger.getLogger(RandomAccessFileSeekableLineReader.class.getName());
	private RandomAccessFile raf;
	private BufferedReader br;
	private FileInputStream fis;
	private InputStreamReader isr;
	private int blockSize;
	private boolean closed;
	public RandomAccessFileSeekableLineReader(RandomAccessFile raf, int blockSize) {
		this.raf = raf;
		this.blockSize = blockSize;
		br = null;
		fis = null;
		isr = null;
		closed = false;
	}
	public void seek(long offset) throws IOException {
		if(closed) {
			throw new IOException("Seek after close()");
		}
		raf.seek(offset);
    	fis = new FileInputStream(raf.getFD());
    	isr = new InputStreamReader(fis, UTF8);
    	br = new BufferedReader(isr, blockSize);
	}
	public void seekWithMaxRead(long offset, boolean gzip, int maxLength) throws IOException {
		if(closed) {
			throw new IOException("Seek after close()");
		}
		raf.seek(offset);
    	fis = new FileInputStream(raf.getFD());
    	InputStream is = new LimitInputStream(fis, maxLength);
    	if (gzip) {
    		is = new GZIPMembersInputStream(is, blockSize);
    	}    	
    	isr = new InputStreamReader(is, UTF8);
    	br = new BufferedReader(isr, blockSize);
    }
	public String readLine() throws IOException {
		if(closed) {
			return null;
		}
		if(br == null) {
			seek(0);
		}
		return br.readLine();
	}
	
	public long getOffset() throws IOException
	{
		return raf.getFilePointer();
	}
	
	public void close() throws IOException {
		if(closed) {
			//LOGGER.info(String.format("Error already closed"));
			return;
		}
		closed = true;
		
		raf.close();
//		fis.close();
	}
	public long getSize() throws IOException {
		return raf.length();
	}
}
