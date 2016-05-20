package org.archive.streamcontext;

import java.io.IOException;
import java.io.InputStream;

import org.archive.util.io.PushBackOneByteInputStream;

public class StreamWrappedInputStream extends InputStream
implements PushBackOneByteInputStream {

	private static final long UNMARKED = -1;
	private long markPos;
	private Stream stream;
	private boolean closeOnClose;

	public StreamWrappedInputStream(Stream stream) {
		this.stream = stream;
		markPos = UNMARKED;
		closeOnClose = false;
	}
	public void setCloseOnClose(boolean closeOnClose) {
		this.closeOnClose = closeOnClose;
	}
	public boolean isCloseOnClose() {
		return closeOnClose;
	}
	
	public int available() throws IOException {
		return 0;
	}

	public void close() throws IOException {
		// Do we really want to do this?
		if(closeOnClose) {
			stream.close();
		}
	}

	public synchronized void mark(int readlimit) {
		markPos = stream.getOffset();
	}

	public boolean markSupported() {
		return true;
	}
	
	public synchronized void reset() throws IOException {
		if(markPos == UNMARKED) {
			throw new IOException("reset without mark() unsupported");
		}
		stream.setOffset(markPos);
		markPos = UNMARKED;
	}

	public int read() throws IOException {
		byte b[] = new byte[1];
		int amt = stream.read(b, 0, 1);
		if(amt == -1) {
			return -1;
		}
		return b[0] & 0xff;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return stream.read(b, off, len);
	}

	public int read(byte[] b) throws IOException {
		return stream.read(b,0,b.length);
	}

	public long skip(long n) throws IOException {
		if(n < 1) {
			return 0;
		}
		// TODO: verify the right thing happens when we skip past EOF..
		long startOffset = stream.getOffset();
		long gotOffset = stream.setOffset(startOffset + n);
		return gotOffset - startOffset;
	}

	public void pushback() throws IOException {
		stream.setOffset(stream.getOffset()-1);
	}
}
