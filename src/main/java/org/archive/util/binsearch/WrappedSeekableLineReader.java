package org.archive.util.binsearch;

import java.io.IOException;
import java.io.InputStream;

/**
 * WrappedSeekableLineReader that wraps an existing SeekableLineReader for custom extension 
 * @author ilya
 *
 */
public class WrappedSeekableLineReader implements SeekableLineReader {

	protected SeekableLineReader slr;
	
	public WrappedSeekableLineReader(SeekableLineReader slr)
	{
		this.slr = slr;
	}
	
	@Override
	public void seek(long offset) throws IOException {
		this.slr.seek(offset);		
	}

	@Override
	public void seekWithMaxRead(long offset, boolean gzip, int maxLength)
			throws IOException {
		slr.seekWithMaxRead(offset, gzip, maxLength);
	}

	@Override
	public InputStream getInputStream() {
		return slr.getInputStream();
	}

	@Override
	public String readLine() throws IOException {
		return slr.readLine();
	}

	@Override
	public void close() throws IOException {
		slr.close();
	}

	@Override
	public long getSize() throws IOException {
		return slr.getSize();
	}

	@Override
	public void setBufferFully(boolean bufferFully) {
		slr.setBufferFully(bufferFully);
	}

	@Override
	public boolean isClosed() {
		return slr.isClosed();
	}

	@Override
	public void skipLine() throws IOException {
		slr.skipLine();
	}

}
