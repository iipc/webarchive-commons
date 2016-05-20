package org.archive.util.binsearch.impl;

import java.io.IOException;

import org.archive.util.binsearch.AbstractSeekableLineReader;

public abstract class HTTPSeekableLineReader extends AbstractSeekableLineReader {
	
	public static class BadHttpStatusException extends IOException
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int status;
		
		public BadHttpStatusException(int status, String details)
		{
			super("Http Status " + status + " returned: " + details);
			this.status = status;
		}
		
		public int getStatus()
		{
			return status;
		}
	}	

	public final static String CONTENT_LENGTH = "Content-Length";
	public final static String LAST_MODIFIED = "Last-Modified";
	
	protected boolean noKeepAlive;
	protected String cookie;
	protected String connectedUrl;
	protected String errHeader;
	protected String saveErrHeader;

	public abstract String getUrl();
	
	public abstract long getSize() throws IOException;

	public abstract String getHeaderValue(String headerName);
	
	public static String makeRangeHeader(long offset, int maxLength)
	{
		StringBuilder builder = new StringBuilder(32);
		builder.append("bytes=");
		builder.append(offset);
		builder.append('-');
		
		long endOffset = -1;
		
		if (maxLength > 0) {
			endOffset = (offset + maxLength) - 1;
			builder.append(endOffset);
		}
		
		return builder.toString();
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public boolean isNoKeepAlive() {
		return noKeepAlive;
	}

	public void setNoKeepAlive(boolean noKeepAlive) {
		this.noKeepAlive = noKeepAlive;
	}
	
	public String getConnectedUrl()
	{
		return connectedUrl;
	}

	public String getSaveErrHeader() {
		return saveErrHeader;
	}

	public void setSaveErrHeader(String saveErrHeader) {
		this.saveErrHeader = saveErrHeader;
	}

	public String getErrHeader() {
		return errHeader;
	}

	public void setErrHeader(String errHeader) {
		this.errHeader = errHeader;
	}
}