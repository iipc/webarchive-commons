package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;

public class ZipNumStreamingLoader {

	private static final Logger LOGGER = Logger.getLogger(
			ZipNumStreamingLoader.class.getName());
	

	protected SeekableLineReaderFactory factory;
	protected SeekableLineReader reader;
		
	protected long offset = 0;
	protected int currLength;
	protected long totalRead = 0;
	
	protected boolean first = false;
	protected boolean last = false;
	protected boolean done = false;
	
	protected String partName;
	protected String[] partLocations = null;
	
	public ZipNumStreamingLoader(long offset, int length, String partName, String[] partLocations) {
		this.partName = partName;
		this.partLocations = partLocations;
		
		this.offset = offset;
		
		this.currLength = length;
		this.totalRead = length;
		
		this.factory = null;
		this.reader = null;
	}
	
	public void close()
	{		
		try {
			if (reader != null) {
				if (LOGGER.isLoggable(Level.INFO)) {
					LOGGER.info("READ SO FAR " + toString());
				}
				reader.close();
			}
		} catch (IOException e) {
			LOGGER.warning(e.toString());
		} finally {
			reader = null;
		}
	}
	
	public boolean isSameBlock(long nextOffset, String nextPartName)
	{
		return (((offset + totalRead) == nextOffset) && partName.equals(nextPartName));
	}
			
	public void addBlock(int size)
	{
		close();
		
		currLength = size;
		totalRead += size;
		done = false;
	}
					
	protected SeekableLineReaderFactory createFactory() throws IOException
	{					
			// Either load from specified location, or from partName path
		if (partLocations != null && partLocations.length > 0) {
			for (String location : partLocations) {
				try {
					return GeneralURIStreamFactory.createSeekableStreamFactory(location);
				} catch (IOException io) {
					continue;
				}
			}
		}
		
		return GeneralURIStreamFactory.createSeekableStreamFactory(partName);
	}
	
	protected SeekableLineReader getReader() throws IOException
	{				
		if (reader == null) {
			if (factory == null) {
				factory = createFactory();
			}
			reader = factory.get();
			reader.seekWithMaxRead(offset + (totalRead - currLength), true, currLength);
		}
		
		return reader;
	}
	
	public String readLine() throws IOException
	{
		if (done) {
			return null;
		}
		
		String line = getReader().readLine();
		
		if (line == null) {
			done = true;
		}
		
		return line;
	}
	
	@Override
	public String toString()
	{
		return "Streaming from " + partName + " (offset, totalRead) = (" + offset + ", " + totalRead + ")";
	}

	public boolean isFirst() {
		return first;
	}

	public void setIsFirst(boolean isFirst) {
		this.first = isFirst;
	}

	public boolean isLast() {
		return last;
	}

	public void setIsLast(boolean isLast) {
		this.last = isLast;
	}
}
