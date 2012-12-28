package org.archive.format.gzip.zipnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.archive.util.zip.OpenJDK7GZIPInputStream;

import com.google.common.io.LimitInputStream;

public class ZipNumStreamingBlock {
		
	protected ZipNumStreamingLoader loader;
	protected BufferedReader reader;
	protected int length;
	protected int numInLoader;

	public ZipNumStreamingBlock(int length, ZipNumStreamingLoader loader) {
		this.length = length;
		this.loader = loader;
		this.reader = null;
		this.numInLoader = loader.addBlock(length);
	}

	protected BufferedReader readBlock() throws IOException
	{
		InputStream nextStream = new LimitInputStream(loader.getSourceInputStream(), length);
		nextStream = new OpenJDK7GZIPInputStream(nextStream);
		return new BufferedReader(new InputStreamReader(nextStream));
	}
	
	public String readLine() throws IOException
	{
		if (reader == null) {
			reader = readBlock();
		}
		
		return reader.readLine();
	}
	
	public void close() throws IOException
	{
		if (reader != null) {
			reader.close();
		}
	}
	
	public void closeIfLast() throws IOException
	{
		// Close only last block in loader
		if (reader != null && (numInLoader == loader.numBlocks)) {
			reader.close();
		}
	}
}
