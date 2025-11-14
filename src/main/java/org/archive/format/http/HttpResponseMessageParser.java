package org.archive.format.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponseMessageParser extends HttpMessageParser {
	public int maxBytes = 1024 * 128;
	public boolean strict = false;

	public HttpResponseMessage parseMessage(InputStream is) 
	throws HttpParseException, IOException {

		HttpResponseMessage message = new HttpResponseMessage();
		parse(is,message);
		return message;
	}

	public int parse(InputStream is, HttpResponseMessageObserver obs) 
	throws HttpParseException, IOException {
		int bytesRead = 0;
		
		byte buf[] = new byte[maxBytes];
		
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

	public int parse(byte buf[], int len, HttpResponseMessageObserver obs) 
	throws HttpParseException, IOException {
		return strict ? parseStrict(buf,len,obs) : parseLax(buf,len,obs);
	}

	public int parseStrict(byte buf[], int len, HttpResponseMessageObserver obs) 
	throws HttpParseException, IOException {
		
		if(buf[len-1] != CR) {
			obs.messageCorrupt();
			throw new HttpParseException("Response Message missing CRLF");
		}
		
		int version = VERSION_0;
		int status = 0;
		String reason = REASON_UNK;
		
		int idx = 0;
		int vs = 0;
		int vl = -1;
		int ss = -1;
		int sl = -1;
		while(idx < len) {
			if(buf[idx] != SP) {
				if(ss == -1) {
					// on version:
					vl++;
				} else {
					// on reason:
					vs++;
				}
			} else {
				if(ss == -1) {
					// were on version:
					ss = idx + 1;
				} else {
					break;
				}
			}
			idx++;
		}
		if(idx == len) {
			obs.messageCorrupt();
			throw new HttpParseException("Response Message Missing Fields");
		}
		// found all 3:
		version = parseVersionStrict(buf, vs, vl);
		status = parseStatusStrict(buf,ss,sl);
		
		reason = new String(buf,idx+1,(len - idx)-1,StandardCharsets.ISO_8859_1);

		obs.messageParsed(version, status, reason, len);

		return len;
	}

	private int parseLax(byte buf[], int len, HttpResponseMessageObserver obs) 
	throws HttpParseException, IOException {
		
		
		int version = VERSION_0;
		int status = 0;
		String reason = REASON_UNK;
		
		// skip leading spaces:
		int idx = 0;
		int vs = -1;
		int vl = 0;
		int ss = -1;
		int sl = 0;
		int bufferEnd = len - 1;
		if((len > 2) && (buf[len - 2] == CR)) {
			bufferEnd--;
		}
		while(idx < bufferEnd) {
			if(buf[idx] != SP) {
				break;
			}
			idx++;
		}
		vs = idx;
		while(idx < bufferEnd) {
			if(buf[idx] != SP) {
				if(ss == -1) {
					// on version:
					vl++;
				} else {
					// on reason:
					sl++;
				}
			} else {
				if(ss == -1) {
					// were on version:
					ss = idx + 1;
				} else {
					break;
				}
			}
			idx++;
		}
		if(idx < bufferEnd) {
			// found all 3:
			version = parseVersionLax(buf, vs, vl);
			status = parseStatusLax(buf,ss,sl);
			idx++;
			int reasonLen = bufferEnd - idx;
			if(reasonLen > 0) {
				reason = new String(buf,idx,reasonLen,StandardCharsets.ISO_8859_1);
			}
		} else {
			// missed some:
			if(vl > 0) {
				version = parseVersionLax(buf, vs, vl);
			}
			if(sl > 0) {
				status = parseStatusLax(buf,ss,sl);
			}
		}
		
		obs.messageParsed(version, status, reason, len);

		return len;
	}

	private int parseStatusStrict(byte buf[], int start, int len)
	throws HttpParseException {
		int status = 0;
		try {
			int testStatus = Integer.valueOf(new String(buf,start,len,UTF8));
			if((testStatus >= 100) && (testStatus < 600)) {
				status = testStatus;
			}
		} catch (NumberFormatException e) {
		}
		if(status == -1) {
			throw new HttpParseException("Bad status code in Response Message");
		}
		return status;
	}

	private int parseStatusLax(byte buf[], int start, int len)
	throws HttpParseException {
		int status = STATUS_UNK;
		try {
			int testStatus = Integer.valueOf(new String(buf,start,len,UTF8));
			if((testStatus >= 100) && (testStatus < 600)) {
				status = testStatus;
			}
		} catch (NumberFormatException e) {
		}
		return status;
	}
}
