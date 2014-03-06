package org.archive.format.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.archive.util.ByteOp;

public class GZIPFExtraRecord implements GZIPConstants {

	private byte[] name = null;
	private byte[] value = null;
	public GZIPFExtraRecord() {
	}
	public GZIPFExtraRecord(byte[] name, long intVal) 
		throws GZIPFormatException {

		if(name.length != GZIP_FEXTRA_NAME_BYTES) {
			throw new GZIPFormatException("FExtra name is 2 bytes");
		}
		this.name = name;
		value = new byte[4];
		ByteOp.writeInt(value, 0, intVal);
	}

	public GZIPFExtraRecord(byte[] name, byte[] value) 
		throws GZIPFormatException {

		if(name.length != GZIP_FEXTRA_NAME_BYTES) {
			throw new GZIPFormatException("FExtra name is 2 bytes");
		}
		if(value != null) {
			if(value.length > GZIP_FEXTRA_VALUE_MAX_LENGTH) {
				throw new GZIPFormatException("FExtra value max is " +
						GZIP_FEXTRA_VALUE_MAX_LENGTH + " bytes");
			}
		}
		this.name = name;
		this.value = value;
	}
	public boolean matchesName(byte name[]) {
		if(name == null) {
			return false;
		}
		if(this.name == null) {
			return false;
		}
		return ByteOp.cmp(this.name, name);
	}
	public byte[] getName() {
		return name;
	}
	public byte[] getValue() {
		return value;
	}
	public void setValue(byte[] value) {
		this.value = value;
	}
	/**
	 * @return the number of bytes used by this record:
	 *   0 - if the name is not set (invalid record),
	 *   4 - if the value is not set (name + 0-length value)
	 *   4 + value.length - if both are set (valid record, name + non-empty value)
	 */
	public int length() {
		if(name == null) {
			return 0;
		}
		if(value == null) {
			return GZIP_FEXTRA_VALUE_IDX;
		}
		return GZIP_FEXTRA_VALUE_IDX + value.length;
	}

	public void writeTo(byte[] buf, int offset) {
		if(buf.length - offset < length()) {
			throw new IndexOutOfBoundsException();
		}
		buf[offset+0] = name[0];
		buf[offset+1] = name[1];
		if(value == null) {
			ByteOp.writeShort(buf, offset + GZIP_FEXTRA_LENGTH_IDX, 0);
		} else {
			ByteOp.writeShort(buf, offset + GZIP_FEXTRA_LENGTH_IDX, value.length);
			System.arraycopy(value, GZIP_FEXTRA_VALUE_IDX, 
					buf, offset + GZIP_FEXTRA_LENGTH_IDX, value.length);
		}
	}
	public void writeTo(OutputStream os) throws IOException {
		if((name == null) || (value == null)) {
			return;
		}
		os.write(name);
		if(value == null) {
			ByteOp.writeShort(os, 0);
		} else {
			ByteOp.writeShort(os, value.length);
			os.write(value);
		}
	}
	public int read(InputStream is, int maxRead) throws IOException {
		byte tmpName[] = null;
		byte tmpVal[] = null;
		int valLen = 0;
		tmpName = ByteOp.readNBytes(is, GZIP_FEXTRA_NAME_BYTES);
		valLen = ByteOp.readShort(is);
		if (valLen > (maxRead - BYTES_IN_SHORT - GZIP_FEXTRA_NAME_BYTES)) {
			/* read in what's left, but throw an exception */
			tmpVal = ByteOp.readNBytes(is, maxRead - BYTES_IN_SHORT - GZIP_FEXTRA_NAME_BYTES);
			throw new GZIPFormatException.GZIPExtraFieldShortException(maxRead);
		}
		if(valLen > 0) {
			tmpVal = ByteOp.readNBytes(is, valLen);
		}
		name = tmpName;
		value = tmpVal;
		return GZIP_FEXTRA_NAME_BYTES + BYTES_IN_SHORT + valLen;
	}
	public int read(byte[] buf, int offset) 
		throws GZIPFormatException, IOException {

		byte tmpName[] = null;
		byte tmpVal[] = null;
		int valLen = 0;

		int remaining = buf.length - offset;
		if(remaining < GZIP_FEXTRA_VALUE_IDX) {
			throw new GZIPFormatException("Short bytes for FExtra field");
		}
		tmpName = ByteOp.copy(buf, offset, GZIP_FEXTRA_NAME_BYTES);
		valLen = ByteOp.bytesToShort(buf, offset + GZIP_FEXTRA_LENGTH_IDX);
		remaining -= GZIP_FEXTRA_NAME_IDX;
		if(valLen > 0) {
			if(valLen > remaining) {
				throw new GZIPFormatException("Short bytes for FExtra value");
			}
			
			tmpVal = ByteOp.copy(buf,offset + GZIP_FEXTRA_VALUE_IDX,valLen);
		}
		name = tmpName;
		value = tmpVal;
		return GZIP_FEXTRA_VALUE_IDX + valLen;		
	}
}
