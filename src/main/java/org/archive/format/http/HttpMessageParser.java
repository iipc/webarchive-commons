package org.archive.format.http;

import java.util.Locale;

public class HttpMessageParser implements HttpConstants {
	
	protected int parseVersionStrict(byte buf[], int start, int len)
	throws HttpParseException {
	
		String v = new String(buf,start,len,UTF8);
		if(v.compareTo(VERSION_0_STATUS) == 0) {
			return VERSION_0;
		} else if(v.compareTo(VERSION_1_STATUS) == 0) {
			return VERSION_1;
		} else if(v.compareTo(VERSION_9_STATUS) == 0) {
			return VERSION_9;
		} else {
			throw new HttpParseException("Unknown version");
		}
	}

	protected int parseVersionLax(byte buf[], int start, int len)
	throws HttpParseException {
	
		String v = new String(buf,start,len,UTF8);
		if(v.toLowerCase(Locale.ROOT).compareTo(VERSION_0_STATUS.toLowerCase(Locale.ROOT)) == 0) {
			return VERSION_0;
		} else if(v.toLowerCase(Locale.ROOT).compareTo(VERSION_1_STATUS.toLowerCase(Locale.ROOT)) == 0) {
			return VERSION_1;
		} else if(v.toLowerCase(Locale.ROOT).compareTo(VERSION_9_STATUS.toLowerCase(Locale.ROOT)) == 0) {
			return VERSION_9;
		}
		return VERSION_0;
	}

}
