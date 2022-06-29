package org.archive.resource.warc.record;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.archive.resource.MetaData;
import org.archive.resource.Resource;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceContainer;
import org.archive.resource.ResourceFactory;
import org.archive.resource.ResourceParseException;
import com.github.openjson.JSONException;
import com.github.openjson.JSONTokener;

public class WARCJSONMetaDataResourceFactory implements ResourceFactory, ResourceConstants {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	public WARCJSONMetaDataResourceFactory() {
	}

	public Resource getResource(InputStream is, MetaData parentMetaData,
			ResourceContainer container) throws ResourceParseException,
			IOException {


		MetaData md;
		try {
			md = new MetaData(new JSONTokener(new InputStreamReader(is, UTF8)));
		} catch (JSONException e) {
			throw new ResourceParseException(e);
		}
		return new WARCJSONMetaDataResource(md, container);
	}

}
