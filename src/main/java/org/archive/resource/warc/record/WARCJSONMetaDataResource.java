package org.archive.resource.warc.record;

import org.archive.resource.AbstractEmptyResource;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceContainer;

public class WARCJSONMetaDataResource extends AbstractEmptyResource implements ResourceConstants {

	public WARCJSONMetaDataResource(MetaData metaData,
			ResourceContainer container) {
		super(metaData, container);
	}

}
