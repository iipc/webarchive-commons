package org.archive.extract;

import java.util.Iterator;
import java.util.logging.Logger;

import org.archive.format.arc.ARCConstants;
import org.archive.format.warc.WARCConstants;
import org.archive.format.warc.WARCConstants.WARCRecordType;
import org.archive.resource.MetaData;
import org.archive.resource.Resource;
import org.archive.resource.ResourceFactory;
import org.archive.resource.arc.ARCResource;
import org.archive.resource.arc.record.FiledescResourceFactory;
import org.archive.resource.html.HTMLResourceFactory;
import org.archive.resource.http.HTTPHeadersResourceFactory;
import org.archive.resource.http.HTTPRequestResourceFactory;
import org.archive.resource.http.HTTPResponseResource;
import org.archive.resource.http.HTTPResponseResourceFactory;
import org.archive.resource.warc.WARCResource;
import org.archive.resource.warc.record.DNSResourceFactory;
import org.archive.resource.warc.record.WARCJSONMetaDataResourceFactory;
import org.archive.resource.warc.record.WARCMetaDataResourceFactory;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

public class ExtractingResourceFactoryMapper implements ResourceFactoryMapper {

	private static final Logger LOG =
		Logger.getLogger(ExtractingResourceFactoryMapper.class.getName());

	private HTTPResponseResourceFactory httpResponseF = 
		new HTTPResponseResourceFactory();

	private HTTPRequestResourceFactory httpRequestF = 
		new HTTPRequestResourceFactory();

	private HTMLResourceFactory htmlF = new HTMLResourceFactory();

	private HTTPHeadersResourceFactory warcinfoF = 
		new HTTPHeadersResourceFactory(WARCINFO_METADATA,PAYLOAD_TYPE_WARCINFO);

	private DNSResourceFactory dnsF = new DNSResourceFactory();

	private WARCMetaDataResourceFactory warcmetaF = 
		new WARCMetaDataResourceFactory();

	private WARCJSONMetaDataResourceFactory warcjsonF = 
		new WARCJSONMetaDataResourceFactory();
	
	private FiledescResourceFactory filedescF = 
		new FiledescResourceFactory();

	private String getChildField(MetaData m, String child, String key) {
		try {
			if(m.has(child)) {
				JSONObject c = m.getJSONObject(child);
				if(c.has(key)) {
					return c.getString(key);
				}
			}
		} catch (JSONException e) {
			LOG.warning(e.getMessage());
		}
		return null;
	}

	private boolean childFieldStartsWith(MetaData m, String child,
			String key, String search) {
		String val = getChildField(m,child,key);
		return val == null ? false : 
			val.toLowerCase().startsWith(search.toLowerCase());
	}

	private boolean childFieldContains(MetaData m, String child,
			String key, String search) {
		String val = getChildField(m,child,key);
		return val == null ? false : 
			val.toLowerCase().contains(search.toLowerCase());
	}

	private boolean childFieldEquals(MetaData m, String child,
			String key, String search) {
		String val = getChildField(m,child,key);
		return val == null ? false : 
			val.equals(search);
	}

	private String caseInsensitiveKeyScan(MetaData m, String child, String k) {
		try {
			if(m.has(child)) {
				String kLC = k.toLowerCase();
				JSONObject childJSObj = m.getJSONObject(child);
				@SuppressWarnings("rawtypes")
				Iterator i = childJSObj.keys();
				while(i.hasNext()) {
					Object kObj = i.next();
					if(kObj instanceof String) {
						String kString = (String) kObj;
						if(kString.toLowerCase().equals(kLC)) {
							return childJSObj.getString(kString);
						}
					}
				}
			}
		} catch (JSONException e) {
			LOG.warning(e.getMessage());
		}
		return null;
	}

	private boolean isFileDescARCResource(MetaData envelope) {
		return childFieldStartsWith(envelope, ARC_HEADER_METADATA,
				ARCConstants.URL_KEY, ARCConstants.FILEDESC_SCHEME);
	}
	private boolean isDNSARCResource(MetaData envelope) {
		return childFieldContains(envelope, ARC_HEADER_METADATA,
				ARCConstants.MIME_KEY, ARCConstants.DNS_MIME);
	}
	private boolean isDATARCResource(MetaData envelope) {
		return childFieldContains(envelope, ARC_HEADER_METADATA,
				ARCConstants.MIME_KEY, ARCConstants.ALEXA_DAT_MIME);
	}
	private boolean isHTTPARCResource(MetaData envelope) {
		return childFieldStartsWith(envelope, ARC_HEADER_METADATA,
				ARCConstants.URL_KEY, "http");
	}

	private boolean isHTMLHttpResource(MetaData m) {
		String type = caseInsensitiveKeyScan(m,HTTP_HEADERS_LIST,
				"Content-Type");
		return type == null ? false : type.toLowerCase().contains("html");
	}

	private boolean isWARCType(MetaData envelope, WARCRecordType type) {
		return childFieldEquals(envelope,WARC_HEADER_METADATA, 
				WARCConstants.HEADER_KEY_TYPE,type.toString());
	}
	private boolean isWARCRevisitResource(MetaData envelope) {
		return isWARCType(envelope, WARCRecordType.revisit);
	}
	private boolean isWARCResponseResource(MetaData envelope) {
		return isWARCType(envelope, WARCRecordType.response);
	}
	private boolean isWARCRequestResource(MetaData envelope) {
		return isWARCType(envelope, WARCRecordType.request);
	}
	private boolean isWARCMetaDataResource(MetaData envelope) {
		return isWARCType(envelope, WARCRecordType.metadata);
	}
	private boolean isWARCInfoResource(MetaData envelope) {
		return isWARCType(envelope, WARCRecordType.warcinfo);
	}
	private boolean isHTTPResponseWARCResource(MetaData envelope) {
		return childFieldEquals(envelope,WARC_HEADER_METADATA,
				WARCConstants.CONTENT_TYPE,
				WARCConstants.HTTP_RESPONSE_MIMETYPE)
			|| childFieldEquals(envelope,WARC_HEADER_METADATA,
				WARCConstants.CONTENT_TYPE,
				WARCConstants.HTTP_RESPONSE_MIMETYPE_NS);
	}
	private boolean isWARCJSONResource(MetaData envelope) {
		return childFieldEquals(envelope,WARC_HEADER_METADATA,
				WARCConstants.CONTENT_TYPE,
				"application/json");
	}
	private boolean isDNSResponseWARCResource(MetaData envelope) {
		return childFieldEquals(envelope,WARC_HEADER_METADATA,
				WARCConstants.CONTENT_TYPE,PAYLOAD_TYPE_DNS);
	}
	
	public ResourceFactory mapResourceToFactory(Resource resource) {
		
		if(resource instanceof WARCResource) {
			WARCResource wr = (WARCResource) resource;
			MetaData envelope = wr.getEnvelopeMetaData();
			if(isWARCMetaDataResource(envelope)) {
				if(isWARCJSONResource(envelope)) {
					return warcjsonF;
				} else {
					return warcmetaF;
				}
			} else if(isWARCRequestResource(envelope)) {
				return httpRequestF;
			} else if(isWARCInfoResource(envelope)) {
				return warcinfoF;
			} else if(isWARCResponseResource(envelope)) {
				if(isHTTPResponseWARCResource(envelope)) {
					return httpResponseF;
				} else if(isDNSResponseWARCResource(envelope)) {
					return dnsF;
				}
			} else if(isWARCRevisitResource(envelope)) {
				return httpResponseF;
			}
		} else if(resource instanceof ARCResource) {
			ARCResource ar = (ARCResource) resource;
			MetaData envelope = ar.getEnvelopeMetaData();
			if(isFileDescARCResource( envelope)) {
				return filedescF;
			} else if(isDNSARCResource(envelope)) {
				return dnsF;
			} else if(isDATARCResource(envelope)) {
				// TODO:
			} else if(isHTTPARCResource(envelope)) {
				return httpResponseF;
			} else {
				// TODO: ftp? what else?
			}
			
		} else if(resource instanceof HTTPResponseResource) {
			if(isHTMLHttpResource(resource.getMetaData())) {
				return htmlF;
			} else {
				// TODO: more formats...
			}
		}
		return null;
	}
}
