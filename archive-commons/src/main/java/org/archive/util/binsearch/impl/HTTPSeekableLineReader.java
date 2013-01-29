package org.archive.util.binsearch.impl;

import java.io.BufferedReader;
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

public class HTTPSeekableLineReader implements SeekableLineReader {
	private final static String CONTENT_LENGTH = "Content-Length";
	private int blockSize = 8 * 1024;
	private HttpClient http;
	private String url;
	private long length = -1;
	private BufferedReader br;
	private InputStreamReader isr;
	private HttpMethod activeMethod;
	
	protected boolean noKeepAlive = false;
	
	public HTTPSeekableLineReader(HttpClient http, String url, boolean noKeepAlive) {
		this.http = http;
		this.url = url;
		this.noKeepAlive = noKeepAlive;
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
	
	public void seekWithMaxRead(long offset, boolean gzip, int maxLength) throws IOException {
		
		if (activeMethod != null) {
			activeMethod.abort();
			close();
		}
		
		activeMethod = new GetMethod(url);
		
		long endOffset = (offset + maxLength) - 1;
		activeMethod.setRequestHeader("Range", 
				String.format("bytes=%d-%d", offset, endOffset));
		
		if (noKeepAlive) {
			activeMethod.setRequestHeader("Connection", "close");
		}
		int code = http.executeMethod(activeMethod);
		if((code != 206) && (code != 200)) {
			throw new IOException("Non 200/6 response code for " + url + " " + offset + ":" + endOffset);
		}
		InputStream is = activeMethod.getResponseBodyAsStream();
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

}
