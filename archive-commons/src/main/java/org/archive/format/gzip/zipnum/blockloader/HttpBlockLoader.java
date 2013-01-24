package org.archive.format.gzip.zipnum.blockloader;

import java.io.IOException;

import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;

public class HttpBlockLoader implements BlockLoader {
	
	protected HTTPSeekableLineReaderFactory factory;
	
	public HttpBlockLoader()
	{
		factory = new HTTPSeekableLineReaderFactory("");
		factory.setMaxHostConnections(400);
		factory.setMaxTotalConnections(500);
	}
	
	public HttpBlockLoader(HTTPSeekableLineReaderFactory factory)
	{
		this.factory = factory;
	}

	@Override
	public SeekableLineReader createBlockReader(String filename)
			throws IOException {
			
		HTTPSeekableLineReader reader = factory.get(filename);
		return reader;
	}
}
