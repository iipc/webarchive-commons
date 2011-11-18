package org.archive.resource.http;

import org.archive.format.arc.ARCConstants;
import org.archive.format.http.HttpHeader;
import org.archive.format.http.HttpHeaders;
import org.archive.resource.AbstractEmptyResource;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceContainer;


public class HTTPHeadersResource extends AbstractEmptyResource
implements ARCConstants {

	public HTTPHeadersResource(MetaData metaData, ResourceContainer container,
			HttpHeaders headers) {
		super(metaData, container);
		for(HttpHeader h : headers) {
			metaData.putString(h.getName(),h.getValue());
		}
	}
}
