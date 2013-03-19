package org.archive.resource.arc;

import java.io.IOException;
import java.io.InputStream;

import org.archive.format.arc.ARCFormatException;
import org.archive.format.arc.ARCMetaData;
import org.archive.format.arc.ARCMetaDataParser;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.Resource;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceFactory;
import org.archive.resource.ResourceParseException;

public class ARCResourceFactory implements ResourceFactory, ResourceConstants {
	public ARCMetaDataParser parser;
	public boolean strict = false;
	public ARCResourceFactory() {
		parser = new ARCMetaDataParser();
	}
	public Resource getResource(InputStream is, MetaData parentMetaData,
			ResourceContainer container) throws ResourceParseException,
			IOException {

		try {
			ARCMetaData m = parser.parse(is,strict,!container.isCompressed());
			if(m == null) {
				return null;
			}
			ARCResource r = new ARCResource(parentMetaData.createChild(ENVELOPE),
					container, m,is);
			return r;

		} catch(ARCFormatException e) {
			throw new ResourceParseException(e);
		}
	}
}
