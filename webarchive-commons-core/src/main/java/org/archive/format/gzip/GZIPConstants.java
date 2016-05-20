package org.archive.format.gzip;

public interface GZIPConstants {
	public static final int GZIP_STATIC_HEADER_SIZE = 10;
	public static final int GZIP_STATIC_FOOTER_SIZE = 8;

	public static final String NO_BYTES_IN_STREAM = "No bytes in stream";

	public static final int GZIP_MAGIC_ONE_IDX = 0;
	public static final int GZIP_MAGIC_ONE = 0x1f;

	public static final int GZIP_MAGIC_TWO_IDX = 1;
	public static final int GZIP_MAGIC_TWO = 0x8b;
	
	public static final int GZIP_COMPRESSION_METHOD_IDX = 2;
	public static final int GZIP_COMPRESSION_METHOD_DEFLATE = 0x08;
	
	public static final int GZIP_FLAG_IDX = 3;
	public static final int GZIP_FLAG_FTEXT = 0x01;
	public static final int GZIP_FLAG_FHCRC = 0x02;
	public static final int GZIP_FLAG_FEXTRA = 0x04;
	public static final int GZIP_FLAG_FNAME = 0x08;
	public static final int GZIP_FLAG_FCOMMENT = 0x10;
	public static final int GZIP_FLAG_VALID_BITS = 
		GZIP_FLAG_FTEXT | 
		GZIP_FLAG_FHCRC | 
		GZIP_FLAG_FEXTRA | 
		GZIP_FLAG_FNAME | 
		GZIP_FLAG_FCOMMENT;

	public static final int GZIP_MTIME_IDX = 4;
	public static final int GZIP_MTIME_LENGTH = 4;
	
	public static final int GZIP_XFL_IDX = 8;
	public static final int GZIP_OS_IDX = 9;
	public static final int GZIP_OS_UNIX = 0x03;

	public static final int GZIP_FEXTRA_NAME_BYTES = 2;
	public static final int GZIP_FEXTRA_LENGTH_BYTES = 2;
	public static final int GZIP_FEXTRA_VALUE_MAX_LENGTH = 65536;
	public static final int GZIP_FEXTRA_NAME_IDX = 0;
	public static final int GZIP_FEXTRA_LENGTH_IDX = 2;
	
	public static final int GZIP_FEXTRA_VALUE_IDX = 4;

	public static final byte[] LX_RECORD = {'L','X'};
	public static final byte[] LX_RECORD_VALUE = {0,0,0,0};
	
	public static final byte[] SL_RECORD = {'S','L'};
	public static final int BYTES_IN_SHORT = 2;
	public static final int BYTES_IN_INT = 4;

	public static final int GZIP_FOOTER_BYTES = BYTES_IN_INT * 2;
	
}
