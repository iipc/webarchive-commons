package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;

public class ZipNumBlockLoader {
		
	protected Map<String, SeekableLineReaderFactory> fileFactoryMap = null;
	protected HTTPSeekableLineReaderFactory httpFactory = null;
	
	protected boolean useNio = false;
	
	protected boolean bufferFully = true;
	protected boolean noKeepAlive = true;
	
	protected int maxHostConnections = 400;
	protected int maxTotalConnections = 500;
	
	protected int connectTimeoutMS = 10000;
	protected int readTimeoutMS = 10000;

	
	public ZipNumBlockLoader()
	{
		
	}
	
	public SeekableLineReader createBlockReader(String uri) throws IOException
	{		
		if (GeneralURIStreamFactory.isHttp(uri)) {
			return getHttpReader(uri);
		} else {
			return getFileReader(uri);
		}
	}
	
	public HTTPSeekableLineReader getHttpReader(String url) throws IOException {
		
		if (httpFactory == null) {
			httpFactory = new HTTPSeekableLineReaderFactory();
			httpFactory.setMaxHostConnections(maxHostConnections);
			httpFactory.setMaxHostConnections(maxTotalConnections);
			httpFactory.setConnectionTimeoutMS(connectTimeoutMS);
			httpFactory.setSocketTimeoutMS(readTimeoutMS);			
		}
		
		HTTPSeekableLineReader reader = httpFactory.get(url);
		reader.setBufferFully(bufferFully);
		reader.setNoKeepAlive(noKeepAlive);
		return reader;
	}
	
	public SeekableLineReader getFileReader(String filename) throws IOException {
		
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
}
