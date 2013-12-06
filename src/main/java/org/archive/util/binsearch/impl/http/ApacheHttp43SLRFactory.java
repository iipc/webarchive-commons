package org.archive.util.binsearch.impl.http;

import java.io.IOException;

import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;

public class ApacheHttp43SLRFactory extends HTTPSeekableLineReaderFactory {
	
	private int readTimeout = 0;
	private int connectTimeout = 0;

	public ApacheHttp43SLRFactory()
	{
		
	}

	@Override
    public HTTPSeekableLineReader get(String url) throws IOException {
		return new ApacheHttp43SLR(url, connectTimeout, readTimeout);
    }

	@Override
    public void close() throws IOException {
	    // TODO Auto-generated method stub 
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
		return connectTimeout;
    }

	@Override
    public void setConnectionTimeoutMS(int connectionTimeoutMS) {
		connectTimeout  = connectionTimeoutMS;
	    
    }

	@Override
    public int getSocketTimeoutMS() {
		return readTimeout;
    }

	@Override
    public void setSocketTimeoutMS(int socketTimeoutMS) {
		readTimeout = socketTimeoutMS;
    }

	@Override
    public void setStaleChecking(boolean enabled) {

    }

	@Override
    public boolean isStaleChecking() {
	    // TODO Auto-generated method stub
	    return false;
    }

	@Override
    public long getModTime() {
	    // TODO Auto-generated method stub
	    return 0;
    }

	@Override
    public void setNumRetries(int numRetries) {
	    // TODO Auto-generated method stub
    }
}
