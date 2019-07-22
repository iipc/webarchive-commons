package org.archive.extract;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.format.gzip.GZIPFormatException;
import org.archive.format.json.JSONUtils;
import org.archive.format.json.SimpleJSONPathSpec;
import org.archive.resource.MetaData;
import org.archive.resource.Resource;
import org.archive.util.IAUtils;
import org.archive.util.StreamCopy;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

import com.google.common.io.ByteStreams;
import com.google.common.io.CountingOutputStream;

public class WARCMetadataRecordExtractorOutput implements ExtractorOutput {
	private static final Logger LOG = 
		Logger.getLogger(WARCMetadataRecordExtractorOutput.class.getName());

	private PrintWriter out;
	SimpleJSONPathSpec formatSpec = new SimpleJSONPathSpec("Envelope.Format");
	SimpleJSONPathSpec warcURL = new SimpleJSONPathSpec("Envelope.WARC-Header-Metadata.WARC-Target-URI");
	SimpleJSONPathSpec warcDate = new SimpleJSONPathSpec("Envelope.WARC-Header-Metadata.WARC-Date");
	SimpleJSONPathSpec warcType = new SimpleJSONPathSpec("Envelope.WARC-Header-Metadata.WARC-Type");
	SimpleJSONPathSpec warcMetadataRecord = new SimpleJSONPathSpec("Envelope.Payload-Metadata.WARC-Metadata-Metadata.Metadata-Records");

	private String outputType = "outlinks";

	public WARCMetadataRecordExtractorOutput(PrintWriter out, String outputType) {
		this.out = out;
		this.outputType = outputType;
	}

	public WARCMetadataRecordExtractorOutput(PrintWriter out) {
		this(out,"outlinks");
	}

	public void output(Resource resource) throws IOException {
		OutputStream nullo = ByteStreams.nullOutputStream();
		CountingOutputStream co = new CountingOutputStream(nullo);
		try {
			StreamCopy.copy(resource.getInputStream(), co);
		} catch(GZIPFormatException e) {
			e.printStackTrace();
			return;
		}
		long bytes = co.getCount();
		if(bytes > 0) {
			LOG.info(bytes + " unconsumed bytes in Resource InputStream.");
		}
		try {
			MetaData m = resource.getMetaData().getTopMetaData();
			// URL DATE OURL MIME HTTP-CODE SHA1 META REDIR OFFSET LENGTH FILE
			String format = getEnvelopeFormat(m);
			String origUrl = "TBD";
			String date = "TBD";
			String canUrl = "TBD";

			if(format.startsWith("WARC")) {
				origUrl = getWARCURL(m);
				date = getWARCDate(m);
				String type = getWARCType(m);
				if(type.equals("metadata")) {
					String warcMetadataRecord = getWARCMetadataRecord(m);
					
					JSONArray array = new JSONArray(warcMetadataRecord);
					String viaUrl = "-";
					String viaPath = "-";
					String sourceTag = "-";
					for(int i=0;i<array.length();i++) {
						JSONObject obj = array.getJSONObject(i);
						if(outputType.equals("outlinks")) {
							if(obj.get("Name").toString().equals("outlink")) {
								String outLinkValue = obj.get("Value").toString();
								String[] linkParts = outLinkValue.split(" ");
								if(linkParts.length > 2)
									//'outlinks': 'origUrl date origOutlinkUrl linktype linktext'
									out.format("%s\t%s\t%s\t%s\t\n",origUrl,date,linkParts[0],linkParts[2]);
							}
						} else if(outputType.equals("hopinfo")) {
							String key = obj.get("Name").toString();
							String value = obj.get("Value").toString();
							if(key.equals("via")) {
								viaUrl = value;
							} else if (key.equals("hopsFromSeed")) {
								viaPath = value;
							} else if (key.equals("sourceTag")) {
								sourceTag = value;
							}
						}
					}
					if(outputType.equals("hopinfo")) {
						//'hopinfo': 'origCrawledUrl date origViaUrl hopPathFromVia sourceTag'
						out.format("%s\t%s\t%s\t%s\t%s\n",origUrl,date,viaUrl,viaPath,sourceTag);
					}
				} 
			}	 
			
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		out.flush();
	}

	private String getEnvelopeFormat(MetaData m) {
		return unwrapFirst(formatSpec.extract(m),"-");
	}
	private String getWARCURL(MetaData m) {
		return unwrapFirst(warcURL.extract(m),"-");
	}
	private String getWARCDate(MetaData m) {
		return unwrapFirst(warcDate.extract(m),"-");
	}
	private String getWARCType(MetaData m) {
		return unwrapFirst(warcType.extract(m),"-");
	}
	private String getWARCMetadataRecord(MetaData m) {
		return unwrapFirst(warcMetadataRecord.extract(m),"-");
	}
	
	private String unwrapFirst(List<List<String>> l, String defaultValue) {
		if(l != null) {
			if(l.size() > 0) {
				if(l.get(0) != null) {
					if(l.get(0).size() > 0) {
						String v = l.get(0).get(0);
						if(v != null) {
							if(v.length() > 0) {
								return v;
							}
						}
					}
				}
			}
		}
		return defaultValue;
	}
}
