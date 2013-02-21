package org.archive.util.binsearch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
	
	public SeekableLineReader()
	{
		
	}
	
	public SeekableLineReader(int blockSize)
	{
		this.blockSize = blockSize;
	}
	
	public InputStream seek(long offset) throws IOException
	{
		return seekWithMaxRead(offset, false, -1);
	}
	
	public InputStream seekWithMaxRead(long offset, boolean gzip, int maxLength) throws IOException
	{
		if (closed) {
			throw new IOException("Seek after close()");
		}
		
		br = null;
		is = doSeekLoad(offset, maxLength);
		
		if (bufferFully && (maxLength > 0)) {
			try {
				byte[] buffer = new byte[maxLength];
				ByteStreams.readFully(is, buffer);
				is.close();
				
				// Create new stream
				is = new ByteArrayInputStream(buffer);
			} finally {
				close();
			}
		}	
		
    	if (gzip) {
    		is = new GZIPMembersInputStream(is, blockSize);
    	}
    	
    	return is;
	}
	
	abstract protected InputStream doSeekLoad(long offset, int maxLength) throws IOException; 

	public String readLine() throws IOException {		
		if (is == null) {
			is = seek(0);
		}
		
		if (br == null) {
	    	InputStreamReader isr = new InputStreamReader(is, UTF8);
	    	br = new BufferedReader(isr, blockSize);
		}
		
		return br.readLine();
	}
	
	public abstract void doClose() throws IOException;
	
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
	
	public long getSize() throws IOException
	{
		return 0;
	}
	
	public void setBufferFully(boolean fully)
	{
		this.bufferFully = fully;
	}
}
