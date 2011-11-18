package org.archive.util.binsearch.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.archive.util.binsearch.SeekableLineReader;

public class NIOSeekableLineReader implements SeekableLineReader {
	private FileChannel fc;
	private long size;
	
	private FileChannelInputStream fcis;
	private InputStreamReader isr;
	private BufferedReader br;

	private int blockSize = 4096;
	public NIOSeekableLineReader(FileChannel fc) throws IOException {
		this.fc = fc;
		size = fc.size();
		fcis = null;
	}
	public void seek(long offset) throws IOException {
		fcis = new FileChannelInputStream(fc, offset);
    	isr = new InputStreamReader(fcis, UTF8);
    	br = new BufferedReader(isr, blockSize);
	}

	public String readLine() throws IOException {
		if(br == null) {
			seek(0);
		}
		return br.readLine();
	}

	public void close() throws IOException {
		// NO-OP
	}

	public long getSize() throws IOException {
		return size;
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
