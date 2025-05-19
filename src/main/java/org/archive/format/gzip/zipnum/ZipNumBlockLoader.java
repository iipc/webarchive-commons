package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.HMACSigner;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory.HttpLibs;
import org.archive.util.io.RuntimeIOException;

public class ZipNumBlockLoader {
		
	private final static Logger LOGGER = Logger.getLogger(ZipNumBlockLoader.class.getName());
	
	protected Map<String, SeekableLineReaderFactory> fileFactoryMap = null;
	protected HTTPSeekableLineReaderFactory httpFactory = null;
	
	// Request signing
	final static int DEFAULT_SIG_DURATION_SECS = 10;
	
	protected HMACSigner signer;
	protected int signDurationSecs = DEFAULT_SIG_DURATION_SECS;
	
	protected boolean useNio = false;
	protected String httpLib = HttpLibs.APACHE_43.name();
	
	protected boolean bufferFully = true;
	protected boolean noKeepAlive = true;
	protected String cookie = null;
	
	protected int maxHostConnections = 100000;
	protected int maxTotalConnections = 100000;
	
	protected int connectTimeoutMS = 10000;
	protected int readTimeoutMS = 10000;
	
	protected int numRetries = -1;
	
	protected boolean staleChecking = false;

	
	public ZipNumBlockLoader()
	{

	}
	
	private static ThreadLocal<Map<String, SeekableLineReader> > slrMap = new ThreadLocal<Map<String, SeekableLineReader> >()
	{
		@Override
		protected Map<String, SeekableLineReader> initialValue() {
			return new HashMap<String, SeekableLineReader>();
		}
	};
	
	public static void closeAllReaders()
	{
		for (Entry<String, SeekableLineReader> entry : slrMap.get().entrySet()) {
			try {
				SeekableLineReader reader = entry.getValue();
				if (!reader.isClosed()) {
					LOGGER.warning("Unclosed reader for: " + entry.getKey());
					reader.close();
				}
			} catch (IOException io) {
				
			}
		}
		
		slrMap.get().clear();
	}
	
	public SeekableLineReader createBlockReader(String uri) throws IOException
	{
		SeekableLineReader reader = null;
		
		if (GeneralURIStreamFactory.isHttp(uri)) {
			reader = getHttpReader(uri);
		} else {
			reader = getFileReader(uri);
		}
		
		slrMap.get().put(uri, reader);
				
		return reader;
	}
	
	protected HTTPSeekableLineReader getHttpReader(String url) throws IOException {
		
		if (httpFactory == null) {
			httpFactory = HTTPSeekableLineReaderFactory.getHttpFactory(HttpLibs.valueOf(httpLib), null);
			httpFactory.setMaxHostConnections(maxHostConnections);
			httpFactory.setMaxTotalConnections(maxTotalConnections);
			httpFactory.setConnectionTimeoutMS(connectTimeoutMS);
			httpFactory.setSocketTimeoutMS(readTimeoutMS);
			httpFactory.setStaleChecking(staleChecking);
			
			if (numRetries >= 0) {
				httpFactory.setNumRetries(numRetries);
			}
		}
		
		HTTPSeekableLineReader reader = httpFactory.get(url);
		reader.setBufferFully(bufferFully);
		reader.setNoKeepAlive(noKeepAlive);
		
		String reqCookie = cookie;
		
		if (signer != null) {
			reqCookie = signer.getHMacCookieStr(signDurationSecs);
		}
		
		if (reqCookie != null) {
			reader.setCookie(reqCookie);
		}
		
		return reader;
	}
	
	protected SeekableLineReader getFileReader(String filename) throws IOException {
		
		if (fileFactoryMap == null) {
			fileFactoryMap = new HashMap<String, SeekableLineReaderFactory>();
			fileFactoryMap = Collections.synchronizedMap(fileFactoryMap);
		}
		
		SeekableLineReaderFactory factory = fileFactoryMap.get(filename);
		
		if (factory == null) {
			factory = GeneralURIStreamFactory.createSeekableStreamFactory(filename, useNio);
			fileFactoryMap.put(filename, factory);
		}
		
		SeekableLineReader reader = factory.get();
		reader.setBufferFully(bufferFully);
		return reader;
	}
	
	public SeekableLineReader attemptLoadBlock(String location, long startOffset, int totalLength, boolean decompress, boolean isRequired)
	{
		SeekableLineReader currReader = null;
		
		try {
			currReader = createBlockReader(location);
			
	        currReader.seekWithMaxRead(startOffset, decompress, totalLength);
		
		} catch (IOException io) {
			Level level = (isRequired ? Level.SEVERE : Level.WARNING);
			
			String actualLocation = null;
			
			if (currReader instanceof HTTPSeekableLineReader) {
				actualLocation = ((HTTPSeekableLineReader)currReader).getConnectedUrl();
			}
			
			if (actualLocation == null) {
				actualLocation = location;
			}
			
			String msg = io.toString() + " -- -r " + startOffset + "-" + (startOffset + totalLength - 1) + " " + actualLocation;
			
			if (LOGGER.isLoggable(level)) {
				LOGGER.log(level, msg);
			}
			
			if (currReader != null) {
				try {
					currReader.close();
				} catch (IOException e) {
	
				}
				currReader = null;
			}
			
			if (isRequired) {
				throw new RuntimeIOException(msg);
			}
		}
		
		return currReader;
	}
	
	public void closeFileFactory(String filename) throws IOException
	{
		if (fileFactoryMap == null) {
			return;
		}
		
		SeekableLineReaderFactory factory = fileFactoryMap.remove(filename);
		
		if (factory != null) {
			factory.close();
		}
	}
	
	public void close() throws IOException
	{
		if (fileFactoryMap != null) {
			for (SeekableLineReaderFactory factory : fileFactoryMap.values()) {
				factory.close();
			}
			fileFactoryMap = null;
		}
		
		if (httpFactory != null) {
			httpFactory.close();
			httpFactory = null;
		}
	}

	public boolean isUseNio() {
		return useNio;
	}

	public void setUseNio(boolean useNio) {
		this.useNio = useNio;
	}
	
	public boolean isBufferFully() {
		return bufferFully;
	}

	public void setBufferFully(boolean bufferFully) {
		this.bufferFully = bufferFully;
	}
	
	public boolean isNoKeepAlive() {
		return noKeepAlive;
	}

	public void setNoKeepAlive(boolean noKeepAlive) {
		this.noKeepAlive = noKeepAlive;
	}

	public int getMaxHostConnections() {
		return maxHostConnections;
	}

	public void setMaxHostConnections(int maxHostConnections) {
		this.maxHostConnections = maxHostConnections;
	}

	public int getMaxTotalConnections() {
		return maxTotalConnections;
	}

	public void setMaxTotalConnections(int maxTotalConnections) {
		this.maxTotalConnections = maxTotalConnections;
	}

	public int getConnectTimeoutMS() {
		return connectTimeoutMS;
	}

	public void setConnectTimeoutMS(int connectTimeoutMS) {
		this.connectTimeoutMS = connectTimeoutMS;
	}

	public int getReadTimeoutMS() {
		return readTimeoutMS;
	}

	public void setReadTimeoutMS(int readTimeoutMS) {
		this.readTimeoutMS = readTimeoutMS;
	}
	
	public int getNumRetries() {
		return numRetries;
	}

	public void setNumRetries(int numRetries) {
		this.numRetries = numRetries;
	}

	public void setStaleChecking(boolean staleChecking)
	{
		this.staleChecking = staleChecking;
	}
	
	public boolean isStaleChecking()
	{
		return this.staleChecking;
	}

	public String getHttpLib() {
		return httpLib;
	}

	public void setHttpLib(String httpLib) {
		this.httpLib = httpLib;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public HMACSigner getSigner() {
		return signer;
	}

	public void setSigner(HMACSigner signer) {
		this.signer = signer;
	}

	public int getSignDurationSecs() {
		return signDurationSecs;
	}

	public void setSignDurationSecs(int signDurationSecs) {
		this.signDurationSecs = signDurationSecs;
	}
}
