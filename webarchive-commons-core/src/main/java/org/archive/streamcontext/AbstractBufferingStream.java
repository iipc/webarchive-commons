package org.archive.streamcontext;

import java.io.IOException;

/**
 * Class which implements the bulk of the functionality needed for Stream
 * behavior, leaving concrete subclasses to implement only 3 simple methods
 * beyond their constructors:
 * 
 *   doSeek()
 *   doRead()
 *   doClose()
 *   
 * This class maintains a buffer of bytes read, and attempts to be efficient
 * about seeks within that buffer.
 * 
 * @author brad
 *
 */
public abstract class AbstractBufferingStream implements Stream {
	protected static int DEFAULT_READ_SIZE = 4096;

	protected long offset = 0L;
	protected boolean closed = false;
	protected boolean atEof = false;
	protected byte buffer[] = null;
	protected int bufferRemaining = 0;
	protected int bufferCursor = 0;
	
	public AbstractBufferingStream() {
		this(0L,DEFAULT_READ_SIZE);
	}
	public AbstractBufferingStream(long offset) {
		this(offset,DEFAULT_READ_SIZE);
	}
	public AbstractBufferingStream(long offset, int readSize) {
		if(offset < 0) {
			throw new IndexOutOfBoundsException();
		}
		this.offset = offset;
		buffer = new byte[readSize];
		closed = false;
		atEof = false;
		bufferRemaining = 0;
		bufferCursor = 0;
	}

	public abstract int doRead(byte[] b, int off, int len) throws IOException;
	public abstract void doSeek(long offset) throws IOException;
	public abstract void doClose() throws IOException;

	public boolean atEof() {
		return atEof;
	}

	public long getOffset() {
		return offset;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		if(closed) {
			throw new IOException("Read after close()");
		}
		if(atEof) {
			return -1;
		}
		int amtRead = 0;
		while(len > 0) {
			if(bufferRemaining > 0) {
				int amtToCopy = Math.min(bufferRemaining, len);
				System.arraycopy(buffer,bufferCursor,b,off,amtToCopy);
				bufferCursor += amtToCopy;
				bufferRemaining -= amtToCopy;
				off += amtToCopy;
				len -= amtToCopy;
				amtRead += amtToCopy;
			}
			// either we satisfied the read request, or the buffer is empty:
			if(len > 0) {
				// our buffer is empty at this point, fill it up:
				int amtReadNow = doRead(buffer,0,buffer.length);
				if(amtReadNow == -1) {
					atEof = true;
					break;
				}
				bufferCursor = 0;
				bufferRemaining = amtReadNow;
			}
		}
		if(amtRead == 0) {
			// must be at EOF:
			amtRead = -1;
		} else {
			// got some data: advance the offset:
			offset += amtRead;
		}
		return amtRead;
	}

	
	public long setOffset(long newOffset) throws IOException {
		if(offset < newOffset) {
			// we're scanning ahead:
			long amtToSkip = newOffset - offset;
			if(amtToSkip < bufferRemaining) {
				// skipping to somewhere in our current buffer:
				bufferRemaining -= amtToSkip;
				bufferCursor += amtToSkip;
			} else {
				// seeking forward beyond buffer:
				// OPTIMIZ: are few read()s a bit rather than seek()?
				doSeek(newOffset);
				bufferRemaining = 0;
			}
			atEof = false;
		} else if(offset > newOffset) {
			long amtToReverse = offset - newOffset;
			if(amtToReverse < bufferCursor) {
				// within our buffer:
				bufferCursor -= amtToReverse;
				bufferRemaining += amtToReverse;
			} else {
				// seeking backwards beyond buffer:
				doSeek(newOffset);
				bufferRemaining = 0;
			}
			atEof = false;
		}
		offset = newOffset;
		return newOffset;		
	}
	
	public void close() throws IOException {
		if(!closed) {
			doClose();
			closed = true;
		}
	}
	
}
