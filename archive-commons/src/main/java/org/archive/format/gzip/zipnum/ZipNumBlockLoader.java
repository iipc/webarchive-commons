package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.HashMap;

import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;

public class ZipNumBlockLoader {
		
	protected HashMap<String, SeekableLineReaderFactory> fileFactoryMap = null;
	protected HTTPSeekableLineReaderFactory httpFactory = null;
	
	protected boolean useNio = false;
	
	protected boolean bufferFully = true;
	protected boolean noKeepAlive = true;
	
	protected int maxHostConnections = 400;
	protected int maxTotalConnections = 500;

	
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
	
	protected SeekableLineReader getHttpReader(String url) throws IOException {
		
		if (httpFactory == null) {
			httpFactory = new HTTPSeekableLineReaderFactory();
			httpFactory.setMaxHostConnections(maxHostConnections);
			httpFactory.setMaxHostConnections(maxTotalConnections);
		}
		
		HTTPSeekableLineReader reader = httpFactory.get(url);
		reader.setBufferFully(bufferFully);
		reader.setNoKeepAlive(noKeepAlive);
		return reader;
	}
	
	protected SeekableLineReader getFileReader(String filename) throws IOException {
		
		if (fileFactoryMap == null) {
			fileFactoryMap = new HashMap<String, SeekableLineReaderFactory>();
		}
		
		SeekableLineReaderFactory factory = fileFactoryMap.get(filename);
		
		if (factory == null) {
			factory = GeneralURIStreamFactory.createSeekableStreamFactory(filename, useNio);
			fileFactoryMap.put(filename, factory);
		}
		
		return factory.get();
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

	public HTTPSeekableLineReaderFactory getHttpFactory() {
		return httpFactory;
	}

	public void setHttpFactory(HTTPSeekableLineReaderFactory httpFactory) {
		this.httpFactory = httpFactory;
	}
}
