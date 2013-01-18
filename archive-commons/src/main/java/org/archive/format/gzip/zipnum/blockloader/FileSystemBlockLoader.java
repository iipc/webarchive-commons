package org.archive.format.gzip.zipnum.blockloader;

import java.io.IOException;
import java.util.HashMap;

import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;

public class FileSystemBlockLoader implements BlockLoader {
		
	protected HashMap<String, SeekableLineReaderFactory> factoryMap = new HashMap<String, SeekableLineReaderFactory>();
	protected boolean useNio = true;
	
	public FileSystemBlockLoader()
	{
		
	}
	
	public FileSystemBlockLoader(boolean useNio)
	{
		this.useNio = useNio;
	}
	
	public SeekableLineReader createBlockReader(String filename) throws IOException
	{
		SeekableLineReaderFactory factory = getFactoryFor(filename);
		
		SeekableLineReader reader = factory.get();
		return reader;
	}
	
	protected SeekableLineReaderFactory getFactoryFor(String filename) throws IOException {
		SeekableLineReaderFactory factory = factoryMap.get(filename);
		
		if (factory == null) {
			factory = GeneralURIStreamFactory.createSeekableStreamFactory(filename, useNio);
			factoryMap.put(filename, factory);
		}
		
		return factory;
	}
}
