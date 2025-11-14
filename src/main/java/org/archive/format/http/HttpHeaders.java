package org.archive.format.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import org.archive.util.ByteOp;
import org.archive.util.DateUtils;

/**
 * @author brad
 *
 */
public class HttpHeaders extends ArrayList<HttpHeader> 
implements HttpHeaderObserver {
	
	private static final Logger LOG =
		Logger.getLogger(HttpHeaders.class.getName());
	
	/** */
	private static final long serialVersionUID = 5737284156382429120L;
	private boolean isCorrupt = false;
	private int totalBytes;

	public void setDateHeader(String name, long ms) {
		setDateHeader(name,new Date(ms));
	}
	public void setDateHeader(String name, Date d) {
		String dv = DateUtils.getRFC1123Date(d);
		set(name, dv);
	}
	public void addDateHeader(String name, long ms) {
		addDateHeader(name,new Date(ms));
	}
	public void addDateHeader(String name, Date d) {
		String dv = DateUtils.getRFC1123Date(d);
		add(name, dv);
	}

	public HttpHeader get(String name) {
		for(HttpHeader h : this) {
			if(h.getName().equals(name)) {
				return h;
			}
		}
		return null;
	}

	public String getValue(String name) {
		HttpHeader header = get(name);
		return header == null ? null : header.getValue();
	}

	public String getValueCaseInsensitive(String name) {
		String lc = name.toLowerCase(Locale.ROOT);
		for(HttpHeader h : this) {
			if(h.getName().toLowerCase(Locale.ROOT).equals(lc)) {
				return h.getValue();
			}
		}
		return null;
	}

	public long getContentLength() {
		String val = getValueCaseInsensitive("content-length");
		if(val != null) {
			try {
				return Long.parseLong(val);
			} catch(NumberFormatException e) {
				LOG.warning(e.getMessage());
			}
		}
		return -1;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("HttpHeaders:\n==========\n");
		for(HttpHeader header : this) {
			sb.append("\t").append(header.toString()).append("\n");
		}
		sb.append("========\n");
		return sb.toString();
	}
	public void headerParsed(byte[] name, int ns, int nl, byte[] value, int vs,
			int vl) {
		String sName = new String(ByteOp.copy(name, 0, nl), UTF8);
		String sValue = new String(ByteOp.copy(value,0,vl), UTF8);
//		String sName = new String(name,0,nl,UTF8);
//		String sValue = new String(value,0,vl,UTF8);
		add(new HttpHeader(sName, sValue));
	}

	public void headersComplete(int bytesRead) {
		totalBytes = bytesRead;
	}
	public void headersCorrupt() {
		isCorrupt = true;
	}
	public boolean isCorrupt() {
		return isCorrupt;
	}
	/**
	 * Only valid if these Headers were read via an HTTP Parser.
	 * 
	 * @return the number of bytes read to produce these headers
	 */
	public int getTotalBytes() {
		return totalBytes;
	}
	/**
	 * Add a new Header with the given name/value, or replace an existing value
	 * if a Header already exists with name
	 * 
	 * @param name
	 * @param value
	 */
	public void set(String name, String value) {
		HttpHeader header = get(name);
		if(header == null) {
			add(name,value);
		} else {
			header.setValue(value);
		}
	}
	/**
	 * Add a new Header with the given Name/Value, allowing duplicates, which
	 * may not be what you want.
	 * @param name
	 * @param value
	 */
	public void add(String name, String value) {
		add(new HttpHeader(name, value));
	}
	
	/**
	 * Write all Headers and a trailing CRLF, CRLF
	 * @param out
	 * @throws IOException
	 */
	public void write(OutputStream out) throws IOException {
		for(HttpHeader header: this) {
			header.write(out);
		}
		out.write(CR);
		out.write(LF);
	}
}
