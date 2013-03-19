package org.archive.util.binsearch.impl.http;

import java.io.IOException;

import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;

public class HTTPURLConnSLRFactory extends HTTPSeekableLineReaderFactory {
	
	protected int connTimeout = 10000;
	protected int readTimeout = 10000;
	
	@Override
	public HTTPSeekableLineReader get(String url) throws IOException {
		return new HTTPURLConnSLR(url, connTimeout, readTimeout);
	}

	@Override
	public void close() throws IOException {
		
	}

	@Override
	public long getModTime() {
		return 0;
	}

	@Override
	public void setProxyHostPort(String hostPort) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaxTotalConnections(int maxTotalConnections) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxTotalConnections() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMaxHostConnections(int maxHostConnections) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxHostConnections() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getConnectionTimeoutMS() {
		return connTimeout;
	}

	@Override
	public void setConnectionTimeoutMS(int connectionTimeoutMS) {
		this.connTimeout = connectionTimeoutMS;		
	}

	@Override
	public int getSocketTimeoutMS() {
		return readTimeout;
	}

	@Override
	public void setSocketTimeoutMS(int socketTimeoutMS) {
		this.readTimeout = socketTimeoutMS;
	}

	@Override
	public void setStaleChecking(boolean enabled) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isStaleChecking() {
		// TODO Auto-generated method stub
		return false;
	}
}
