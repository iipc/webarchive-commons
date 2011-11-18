package org.archive.url;

import org.apache.commons.httpclient.URIException;

public class WaybackURLKeyMaker implements URLKeyMaker {
//	URLCanonicalizer canonicalizer = new NonMassagingIAURLCanonicalizer();
	URLCanonicalizer canonicalizer = new DefaultIAURLCanonicalizer();

	public String makeKey(String url) {
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
			String surt = URLRegexTransformer.hostToSURT(authority);
			return surt + ")";
		}
		HandyURL hURL;
		try {
			hURL = URLParser.parse(url);
			canonicalizer.canonicalize(hURL);
			String key = hURL.getURLString(true, false);
			int parenIdx = key.indexOf('(');
			if(parenIdx == -1) {
				// something very wrong..
				return url;
			}
			return key.substring(parenIdx+1);
		} catch (URIException e) {
			e.printStackTrace();
		}
		return url;
	}
}
