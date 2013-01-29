package org.archive.format.gzip.zipnum.blockloader;

import java.io.IOException;

import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;

public class HttpBlockLoader extends HTTPSeekableLineReaderFactory implements BlockLoader {
	
	protected boolean bufferFully = false;
	protected boolean noKeepAlive = false;

	public HttpBlockLoader()
	{
		super();
		this.setMaxHostConnections(400);
		this.setMaxTotalConnections(500);
	}
	
	@Override
	public SeekableLineReader createBlockReader(String filename)
			throws IOException {
			
		HTTPSeekableLineReader reader = get(filename);
		reader.setBufferFully(bufferFully);
		reader.setNoKeepAlive(noKeepAlive);
		return reader;
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
}
