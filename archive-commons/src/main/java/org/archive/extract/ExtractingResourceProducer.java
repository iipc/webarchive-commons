package org.archive.extract;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.resource.Resource;
import org.archive.resource.ResourceFactory;
import org.archive.resource.ResourceParseException;
import org.archive.resource.ResourceProducer;

public class ExtractingResourceProducer implements ResourceProducer {
	private static final Logger LOG =
		Logger.getLogger(ExtractingResourceProducer.class.getName());
	private ResourceProducer producer;
	private ResourceFactoryMapper mapper;

	public ExtractingResourceProducer(ResourceProducer producer, 
			ResourceFactoryMapper mapper) {

		this.producer = producer;
		this.mapper = mapper;
	}
	
	public Resource getNext() throws ResourceParseException, IOException {
		Resource current = producer.getNext();
		if(current == null) {
			return null;
		}
		while(true) {
			ResourceFactory f = mapper.mapResourceToFactory(current);
			if(f == null) {
				return current;
			}
			if(LOG.isLoggable(Level.INFO)) {
				LOG.info(String.format("Extracting (%s) with (%s)\n", 
						current.getClass().toString(),
						f.getClass().toString()));
			}
			current = f.getResource(current.getInputStream(),
					current.getMetaData(), current.getContainer());
		}
	}

	public void close() throws IOException {
		producer.close();
	}

	public String getContext() {
		return producer.getContext();
	}

}
