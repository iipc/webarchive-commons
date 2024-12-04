package org.archive.util.binsearch.impl.http;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;

/**
 *
 * @deprecated Commons HttpClient 3 is end of life, this will be removed in webarchive-commons 2.0
 */
@Deprecated
public class ApacheHttp31SLRFactory extends HTTPSeekableLineReaderFactory {
	private final static Logger LOGGER = Logger.getLogger(ApacheHttp31SLRFactory.class.getName());
	
	private HttpConnectionManager connectionManager = null;
    private HostConfiguration hostConfiguration = null;
    private HttpClient http = null;
    
    public ApacheHttp31SLRFactory(String uriString) {
    	this();
    }

    public ApacheHttp31SLRFactory() {
    	connectionManager = new MultiThreadedHttpConnectionManager();
    	//connectionManager = new ThreadLocalHttpConnectionManager();
    	hostConfiguration = new HostConfiguration();
		HttpClientParams params = new HttpClientParams();
    	http = new HttpClient(params,connectionManager);
    	http.setHostConfiguration(hostConfiguration);
    }
    
    public void close() throws IOException
    {
    	//connectionManager.deleteClosedConnections();
    	connectionManager.closeIdleConnections(0);
    }
	
	@Override
	public ApacheHttp31SLR get(String url) throws IOException {
		
//		if (LOGGER.isLoggable(Level.FINEST)) {
//			LOGGER.finest("Connections: " + connectionManager.getConnectionsInPool(hostConfiguration));
//		}
		
		return new ApacheHttp31SLR(http, url);
	}
    /* (non-Javadoc)
	 * @see org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory#setProxyHostPort(java.lang.String)
	 */
    @Override
	public void setProxyHostPort(String hostPort) {
    	int colonIdx = hostPort.indexOf(':');
    	if(colonIdx > 0) {
    		String host = hostPort.substring(0,colonIdx);
    		int port = Integer.valueOf(hostPort.substring(colonIdx+1));
    		
//            http.getHostConfiguration().setProxy(host, port);
    		hostConfiguration.setProxy(host, port);
    	}
    }
    /* (non-Javadoc)
	 * @see org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory#setMaxTotalConnections(int)
	 */
    @Override
	public void setMaxTotalConnections(int maxTotalConnections) {
    	connectionManager.getParams().
    		setMaxTotalConnections(maxTotalConnections);
    }
    /* (non-Javadoc)
	 * @see org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory#getMaxTotalConnections()
	 */
    @Override
	public int getMaxTotalConnections() {
    	return connectionManager.getParams().getMaxTotalConnections();
    }
 
    /* (non-Javadoc)
	 * @see org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory#setMaxHostConnections(int)
	 */
    @Override
	public void setMaxHostConnections(int maxHostConnections) {
    	connectionManager.getParams().setDefaultMaxConnectionsPerHost(maxHostConnections);
    	connectionManager.getParams().setMaxConnectionsPerHost(hostConfiguration, maxHostConnections);
    }

    /* (non-Javadoc)
	 * @see org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory#getMaxHostConnections()
	 */
    @Override
	public int getMaxHostConnections() {
    	return connectionManager.getParams().
    		getMaxConnectionsPerHost(hostConfiguration);
    }

    /* (non-Javadoc)
	 * @see org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory#getConnectionTimeoutMS()
	 */
	@Override
	public int getConnectionTimeoutMS() {
		return connectionManager.getParams().getConnectionTimeout();
	}

	/* (non-Javadoc)
	 * @see org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory#setConnectionTimeoutMS(int)
	 */
	@Override
	public void setConnectionTimeoutMS(int connectionTimeoutMS) {
    	connectionManager.getParams().setConnectionTimeout(connectionTimeoutMS);
    	http.getParams().setConnectionManagerTimeout(connectionTimeoutMS);
	}

	/* (non-Javadoc)
	 * @see org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory#getSocketTimeoutMS()
	 */
	@Override
	public int getSocketTimeoutMS() {
		return connectionManager.getParams().getSoTimeout();
	}

	/* (non-Javadoc)
	 * @see org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory#setSocketTimeoutMS(int)
	 */
	@Override
	public void setSocketTimeoutMS(int socketTimeoutMS) {
    	connectionManager.getParams().setSoTimeout(socketTimeoutMS);
	}
	
	/* (non-Javadoc)
	 * @see org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory#setStaleChecking(boolean)
	 */
	@Override
	public void setStaleChecking(boolean enabled)
	{
		connectionManager.getParams().setStaleCheckingEnabled(enabled);
	}
	
	/* (non-Javadoc)
	 * @see org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory#isStaleChecking()
	 */
	@Override
	public boolean isStaleChecking()
	{
		return connectionManager.getParams().isStaleCheckingEnabled();
	}
	
	// Experimental
	/* (non-Javadoc)
	 * @see org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory#getModTime()
	 */
	@Override
	public long getModTime()
	{
		HTTPSeekableLineReader reader = null;
		SimpleDateFormat lastModFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
	
		try {
			reader = get();
			String result = reader.getHeaderValue(HTTPSeekableLineReader.LAST_MODIFIED);
			Date date = lastModFormat.parse(result);
			return date.getTime();
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {

				}
			}
		}
		
		return 0;
	}

	@Override
    public void setNumRetries(int numRetries) {
		http.getParams().setParameter(HttpClientParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(numRetries, true));
    }
}
