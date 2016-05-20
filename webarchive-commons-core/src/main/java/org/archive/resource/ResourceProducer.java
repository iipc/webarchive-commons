package org.archive.resource;

import java.io.IOException;

public interface ResourceProducer {
	public Resource getNext() throws ResourceParseException, IOException;
	public void close() throws IOException;
	public String getContext();
}
