package org.archive.resource.generic;

import java.io.InputStream;

import org.archive.resource.AbstractResource;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceContainer;
import org.archive.streamcontext.StreamWrappedInputStream;
import org.archive.streamcontext.Stream;

public class GenericStreamResource extends AbstractResource implements ResourceConstants {
	private Stream stream;
	public GenericStreamResource(MetaData metaData, ResourceContainer container, Stream stream) {
		super(metaData, container);
		this.stream = stream;

		MetaData containerMD = new MetaData(metaData, CONTAINER);

		containerMD.putString(CONTAINER_FILENAME, container.getName());
		containerMD.putBoolean(CONTAINER_COMPRESSED, container.isCompressed());
		containerMD.putLong(CONTAINER_OFFSET, stream.getOffset());
	}

	public InputStream getInputStream() {
		return new StreamWrappedInputStream(stream);
	}
}
