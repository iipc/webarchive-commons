package org.archive.format.gzip.zipnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.archive.util.zip.OpenJDK7GZIPInputStream;

import com.google.common.io.LimitInputStream;

public class ZipNumStreamingBlock {
		
	protected ZipNumStreamingLoader loader;
	protected int length;
	protected int numInLoader;

	public ZipNumStreamingBlock(int length, ZipNumStreamingLoader loader) {
		this.length = length;
		this.loader = loader;
		numInLoader = loader.addBlock(length);
	}

	public BufferedReader readBlock() throws IOException
	{
		InputStream nextStream = new LimitInputStream(loader.getSourceInputStream(), length);
		nextStream = new OpenJDK7GZIPInputStream(nextStream);
		return new BufferedReader(new InputStreamReader(nextStream));
	}
	
	public void closeIfLast(BufferedReader reader) throws IOException
	{
		// If last block in loader
		if (numInLoader == loader.numBlocks) {
			reader.close();
		}
	}
}
