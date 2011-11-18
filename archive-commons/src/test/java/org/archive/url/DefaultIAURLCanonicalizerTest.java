package org.archive.url;

import org.apache.commons.httpclient.URIException;

import junit.framework.TestCase;

public class DefaultIAURLCanonicalizerTest extends TestCase {
	static DefaultIAURLCanonicalizer ia = new DefaultIAURLCanonicalizer();
	public void testCanonicalize() throws URIException {
		// FULL end-to-end tests:
		check("http://www.alexa.com/","http://alexa.com/");
		check("http://archive.org/index.html","http://archive.org/index.html");
		check("http://archive.org/index.html?","http://archive.org/index.html");
		check("http://archive.org/index.html?a=b","http://archive.org/index.html?a=b");
		check("http://archive.org/index.html?b=b&a=b","http://archive.org/index.html?a=b&b=b");
		check("http://archive.org/index.html?b=a&b=b&a=b","http://archive.org/index.html?a=b&b=a&b=b");
		check("http://www34.archive.org/index.html?b=a&b=b&a=b","http://archive.org/index.html?a=b&b=a&b=b");
	}

	private static void check(String orig, String want) throws URIException {
		HandyURL u = URLParser.parse(orig);
		ia.canonicalize(u);
		String got = u.getURLString();
		assertEquals(want,got);
		
		HandyURL u2 = URLParser.parse(got);
		ia.canonicalize(u2);
		String got2 = u2.getURLString();
		assertEquals("Second passs changed!",got,got2);
	}
}
