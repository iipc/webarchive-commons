package org.archive.util.binsearch.impl.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.message.BasicHttpRequest;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;

public class ApacheHttp43SLR extends HTTPSeekableLineReader {

	private String urlString;
	
	private int connectTimeout = 0;
	private int readTimeout = 0;
	
	private Socket socket = null;
	private DefaultBHttpClientConnection activeConn = null;
	private HttpResponse response = null;
	
	private final static int BUFF_SIZE = 8192;
	
	public ApacheHttp43SLR(String url)
	{
		urlString = url;
	}
	
	public ApacheHttp43SLR(String url, int connectTimeout, int readTimeout)
	{
		this.urlString = url;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
	}
	
	@Override
    public String getUrl() {
	    return urlString;
    }

	@Override
    public long getSize() throws IOException {
		if (response == null) {
			return 0;
		}
		
		return response.getEntity().getContentLength();
    }

	@Override
    public String getHeaderValue(String headerName) {
		if (response == null) {
			return null;
		}
		
		Header header = response.getFirstHeader(headerName);
		if (header == null) {
			return null;
		}
		
		return header.getValue();
	}
	
	protected static int getPort(URL url)
	{
		int port = url.getPort();
		
		if (port > 0) {
			return port;
		}
		
		return url.getDefaultPort();
	}
	
    protected InputStream doSeekLoad(long offset, int maxLength, URL url)
            throws IOException {
		
		SocketAddress endpoint = null;
		
		try {
			socket = new Socket();
			endpoint = new InetSocketAddress(url.getHost(), getPort(url));
			socket.connect(endpoint, connectTimeout);
			
			activeConn = new DefaultBHttpClientConnection(BUFF_SIZE);
			activeConn.bind(socket);
			activeConn.setSocketTimeout(readTimeout);
			
			HttpRequest request = new BasicHttpRequest("GET", url.getFile(), HttpVersion.HTTP_1_1);
			
			String rangeHeader = makeRangeHeader(offset, maxLength);
			
			if (rangeHeader != null) {
				request.setHeader("Range", rangeHeader);
			}
			
			if (this.isNoKeepAlive()) {
				request.setHeader("Connection", "close");
			}
			
			if (this.getCookie() != null) {
				request.setHeader("Cookie", this.getCookie());
			}
			
			request.setHeader("Accept", "*/*");
			request.setHeader("Host", url.getHost());
			
			activeConn.sendRequestHeader(request);
			activeConn.flush();
						
			response = activeConn.receiveResponseHeader();
			
			int code = response.getStatusLine().getStatusCode();
			
			connectedUrl = url.toString();
			
			if (code > 300 && code < 400) {
				Header header = response.getFirstHeader("Location");
				
				doClose();
				
				if (header != null) {
					URL redirectURL = new URL(header.getValue());
					return doSeekLoad(offset, maxLength, redirectURL);
				}
			}
			
			if (code != 200 && code != 206) {
				throw new BadHttpStatusException(code, connectedUrl + " " + rangeHeader);
			}
			
			activeConn.receiveResponseEntity(response);
			
			return response.getEntity().getContent();
			
		} catch (HttpException e) {
			doClose();
			throw new IOException(e);
			
        } catch (IOException io) {
        	
			if (saveErrHeader != null) {
				errHeader = getHeaderValue(saveErrHeader);	
			}
			
			connectedUrl = url.toString();
			
			doClose();
			throw io;
        }
    }

	@Override
    protected void doClose() throws IOException {
		if (activeConn != null) {
			activeConn.close();
			activeConn = null;
			socket = null;
		} else if (socket != null) {
			socket.close();
			socket = null;
		}
		response = null;
	}

	@Override
    protected InputStream doSeekLoad(long offset, int maxLength)
            throws IOException {
		
		return doSeekLoad(offset, maxLength, new URL(urlString));
    }
}
