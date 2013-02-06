package org.archive.url;

import org.apache.commons.httpclient.URIException;

public class WaybackURLKeyMaker implements URLKeyMaker {
//	URLCanonicalizer canonicalizer = new NonMassagingIAURLCanonicalizer();
	URLCanonicalizer canonicalizer = new DefaultIAURLCanonicalizer();
	
	public URLCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(URLCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	private boolean surtMode = true;
	
	public WaybackURLKeyMaker()
	{

	}
	
	public boolean isSurtMode()
	{
		return surtMode;
	}
	
	public WaybackURLKeyMaker(boolean surtMode)
	{
		this.surtMode = surtMode;
	}

	public String makeKey(String url) throws URIException {
		if(url == null) {
			return "-";
		}
		if(url.length() == 0) {
			return "-";
		}
		if(url.startsWith("filedesc")) {
			return url;
		}
		if(url.startsWith("warcinfo")) {
			return url;
		}
		if(url.startsWith("dns:")) {
			String authority = url.substring(4);
			if (!surtMode) {
				return authority;
			}
			String surt = URLRegexTransformer.hostToSURT(authority);
			return surt + ")";
		}
		HandyURL hURL;

			hURL = URLParser.parse(url);
			canonicalizer.canonicalize(hURL);
			String key = hURL.getURLString(surtMode, surtMode, false);
			if (!surtMode) {
				return key;
			}
			int parenIdx = key.indexOf('(');
			if(parenIdx == -1) {
				// something very wrong..
				return url;
			}
			return key.substring(parenIdx+1);
//		} catch (URIException e) {
//			e.printStackTrace();
//		}
//		return url;
	}
}
