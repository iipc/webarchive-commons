package org.archive.resource.http;

import java.io.IOException;
import java.io.InputStream;

import org.archive.format.http.HttpHeaderParser;
import org.archive.format.http.HttpHeaders;
import org.archive.format.http.HttpParseException;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.Resource;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceFactory;
import org.archive.resource.ResourceParseException;
import org.archive.util.StreamCopy;

public class HTTPHeadersResourceFactory 
implements ResourceFactory, ResourceConstants {

	private String name;
	private String type;
	HttpHeaderParser parser;

	public HTTPHeadersResourceFactory(String name) {
		this(name,null);
	}

	public HTTPHeadersResourceFactory(String name, String type) {
		this.name = name;
		this.type = type;
		parser = new HttpHeaderParser();
	}

	@Override
	public Resource getResource(InputStream is, MetaData parentMetaData,
			ResourceContainer container) throws ResourceParseException,
			IOException {
		HttpHeaders headers = new HttpHeaders();
		try {
			int bytes = parser.doParse(is,headers);
			if(headers.isCorrupt()) {
				parentMetaData.putBoolean(HTTP_HEADERS_CORRUPT, true);
			}
			if (!parentMetaData.has(PAYLOAD_LENGTH) || bytes != parentMetaData.getLong(PAYLOAD_LENGTH)) {
				parentMetaData.putLong(PAYLOAD_LENGTH, bytes);
			}
			long trailingSlopBytes = StreamCopy.readToEOF(is);
			if (!parentMetaData.has(PAYLOAD_SLOP_BYTES) || trailingSlopBytes > 0) {
				parentMetaData.putLong(PAYLOAD_SLOP_BYTES, trailingSlopBytes);
			}
			if(type != null) {
				parentMetaData.putString(PAYLOAD_CONTENT_TYPE, type);
			}
			return new HTTPHeadersResource(parentMetaData.createChild(name),
					container, headers);
		} catch (HttpParseException e) {
			throw new ResourceParseException(e);
		}
	}
	
}
