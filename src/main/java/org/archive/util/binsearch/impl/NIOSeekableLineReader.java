package org.archive.util.binsearch.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.archive.util.binsearch.AbstractSeekableLineReader;

import com.google.common.io.LimitInputStream;

public class NIOSeekableLineReader extends AbstractSeekableLineReader {
	private FileChannel fc;
	private long size;
	
	private boolean isMMap = false;
	
	private FileChannelInputStream fcis;
	private ByteBufferBackedInputStream bbis;
	
	public NIOSeekableLineReader(FileChannel fc, int blockSize) throws IOException {
		super(blockSize);
		
		this.fc = fc;
		size = fc.size();
		fcis = null;
		bbis = null;
	}
	
	public InputStream doSeekLoad(long offset, int maxLength) throws IOException {
		
		if (isMMap && (maxLength >= 0)) {
			ByteBuffer mapBuff = fc.map(MapMode.READ_ONLY, offset, maxLength);
			bbis = new ByteBufferBackedInputStream(mapBuff, offset);	
			return new LimitInputStream(bbis, maxLength);
			
		} else {
			fcis = new FileChannelInputStream(fc, offset, maxLength);
			return fcis;
//			if (maxLength > 0) {
//				return new LimitInputStream(fcis, maxLength);
//			} else {
//				return fcis;
//			}
		}
    }
	
	public long getOffset() throws IOException
	{
		if (fcis != null) {
			return fcis.fcOffset;
		} else if (bbis != null) {
			return bbis.position();
		} else {
			return 0;
		}
	}

	public void doClose() throws IOException {
		// Not closing the channel, shared with factory
		fcis = null;
		bbis = null;
	}

	public long getSize() throws IOException {
		return size;
	}
	
	//From
	//http://stackoverflow.com/questions/4332264/wrapping-a-bytebuffer-with-an-inputstream/6603018#6603018
	public static class ByteBufferBackedInputStream extends InputStream {

	    ByteBuffer buf;
	    long fcOffset;

	    public ByteBufferBackedInputStream(ByteBuffer buf, long offset) {
	        this.buf = buf;
	        this.fcOffset = offset;
	    }

	    public synchronized int read() throws IOException {
	        if (!buf.hasRemaining()) {
	            return -1;
	        }
	        return buf.get() & 0xFF;
	    }

	    public synchronized int read(byte[] bytes, int off, int len)
	            throws IOException {
	        if (!buf.hasRemaining()) {
	            return -1;
	        }

	        len = Math.min(len, buf.remaining());
	        buf.get(bytes, off, len);
	        return len;
	    }
	    
	    public long position()
	    {
	    	return fcOffset + buf.position();
	    }
	}
	
	
	public class FileChannelInputStream extends InputStream {
		FileChannel fc;
		long fcOffset;
		int remaining;
		boolean bounded;
		
		public FileChannelInputStream(FileChannel fc, long offset, int maxLength) {
			this.fc = fc;
			this.fcOffset = offset;
			this.remaining = maxLength;
			this.bounded = (remaining > 0);
		}
		@Override
		public int read() throws IOException {
			byte b[] = new byte[1];
			int amt = read(b, 0, 1);
			if (amt == -1) {
				return -1;
			}
			return b[0] & 0xff;

		}
		@Override
		public int read(byte[] b) throws IOException {
			return read(b, 0, b.length);
		}
		
		public int read(byte[] buffer, int offset, int length) throws IOException {
			if (bounded) {
				remaining = Math.min(length, remaining);
			}
			ByteBuffer bb = ByteBuffer.wrap(buffer,offset,length);
			int totalRead = 0;
			while(length > 0) {
				int amtRead = fc.read(bb, fcOffset);
				if(amtRead == -1) {
					// EOF:
					break;
				}
				totalRead += amtRead;
				fcOffset += amtRead;
				length -= amtRead;
			}
			if (bounded) {
				remaining -= totalRead;
			}
			return totalRead == 0 ? -1 : totalRead;
		}
		
		@Override
		public int available()
		{
			return remaining;
		}
	}
}
