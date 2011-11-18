package org.archive.format.arc;

import java.nio.charset.Charset;

public interface ARCConstants {
	public final static int MAX_META_LENGTH = 1024 * 32;
	public final static Charset ARC_META_CHARSET = Charset.forName("utf-8");
	public final static int NEW_LINE_ORD = 10;
	public static final int CARRIAGE_RETURN_ORD = 13;
	public final static String DELIMITER = " ";

	public static final int FIELD_COUNT = 5;
	public static final String URL_KEY = "Target-URI";
	public static final String IP_KEY = "IP-Address";
	public static final String DATE_STRING_KEY = "Date";
	public static final String MIME_KEY = "Content-Type";
	public static final String DECLARED_LENGTH_KEY = "Content-Length";
	public static final String ACTUAL_LENGTH_KEY = "Actual-Length";
	public static final String LEADING_NEWLINES_KEY = "Leading-Newlines";
	public static final String TRAILING_NEWLINES_KEY = "Trailing-Newlines";
	public static final String TRAILING_CRUFT_KEY = "Trailing-Slop";
	public static final String HEADER_LENGTH = "Header-Length";
	
	public static final String DEFAULT_MIME = "x-archive/unknown";
	public static final String FILEDESC_SCHEME = "filedesc:/";
	public static final String DNS_MIME = "text/dns";
	public static final String ALEXA_DAT_MIME = "alexa/dat";
}
