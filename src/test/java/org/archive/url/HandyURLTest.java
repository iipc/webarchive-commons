package org.archive.url;

import junit.framework.TestCase;

public class HandyURLTest extends TestCase {

	public void testGetPublicSuffix() {
		HandyURL h = new HandyURL();
		h.setHost("www.fool.com");
		assertEquals("fool.com",h.getPublicSuffix());
		assertEquals("www",h.getPublicPrefix());

		h.setHost("www.amazon.co.uk");
		assertEquals("amazon.co.uk",h.getPublicSuffix());
		assertEquals("www",h.getPublicPrefix());

		h.setHost("www.images.amazon.co.uk");
		assertEquals("amazon.co.uk",h.getPublicSuffix());
		assertEquals("www.images",h.getPublicPrefix());

		h.setHost("funky-images.fancy.co.jp");
		assertEquals("fancy.co.jp",h.getPublicSuffix());
		assertEquals("funky-images",h.getPublicPrefix());
	
	}

	public void testGetPublicPrefix() {
//		
//		fail("Not yet implemented");
	}

}
