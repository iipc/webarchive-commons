package org.archive.streamcontext;

import java.io.IOException;
import java.io.InputStream;

public class SimpleStream extends AbstractBufferingStream {
	private InputStream is;

	public SimpleStream(InputStream is) {
		this(is,0L,DEFAULT_READ_SIZE);
	}

	public SimpleStream(InputStream is, long offset) {
		this(is,offset,DEFAULT_READ_SIZE);
	}

	public SimpleStream(InputStream is, long offset, int readSize) {
		super(offset,readSize);
		this.is = is;
	}

	@Override
	public void doClose() throws IOException {
		is.close();
	}

	@Override
	public int doRead(byte[] b, int off, int len) throws IOException {
		return is.read(b,off,len);
	}

	@Override
	public void doSeek(long offset) throws IOException {
		throw new IOException("Unable to seek!");
	}
}
