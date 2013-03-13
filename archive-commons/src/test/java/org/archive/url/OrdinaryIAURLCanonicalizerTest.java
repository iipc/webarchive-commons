package org.archive.url;

import junit.framework.TestCase;

import org.apache.commons.httpclient.URIException;

public class OrdinaryIAURLCanonicalizerTest extends TestCase {
	private OrdinaryIAURLCanonicalizer canon = new OrdinaryIAURLCanonicalizer();
	
	public void testMisc() throws URIException {
		checkCanonicalization("http://...host..com..", "http://host.com/");
		checkCanonicalization("http://example.org:80/", "http://example.org/");
		checkCanonicalization("https://example.org:443/", "https://example.org/");
		checkCanonicalization("http://example.org:443/", "http://example.org:443/");
		checkCanonicalization("http://example.org/?", "http://example.org/");
		checkCanonicalization("http://example.org/foo?", "http://example.org/foo");
		checkCanonicalization("http://example.org/foo/?", "http://example.org/foo/");
	}

	public void testSchemeCapitals() throws URIException {
		checkCanonicalization("Http://example.com", "http://example.com/");
		checkCanonicalization("HTTP://example.com", "http://example.com/");
		checkCanonicalization("ftP://example.com", "ftp://example.com/");
	}
	
	private void checkCanonicalization(String in, String want) throws URIException {
		HandyURL h = URLParser.parse(in);
		canon.canonicalize(h);
		String got = h.getURLString();
		assertEquals(want, got);
	}

}
