package org.archive.util.binsearch.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.archive.util.binsearch.AbstractSeekableLineReader;

import com.google.common.io.LimitInputStream;

public class RandomAccessFileSeekableLineReader extends AbstractSeekableLineReader {
	
	private RandomAccessFile raf;

	public RandomAccessFileSeekableLineReader(RandomAccessFile raf, int blockSize) {
		super(blockSize);
		this.raf = raf;
	}

	public InputStream doSeekLoad(long offset, int maxLength) throws IOException {
		raf.seek(offset);
		
    	FileInputStream fis = new FileInputStream(raf.getFD());
    	
    	if (maxLength > 0) {
    		return new LimitInputStream(fis, maxLength);
    	} else {
    		return fis;
    	}
    }
		
	public long getOffset() throws IOException
	{
		if (closed) {
			return 0;
		}
		
		return raf.getFilePointer();
	}
	
	public void doClose() throws IOException {
		if (raf != null) {
			raf.close();
		}
		raf = null;
	}
	
	public long getSize() throws IOException {
		return raf.length();
	}
}
