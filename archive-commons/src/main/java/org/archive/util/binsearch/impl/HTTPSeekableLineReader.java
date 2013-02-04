package org.archive.util.binsearch.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.zip.GZIPMembersInputStream;

import com.google.common.io.ByteStreams;

public class HTTPSeekableLineReader implements SeekableLineReader {
	public final static String CONTENT_LENGTH = "Content-Length";
	public final static String LAST_MODIFIED = "Last-Modified";
	private int blockSize = 128 * 1024;
	private HttpClient http;
	private String url;
	private long length = -1;
	private BufferedReader br;
	private InputStreamReader isr;
	private HttpMethod activeMethod;
	
	protected boolean noKeepAlive = false;
	protected boolean bufferFully = false;
	
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
	
	public void seek(long offset) throws IOException {
		if(activeMethod != null) {
			activeMethod.abort();
			activeMethod.releaseConnection();
		}
		activeMethod = new GetMethod(url);
		activeMethod.setRequestHeader("Range", 
				String.format("bytes=%d-", offset));
		
		if (noKeepAlive) {
			activeMethod.setRequestHeader("Connection", "close");
		}
		
		int code = http.executeMethod(activeMethod);
		if((code != 206) && (code != 200)) {
			throw new IOException("Non 200/6 response code for " + url + ":" + offset);
		}
    	isr = new InputStreamReader(activeMethod.getResponseBodyAsStream(), UTF8);
    	br = new BufferedReader(isr, blockSize);
	}
	
	protected InputStream seekReadInputStream(long offset, int maxLength) throws IOException {
		if (activeMethod != null) {
			activeMethod.abort();
			close();
		}
		
		activeMethod = new GetMethod(url);
		
		long endOffset = (offset + maxLength) - 1;
		
		StringBuilder builder = new StringBuilder(24);
		builder.append("bytes=");
		builder.append(offset);
		builder.append('-');
		builder.append(endOffset);
		activeMethod.setRequestHeader("Range", builder.toString()); 
				//String.format("bytes=%d-%d", offset, endOffset));
		
		if (noKeepAlive) {
			activeMethod.setRequestHeader("Connection", "close");
		}
		
		int code = http.executeMethod(activeMethod);
		
		if((code != 206) && (code != 200)) {
			throw new IOException("Non 200/6 response code for " + url + " " + offset + ":" + endOffset);
		}
		
		return activeMethod.getResponseBodyAsStream();
	}
		
	public void seekWithMaxRead(long offset, boolean gzip, int maxLength) throws IOException {
		
		InputStream is = seekReadInputStream(offset, maxLength);
		
		if (bufferFully) {
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
		}
    	
		if (gzip) {
    		is = new GZIPMembersInputStream(is, blockSize);
    	}   
    	
    	isr = new InputStreamReader(is, UTF8);
    	br = new BufferedReader(isr, blockSize);
    }

	public String readLine() throws IOException {
		if(br == null) {
			seek(0);
		}
		return br.readLine();
	}
	
	public long getOffset() throws IOException {
		//Unsupported for now
		return -1;
	}

	public void close() throws IOException {
		if (activeMethod != null) {
			activeMethod.releaseConnection();
			activeMethod = null;
		}
		
		if (br != null) {
			br.close();
			br = null;
		}
		
		if (isr != null) {
			isr = null;
		}
	}

	public long getSize() throws IOException {
		if (length < 0) {
			try {
				acquireLength();
			} catch (URISyntaxException e) {
				throw new IOException(e);
			}
		}
		return length;
	}
	
	public void setBufferFully(boolean fully)
	{
		this.bufferFully = fully;
	}
	
	public void setNoKeepAlive(boolean noKeepAlive)
	{
		this.noKeepAlive = noKeepAlive;
	}
}
