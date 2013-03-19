package org.archive.resource.http;

import java.io.IOException;
import java.io.InputStream;

import org.archive.format.http.HttpParseException;
import org.archive.format.http.HttpRequest;
import org.archive.format.http.HttpRequestParser;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.Resource;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceFactory;
import org.archive.resource.ResourceParseException;

public class HTTPRequestResourceFactory implements ResourceFactory, ResourceConstants {
	private HttpRequestParser parser;
	public HTTPRequestResourceFactory() {
		parser = new HttpRequestParser();
	}

	public Resource getResource(InputStream is, MetaData metaData,
			ResourceContainer container) 
	throws ResourceParseException, IOException {
		try {

			HttpRequest response = parser.parse(is);
			metaData.putString(PAYLOAD_CONTENT_TYPE, 
					PAYLOAD_TYPE_HTTP_REQUEST);
			return new HTTPRequestResource(metaData.createChild(HTTP_REQUEST_METADATA),
					container, response, true);

		} catch(HttpParseException e) {
			throw new ResourceParseException(e);
		}
	}

}
