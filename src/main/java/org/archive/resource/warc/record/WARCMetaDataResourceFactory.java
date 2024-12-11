package org.archive.resource.warc.record;

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

public class WARCMetaDataResourceFactory implements ResourceFactory, ResourceConstants {

	HttpHeaderParser parser;
	public WARCMetaDataResourceFactory() {
		parser = new HttpHeaderParser();
	}

	@Override
	public Resource getResource(InputStream is, MetaData parentMetaData,
			ResourceContainer container) throws ResourceParseException,
			IOException {
		HttpHeaders headers = new HttpHeaders();
		try {
			parentMetaData.putString(PAYLOAD_CONTENT_TYPE, 
					PAYLOAD_TYPE_WARC_META_FIELDS);
			MetaData md = parentMetaData.createChild(WARC_META_FIELDS_METADATA);
			int bytes = parser.doParse(is,headers);
			if(headers.isCorrupt()) {
				md.putBoolean(WARC_META_FIELDS_CORRUPT, true);
			}
			long trailingSlopBytes = StreamCopy.readToEOF(is);
			if (!parentMetaData.has(PAYLOAD_SLOP_BYTES) || trailingSlopBytes > 0) {
				parentMetaData.putLong(PAYLOAD_SLOP_BYTES, trailingSlopBytes);
			}
			if (!parentMetaData.has(PAYLOAD_LENGTH) || bytes != parentMetaData.getLong(PAYLOAD_LENGTH)) {
				parentMetaData.putLong(PAYLOAD_LENGTH, bytes);
			}
			return new WARCMetaDataResource(md,container, headers);
			
		} catch (HttpParseException e) {
			throw new ResourceParseException(e);
		}
	}
	
}
