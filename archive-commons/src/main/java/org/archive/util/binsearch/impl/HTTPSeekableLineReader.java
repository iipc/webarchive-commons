package org.archive.util.binsearch.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.io.input.CountingInputStream;
import org.archive.util.binsearch.SeekableLineReader;

public class HTTPSeekableLineReader extends SeekableLineReader {
	public final static String CONTENT_LENGTH = "Content-Length";
	public final static String LAST_MODIFIED = "Last-Modified";
	
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

	private HttpClient http;
	private String url;
	private long length = -1;
	
	protected CountingInputStream cin;
		
	private GetMethod activeMethod;
	
	protected boolean noKeepAlive = false;
		
	public HTTPSeekableLineReader(HttpClient http, String url) {
		this.http = http;
		this.url = url;
	}
	
	private void acquireLength() throws URISyntaxException, HttpException, IOException {
		HttpMethod head = new HeadMethod(url);
		int code = http.executeMethod(head);
		if(code != 200) {
			throw new IOException("Unable to retrieve from " + url);
		}
		Header lengthHeader = head.getResponseHeader(CONTENT_LENGTH);
		if(lengthHeader == null) {
			throw new IOException("No Content-Length header for " + url);
		}
		String val = lengthHeader.getValue();
		try {
			length = Long.parseLong(val);
		} catch(NumberFormatException e) {
			throw new IOException("Bad Content-Length value " +url+ ": " + val);
		}
	}
	
	protected String getHeader(String header) throws URISyntaxException, HttpException, IOException {
		HttpMethod head = new HeadMethod(url);
		int code = http.executeMethod(head);
		if(code != 200) {
			throw new IOException("Unable to retrieve from " + url);
		}
		Header theHeader = head.getResponseHeader(header);
		if(theHeader == null) {
			throw new IOException("No " + header + " header for " + url);
		}
		String val = theHeader.getValue();
		return val;
	}	
	
	public String getUrl()
	{
		return url;
	}
		
//	public void seek(long offset, boolean gzip) throws IOException {		
//		is = doSeekLoad(offset, -1);
//				
//		if (gzip) {
//    		is = new GZIPMembersInputStream(is, blockSize);
//    	}
//	}
	
//	public void seekWithMaxRead(long offset, boolean gzip, int maxLength) throws IOException {
//		is = doSeekLoad(offset, maxLength);
//		
//		if (bufferFully && (maxLength > 0) && (maxLength < 1e10)) {
//			try {
//				byte[] buffer = new byte[maxLength];
//				ByteStreams.readFully(is, buffer);
//				is.close();
//				
//				// Create new stream
//				is = new ByteArrayInputStream(buffer);
//			} finally {
//				activeMethod.releaseConnection();
//				activeMethod = null;				
//			}
//		}
//    	
//		if (gzip) {
//    		is = new GZIPMembersInputStream(is, blockSize);
//    	} 
//    }
	
	protected InputStream doSeekLoad(long offset, int maxLength) throws IOException {
		if (activeMethod != null) {
			doClose();
		}
		
		br = null;
		
		try {
		
			activeMethod = new GetMethod(url);
			
			StringBuilder builder = new StringBuilder(24);
			builder.append("bytes=");
			builder.append(offset);
			builder.append('-');
			
			long endOffset = -1;
			
			if (maxLength > 0) {
				endOffset = (offset + maxLength) - 1;
				builder.append(endOffset);
			}
			
			activeMethod.setRequestHeader("Range", builder.toString()); 
			
			if (noKeepAlive) {
				activeMethod.setRequestHeader("Connection", "close");
			}
			
			int code = http.executeMethod(activeMethod);
			
			if ((code != 206) && (code != 200)) {
				throw new BadHttpStatusException(code, url + " " + offset + ":" + endOffset);
			}
			
			InputStream is = activeMethod.getResponseBodyAsStream();
			cin = new CountingInputStream(is);
			return cin;
			
		} catch (IOException io) {
			doClose();
			throw io;
		}
	}
	
	public GetMethod getHttpMethod()
	{
		return activeMethod;
	}

	public void doClose() throws IOException {
		
		if (activeMethod == null) {
			return;
		}
		
		try {
			long contentLength = activeMethod.getResponseContentLength();
			
			long bytesRead = (cin != null ? cin.getByteCount() : 0);
			
			// If fully read, close gracefully, otherwise abort
			if ((contentLength > 0) && (contentLength == bytesRead)) {
//				try {
//					cin.close();
//				} catch (IOException e) {
//					activeMethod.abort();
//				}
			} else {
				activeMethod.abort();
			}
			
			activeMethod.releaseConnection();
			activeMethod = null;
			
		} finally {
			if (activeMethod != null) {
				activeMethod.abort();
				activeMethod.releaseConnection();
				activeMethod = null;
			}
		}
		
		cin = null;
	}

	public long getSize() throws IOException {
		if (length < 0) {
			try {
				if (activeMethod != null) {
					length = activeMethod.getResponseContentLength();
				} else {
					acquireLength();
				}
			} catch (URISyntaxException e) {
				throw new IOException(e);
			}
		}
		return length;
	}
		
	public void setNoKeepAlive(boolean noKeepAlive)
	{
		this.noKeepAlive = noKeepAlive;
	}

	public String getHeaderValue(String headerName) {
		if (activeMethod == null) {
			return null;
		}
		
		Header header = activeMethod.getResponseHeader(headerName);
		
		if (header == null) {
			return null;
		}
		
		return header.getValue();
	}
}
