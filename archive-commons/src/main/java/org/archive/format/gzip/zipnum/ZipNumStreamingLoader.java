package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.io.InputStream;

import org.archive.streamcontext.Stream;
import org.archive.streamcontext.StreamWrappedInputStream;
import org.archive.util.GeneralURIStreamFactory;

public class ZipNumStreamingLoader {

	//private static final Logger LOGGER = Logger.getLogger(
	//		ZipNumStreamingLoader.class.getName());
	
	//protected StreamWrappedInputStream source;
	protected Stream stream;
	
	protected String partName;
	protected String[] partLocations = null;
	protected long offset;
	protected int count;
	protected int numBlocks;
	
	public ZipNumStreamingLoader(long offset, String partName, String[] partLocations) {
		this.partName = partName;
		this.partLocations = partLocations;
		this.offset = offset;
		this.count = 0;
		this.numBlocks = 0;
	}
	
	public void close() throws IOException
	{
		if (stream != null) {
			stream.close();
			stream = null;
		}
	}
	
	public boolean isSameBlock(long nextOffset, String nextPartName)
	{
		return ((offset + count) == nextOffset) && partName.equals(nextPartName);
	}
	
	public int addBlock(int size)
	{
		count += size;
		return ++numBlocks;
	}
		
	public InputStream getSourceInputStream() throws IOException
	{
		if (stream == null) {
			// Either load from specified location, or from partName path
			if (partLocations != null) {
				for (String location : partLocations) {
					try {
						stream = GeneralURIStreamFactory.createStream(location, offset, count);
						break;
					} catch (IOException io) {
						continue;
					}
				}
			} else {
				stream = GeneralURIStreamFactory.createStream(partName, offset, count);
			}
		}		
		
		return new StreamWrappedInputStream(stream);
	}
	
	@Override
	public String toString()
	{
		return "Streaming " + numBlocks + " blocks from " + partName + " (offset,len) = (" + offset + "," + count + ")";
	}
}
