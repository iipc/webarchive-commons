package org.archive.resource;

import java.io.IOException;

public class TransformingResourceProducer implements ResourceProducer {
	private ResourceProducer producer;
	private ResourceFactory factory;
	public TransformingResourceProducer(ResourceProducer producer, ResourceFactory factory) {
		this.producer = producer;
		this.factory = factory;
	}
	public Resource getNext() throws ResourceParseException, IOException {
		Resource inner = producer.getNext();
		if(inner == null) {
			return null;
		}
		return factory.getResource(inner.getInputStream(), inner.getMetaData(),
				inner.getContainer());
	}
	public void close() throws IOException {
		producer.close();
	}
	public String getContext() {
		return producer.getContext();
	}
}
