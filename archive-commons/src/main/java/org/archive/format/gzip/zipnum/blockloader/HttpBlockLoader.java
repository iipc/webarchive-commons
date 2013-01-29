package org.archive.format.gzip.zipnum.blockloader;

import java.io.IOException;

import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;

public class HttpBlockLoader extends HTTPSeekableLineReaderFactory implements BlockLoader {

	public HttpBlockLoader()
	{
		super("");
		this.setMaxHostConnections(400);
		this.setMaxTotalConnections(500);
	}
	
	@Override
	public SeekableLineReader createBlockReader(String filename)
			throws IOException {
			
		HTTPSeekableLineReader reader = get(filename);
		return reader;
	}
}
