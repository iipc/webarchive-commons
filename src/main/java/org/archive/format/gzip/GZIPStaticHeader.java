package org.archive.format.gzip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.archive.util.ByteOp;


/**
 * The first 10-bytes of a GZip Member record. Exactly 10 bytes.
 * @author Brad
 * 
 * +---+---+---+---+---+---+---+---+---+---+
 * |ID1|ID2|CM |FLG|     MTIME     |XFL|OS | (more--&gt;)
 * +---+---+---+---+---+---+---+---+---+---+
 */
public class GZIPStaticHeader implements GZIPConstants {
	
	private final static int GZIP_STATIC_HEADER_SIZE_MINUS_3 = 
		GZIP_STATIC_HEADER_SIZE - 3;
	public final static byte[] DEFAULT_HEADER_DATA = 
       {
		(byte) (GZIP_MAGIC_ONE & 0xff),
		(byte) (GZIP_MAGIC_TWO & 0xff),
		(byte) (GZIP_COMPRESSION_METHOD_DEFLATE & 0xff),
                       0x00,0x00,0x00,0x00,0x00,0x00,0x03};

	private byte[] data = null;
	public GZIPStaticHeader() {
		data = ByteOp.copy(DEFAULT_HEADER_DATA);
	}
	public GZIPStaticHeader(InputStream is, boolean assume1st3) 
	throws GZIPFormatException, IOException {

		data = ByteOp.copy(DEFAULT_HEADER_DATA);
		// TODO loop for full read
		int amt = is.read(data, 3, GZIP_STATIC_HEADER_SIZE_MINUS_3);
		if(amt != GZIP_STATIC_HEADER_SIZE_MINUS_3) {
			throw new GZIPFormatException("Short header");
		}
		validateBuffer();
	}
	public GZIPStaticHeader(InputStream is) throws GZIPFormatException, 
	IOException {
		try {
			data = ByteOp.readNBytes(is, GZIP_STATIC_HEADER_SIZE);
		} catch (EOFException e) {
			throw new GZIPFormatException("Short header",e);
		}
		validateBuffer();
	}
	public GZIPStaticHeader(byte[] data) throws GZIPFormatException {
		if(data.length != GZIP_STATIC_HEADER_SIZE) {
			throw new GZIPFormatException("Short header");
		}
		this.data = data;
		validateBuffer();
	}
	public int getLength() {
		return GZIP_STATIC_HEADER_SIZE;
	}
	public void writeTo(byte[] buf, int offset) {
		if(buf.length - offset < GZIP_STATIC_HEADER_SIZE) {
			throw new IndexOutOfBoundsException();
		}
		System.arraycopy(data, 0, buf, offset, GZIP_STATIC_HEADER_SIZE);
	}
	public void writeTo(OutputStream os) throws IOException {
		os.write(data);
	}
	private void validateBuffer() throws GZIPFormatException {
		if((data[GZIP_MAGIC_ONE_IDX] & 0xff) != GZIP_MAGIC_ONE) {
			throw new GZIPFormatException("bad magic 1");
		}
		if((data[GZIP_MAGIC_TWO_IDX] & 0xff) != GZIP_MAGIC_TWO) {
			throw new GZIPFormatException("bad magic 2");
		}
		if((data[GZIP_COMPRESSION_METHOD_IDX] & 0xff) != GZIP_COMPRESSION_METHOD_DEFLATE) {
			throw new GZIPFormatException("bad compression method");
		}
		byte flg = (byte) (data[GZIP_FLAG_IDX] & 0xff);
		if((flg & GZIP_FLAG_VALID_BITS) != flg) {
			throw new GZIPFormatException("bad flag bits");
		}
		// all else can be anything...
	}
	private void setFLG(int flag, boolean val) {
		if(val) {
			data[GZIP_FLAG_IDX] = (byte) (data[GZIP_FLAG_IDX] | (byte)(flag));
		} else {
			data[GZIP_FLAG_IDX] = (byte) (data[GZIP_FLAG_IDX] & (byte)(~flag));
		}
	}
	private boolean isFLGSet(int flag) {
		return (data[GZIP_FLAG_IDX] & flag) == flag;
	}
	public int getIntVal(int offset) {
		return data[offset] & 0xff;
	}
	public int getOS() {
		return getIntVal(GZIP_OS_IDX);
	}
	public long getMTime() {
		return ByteOp.bytesToInt(data, GZIP_MTIME_IDX);
	}
	public boolean isFTextSet() {
		return isFLGSet(GZIP_FLAG_FTEXT); 
	}
	public void setFTextFlag(boolean val) {
		setFLG(GZIP_FLAG_FTEXT,val);
	}
	public boolean isFHCRCSet() {
		return isFLGSet(GZIP_FLAG_FHCRC); 
	}
	public void setFHCRCFlag(boolean val) {
		setFLG(GZIP_FLAG_FHCRC,val);
	}
	public boolean isFExtraSet() {
		return isFLGSet(GZIP_FLAG_FEXTRA);
	}
	public void setFExtraFlag(boolean val) {
		setFLG(GZIP_FLAG_FEXTRA,val);
	}
	public boolean isFNameSet() {
		return isFLGSet(GZIP_FLAG_FNAME); 
	}
	public void setFNameFlag(boolean val) {
		setFLG(GZIP_FLAG_FNAME,val);
	}
	public boolean isFCommentSet() {
		return isFLGSet(GZIP_FLAG_FCOMMENT); 
	}
	public void setFCommentFlag(boolean val) {
		setFLG(GZIP_FLAG_FCOMMENT,val);
	}
}
