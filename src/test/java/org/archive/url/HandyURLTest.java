package org.archive.url;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HandyURLTest {

	@Test
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
}
