package org.archive.util.io;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

public class CRCInputStream extends InputStream {
	private InputStream is = null;
	private CRC32 crc = null;
	private long count = 0;
	public CRCInputStream(InputStream is) {
		this(is,new CRC32());
	}
	public CRCInputStream(InputStream is, CRC32 crc) {
		this.is = is;
		this.crc = crc;
		count = 0;
	}
	@Override
	public int read() throws IOException {
		int b = is.read();
		if(b != -1) {
			crc.update(b);
			count++;
		}
		return b;
	}
	public int read(byte[] b) throws IOException {
		return read(b,0,b.length);
	}
	public int read(byte[] b, int off, int len) throws IOException {
		int amt = is.read(b, off, len);
		if(amt > -1) {
			count += amt;
			crc.update(b, off, amt);
		}
		return amt;
	}
	public long getCRCValue() {
		return crc.getValue();
	}
	public long getByteCount() {
		return count;
	}
}
