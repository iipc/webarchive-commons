package org.archive.util.binsearch.impl;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
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
import org.archive.util.zip.GZIPMembersInputStream;

import com.google.common.io.ByteStreams;

public class HTTPSeekableLineReader extends SeekableLineReader {
	public final static String CONTENT_LENGTH = "Content-Length";
	public final static String LAST_MODIFIED = "Last-Modified";

	private HttpClient http;
	private String url;
	private long length = -1;
		
	private GetMethod activeMethod;
	
	protected boolean noKeepAlive = false;
	
	public static class AbortableInputStream extends FilterInputStream
	{
		protected GetMethod method;
		protected CountingInputStream cin;
		
		public AbortableInputStream(GetMethod method, InputStream input)
		{
			super(new CountingInputStream(input));
			cin = (CountingInputStream)super.in;
			this.method = method;
		}
		
		@Override
		public void close()
		{
			long contentLength = method.getResponseContentLength();
			long bytesRead = cin.getByteCount();
			
			// If fully read, close gracefully, otherwise abort
			if ((contentLength > 0) && (contentLength == bytesRead)) {
				try {
					this.in.close();
				} catch (IOException e) {
					method.abort();
				}
			} else {
				method.abort();
			}
			
			method.releaseConnection();
		}
	}
	
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
		
	public InputStream seek(long offset, boolean gzip) throws IOException {		
		is = doSeekLoad(offset, -1);
		
		is = new AbortableInputStream(activeMethod, is);
		
		if (gzip) {
    		is = new GZIPMembersInputStream(is, blockSize);
    	}
		
		return is;
	}
	
	public InputStream seekWithMaxRead(long offset, boolean gzip, int maxLength) throws IOException {
		is = doSeekLoad(offset, maxLength);
		
		if (bufferFully && (maxLength > 0) && (maxLength < 1e10)) {
			try {
				byte[] buffer = new byte[maxLength];
				ByteStreams.readFully(is, buffer);
				is.close();
				
				// Create new stream
				is = new ByteArrayInputStream(buffer);
			} finally {
				activeMethod.releaseConnection();
				activeMethod = null;				
			}
		} else {
			is = new AbortableInputStream(activeMethod, is);
		}
    	
		if (gzip) {
    		is = new GZIPMembersInputStream(is, blockSize);
    	} 
		
		return is;
    }
	
	protected InputStream doSeekLoad(long offset, int maxLength) throws IOException {
		if (activeMethod != null) {
			doClose();
		}
		br = null;
		
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
				//String.format("bytes=%d-%d", offset, endOffset));
		
		if (noKeepAlive) {
			activeMethod.setRequestHeader("Connection", "close");
		}
		
		int code = http.executeMethod(activeMethod);
		
		if ((code != 206) && (code != 200)) {
			throw new IOException("Non 200/6 response code for " + url + " " + offset + ":" + endOffset);
		}
		
		return activeMethod.getResponseBodyAsStream();
	}
	
	public GetMethod getHttpMethod()
	{
		return activeMethod;
	}

	public void doClose() throws IOException {
		if (activeMethod != null) {
			activeMethod.abort();
			activeMethod.releaseConnection();
			activeMethod = null;
		}
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
}
