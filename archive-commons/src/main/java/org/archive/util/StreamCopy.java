package org.archive.util;

import java.io.IOException;
import java.io.OutputStream;

import java.io.InputStream;

import org.archive.util.io.PushBackOneByteInputStream;

public class StreamCopy {
	private static final int DEFAULT_READ_SIZE = 4096;
	public static long copy(InputStream i, OutputStream o) throws IOException {
		return copy(i,o,DEFAULT_READ_SIZE);
	}
	public static long copy(InputStream i, OutputStream o, int bytes) throws IOException {
		long total = 0;
		byte[] buf = new byte[bytes];
		int amt = 1;
		while(amt != -1) {
			amt = i.read(buf,0,bytes);
			if(amt > 0) {
				o.write(buf, 0, amt);
				total += amt;
			}
		}
		return total;
	}

	public static long copyLength(InputStream i, OutputStream o, long bytes) throws IOException {
		return copyLength(i,o,DEFAULT_READ_SIZE);
	}

	public static long copyLength(InputStream i, OutputStream o, long bytes, int readSize) throws IOException {
		long total = 0;
		byte[] buf = new byte[readSize];
		while(bytes > 0) {
            int amtToRead = (int) Math.min(bytes,readSize);
            int amtRead = i.read(buf,0,amtToRead);
            if(amtRead == -1) {
            	return total;
            }
            if(amtRead > 0) {
				o.write(buf, 0, amtRead);
				total += amtRead;
            }
		}
		return total;
	}

	public static long readToEOF(InputStream i) throws IOException {
		return readToEOF(i,DEFAULT_READ_SIZE);
	}
	public static long readToEOF(InputStream i, int bufferSize) throws IOException {
		long numBytes = 0;
		byte buffer[] = new byte[bufferSize];
		while(true) {
			int amt = i.read(buffer,0,bufferSize);
			if(amt == -1) {
				return numBytes;
			}
			numBytes += amt;
		}
	}
	public static long readToEOFSingle(InputStream i) throws IOException {
		long numBytes = 0;
		while(true) {
			int c = i.read();
			if(c == -1) {
				return numBytes;
			}
			numBytes++;
		}
	}
	public static long skipChars(PushBackOneByteInputStream i, int [] skips) throws IOException {
		long numBytes = 0;
		while(true) {
			int b = i.read();
			if(b == -1) {
				break;
			}
			boolean pushBack = true;
			for(int c : skips) {
				if(b == c) {
					pushBack = false;
					break;
				}
			}
			if(pushBack) {
				i.pushback();
				break;
			}
			numBytes++;
		}
		return numBytes;
	}

}
