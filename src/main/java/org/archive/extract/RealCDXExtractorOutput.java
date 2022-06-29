package org.archive.extract;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
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
import org.archive.url.URLKeyMaker;
import org.archive.url.WaybackURLKeyMaker;
import org.archive.util.IAUtils;
import org.archive.util.StreamCopy;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

import com.google.common.io.ByteStreams;
import com.google.common.io.CountingOutputStream;

public class RealCDXExtractorOutput implements ExtractorOutput {
	private static final Logger LOG = 
		Logger.getLogger(RealCDXExtractorOutput.class.getName());
	public final static String X_ROBOTS_HTTP_HEADER = "X-Robots-Tag";

	private PrintWriter out;
	SimpleJSONPathSpec filenameSpec = new SimpleJSONPathSpec("Container.Filename");
	SimpleJSONPathSpec offsetSpec = new SimpleJSONPathSpec("Container.Offset");
	SimpleJSONPathSpec gzDeflateLengthSpec = new SimpleJSONPathSpec("Container.Gzip-Metadata.Deflate-Length");
	SimpleJSONPathSpec formatSpec = new SimpleJSONPathSpec("Envelope.Format");
	
	SimpleJSONPathSpec arcURL = new SimpleJSONPathSpec("Envelope.ARC-Header-Metadata.Target-URI");
	SimpleJSONPathSpec arcDate = new SimpleJSONPathSpec("Envelope.ARC-Header-Metadata.Date");
	SimpleJSONPathSpec arcContentType = new SimpleJSONPathSpec("Envelope.ARC-Header-Metadata.Content-Type");

	SimpleJSONPathSpec warcURL = new SimpleJSONPathSpec("Envelope.WARC-Header-Metadata.WARC-Target-URI");
	SimpleJSONPathSpec warcDate = new SimpleJSONPathSpec("Envelope.WARC-Header-Metadata.WARC-Date");
	SimpleJSONPathSpec warcType = new SimpleJSONPathSpec("Envelope.WARC-Header-Metadata.WARC-Type");
	SimpleJSONPathSpec warcContentType = new SimpleJSONPathSpec("Envelope.WARC-Header-Metadata.Content-Type");

	SimpleJSONPathSpec envBlockDigest = new SimpleJSONPathSpec("Envelope.Block-Digest");
	SimpleJSONPathSpec warcPayloadDigest = new SimpleJSONPathSpec("Envelope.WARC-Header-Metadata.WARC-Payload-Digest");
	
	SimpleJSONPathSpec httpResponseCode = new SimpleJSONPathSpec("Envelope.Payload-Metadata.HTTP-Response-Metadata.Response-Message.Status");
	SimpleJSONPathSpec httpEntityDigest = new SimpleJSONPathSpec("Envelope.Payload-Metadata.HTTP-Response-Metadata.Entity-Digest");
	
	SimpleJSONPathSpec HTTPLocation = new SimpleJSONPathSpec("Envelope.Payload-Metadata.HTTP-Response-Metadata.Headers");
	private final static Pattern refreshURLPattern = 
		Pattern.compile("^\\d+\\s*;\\s*url\\s*=\\s*(.+?)\\s*$",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private boolean dumpJSON = false;
	private URLKeyMaker keyMaker;

	public RealCDXExtractorOutput(PrintWriter out, URLKeyMaker keyMaker) {
		this.out = out;
		this.keyMaker = keyMaker;
		out.println(" CDX N b a m s k r M S V g");
		out.flush();
	}

	public RealCDXExtractorOutput(PrintWriter out) {
		this(out,new WaybackURLKeyMaker());
	}
//	SimpleJSONPathSpec gzFooterLengthSpec = new SimpleJSONPathSpec("Container.Gzip-Metadata.Footer-Length");
//	SimpleJSONPathSpec gzHeaderLengthSpec = new SimpleJSONPathSpec("Container.Gzip-Metadata.Header-Length");
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

			String filename = getContainerFilename(m);
			String offset = getContainerOffset(m);
			String gzLen = getGZLength(m);
			String format = getEnvelopeFormat(m);
			String origUrl = "TBD";
			String date = "TBD";
			String canUrl = "TBD";

			String mime = "TBD";
			String httpCode = "TBD";
			String digest = "TBD";
			String meta = "TBD";
			String redir = "TBD";
			
			if(format.startsWith("WARC")) {
				origUrl = getWARCURL(m);
				date = getWARCDate(m);
				String type = getWARCType(m);
				if(type.equals("response")) {
					// is it http or DNS:
					String recType = getWARCContentType(m);
					if(recType.equals("text/dns")) {
						// hrmm..
						redir = "-";
						meta = "-";
						httpCode = "-";
						mime = recType;
						digest = getEnvelopeBlockDigest(m);

					} else if(recType.equals("application/http; msgtype=response")) {
						httpCode = getHTTPStatus(m);
						digest = getHTTPEntityDigest(m);
						JSONObject headers = JSONUtils.extractObject(m, "Envelope.Payload-Metadata.HTTP-Response-Metadata.Headers");
						mime = normalizeHTTPMime(scanHeadersLC(headers, "content-type", "unk"));
						redir = scanHeadersLC(headers, "location", "-");
						meta = scanHeadersLC(headers, X_ROBOTS_HTTP_HEADER, null);
						if(meta != null) {
							meta = parseRobotInstructions(meta);
						} else {
							meta = "-";
						}
						if(mime.toLowerCase().contains("html")) {
							if(redir.equals("-")) {
								// maybe an obvious meta-refresh?
								redir = extractHTMLMetaRefresh(origUrl,m);
							}
							if(meta.equals("-")) {
								// see if there are HTML robot instructions:
								meta = extractHTMLRobots(m);
							}
						}
					}
				} else if(type.equals("warcinfo")) {
					origUrl = "warcinfo:/" + filename + "/" + IAUtils.COMMONS_VERSION.replaceAll(" ", "_");
					redir = "-";
					meta = "-";
					httpCode = "-";
					mime = "warc-info";
					digest = getEnvelopeBlockDigest(m);
				
				} else if(type.equals("request")) {
					mime = "warc/request";
					redir = "-";
					meta = "-";
					httpCode = "-";
					digest = getEnvelopeBlockDigest(m);
				} else if(type.equals("metadata")) {
					// interesting...?
					mime = "warc/metadata";
					redir = "-";
					meta = "-";
					httpCode = "-";
					digest = getEnvelopeBlockDigest(m);
				} else if(type.equals("revisit")) {
					mime = "warc/revisit";
					redir = "-";
					meta = "-";
					httpCode = "-";
					digest = getWARCPayloadDigest(m);
				}
			} else if(format.equals("ARC")) {
				origUrl = getARCURL(m);
				date = getARCDate(m);
				if(origUrl.startsWith("filedesc:")) {
					// ARC header record:
					origUrl = "filedesc:/" +filename + "/" + IAUtils.COMMONS_VERSION.replaceAll(" ", "_");
					mime = "arc-filedesc";
					redir = "-";
					meta = "-";
					httpCode = "-";
					digest = getEnvelopeBlockDigest(m);
				} else {
					// either an alexa/dat, or an HTTP response (we hope):
					mime = getARCContentType(m);
					if(mime.equals("alexa/dat")) {
						redir = "-";
						meta = "-";
						httpCode = "-";
						digest = getEnvelopeBlockDigest(m);
					} else {

						httpCode = getHTTPStatus(m);
						digest = getHTTPEntityDigest(m);
						JSONObject headers = JSONUtils.extractObject(m, "Envelope.Payload-Metadata.HTTP-Response-Metadata.Headers");
						mime = normalizeHTTPMime(scanHeadersLC(headers, "content-type", "unk"));
						redir = scanHeadersLC(headers, "location", "-");
						meta = scanHeadersLC(headers, X_ROBOTS_HTTP_HEADER, null);
						if(meta != null) {
							meta = parseRobotInstructions(meta);
						} else {
							meta = "-";
						}
						if(mime.toLowerCase().contains("html")) {
							if(redir.equals("-")) {
								// maybe an obvious meta-refresh?
								redir = extractHTMLMetaRefresh(origUrl,m);
							}
							if(meta.equals("-")) {
								// see if there are HTML robot instructions:
								meta = extractHTMLRobots(m);
							}
						}

					}
				}
			}
			if(!redir.equals("-")) {
				redir = resolve(origUrl, redir);
			}
			canUrl = keyMaker.makeKey(origUrl);
			// URL DATE OURL MIME HTTP-CODE SHA1 META REDIR OFFSET LENGTH FILE
			if(dumpJSON) {
				out.format("%s %s %s %s %s %s %s %s %s %s %s %s\n", 
						canUrl, 
						date, 
						origUrl,
						mime,
						httpCode,
						digest,
						redir,
						meta,
						gzLen, 
						offset, 
						filename, 
						m.toString(1));
			} else {
				out.format("%s %s %s %s %s %s %s %s %s %s %s\n", 
						canUrl, 
						date, 
						origUrl,
						mime,
						httpCode,
						digest,
						redir,
						meta,
						gzLen, 
						offset, 
						filename);

			}
//			out.println(filename + " "+resource.getMetaData().getTopMetaData().toString(1));
		} catch (JSONException e) {
			throw new IOException(e);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
		out.flush();
	}

	
	private String extractHTMLRobots(MetaData m) {
		JSONArray metas = JSONUtils.extractArray(m, "Envelope.Payload-Metadata.HTTP-Response-Metadata.HTML-Metadata.Head.Metas");
		if(metas != null) {
			int count = metas.length();
			for(int i = 0; i < count; i++) {
				JSONObject meta = metas.optJSONObject(i);
				if(meta != null) {
					String name = scanHeadersLC(meta, "name", null);
					if(name != null) {
						if(name.toLowerCase().equals("robots")) {
							// alright - some robot instructions:
							String content = scanHeadersLC(meta, "content", null);
							if(content != null) {
								return parseRobotInstructions(content);
							}
						}
					}
				}
			}
		}
		return "-";
	}
	private String extractHTMLMetaRefresh(String origUrl, MetaData m) {
		JSONArray metas = JSONUtils.extractArray(m, "Envelope.Payload-Metadata.HTTP-Response-Metadata.HTML-Metadata.Head.Metas");
		if(metas != null) {
			int count = metas.length();
			for(int i = 0; i < count; i++) {
				JSONObject meta = metas.optJSONObject(i);
				if(meta != null) {
					String name = scanHeadersLC(meta, "http-equiv", null);
					if(name != null) {
						if(name.toLowerCase().equals("refresh")) {
							// alright - some robot instructions:
							String content = scanHeadersLC(meta, "content", null);
							if(content != null) {
								String fragment = parseMetaRefreshContent(content);
								if(fragment != null) {
									return fragment;
								}
							}
						}
					}
				}
			}
		}
		return "-";
	}
	
	static String resolve(String context, String spec) {
		// TODO: test!
		try {
			URL cUrl = new URL(context);
			URL url = new URL(cUrl, spec);
			// this constructor escapes its arguments, if necessary
			URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), url.getRef());
			return uri.toASCIIString();
			
		} catch (URISyntaxException e) {			
		} catch (MalformedURLException e) {
		} catch (NullPointerException e) {
			
		}
		return spec;
	}
	
	private String scanHeadersLC(JSONObject o, String match, String defaultVal) {
		if(o != null) {
			if(o.length() == 0) {
				return defaultVal;
			}
			String lc = match.toLowerCase().trim();
//			try {
//				System.err.println("REC:" + o.toString(1));
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			for(String key : JSONObject.getNames(o)) {
				if(lc.equals(key.toLowerCase().trim())) {
					try {
						return o.getString(key).trim();
					} catch (JSONException e) {
						e.printStackTrace();
						return defaultVal;
					}
				}
			}
		}
		return defaultVal;
	}
	private String getContainerFilename(MetaData m) {
		return unwrapFirst(filenameSpec.extract(m),"-");
	}
	private String getContainerOffset(MetaData m) {
		return unwrapFirst(offsetSpec.extract(m),"-");
	}
	private String getGZLength(MetaData m) {
		return unwrapFirst(gzDeflateLengthSpec.extract(m),"-");
	}
	private String getEnvelopeFormat(MetaData m) {
		return unwrapFirst(formatSpec.extract(m),"-");
	}
	private String getWARCURL(MetaData m) {
		return unwrapFirst(warcURL.extract(m),"-");
	}
	private String getWARCDate(MetaData m) {
		return normalizeWARCDate(unwrapFirst(warcDate.extract(m),"-"));
	}
	private String getWARCType(MetaData m) {
		return unwrapFirst(warcType.extract(m),"-");
	}
	private String getWARCPayloadDigest(MetaData m) {
		return normalizeSHA1(unwrapFirst(warcPayloadDigest.extract(m),"-"));
	}
	private String getHTTPStatus(MetaData m) {
		return unwrapFirst(httpResponseCode.extract(m),"-");
	}
	
	private String getWARCContentType(MetaData m) {
		return unwrapFirst(warcContentType.extract(m),"-");
	}
	private String getEnvelopeBlockDigest(MetaData m) {
		return normalizeSHA1(unwrapFirst(envBlockDigest.extract(m),"-"));
	}
	private String getHTTPEntityDigest(MetaData m) {
		return normalizeSHA1(unwrapFirst(httpEntityDigest.extract(m),"-"));
	}
	private String getARCURL(MetaData m) {
		return unwrapFirst(arcURL.extract(m),"-");
	}
	private String getARCDate(MetaData m) {
		return unwrapFirst(arcDate.extract(m),"-");
	}
	private String getARCContentType(MetaData m) {
		return normalizeHTTPMime(unwrapFirst(arcContentType.extract(m),"-"));
	}

	public String normalizeSHA1(String sha1) {
		if(sha1.startsWith("sha1:")) {
			return sha1.substring(5);
		}
		return sha1;
	}
	public String normalizeWARCDate(String date) {
		if(date == null) {
			return "-";
		}
		if(date.length() != 20) {
			return date;
		}
		char[] norm = new char[14];
		//2009-11-02T23:30:38Z
		norm[0] = date.charAt(0);
		norm[1] = date.charAt(1);
		norm[2] = date.charAt(2);
		norm[3] = date.charAt(3);
		norm[4] = date.charAt(5);
		norm[5] = date.charAt(6);
		norm[6] = date.charAt(8);
		norm[7] = date.charAt(9);
		norm[8] = date.charAt(11);
		norm[9] = date.charAt(12);
		norm[10] = date.charAt(14);
		norm[11] = date.charAt(15);
		norm[12] = date.charAt(17);
		norm[13] = date.charAt(18);
		return new String(norm);
	}

	private String escapeSpaces(final String input) {
		if(input.contains(" ")) {
			return input.replace(" ", "%20");
		}
		return input;
	}
	
	public String normalizeHTTPMime(String input) {
		if(input == null) {
			return null;
		}
		int semiIdx = input.indexOf(";");
		if(semiIdx > 0) {
			return escapeSpaces(input.substring(0,semiIdx).trim());
		}
		return escapeSpaces(input.trim());
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
	private static String NO_NOTHIN_MATCH = "NONE";
	private static String NO_FOLLOW_MATCH = "NOFOLLOW";
	private static String NO_INDEX_MATCH = "NOINDEX";
	private static String NO_ARCHIVE_MATCH = "NOARCHIVE";
	private String parseRobotInstructions(String input) {
		if(input == null) {
			return "-";
		}
		String up = input.replaceAll("-", "").toUpperCase();
		StringBuilder sb = new StringBuilder(3);
		if(up.contains(NO_FOLLOW_MATCH)) {
			sb.append("F");
		}
		if(up.contains(NO_ARCHIVE_MATCH)) {
			sb.append("A");
		}
		if(up.contains(NO_INDEX_MATCH)) {
			sb.append("I");
		}
		if(up.contains(NO_NOTHIN_MATCH)) {
			sb.setLength(0);
			sb.append("AIF");
		}
		return (sb.length() == 0) ? "-" : sb.toString();
	}

	private String parseMetaRefreshContent(String content) {
		Matcher m = refreshURLPattern.matcher(content);
		if(m.matches()) {
			if(m.groupCount() == 1) {
				return m.group(1);
			}
		}
		return "-";
	}
}
