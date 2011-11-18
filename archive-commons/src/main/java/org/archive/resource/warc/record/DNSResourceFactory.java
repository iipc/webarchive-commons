package org.archive.resource.warc.record;

import java.io.IOException;
import java.io.InputStream;

import org.archive.format.dns.DNSResponse;
import org.archive.format.dns.DNSResponseParser;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.Resource;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceFactory;
import org.archive.resource.ResourceParseException;

public class DNSResourceFactory implements ResourceFactory, ResourceConstants {

	DNSResponseParser parser = new DNSResponseParser();
	
	public Resource getResource(InputStream is, MetaData parentMetaData,
			ResourceContainer container) throws ResourceParseException,
			IOException {
		DNSResponse response = new DNSResponse();
		parser.parse(is, response);
		parentMetaData.putString(PAYLOAD_CONTENT_TYPE, PAYLOAD_TYPE_DNS);
		return new DNSResource(parentMetaData.createChild(DNS_METADATA), container, response);
	}

}
