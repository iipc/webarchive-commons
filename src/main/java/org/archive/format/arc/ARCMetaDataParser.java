package org.archive.format.arc;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;

import org.archive.util.DateUtils;
import org.archive.util.StringParse;

public class ARCMetaDataParser implements ARCConstants {
	private boolean strict = false;
	private byte[] buffer;
	
	public ARCMetaDataParser() {
		this(MAX_META_LENGTH);
	}

	public ARCMetaDataParser(int maxLength) {
		buffer = new byte[maxLength];
	}

	public ARCMetaData parse(InputStream is) 
	throws ARCFormatException, IOException {
		return parse(is,strict);
	}
	public ARCMetaData parse(InputStream is, boolean strict) 
	throws ARCFormatException, IOException {
		return parse(is,strict,false);
	}
	public ARCMetaData parseEOFOK(InputStream is) 
	throws ARCFormatException, IOException {
		return parseEOFOK(is,strict);
	}
	public ARCMetaData parseEOFOK(InputStream is, boolean strict) 
	throws ARCFormatException, IOException {
		return parse(is,strict,true);
	}

	public ARCMetaData parse(InputStream is, boolean strict, boolean emptyOK) 
	throws ARCFormatException, IOException {

		ARCMetaData data = new ARCMetaData();

		int bufPos = 0;
		int leadingNL = 0;
		boolean gotNonNL = false;
		boolean gotNL = false;
		boolean sawSpace = false;
		
		int bufLen = buffer.length;
		// TODO: lax on leading newlines?
		while(bufPos < bufLen) {
			int c = is.read();

			if(c == -1) {
				// EOF at start
				if(emptyOK && (bufPos == 0)) {
					return null;
				}
				throw new ARCFormatException("Got EOF before newline");
			} else if(c == 10) {
				if(gotNonNL) {
					gotNL = true;
					break;
				} else {
					leadingNL++;
					continue;
				}
			} else {
				gotNonNL = true;
				if(sawSpace) {
					// check for non-ASCII bytes, they are not allowed after URL
					if((c < 32) || (c > 127)) {
						throw new ARCFormatException("Non-ASCII CHARS in metadata.");
					}
				} else {
					// In the URL field, non-ASCII OK until we see a ' ':
					sawSpace = (c == 32);
				}
			}
			buffer[bufPos] = (byte) (c & 0xff);
			bufPos++;
		}
		if(!gotNL) {
			throw new ARCFormatException("No newline");
		}
		data.setLeadingNL(leadingNL);
		data.setHeaderLength(bufPos + 1);// +1 for the newline

		String line = new String(buffer,0,bufPos,ARC_META_CHARSET);
		String parts[] = line.split(DELIMITER,-1);

		if(parts.length == FIELD_COUNT) {

			parseUrl(data,parts[0],strict);
			parseIP(data,parts[1],strict);
			parseDate(data,parts[2],strict);
			parseMime(data,parts[3],strict);
			parseLength(data,parts[4],strict);

		} else if(strict) {
			throw new ARCFormatException("Wrong number of fields in("+line+")");
		} else if(parts.length < FIELD_COUNT) {
				throw new ARCFormatException("Too few fields in("+line+")");
		} else if(parts.length == FIELD_COUNT + 1) {
			
			// URL IP DATE MIME LEN
			// mostly likely extra is in the MIME, but could be in the URL...
			// for now, let's only allow in the MIME:
			
			parseUrl(data,parts[0],strict);
			parseIP(data,parts[1],strict);
			parseDate(data,parts[2],strict);
			parseMime(data,parts[3]+"%20"+parts[4],strict);
			parseLength(data,parts[5],strict);
	
			
		} else {

			// if they're all "blank" that is, if there are some trailing
			// spaces, that's OK in non-strict mode...
			for(int i=FIELD_COUNT; i < parts.length; i++) {
				if(parts[i].length() != 0) {
					throw new ARCFormatException("Extra fields in("+line+")");
				}
			}
			// TODO: warn
			parseUrl(data,parts[0],strict);
			parseIP(data,parts[1],strict);
			parseDate(data,parts[2],strict);
			parseMime(data,parts[3],strict);
			parseLength(data,parts[4],strict);

		}

		return data;
	}
	private void parseUrl(ARCMetaData data, final String u, boolean strict)
	throws ARCFormatException {
		if(u.length() < 1) {
			throw new ARCFormatException("Bad Url(" + u + ")");
		}
		data.setUrl(u);
	}
	private void parseIP(ARCMetaData data, final String ip, boolean strict)
	throws ARCFormatException {
		if(strict) {
			if(StringParse.isIP(ip)) {
				data.setIP(ip);
			} else {
				throw new ARCFormatException("Bad IP address(" + ip + ")");
			}
		} else {
			if(ip.length() < 1) {
				throw new ARCFormatException("Bad IP(" + ip + ")");
			}
			data.setIP(ip);
		}
	}
	private void parseDate(ARCMetaData data, final String ds, boolean strict)
	throws ARCFormatException {
		try {
			Date d = DateUtils.getDate(ds);
			data.setDateBoth(d,ds);
		} catch (ParseException e) {
			throw new ARCFormatException("Bad date(" + ds + ")");
		}
	}
	private void parseMime(ARCMetaData data, final String m, boolean strict)
	throws ARCFormatException {
		if(strict) {
			if(m.length() < 1) {
				throw new ARCFormatException("Bad Mime(" + m + ")");
			}
			data.setMime(m);
		} else {
			if(m.length() < 1) {
				data.setMime(DEFAULT_MIME);
			} else {
				data.setMime(m);
			}
		}
	}
	private void parseLength(ARCMetaData data, final String ls, boolean strict)
	throws ARCFormatException {

		try {
			long l = Long.valueOf(ls);
			if(l < 0) {
				throw new ARCFormatException("Bad Length(" + ls + ")");
			}
			data.setLength(l);
		} catch(NumberFormatException e) {
			throw new ARCFormatException("Bad Length(" + ls + ")");
		}
	}
}
