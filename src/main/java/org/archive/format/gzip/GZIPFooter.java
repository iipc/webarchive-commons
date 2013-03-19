package org.archive.format.gzip;

import java.io.IOException;
import java.io.OutputStream;

import org.archive.util.ByteOp;

public class GZIPFooter implements GZIPConstants {
	byte buffer[] = null;

	public GZIPFooter(byte buffer[]) throws GZIPFormatException {
		if(buffer.length != GZIP_FOOTER_BYTES) {
			throw new GZIPFormatException("Wrong length footer");
		}
		this.buffer = buffer;
	}
	public GZIPFooter(long crc, long length) {
		buffer = new byte[GZIP_FOOTER_BYTES];
		ByteOp.writeInt(buffer, 0, crc);
		ByteOp.writeInt(buffer, BYTES_IN_INT, length);
	}
	public long getCRC() {
		return ByteOp.bytesToInt(buffer, 0);
	}
	public long getLength() {
		return ByteOp.bytesToInt(buffer, BYTES_IN_INT);
	}
	public void verify(long crc, long length) throws GZIPFormatException {
//		long gotCRC = getCRC() & 0xffffffff;
//		long gotCRC2 = getCRC();
//		int gotCRCi = (int) (getCRC() & 0xffffffff);
//		
//		long wantCRC = crc & 0xffffffff;
		int wantCRCi = (int) (crc & 0xffffffff);
		if(wantCRCi != getCRC()) {
			throw new GZIPFormatException("GZip crc error");
		}
		if(length != getLength()) {
			throw new GZIPFormatException("GZip length error");
		}
	}
	public void writeBytes(OutputStream os) throws IOException {
		os.write(buffer);
	}
}
