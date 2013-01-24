package org.archive.util.binsearch.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.zip.GZIPMembersInputStream;

import com.google.common.io.LimitInputStream;

public class NIOSeekableLineReader implements SeekableLineReader {
	private FileChannel fc;
	private long size;
	
	private FileChannelInputStream fcis;
	private ByteBufferBackedInputStream bbis;
	
	private InputStreamReader isr;
	private BufferedReader br;

	private int blockSize;
	
	protected boolean useMMap;
	
	public NIOSeekableLineReader(FileChannel fc, int blockSize) throws IOException {
		this.fc = fc;
		size = fc.size();
		fcis = null;
		bbis = null;
		this.blockSize = blockSize;
	}
	
	public void seek(long offset) throws IOException {
		fcis = new FileChannelInputStream(fc, offset);
		bbis = null;
    	isr = new InputStreamReader(fcis, UTF8);
    	br = new BufferedReader(isr, blockSize);
	}
	
	public void seekWithMaxRead(long offset, boolean gzip, int maxLength) throws IOException {
		
		ByteBuffer mapBuff = fc.map(MapMode.READ_ONLY, offset, maxLength);
		
		bbis = new ByteBufferBackedInputStream(mapBuff, offset);
		
		//fcis = new FileChannelInputStream(fc, offset);
    	
    	InputStream is = new LimitInputStream(bbis, maxLength);
    	if (gzip) {
    		is = new GZIPMembersInputStream(is, blockSize);
    	}    	
    	isr = new InputStreamReader(is, UTF8);
    	br = new BufferedReader(isr, blockSize);
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

	public String readLine() throws IOException {
		if(br == null) {
			seek(0);
		}
		return br.readLine();
	}

	public void close() throws IOException {
		if (br != null) {
			br.close();
			br = null;
		}
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
		public FileChannelInputStream(FileChannel fc, long offset) {
			this.fc = fc;
			this.fcOffset = offset;
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
			return totalRead == 0 ? -1 : totalRead;
		}
	}
}
