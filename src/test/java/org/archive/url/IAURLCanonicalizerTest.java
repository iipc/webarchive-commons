package org.archive.url;

import java.net.URISyntaxException;

import junit.framework.TestCase;

public class IAURLCanonicalizerTest extends TestCase {

	public void testFull() throws URISyntaxException {
		IAURLCanonicalizer iaC = new IAURLCanonicalizer(new DefaultIACanonicalizerRules());
		compCan(iaC,"http://www.archive.org:80/","http://archive.org/");
		compCan(iaC,"https://www.archive.org:80/","https://archive.org:80/");
		compCan(iaC,"http://www.archive.org:443/","http://archive.org:443/");
		compCan(iaC,"https://www.archive.org:443/","https://archive.org/");
		compCan(iaC,"http://www.archive.org/big/","http://archive.org/big");
		compCan(iaC,"dns:www.archive.org","dns:www.archive.org");

		//assertEquals("http")
	}	

	private void compCan(URLCanonicalizer c, String orig, String want) throws URISyntaxException {
		HandyURL u = URLParser.parse(orig);
		c.canonicalize(u);
		String got = u.getURLString();
		assertEquals(want,got);
	}

	public void testAlphaReorderQuery() {
		assertEquals(null,IAURLCanonicalizer.alphaReorderQuery(null));
		assertEquals("",IAURLCanonicalizer.alphaReorderQuery(""));
		assertEquals("a",IAURLCanonicalizer.alphaReorderQuery("a"));
		assertEquals("ab",IAURLCanonicalizer.alphaReorderQuery("ab"));
		assertEquals("a=1",IAURLCanonicalizer.alphaReorderQuery("a=1"));
		assertEquals("ab=1",IAURLCanonicalizer.alphaReorderQuery("ab=1"));
		assertEquals("&a=1",IAURLCanonicalizer.alphaReorderQuery("a=1&"));
		assertEquals("a=1&b=1",IAURLCanonicalizer.alphaReorderQuery("a=1&b=1"));
		assertEquals("a=1&b=1",IAURLCanonicalizer.alphaReorderQuery("b=1&a=1"));
		assertEquals("a=a&a=a",IAURLCanonicalizer.alphaReorderQuery("a=a&a=a"));
		assertEquals("a=a&a=b",IAURLCanonicalizer.alphaReorderQuery("a=b&a=a"));
		assertEquals("a=a&a=b&b=a&b=b",IAURLCanonicalizer.alphaReorderQuery("b=b&a=b&b=a&a=a"));
	}

	public void testMassageHost() {
		assertEquals("foo.com",IAURLCanonicalizer.massageHost("foo.com"));
		assertEquals("foo.com",IAURLCanonicalizer.massageHost("www.foo.com"));
		assertEquals("foo.com",IAURLCanonicalizer.massageHost("www12.foo.com"));
		assertEquals("www2foo.com",IAURLCanonicalizer.massageHost("www2foo.com"));
		assertEquals("www2foo.com",IAURLCanonicalizer.massageHost("www2.www2foo.com"));
	}

	public void testGetDefaultPort() {
		assertEquals(0,IAURLCanonicalizer.getDefaultPort("foo"));
		assertEquals(80,IAURLCanonicalizer.getDefaultPort("http"));
		assertEquals(443,IAURLCanonicalizer.getDefaultPort("https"));
	}

}
