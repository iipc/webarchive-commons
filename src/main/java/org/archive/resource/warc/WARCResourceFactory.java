package org.archive.resource.warc;

import java.io.IOException;
import java.io.InputStream;

import org.archive.format.http.HttpParseException;
import org.archive.format.http.HttpResponse;
import org.archive.format.http.HttpResponseParser;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.Resource;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceFactory;
import org.archive.resource.ResourceParseException;

public class WARCResourceFactory implements ResourceFactory, ResourceConstants {
	private HttpResponseParser parser;
	public WARCResourceFactory() {
		parser = new HttpResponseParser();
	}

	public Resource getResource(InputStream is, MetaData parentMetaData,
			ResourceContainer container) throws ResourceParseException,
			IOException {
		try {

			HttpResponse response = parser.parse(is);
			WARCResource r = new WARCResource(parentMetaData.createChild(ENVELOPE),
					container, response);
			return r;

		} catch(HttpParseException e) {
			throw new ResourceParseException(e);
		}
	}
}
