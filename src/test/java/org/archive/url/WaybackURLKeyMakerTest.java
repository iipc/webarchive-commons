package org.archive.url;

import java.net.URISyntaxException;

import junit.framework.TestCase;

public class WaybackURLKeyMakerTest extends TestCase {

	public void testMakeKey() throws URISyntaxException {
		WaybackURLKeyMaker km = new WaybackURLKeyMaker();
		assertEquals("-", km.makeKey(null));
		assertEquals("-", km.makeKey(""));
		assertEquals("dskgfljsdlkgjslkj)/", km.makeKey("dskgfljsdlkgjslkj"));
		assertEquals("filedesc:foo.arc.gz", km.makeKey("filedesc:foo.arc.gz"));
		assertEquals("filedesc:/foo.arc.gz", km.makeKey("filedesc:/foo.arc.gz"));
		assertEquals("filedesc://foo.arc.gz", km.makeKey("filedesc://foo.arc.gz"));
		assertEquals("warcinfo:foo.warc.gz", km.makeKey("warcinfo:foo.warc.gz"));
		assertEquals("com,alexa)", km.makeKey("dns:alexa.com"));
		assertEquals("org,archive)", km.makeKey("dns:archive.org"));
		assertEquals("org,archive)/", km.makeKey("http://archive.org/"));
		assertEquals("org,archive)/goo", km.makeKey("http://archive.org/goo/"));
		assertEquals("org,archive)/goo", km.makeKey("http://archive.org/goo/?"));
		assertEquals("org,archive)/goo?a&b", km.makeKey("http://archive.org/goo/?b&a"));
		assertEquals("org,archive)/goo?a=1&a=2&b", km.makeKey("http://archive.org/goo/?a=2&b&a=1"));
		assertEquals("org,archive)/", km.makeKey("http://archive.org:/"));
	}

}
