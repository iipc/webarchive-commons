package org.archive.resource.http;

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

public class HTTPResponseResourceFactory implements ResourceFactory, ResourceConstants {
	private HttpResponseParser parser;
	public HTTPResponseResourceFactory() {
		parser = new HttpResponseParser();
	}

	public Resource getResource(InputStream is, MetaData metaData,
			ResourceContainer container) 
	throws ResourceParseException, IOException {
		try {

			HttpResponse response = parser.parse(is);
			metaData.putString(PAYLOAD_CONTENT_TYPE, 
					PAYLOAD_TYPE_HTTP_RESPONSE);
			return new HTTPResponseResource(metaData.createChild(HTTP_RESPONSE_METADATA),
					container, response, true);

		} catch(HttpParseException e) {
			throw new ResourceParseException(e);
		}
	}
}
