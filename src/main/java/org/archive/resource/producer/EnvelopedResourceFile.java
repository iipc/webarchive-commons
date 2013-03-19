package org.archive.resource.producer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.hadoop.fs.FSDataInputStream;
import org.archive.format.gzip.GZIPMemberSeries;
import org.archive.resource.ResourceFactory;
import org.archive.resource.ResourceProducer;
import org.archive.resource.TransformingResourceProducer;
import org.archive.resource.generic.GenericResourceProducer;
import org.archive.resource.gzip.GZIPResourceContainer;
import org.archive.streamcontext.HDFSStream;
import org.archive.streamcontext.HTTP11Stream;
import org.archive.streamcontext.RandomAccessFileStream;
import org.archive.streamcontext.Stream;

public class EnvelopedResourceFile {
	private ResourceFactory factory;
	private boolean strict = true;
	private long startOffset = 0;
	
	public EnvelopedResourceFile(ResourceFactory factory) {
		this.factory = factory;
	}

	private ResourceProducer getProducer(Stream stream, String name) {
		GenericResourceProducer producer =
			new GenericResourceProducer(stream, name);
		return new TransformingResourceProducer(producer,factory);
	}

	private ResourceProducer getGZProducer(Stream stream, String name) {
		GZIPMemberSeries series = new GZIPMemberSeries(stream, name, startOffset, strict);
		GZIPResourceContainer producer = new GZIPResourceContainer(series);
		if(factory == null) {
			return producer;
		}
		return new TransformingResourceProducer(producer,factory);
	}

	public ResourceProducer getResourceProducer(File file) throws IOException {
		return getResourceProducer(file,0);
	}
	public ResourceProducer getResourceProducer(File file, long offset) 
	throws IOException {
		Stream stream = new RandomAccessFileStream(file);
		if(offset > 0) {
			stream.setOffset(offset);
		}
		return getProducer(stream, file.getName());
	}

	public ResourceProducer getGZResourceProducer(File file)
	throws IOException {
		return getGZResourceProducer(file,0);
	}
	public ResourceProducer getGZResourceProducer(File file, long offset)
	throws IOException {

		Stream stream = new RandomAccessFileStream(file);
		if(offset > 0) {
			stream.setOffset(offset);
		}
		return getGZProducer(stream, file.getName());
	}

	public ResourceProducer getGZResourceProducer(FSDataInputStream fsdis, 
			String name) throws IOException {
		return getGZResourceProducer(fsdis,name,0);
	}
	public ResourceProducer getGZResourceProducer(FSDataInputStream fsdis, 
			String name, long offset) throws IOException {
		Stream stream = new HDFSStream(fsdis);
		if(offset > 0) {
			stream.setOffset(offset);
		}
		return getGZProducer(stream, name);
	}

	public ResourceProducer getResourceProducer(FSDataInputStream fsdis, 
			String name) throws IOException {

		return getResourceProducer(fsdis,name,0);
	}
	public ResourceProducer getResourceProducer(FSDataInputStream fsdis, 
			String name, long offset) throws IOException {

		Stream stream = new HDFSStream(fsdis);
		if(offset > 0) {
			stream.setOffset(offset);
		}
		return getProducer(stream, name);
	}

	private String urlName(URL url) {
		File file = new File(url.getPath());
		String name = file.getName();
		if((name == null) || name.length() == 0) {
			return "UNKNOWN";
		}
		return name;
	}

	public ResourceProducer getResourceProducer(URL url) throws IOException {
		return getResourceProducer(url,0);
	}
	public ResourceProducer getResourceProducer(URL url, long offset) throws IOException {

		return getResourceProducer(url,urlName(url), offset);
	}

	public ResourceProducer getResourceProducer(URL url, String name) 
	throws IOException {
		return getResourceProducer(url,name,0);
	}
	public ResourceProducer getResourceProducer(URL url, String name, long offset) 
	throws IOException {

		Stream stream = new HTTP11Stream(url);
		if(offset > 0) {
			stream.setOffset(offset);
		}
		return getProducer(stream, name);
	}

	public ResourceProducer getGZResourceProducer(URL url) throws IOException {
		return getGZResourceProducer(url,0);
	}
	public ResourceProducer getGZResourceProducer(URL url, long offset) throws IOException {

		return getGZResourceProducer(url,urlName(url), offset);
	}

	public ResourceProducer getGZResourceProducer(URL url, String name) 
	throws IOException {
		return getGZResourceProducer(url,name,0);
	}
	public ResourceProducer getGZResourceProducer(URL url, String name, long offset) 
	throws IOException {

		Stream stream = new HTTP11Stream(url);
		if(offset > 0) {
			stream.setOffset(offset);
		}
		return getGZProducer(stream, name);
	}

	/**
	 * @return the strict
	 */
	public boolean isStrict() {
		return strict;
	}

	/**
	 * @param strict the strict to set
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	/**
	 * @return the startOffset
	 */
	public long getStartOffset() {
		return startOffset;
	}

	/**
	 * @param startOffset the startOffset to set
	 */
	public void setStartOffset(long startOffset) {
		this.startOffset = startOffset;
	}
}
