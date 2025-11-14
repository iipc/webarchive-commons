package org.archive.util.binsearch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.archive.util.zip.GZIPMembersInputStream;

import com.google.common.io.ByteStreams;

public abstract class AbstractSeekableLineReader implements SeekableLineReader {
	public final static Charset UTF8 = StandardCharsets.UTF_8;
	
	protected int blockSize = 128 * 1024;
	
	protected boolean closed = false;
	
	protected boolean bufferFully = false;
	
	protected BufferedReader br;
	protected InputStream is;
	
	class SLRClosingInputStream extends FilterInputStream
	{
		protected SLRClosingInputStream(InputStream in) {
			super(in);
		}
		
		@Override
		public void close() throws IOException
		{
			AbstractSeekableLineReader.this.close();
			//in.close();
		}
	}
	
	public AbstractSeekableLineReader()
	{
		
	}
	
	public AbstractSeekableLineReader(int blockSize)
	{
		this.blockSize = blockSize;
	}
	
	public void seek(long offset) throws IOException
	{
		seekWithMaxRead(offset, false, -1);
	}
	
	public void seekWithMaxRead(long offset, boolean gzip, int maxLength) throws IOException
	{
		if (closed) {
			throw new IOException("Seek after close()");
		}
		
		br = null;
		
		try {
			is = doSeekLoad(offset, maxLength);
		
			if (bufferFully && (maxLength > 0)) {
				byte[] buffer = new byte[maxLength];
				ByteStreams.readFully(is, buffer);
				doClose();
				
				is = new ByteArrayInputStream(buffer);
			}
		
	    	if (gzip) {
	    		is = new GZIPMembersInputStream(is, blockSize);
	    	}
		} catch (IOException io) {
			doClose();
			throw io;
		}
	}
	
	abstract protected InputStream doSeekLoad(long offset, int maxLength) throws IOException; 	
	
	abstract protected void doClose() throws IOException;
	
	public InputStream getInputStream()
	{		
		return new SLRClosingInputStream(is);
	}
	
	public void skipLine() throws IOException
	{
		readLine();
	}
	
	public String readLine() throws IOException {		
		if (is == null) {
			seek(0);
		}
		
		if (br == null) {
	    	InputStreamReader isr = new InputStreamReader(is, UTF8);
	    	br = new BufferedReader(isr, blockSize);
		}
		
		return br.readLine();
	}
	
	public final void close() throws IOException
	{
		if (closed) {
			return;
		}
		
		doClose();
		
		if (br != null) {
			br.close();
			br = null;
		} else if (is != null) {
			is.close();
		}
		
		br = null;
		is = null;
		closed = true;
	}
	
	public boolean isClosed()
	{
		return closed;
	}
	
	public long getSize() throws IOException
	{
		return 0;
	}
	
	public void setBufferFully(boolean fully)
	{
		this.bufferFully = fully;
	}
	
	@Override
	public void finalize()
	{
		if (!closed) {
			try {
				close();
			} catch (IOException e) {
	
			}
		}
	}
}
