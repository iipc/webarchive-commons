package org.archive.resource.warc.record;


import java.util.logging.Logger;

import org.archive.format.dns.DNSRecord;
import org.archive.format.dns.DNSResponse;
import org.archive.resource.AbstractEmptyResource;
import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import org.archive.resource.ResourceContainer;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

public class DNSResource extends AbstractEmptyResource implements ResourceConstants {
	private static final Logger LOG = 
		Logger.getLogger(DNSResource.class.getName()); 

	public DNSResource(MetaData metaData, ResourceContainer container,
			DNSResponse response) {
		super(metaData, container);
		metaData.putString(DNS_DATE, response.getDate());
		try {
			for(DNSRecord rec : response) {
				JSONObject rjo = new JSONObject();
				rjo.put(DNS_NAME, rec.getName());
				rjo.put(DNS_TTL, rec.getTtl());
				rjo.put(DNS_NETCLASS, rec.getNetClass());
				rjo.put(DNS_TYPE, rec.getType());
				rjo.put(DNS_VALUE, rec.getValue());
				metaData.appendChild(DNS_ENTRIES, rjo);
			}
		} catch(JSONException e) {
			LOG.severe(e.getMessage());
		}
	}
}
