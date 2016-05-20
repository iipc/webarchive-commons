package org.archive.streamcontext;

import java.io.IOException;

public class ByteArrayWrappedStream extends AbstractBufferingStream {
	private byte[] buffer = null;
	int offset = 0;
	public ByteArrayWrappedStream(byte b[]) {
		buffer = b;
		offset = 0;
	}
	@Override
	public int doRead(byte[] b, int off, int len) throws IOException {
		if(offset == buffer.length) {
			return -1;
		}
		int amtToCopy = Math.min(buffer.length - offset, len);
		System.arraycopy(buffer, offset, b, off, amtToCopy);
		offset += amtToCopy;
		return amtToCopy;
	}

	@Override
	public void doSeek(long offset) throws IOException {
		if(offset > this.offset) {
			throw new IOException("seek past end..");
		}
		this.offset = (int) offset;
	}

	@Override
	public void doClose() throws IOException {		
	}

}
