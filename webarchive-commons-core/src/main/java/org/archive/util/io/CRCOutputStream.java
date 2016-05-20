package org.archive.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

public class CRCOutputStream extends OutputStream {
	OutputStream os = null;
	private CRC32 crc = null;
	boolean autoFlush = false;
	long bytesWritten = 0;
	public CRCOutputStream(OutputStream os) {
		this(os,false);
	}
	public CRCOutputStream(OutputStream os, boolean autoFlush) {
		this.os = os;
		this.crc = new CRC32();
		this.autoFlush = autoFlush;
		bytesWritten = 0;
	}

	@Override
	public void write(int b) throws IOException {
		crc.update(b);
		os.write(b);
		if(autoFlush) 
			os.flush();
		bytesWritten++;
	}
	@Override
	public void write(byte[] b) throws IOException {
		write(b,0,b.length);
	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		crc.update(b, off, len);
		os.write(b,0,len);
		if(autoFlush) {
			os.flush();
		}
		bytesWritten += len;
	}
	public long getCRCValue() {
		return crc.getValue();
	}
	public long getBytesWritten() {
		return bytesWritten;
	}
}
