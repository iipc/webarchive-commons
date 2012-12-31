package org.archive.format.gzip.zipnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.streamcontext.Stream;
import org.archive.streamcontext.StreamWrappedInputStream;
import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.zip.OpenJDK7GZIPInputStream;

public class ZipNumStreamingLoader {

	private static final Logger LOGGER = Logger.getLogger(
			ZipNumStreamingLoader.class.getName());
	

	protected Stream stream;
	protected BufferedReader reader;
	
	protected long offset = 0;
	//protected int count = 0;
	//protected int numBlocks = 0;
	
	protected String partName;
	protected String[] partLocations = null;
	
	public ZipNumStreamingLoader(long offset, String partName, String[] partLocations) {
		this.partName = partName;
		this.partLocations = partLocations;
		this.offset = offset;
	}
	
	public void close()
	{		
		try {
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("FINISHED " + toString());
			}
			if (stream != null) {
				stream.close();
			}
			if (reader != null) {
				reader.close();
			}
		} catch (IOException e) {
			LOGGER.warning(e.toString());
		} finally {
			reader = null;
			stream = null;
		}
	}
	
//	public boolean isSameBlock(long nextOffset, String nextPartName)
//	{
//		return ((offset + count) == nextOffset) && partName.equals(nextPartName);
//	}
		
//	public int addBlock(int size)
//	{
//		count += size;
//		return ++numBlocks;
//	}
			
	public Stream getSourceStream() throws IOException
	{		
		if (stream == null) {
			
			// Either load from specified location, or from partName path
			if (partLocations != null && partLocations.length > 0) {
				for (String location : partLocations) {
					try {
						stream = GeneralURIStreamFactory.createStream(location, offset);
						break;
					} catch (IOException io) {
						continue;
					}
				}
			} else {
				stream = GeneralURIStreamFactory.createStream(partName, offset);
			}
		}
		
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("BEGIN " + toString());
		}
		
		return stream;
	}
	
	public BufferedReader getReader() throws IOException
	{
		if (reader == null) {
			InputStream nextStream = new StreamWrappedInputStream(getSourceStream());
			nextStream = new OpenJDK7GZIPInputStream(nextStream);
			reader = new BufferedReader(new InputStreamReader(nextStream));
		}
		
		return reader;
	}
	
	public String readLine() throws IOException
	{
		return getReader().readLine();
	}
	
	@Override
	public String toString()
	{
		return "Streaming from " + partName + " offset = " + offset;
	}
}
