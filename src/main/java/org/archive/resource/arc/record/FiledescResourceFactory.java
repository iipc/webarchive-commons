package org.archive.resource.arc.record;

import java.io.IOException;
import java.io.InputStream;

import org.archive.format.arc.FiledescRecord;
import org.archive.format.arc.FiledescRecordParser;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.Resource;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceFactory;
import org.archive.resource.ResourceParseException;

public class FiledescResourceFactory implements ResourceFactory, ResourceConstants {
	FiledescRecordParser parser = new FiledescRecordParser();
	public Resource getResource(InputStream is, MetaData parentMetaData,
			ResourceContainer container) throws ResourceParseException,
			IOException {
		FiledescRecord rec = parser.parse(is);

		parentMetaData.putString(PAYLOAD_CONTENT_TYPE, PAYLOAD_TYPE_FILEDESC);
		return new FiledescResource(
				parentMetaData.createChild(FILEDESC_METADATA), container, rec);
	}

}
