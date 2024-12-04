package org.archive.util.binsearch.impl;

import java.io.IOException;

import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.impl.http.ApacheHttp31SLRFactory;
import org.archive.util.binsearch.impl.http.ApacheHttp43SLRFactory;
import org.archive.util.binsearch.impl.http.HTTPURLConnSLRFactory;

public abstract class HTTPSeekableLineReaderFactory implements SeekableLineReaderFactory {

	public abstract HTTPSeekableLineReader get(String url) throws IOException;
	
	protected String defaultURL;
	
	protected HTTPSeekableLineReaderFactory()
	{
		
	}
	
	public enum HttpLibs
	{
		@Deprecated
		APACHE_31,
		APACHE_43,
		URLCONN,
	}
		
	public static HTTPSeekableLineReaderFactory getHttpFactory()
	{
		return getHttpFactory(HttpLibs.APACHE_31);
	}
	
	public static HTTPSeekableLineReaderFactory getHttpFactory(HttpLibs type)
	{
		return getHttpFactory(type, null);
	}
	
	public static HTTPSeekableLineReaderFactory getHttpFactory(String defaultURL)
	{
		return getHttpFactory(HttpLibs.APACHE_31, defaultURL);
	}
	
	public static HTTPSeekableLineReaderFactory getHttpFactory(HttpLibs type, String defaultURL)
	{
		HTTPSeekableLineReaderFactory factory = null;
		
		switch (type) {
		case APACHE_31:
			factory = new ApacheHttp31SLRFactory();
			break;
			
		case URLCONN:
			factory = new HTTPURLConnSLRFactory();
			break;
			
		case APACHE_43:
			factory = new ApacheHttp43SLRFactory();
			break;
		}
		
		if (factory == null) {
			factory = new ApacheHttp31SLRFactory();
		}
		
		factory.defaultURL = defaultURL;
		
		return factory;
	}
	
	@Override
	public HTTPSeekableLineReader get() throws IOException {
		//TODO: improve interface
		return get(defaultURL);
	}
	
	/**
	 * 
	 */
	public abstract void close() throws IOException;

	/**
	 * @param hostPort to proxy requests through - ex. "localhost:3128"
	 */
	public abstract void setProxyHostPort(String hostPort);

	/**
	 * @param maxTotalConnections the HttpConnectionManagerParams config
	 */
	public abstract void setMaxTotalConnections(int maxTotalConnections);

	/**
	 * @return the HttpConnectionManagerParams maxTotalConnections config
	 */
	public abstract int getMaxTotalConnections();

	/**
	 * @param maxHostConnections the HttpConnectionManagerParams config 
	 */
	public abstract void setMaxHostConnections(int maxHostConnections);

	/**
	 * @return the HttpConnectionManagerParams maxHostConnections config 
	 */
	public abstract int getMaxHostConnections();

	/**
	 * @return the connectionTimeoutMS
	 */
	public abstract int getConnectionTimeoutMS();

	/**
	 * @param connectionTimeoutMS the connectionTimeoutMS to set
	 */
	public abstract void setConnectionTimeoutMS(int connectionTimeoutMS);

	/**
	 * @return the socketTimeoutMS
	 */
	public abstract int getSocketTimeoutMS();

	/**
	 * @param socketTimeoutMS the socketTimeoutMS to set
	 */
	public abstract void setSocketTimeoutMS(int socketTimeoutMS);

	public abstract void setStaleChecking(boolean enabled);

	public abstract boolean isStaleChecking();

	// Experimental
	public abstract long getModTime();
	
	public void reload()
	{
		
	}

	public abstract void setNumRetries(int numRetries);
}