package org.archive.resource.warc.record;

//import java.util.logging.Logger;

import org.archive.format.http.HttpHeader;
import org.archive.format.http.HttpHeaders;
import org.archive.resource.AbstractEmptyResource;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceContainer;

public class WARCMetaDataResource extends AbstractEmptyResource implements ResourceConstants {
//	private static final Logger LOG = 
//		Logger.getLogger(WARCMetaDataResource.class.getName());
	
	public WARCMetaDataResource(MetaData metaData, ResourceContainer container,
			HttpHeaders headers) {
		super(metaData, container);
		for(HttpHeader h : headers) {
			metaData.appendObj(WARC_META_FIELDS_LIST,
					METADATA_KV_NAME, h.getName(),
					METADATA_KV_VALUE,h.getValue());
		}
	}
}
