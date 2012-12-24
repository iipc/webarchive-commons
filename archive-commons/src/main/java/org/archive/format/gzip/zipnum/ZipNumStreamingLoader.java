package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.io.InputStream;

import org.archive.streamcontext.StreamWrappedInputStream;
import org.archive.util.GeneralURIStreamFactory;

public class ZipNumStreamingLoader {

//	private static final Logger LOGGER = Logger.getLogger(
//			ZipNumStreamingLoader.class.getName());
	
	protected StreamWrappedInputStream source;
	protected String partName;
	protected long offset;
	protected int count;
	protected int numBlocks;
	
	public ZipNumStreamingLoader(long offset, String partName) {
		this.partName = partName;
		this.offset = offset;
		this.count = 0;
		this.numBlocks = 0;
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
		if (source == null) {
			source = new StreamWrappedInputStream(GeneralURIStreamFactory.createStream(partName, offset, count));
			source.setCloseOnClose(true);
		}
		return source;
	}
	
	@Override
	public String toString()
	{
		return "Streaming " + numBlocks + " from " + partName + " (offset,len) = (" + offset + "," + count + ")";
	}
}
