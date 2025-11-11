package org.archive.format.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class HttpRequestMessageParser extends HttpMessageParser {
	public int maxBytes = 1024 * 1024;
	public boolean strict = false;
	public HttpRequestMessage parse(InputStream is) throws HttpParseException, IOException {
		HttpRequestMessage message = new HttpRequestMessage();
		parse(is,message);
		return message;
	}

	public int parse(InputStream is, HttpRequestMessageObserver obs) throws HttpParseException, IOException {
		byte buf[] = new byte[maxBytes];
		int bytesRead = 0;
		
		while(bytesRead < maxBytes) {
			int i = is.read();
			if(i == -1) {
				if(strict) {
					throw new HttpParseException("EOF before CRLF");
				}
				obs.messageCorrupt();
				return bytesRead;
			}
			if(i > 127) {
				if(strict) {
					throw new HttpParseException("Non ASCII byte in message");
				}
				obs.messageCorrupt();
				return bytesRead;
			}
			byte b = (byte) (i & 0xff);
			buf[bytesRead] = b;
			bytesRead++;
			if(b == LF) {
				return parse(buf,bytesRead,obs);
			}
		}
		// TODO: under Lax consume till EOL and continue?
		throw new HttpParseException("Response Message too long");

	}
	
	public int parse(byte buf[], int len, HttpRequestMessageObserver obs) 
	throws HttpParseException, IOException {
		return strict ? parseStrict(buf,len,obs) : parseLax(buf,len,obs);
	}


	public int parseStrict(byte buf[], int len, HttpRequestMessageObserver obs) throws HttpParseException {
		int origLen = len;
		if(buf[len-1] != LF) {
			throw new HttpParseException("Response Message missing LF");
		}
		len--;
		if(buf[len-1] != CR) {
			throw new HttpParseException("Response Message missing CRLF");
		}
		len--;
		
		int version = VERSION_0;
		int method = 0;
		String path = null;

		int idx = 0;
		int ms = 0;
		int ml = 0;
		int ps = -1;
		int pl = 0;
		int vs = -1;
		int vl = 0;
		while(buf[idx] != SP) {
			ml++;
			idx++;
			if(idx >= len) {
				throw new HttpParseException("No spaces in message");
			}
		}
		if(idx == 0) {
			throw new HttpParseException("Http Request starts with SP");
		}
		method = parseMethodStrict(buf, ms, idx);
		idx++;
		ps = idx;
		while(buf[idx] != SP) {
			pl++;
			idx++;
			if(idx >= len) {
				throw new HttpParseException("No spaces in message");
			}
		}
		if(pl == 0) {
			throw new HttpParseException("Empty Path");
		}
		path = new String(buf,ps,pl,UTF8);
		idx++;
		vs = idx;
		vl = len - vs;
		while(idx < len) {
			if(buf[idx] == SP) {
				throw new HttpParseException("Too many fields in HTTP Request");
			}
			idx++;
		}
		version = parseVersionStrict(buf, vs,vl);
		
		obs.messageParsed(method,path,version, origLen);

		return origLen;
	}

	public int parseLax(byte buf[], int len, HttpRequestMessageObserver obs)
		throws HttpParseException {
		/* TODO: make this a lot more lax:
		 *       * auto trim leading and trailing whitespace
		 *       * first pass looks for 2 spaces, if found, go easy case
		 *       * if less than 2 whitespace, attempt to parse first and last
		 *             tokens as method and version, vary parsing based on that
		 *       * if more than 2 tokens. attempt to find leading method and
		 *             trailing version, and interpret intervening fields as
		 *             path.
		 *       * etc..
		 */
		int origLen = len;
		if(buf[len-1] != LF) {
			throw new HttpParseException("Response Message missing LF");
		}
		len--;
		if(buf[len-1] == CR) {
			len--;
		}
		
		int version = VERSION_0;
		int method = METHOD_UNK;
		String path = "";

		int idx = 0;
		int ms = 0;
		int ml = 0;
		int ps = -1;
		int pl = 0;
		int vs = -1;
		int vl = 0;
		
		// consume leading spaces:
		while(buf[idx] == SP) {
			idx++;
			if(idx >= len) {
				throw new HttpParseException("No spaces in message");
			}
		}
		ms = idx;
		while(buf[idx] != SP) {
			ml++;
			idx++;
			if(idx >= len) {
				throw new HttpParseException("No spaces in message");
			}
		}

		method = parseMethodLax(buf, ms, ml);
		while(buf[idx] == SP) {
			idx++;
			if(idx >= len) {
				throw new HttpParseException("No spaces in message");
			}
		}
		ps = idx;
		while(buf[idx] != SP) {
			pl++;
			idx++;
			if(idx >= len) {
				throw new HttpParseException("No spaces in message");
			}
		}
		if(pl > 0) {
			path = new String(buf,ps,pl,UTF8);
		}
		while(buf[idx] == SP) {
			idx++;
			if(idx >= len) {
				throw new HttpParseException("No spaces in message");
			}
		}
		vs = idx;
		while(idx < len) {
			if(buf[idx] == SP) {
				break;
			}
			vl++;
			idx++;
		}
		version = parseVersionLax(buf, vs,vl);

		obs.messageParsed(method,path,version, origLen);
		return len;
	}

	protected int parseMethodStrict(byte buf[], int start, int len)
	throws HttpParseException {
		String v = new String(buf,start,len,UTF8);
		if(v.compareTo(METHOD_GET_STRING) == 0) {
			return METHOD_GET;
		} else if(v.compareTo(METHOD_HEAD_STRING) == 0) {
			return METHOD_HEAD;
		} else if(v.compareTo(METHOD_POST_STRING) == 0) {
			return METHOD_POST;
		} else if(v.compareTo(METHOD_PUT_STRING) == 0) {
			return METHOD_PUT;
		} else if(v.compareTo(METHOD_TRACE_STRING) == 0) {
			return METHOD_TRACE;
		} else if(v.compareTo(METHOD_DELETE_STRING) == 0) {
			return METHOD_DELETE;
		} else if(v.compareTo(METHOD_CONNECT_STRING) == 0) {
			return METHOD_CONNECT;
		} else {
			throw new HttpParseException("Unknown version");
		}
	}

	protected int parseMethodLax(byte buf[], int start, int len)
	throws HttpParseException {
		String v = new String(buf,start,len,UTF8).toUpperCase(Locale.ROOT);
		if(v.compareTo(METHOD_GET_STRING) == 0) {
			return METHOD_GET;
		} else if(v.compareTo(METHOD_HEAD_STRING) == 0) {
			return METHOD_HEAD;
		} else if(v.compareTo(METHOD_POST_STRING) == 0) {
			return METHOD_POST;
		} else if(v.compareTo(METHOD_PUT_STRING) == 0) {
			return METHOD_PUT;
		} else if(v.compareTo(METHOD_TRACE_STRING) == 0) {
			return METHOD_TRACE;
		} else if(v.compareTo(METHOD_DELETE_STRING) == 0) {
			return METHOD_DELETE;
		} else if(v.compareTo(METHOD_CONNECT_STRING) == 0) {
			return METHOD_CONNECT;
		}
		return METHOD_UNK;
	}
}
