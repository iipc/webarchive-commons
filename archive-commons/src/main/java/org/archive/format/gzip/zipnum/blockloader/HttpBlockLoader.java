package org.archive.format.gzip.zipnum.blockloader;

import java.io.IOException;

import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;

public class HttpBlockLoader implements BlockLoader {
	
	HTTPSeekableLineReaderFactory factory;
	
	public HttpBlockLoader()
	{
		factory = new HTTPSeekableLineReaderFactory("");
	}

	@Override
	public SeekableLineReader createBlockReader(String filename)
			throws IOException {
			
		HTTPSeekableLineReader reader = factory.get(filename);
		//reader.seekWithMaxRead(offset, true, length);
		return reader;
	}
}
