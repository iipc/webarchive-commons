package org.archive.resource.generic;

import java.io.IOException;
import java.util.Locale;

import org.archive.resource.MetaData;
import org.archive.resource.Resource;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceParseException;
import org.archive.resource.ResourceProducer;
import org.archive.streamcontext.Stream;

public class GenericResourceProducer implements ResourceContainer, ResourceProducer {
	private static long UNLIMITED = -1;
	private Stream stream;
	private String name;
	private long endOffset;
	public GenericResourceProducer(Stream stream, String name) {
		this(stream,name,UNLIMITED);
	}
	public GenericResourceProducer(Stream stream, String name, long endOffset) {
		this.stream = stream;
		this.name = name;
		this.endOffset = endOffset;
	}
	public Resource getNext() throws ResourceParseException, IOException {
		if(stream.atEof()) {
			return null;
		}
		if(endOffset != UNLIMITED) {
			if(stream.getOffset() > endOffset) {
				return null;
			}
		}
		return new GenericStreamResource(new MetaData(), this, stream);
	}
	
	public String getName() {
		return name;
	}

	public boolean isCompressed() {
		return false;
	}
	public void close() throws IOException {
		stream.close();
	}
	public String getContext() {
		return String.format(Locale.ROOT, "Context(%s)(%d)", name, stream.getOffset());
	}
}
