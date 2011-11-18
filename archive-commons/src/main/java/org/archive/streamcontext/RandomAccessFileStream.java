package org.archive.streamcontext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileStream extends AbstractBufferingStream {
	
	private RandomAccessFile raf = null;
	private File file = null;
	public RandomAccessFileStream(File file)
		throws IndexOutOfBoundsException, FileNotFoundException, IOException {
		this(file,0L,DEFAULT_READ_SIZE);
	}	
	public RandomAccessFileStream(File file, long offset)
		throws IndexOutOfBoundsException, FileNotFoundException, IOException {
		this(file,offset,DEFAULT_READ_SIZE);
	}	
	public RandomAccessFileStream(File file, long offset, int readSize) 
		throws IndexOutOfBoundsException, FileNotFoundException, IOException {
		super(offset,readSize);
		raf = new RandomAccessFile(file, "r");
		if(offset > 0) {
			raf.seek(offset);
		}
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public void doClose() throws IOException {
		raf.close();
	}

	public int doRead(byte[] b, int off, int len) throws IOException {
		return raf.read(b, off, len);
	}

	public void doSeek(long offset) throws IOException {
		raf.seek(offset);
	}
}
