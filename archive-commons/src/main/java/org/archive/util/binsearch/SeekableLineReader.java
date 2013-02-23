package org.archive.util.binsearch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.archive.util.zip.GZIPMembersInputStream;

import com.google.common.io.ByteStreams;

public abstract class SeekableLineReader {
	public final static Charset UTF8 = Charset.forName("UTF-8");
	
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
			SeekableLineReader.this.close();
			in.close();
		}
	}
	
	public SeekableLineReader()
	{
		
	}
	
	public SeekableLineReader(int blockSize)
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
				try {
					byte[] buffer = new byte[maxLength];
					ByteStreams.readFully(is, buffer);
					is.close();
					
					// Create new stream
					is = new ByteArrayInputStream(buffer);
				} finally {
					doClose();
				}
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
}
