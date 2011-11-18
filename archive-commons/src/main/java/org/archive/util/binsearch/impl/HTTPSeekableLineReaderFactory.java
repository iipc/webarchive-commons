package org.archive.util.binsearch.impl;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;

public class HTTPSeekableLineReaderFactory implements SeekableLineReaderFactory {
    private MultiThreadedHttpConnectionManager connectionManager = null;
    private HostConfiguration hostConfiguration = null;
    private HttpClient http = null;
    private String uriString;

    public HTTPSeekableLineReaderFactory(String uriString) {
    	connectionManager = new MultiThreadedHttpConnectionManager();
    	hostConfiguration = new HostConfiguration();
		HttpClientParams params = new HttpClientParams();
//        params.setParameter(HttpClientParams.RETRY_HANDLER, new NoRetryHandler());
    	http = new HttpClient(params,connectionManager);
    	http.setHostConfiguration(hostConfiguration);
    	this.uriString = uriString;
    }

	public SeekableLineReader get() throws IOException {
		try {
			return new HTTPSeakableLineReader(http, uriString);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}
    /**
     * @param hostPort to proxy requests through - ex. "localhost:3128"
     */
    public void setProxyHostPort(String hostPort) {
    	int colonIdx = hostPort.indexOf(':');
    	if(colonIdx > 0) {
    		String host = hostPort.substring(0,colonIdx);
    		int port = Integer.valueOf(hostPort.substring(colonIdx+1));
    		
//            http.getHostConfiguration().setProxy(host, port);
    		hostConfiguration.setProxy(host, port);
    	}
    }
    /**
     * @param maxTotalConnections the HttpConnectionManagerParams config
     */
    public void setMaxTotalConnections(int maxTotalConnections) {
    	connectionManager.getParams().
    		setMaxTotalConnections(maxTotalConnections);
    }
    /**
     * @return the HttpConnectionManagerParams maxTotalConnections config
     */
    public int getMaxTotalConnections() {
    	return connectionManager.getParams().getMaxTotalConnections();
    }
 
    /**
     * @param maxHostConnections the HttpConnectionManagerParams config 
     */
    public void setMaxHostConnections(int maxHostConnections) {
    	connectionManager.getParams().
    		setMaxConnectionsPerHost(hostConfiguration, maxHostConnections);
    }

    /**
     * @return the HttpConnectionManagerParams maxHostConnections config 
     */
    public int getMaxHostConnections() {
    	return connectionManager.getParams().
    		getMaxConnectionsPerHost(hostConfiguration);
    }

    /**
	 * @return the connectionTimeoutMS
	 */
	public int getConnectionTimeoutMS() {
		return connectionManager.getParams().getConnectionTimeout();
	}

	/**
	 * @param connectionTimeoutMS the connectionTimeoutMS to set
	 */
	public void setConnectionTimeoutMS(int connectionTimeoutMS) {
    	connectionManager.getParams().setConnectionTimeout(connectionTimeoutMS);
	}

	/**
	 * @return the socketTimeoutMS
	 */
	public int getSocketTimeoutMS() {
		return connectionManager.getParams().getSoTimeout();
	}

	/**
	 * @param socketTimeoutMS the socketTimeoutMS to set
	 */
	public void setSocketTimeoutMS(int socketTimeoutMS) {
    	connectionManager.getParams().setSoTimeout(socketTimeoutMS);
	}

}
