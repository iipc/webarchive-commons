package org.archive.url;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IAURLCanonicalizerTest {

	@Test
	public void testFull() throws URISyntaxException {
		IAURLCanonicalizer iaC = new IAURLCanonicalizer(new AggressiveIACanonicalizerRules());
		compCan(iaC,"http://www.archive.org:80/","http://archive.org/");
		compCan(iaC,"https://www.archive.org:80/","https://archive.org:80/");
		compCan(iaC,"http://www.archive.org:443/","http://archive.org:443/");
		compCan(iaC,"https://www.archive.org:443/","https://archive.org/");
		compCan(iaC,"http://www.archive.org:/","http://archive.org/");
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

	@Test
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

	@Test
	public void testMassageHost() {
		assertEquals("foo.com",IAURLCanonicalizer.massageHost("foo.com"));
		assertEquals("foo.com",IAURLCanonicalizer.massageHost("www.foo.com"));
		assertEquals("foo.com",IAURLCanonicalizer.massageHost("www12.foo.com"));
		assertEquals("www2foo.com",IAURLCanonicalizer.massageHost("www2foo.com"));
		assertEquals("www2foo.com",IAURLCanonicalizer.massageHost("www2.www2foo.com"));
	}

	@Test
	public void testGetDefaultPort() {
		assertEquals(0,IAURLCanonicalizer.getDefaultPort("foo"));
		assertEquals(80,IAURLCanonicalizer.getDefaultPort("http"));
		assertEquals(443,IAURLCanonicalizer.getDefaultPort("https"));
	}

	@Test
	public void testStripSessionId() throws URISyntaxException {
	    IAURLCanonicalizer iaC = new IAURLCanonicalizer(new AggressiveIACanonicalizerRules());
	    compCan(iaC,
                "http://www.nsf.gov/statistics/sed/2009/SED_2009.zip?CFID=14387305&CFTOKEN=72942008&jsessionid=f030eacc7e49c4ca0b077922347418418766",
                "http://nsf.gov/statistics/sed/2009/sed_2009.zip?jsessionid=f030eacc7e49c4ca0b077922347418418766");
	    compCan(iaC,
                "http://www.nsf.gov/statistics/sed/2009/SED_2009.zip?CFID=14387305&CFTOKEN=72942008",
                "http://nsf.gov/statistics/sed/2009/sed_2009.zip");        
    }	

}
