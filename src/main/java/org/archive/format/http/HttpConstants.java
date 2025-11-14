package org.archive.format.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface HttpConstants {
	public static final Charset UTF8 = StandardCharsets.UTF_8;
	public static final byte CR = 13;
	public static final byte LF = 10;
	public static final byte SP = 32;
	public static final byte HTAB = 9;
	public static final byte COLON = 58;
	
	public static final int VERSION_9 = 9;
	public static final int VERSION_0 = 0;
	public static final int VERSION_1 = 1;

	public static final String VERSION_9_STATUS = "HTTP/0.9";
	public static final String VERSION_0_STATUS = "HTTP/1.0";
	public static final String VERSION_1_STATUS = "HTTP/1.1";

	public static final String VERSION_1_UNK = "UNK";
	public static final int STATUS_UNK = 0;
	public static final String REASON_UNK = "UNK";
	
	public static final String CRLF = "\r\n";
	
	public static final int METHOD_UNK     = 0;
	public static final int METHOD_GET     = 1;
	public static final int METHOD_HEAD    = 2;
	public static final int METHOD_POST    = 3;
	public static final int METHOD_PUT     = 4;
	public static final int METHOD_TRACE   = 5;
	public static final int METHOD_DELETE  = 6;
	public static final int METHOD_CONNECT = 7;

	public static final String METHOD_UNK_STRING     = "UNK";
	public static final String METHOD_GET_STRING     = "GET";
	public static final String METHOD_HEAD_STRING    = "HEAD";
	public static final String METHOD_POST_STRING    = "POST";
	public static final String METHOD_PUT_STRING     = "PUT";
	public static final String METHOD_TRACE_STRING   = "TRACE";
	public static final String METHOD_DELETE_STRING  = "DELETE";
	public static final String METHOD_CONNECT_STRING = "CONNECT";
}
