package org.archive.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class AbstractEmptyResource extends AbstractResource {

	public AbstractEmptyResource(MetaData metaData, ResourceContainer container) {
		super(metaData, container);
	}

	public InputStream getInputStream() {
		byte bytes[] = new byte[0];
		return new ByteArrayInputStream(bytes);
	}
}
