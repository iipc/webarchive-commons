package org.archive.util.binsearch.impl.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.input.CountingInputStream;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;

public class HTTPURLConnSLR extends HTTPSeekableLineReader {
	
	protected String url;
	protected int connTimeout;
	protected int readTimeout;
	
	protected HttpURLConnection httpUrlConn = null;
	
	protected CountingInputStream cin = null;
	

	public HTTPURLConnSLR(String url, int connTimeout, int socketTimeout)
	{
		this.url = url;
		this.connTimeout = connTimeout;
		this.readTimeout = socketTimeout;
	}
	
	@Override
	public String getUrl() {
		return this.url;
	}

	@Override
	public long getSize() throws IOException {
		return 0;
	}

	@Override
	public String getHeaderValue(String headerName) {
		if (httpUrlConn != null) {
			httpUrlConn.getHeaderField(headerName);
		}
		
		return null;
	}

	@Override
	protected InputStream doSeekLoad(long offset, int maxLength)
			throws IOException {
		
		URL theUrl = new URL(url);		
		
        URLConnection connection = theUrl.openConnection();
        
        httpUrlConn = (HttpURLConnection)connection;
        
        connection.setConnectTimeout(connTimeout);
        connection.setReadTimeout(readTimeout);
        
        String rangeHeader = makeRangeHeader(offset, maxLength);
        
        if (rangeHeader != null) {
        	httpUrlConn.addRequestProperty("Range", rangeHeader);
        }
        
		if (this.isNoKeepAlive()) {
			httpUrlConn.addRequestProperty("Connection", "close");
		}
		
		if (this.getCookie() != null) {
			httpUrlConn.addRequestProperty("Cookie", cookie);
		}
		
		httpUrlConn.connect();
		
		int code = httpUrlConn.getResponseCode();
		
		if ((code != 206) && (code != 200)) {
			throw new BadHttpStatusException(code, url + " " + rangeHeader);
		}
		
		InputStream is = httpUrlConn.getInputStream();
		cin = new CountingInputStream(is);
		return cin;
	}

	@Override
	protected void doClose() throws IOException {
		
		if (httpUrlConn == null) {
			return;
		}
		
		try {
			long contentLength = httpUrlConn.getContentLength();
			
			long bytesRead = (cin != null ? cin.getByteCount() : 0);
			
			// If fully read, close gracefully, otherwise abort
			if ((contentLength > 0) && (contentLength == bytesRead)) {
				try {
					cin.close();
				} catch (IOException e) {
					httpUrlConn.disconnect();
				}
			} else {
				httpUrlConn.disconnect();
			}
			
			httpUrlConn = null;
			
		} finally {
			if (httpUrlConn != null) {
				httpUrlConn.disconnect();
				httpUrlConn = null;
			}
		}
		
		cin = null;
		is = null;
		br = null;
	}
}
