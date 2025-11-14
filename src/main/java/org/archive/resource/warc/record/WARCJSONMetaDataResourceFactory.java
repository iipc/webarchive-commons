package org.archive.resource.warc.record;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.archive.resource.MetaData;
import org.archive.resource.Resource;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceFactory;
import org.archive.resource.ResourceParseException;
import org.json.JSONException;
import org.json.JSONTokener;

import static java.nio.charset.StandardCharsets.UTF_8;

public class WARCJSONMetaDataResourceFactory implements ResourceFactory, ResourceConstants {
	public WARCJSONMetaDataResourceFactory() {
	}

	public Resource getResource(InputStream is, MetaData parentMetaData,
			ResourceContainer container) throws ResourceParseException,
			IOException {


		MetaData md;
		try {
			md = new MetaData(new JSONTokener(new InputStreamReader(is, UTF_8)));
		} catch (JSONException e) {
			throw new ResourceParseException(e);
		}
		return new WARCJSONMetaDataResource(md, container);
	}

}
