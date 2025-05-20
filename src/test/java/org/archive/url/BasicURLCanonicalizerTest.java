package org.archive.url;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BasicURLCanonicalizerTest {
	BasicURLCanonicalizer guc = new BasicURLCanonicalizer();

	@Test
	public void testGetHex() {
		assertEquals(0,guc.getHex('0'));
		assertEquals(1,guc.getHex('1'));
		assertEquals(2,guc.getHex('2'));
		assertEquals(3,guc.getHex('3'));
		assertEquals(4,guc.getHex('4'));
		assertEquals(5,guc.getHex('5'));
		assertEquals(6,guc.getHex('6'));
		assertEquals(7,guc.getHex('7'));
		assertEquals(8,guc.getHex('8'));
		assertEquals(9,guc.getHex('9'));
		assertEquals(10,guc.getHex('a'));
		assertEquals(11,guc.getHex('b'));
		assertEquals(12,guc.getHex('c'));
		assertEquals(13,guc.getHex('d'));
		assertEquals(14,guc.getHex('e'));
		assertEquals(15,guc.getHex('f'));
		assertEquals(10,guc.getHex('A'));
		assertEquals(11,guc.getHex('B'));
		assertEquals(12,guc.getHex('C'));
		assertEquals(13,guc.getHex('D'));
		assertEquals(14,guc.getHex('E'));
		assertEquals(15,guc.getHex('F'));
		assertEquals(-1,guc.getHex('G'));
		assertEquals(-1,guc.getHex('G'));
		assertEquals(-1,guc.getHex('q'));
		assertEquals(-1,guc.getHex(' '));
	}

	@Test
	public void testDecode() {
		assertEquals("A",guc.decode("A"));
		assertEquals("AA",guc.decode("AA"));
		assertEquals("Aa",guc.decode("Aa"));
		assertEquals("aA",guc.decode("aA"));
		assertEquals("%aq",guc.decode("%aq"));
		assertEquals("%aQ",guc.decode("%aQ"));
		assertEquals("\n",guc.decode("%0a"));
		assertEquals("\t",guc.decode("%09"));
		
		assertEquals("!",guc.decode("%21"));
		assertEquals("}",guc.decode("%7d"));

		assertEquals("!!",guc.decode("%21%21"));
		assertEquals("A!!",guc.decode("A%21%21"));
		assertEquals("!A!!",guc.decode("!A%21%21"));
		assertEquals("!A!!",guc.decode("%21A%21%21"));
		assertEquals("%!A!!",guc.decode("%!A%21%21"));
		assertEquals("%!A!!%",guc.decode("%!A%21%21%"));
		assertEquals("%!A!!%5",guc.decode("%!A%21%21%5"));
		assertEquals("%!A!!%",guc.decode("%!A%21%21%25"));

		// unicode
		assertEquals("\u2691", guc.decode("%E2%9A%91"));
		assertEquals("\u2691x", guc.decode("%E2%9A%91x"));
		assertEquals("\u2691xx", guc.decode("%E2%9A%91xx"));
		assertEquals("\u2691xxx", guc.decode("%E2%9A%91xxx"));
		assertEquals("x\u2691", guc.decode("x%E2%9A%91"));
		assertEquals("xx\u2691", guc.decode("xx%E2%9A%91"));
		assertEquals("xxx\u2691", guc.decode("xxx%E2%9A%91"));
		assertEquals("\u2691\u2691", guc.decode("%E2%9A%91%E2%9A%91"));
		assertEquals("\u2691x\u2691", guc.decode("%E2%9A%91x%E2%9A%91"));
		assertEquals("\u2691xx\u2691", guc.decode("%E2%9A%91xx%E2%9A%91"));
		assertEquals("\u2691xxx\u2691", guc.decode("%E2%9A%91xxx%E2%9A%91"));
		assertEquals("\u2691x\u2691x", guc.decode("%E2%9A%91x%E2%9A%91x"));
		assertEquals("\u2691x\u2691xx", guc.decode("%E2%9A%91x%E2%9A%91xx"));
		assertEquals("\u2691x\u2691xxx", guc.decode("%E2%9A%91x%E2%9A%91xxx"));
		assertEquals("\u2691xx\u2691x", guc.decode("%E2%9A%91xx%E2%9A%91x"));
		assertEquals("\u2691xx\u2691xx", guc.decode("%E2%9A%91xx%E2%9A%91xx"));
		assertEquals("\u2691xx\u2691xxx", guc.decode("%E2%9A%91xx%E2%9A%91xxx"));
		assertEquals("\u2691xxx\u2691%", guc.decode("%E2%9A%91xxx%E2%9A%91%"));
		assertEquals("\u2691xxx\u2691%a", guc.decode("%E2%9A%91xxx%E2%9A%91%a"));
		assertEquals("\u2691xxx\u2691%a%", guc.decode("%E2%9A%91xxx%E2%9A%91%a%"));
		// character above u+ffff represented in java with surrogate pair
		assertEquals("\ud83c\udca1", guc.decode("%F0%9F%82%A1"));
		assertEquals("\ud83c\udca1x", guc.decode("%F0%9F%82%A1x"));
		assertEquals("x\ud83c\udca1", guc.decode("x%F0%9F%82%A1"));
		assertEquals("x\ud83c\udca1x", guc.decode("x%F0%9F%82%A1x"));
		assertEquals("x\ud83c\udca1xx", guc.decode("x%F0%9F%82%A1xx"));
		assertEquals("x\ud83c\udca1xxx", guc.decode("x%F0%9F%82%A1xxx"));
		
		// mixing unicode and non-unicode
		assertEquals("!\u2691",guc.decode("%21%E2%9A%91"));
		assertEquals("!\u2691x",guc.decode("%21%E2%9A%91x"));
		assertEquals("!\u2691xx",guc.decode("%21%E2%9A%91xx"));
		assertEquals("!\u2691xxx",guc.decode("%21%E2%9A%91xxx"));
		assertEquals("\u2691!",guc.decode("%E2%9A%91%21"));
		assertEquals("\u2691!x",guc.decode("%E2%9A%91%21x"));
		assertEquals("\u2691!xx",guc.decode("%E2%9A%91%21xx"));
		assertEquals("\u2691!xxx",guc.decode("%E2%9A%91%21xxx"));
		
		// bad unicode and mixtures
		assertEquals("%E2%9A", guc.decode("%E2%9A"));
		assertEquals("%E2%9Ax", guc.decode("%E2%9Ax"));
		assertEquals("%E2%9Axx", guc.decode("%E2%9Axx"));
		assertEquals("%E2%9Axxx", guc.decode("%E2%9Axxx"));
		assertEquals("x%E2%9A", guc.decode("x%E2%9A"));
		assertEquals("xx%E2%9A", guc.decode("xx%E2%9A"));
		assertEquals("xxx%E2%9A", guc.decode("xxx%E2%9A"));
		assertEquals("%E2x%9A", guc.decode("%E2x%9A"));
		assertEquals("%E2!%9A", guc.decode("%E2%21%9A"));
		assertEquals("\u2691%E2%9A", guc.decode("%E2%9A%91%E2%9A"));
		assertEquals("\u2691%E2%9Ax", guc.decode("%E2%9A%91%E2%9Ax"));
		assertEquals("\u2691%E2%9Axx", guc.decode("%E2%9A%91%E2%9Axx"));
		assertEquals("\u2691%E2%9Axxx", guc.decode("%E2%9A%91%E2%9Axxx"));
		assertEquals("x\u2691%E2%9A", guc.decode("x%E2%9A%91%E2%9A"));
		assertEquals("xx\u2691%E2%9A", guc.decode("xx%E2%9A%91%E2%9A"));
		assertEquals("xxx\u2691%E2%9A", guc.decode("xxx%E2%9A%91%E2%9A"));
		assertEquals("%E2%9A\u2691", guc.decode("%E2%9A%E2%9A%91"));
		assertEquals("%E2%9A\u2691x", guc.decode("%E2%9A%E2%9A%91x"));
		assertEquals("%E2%9A\u2691xx", guc.decode("%E2%9A%E2%9A%91xx"));
		assertEquals("%E2%9A\u2691xxx", guc.decode("%E2%9A%E2%9A%91xxx"));
		assertEquals("\u2691%E2%9A\u2691%E2%9A", guc.decode("%E2%9A%91%E2%9A%E2%9A%91%E2%9A"));
		assertEquals("\u2691%E2%9A\u2691%E2%9Ax", guc.decode("%E2%9A%91%E2%9A%E2%9A%91%E2%9Ax"));
		assertEquals("\u2691%E2%9A\u2691%E2%9Axx", guc.decode("%E2%9A%91%E2%9A%E2%9A%91%E2%9Axx"));
		assertEquals("\u2691%E2%9A\u2691%E2%9Axxx", guc.decode("%E2%9A%91%E2%9A%E2%9A%91%E2%9Axxx"));
		assertEquals("%E2%9A!\u2691", guc.decode("%E2%9A%21%E2%9A%91"));
		assertEquals("%E2%9A\u2691!x", guc.decode("%E2%9A%E2%9A%91%21x"));
		assertEquals("%E2!%9A\u2691xx", guc.decode("%E2%21%9A%E2%9A%91xx"));
		assertEquals("%E2%9A\u2691xxx", guc.decode("%E2%9A%E2%9A%91xxx"));
		assertEquals("\u2691%E2%9A!\u2691%E2%9A", guc.decode("%E2%9A%91%E2%9A%21%E2%9A%91%E2%9A"));
	}
	
	@Test
	public void testUnescapeRepeatedly() {
		assertEquals("%!A!!%",guc.unescapeRepeatedly("%!A%21%21%25"));
		assertEquals("%",guc.unescapeRepeatedly("%"));
		assertEquals("%2",guc.unescapeRepeatedly("%2"));
		assertEquals("%",guc.unescapeRepeatedly("%25"));
		assertEquals("%%",guc.unescapeRepeatedly("%25%"));
		assertEquals("%",guc.unescapeRepeatedly("%2525"));
		assertEquals("%",guc.unescapeRepeatedly("%252525"));
		assertEquals("%",guc.unescapeRepeatedly("%25%32%35"));
		
		assertEquals("168.188.99.26",guc.unescapeRepeatedly("%31%36%38%2e%31%38%38%2e%39%39%2e%32%36"));

		assertEquals("tag=%E4%EE%F8%EA%EE%EB%FC%ED%EE%E5",
				guc.unescapeRepeatedly("tag=%E4%EE%F8%EA%EE%EB%FC%ED%EE%E5"));
	}

	@Test
	public void testAttemptIPFormats() throws URIException {
        assertNull(guc.attemptIPFormats(null));
        assertNull(guc.attemptIPFormats("www.foo.com"));
		assertEquals("127.0.0.1",guc.attemptIPFormats("127.0.0.1"));
		assertEquals("15.0.0.1",guc.attemptIPFormats("017.0.0.1"));
		assertEquals("168.188.99.26",guc.attemptIPFormats("168.188.99.26"));
		
		// TODO: should flush these out. No IPv6 tests..
		/*
		 * These test may not be complete. Specifically there is mention in the
		 * spec that "partial" IP addresses should be handled. One non-googler
		 * suggested the following:
		 *     
		 *     http://10.9
		 *     http://10.0.0.011
		 *     http://10.0.0.0x09
		 *     http://10.0.0.9/#link
		 *     
		 * Are all equivalent to:
		 * 
		 *     http://10.0.0.9/
		 * 
		 * Further mention, from page 8 of:
		 * 
		 *    http://tools.ietf.org/html/draft-iab-identifier-comparison-00
		 *

   In specifying the inet_addr() API, the POSIX standard [IEEE-1003.1]
   defines "IPv4 dotted decimal notation" as allowing not only strings
   of the form "10.0.1.2", but also allows octal and hexadecimal, and
   addresses with less than four parts.  For example, "10.0.258",
   "0xA000001", and "012.0x102" all represent the same IPv4 address in
   standard "IPv4 dotted decimal" notation.  We will refer to this as
   the "loose" syntax of an IPv4 address literal.

		 * 
		 * I found few other examples of partial ports in a semi-quick google
		 * search.. should verify in a browser and add them as tests..
		 * 
		 *  For now, we'll enforce some strictness:
		 */

        assertNull(guc.attemptIPFormats("10.0.258"));
        assertNull(guc.attemptIPFormats("1.2.3.256"));
		
	}

	@Test
	public void testFoo() {
		String path = "/a/b/c/";
		String[] paths = path.split("/",-1);
		for(String p : paths) {
			System.out.format("(%s)",p);
		}
		System.out.println();
		paths = path.split("/");
		for(String p : paths) {
			System.out.format("(%s)",p);
		}
		System.out.println();
	}
	
	/*
	 * Tests copied from https://developers.google.com/safe-browsing/developers_guide_v2#Canonicalization
	 */
	@Test
	public void testGoogleExamples() throws URISyntaxException  {
		checkCanonicalization("http://host/%25%32%35", "http://host/%25");
		checkCanonicalization("http://host/%25%32%35%25%32%35", "http://host/%25%25");
		checkCanonicalization("http://host/%2525252525252525", "http://host/%25");
		checkCanonicalization("http://host/asdf%25%32%35asd", "http://host/asdf%25asd");
		checkCanonicalization("http://host/%%%25%32%35asd%%", "http://host/%25%25%25asd%25%25");
		checkCanonicalization("http://www.google.com/", "http://www.google.com/");
		checkCanonicalization("http://%31%36%38%2e%31%38%38%2e%39%39%2e%32%36/%2E%73%65%63%75%72%65/%77%77%77%2E%65%62%61%79%2E%63%6F%6D/", "http://168.188.99.26/.secure/www.ebay.com/");
		checkCanonicalization("http://195.127.0.11/uploads/%20%20%20%20/.verify/.eBaysecure=updateuserdataxplimnbqmn-xplmvalidateinfoswqpcmlx=hgplmcx/", "http://195.127.0.11/uploads/%20%20%20%20/.verify/.eBaysecure=updateuserdataxplimnbqmn-xplmvalidateinfoswqpcmlx=hgplmcx/");
		checkCanonicalization("http://host%23.com/%257Ea%2521b%2540c%2523d%2524e%25f%255E00%252611%252A22%252833%252944_55%252B", "http://host%23.com/~a!b@c%23d$e%25f^00&11*22(33)44_55+");
		checkCanonicalization("http://3279880203/blah", "http://195.127.0.11/blah");
		checkCanonicalization("http://www.google.com/blah/..", "http://www.google.com/");
		checkCanonicalization("www.google.com/", "http://www.google.com/");
		checkCanonicalization("www.google.com", "http://www.google.com/");
		checkCanonicalization("http://www.evil.com/blah#frag", "http://www.evil.com/blah");
		checkCanonicalization("http://www.GOOgle.com/", "http://www.google.com/");
		checkCanonicalization("http://www.google.com.../", "http://www.google.com/");
		checkCanonicalization("http://www.google.com/foo\tbar\rbaz\n2", "http://www.google.com/foobarbaz2");
		checkCanonicalization("http://www.google.com/q?", "http://www.google.com/q?");
		checkCanonicalization("http://www.google.com/q?r?", "http://www.google.com/q?r?");
		checkCanonicalization("http://www.google.com/q?r?s", "http://www.google.com/q?r?s");
		checkCanonicalization("http://evil.com/foo#bar#baz", "http://evil.com/foo");
		checkCanonicalization("http://evil.com/foo;", "http://evil.com/foo;");
		checkCanonicalization("http://evil.com/foo?bar;", "http://evil.com/foo?bar;");
		// XXX this test not possible with java string, uses raw byte 0x80
		// XXX should canonicalizer accept byte array as input?
		// checkCanonicalization("http://\x01\x80.com/", "http://%01%80.com/");
		checkCanonicalization("http://notrailingslash.com", "http://notrailingslash.com/");
		checkCanonicalization("http://www.gotaport.com:1234/", "http://www.gotaport.com:1234/");
		checkCanonicalization("  http://www.google.com/  ", "http://www.google.com/");
		checkCanonicalization("http:// leadingspace.com/", "http://%20leadingspace.com/");
		checkCanonicalization("http://%20leadingspace.com/", "http://%20leadingspace.com/");
		checkCanonicalization("%20leadingspace.com/", "http://%20leadingspace.com/");
		checkCanonicalization("https://www.securesite.com/", "https://www.securesite.com/");
		checkCanonicalization("http://host.com/ab%23cd", "http://host.com/ab%23cd");
		checkCanonicalization("http://host.com//twoslashes?more//slashes", "http://host.com/twoslashes?more//slashes");
	}

	@Test
	public void testStraySpacing() throws URISyntaxException {
		checkCanonicalization("http://example.org/\u2028", "http://example.org/");
		checkCanonicalization("\nhttp://examp\rle.org/", "http://example.org/");
		checkCanonicalization("\nhttp://examp\u2029\t\rle.org/         ", "http://example.org/");
	}

	@Test
	public void testSchemeCapitalsPreserved() throws URISyntaxException {
		checkCanonicalization("Http://example.com", "Http://example.com/");
		checkCanonicalization("HTTP://example.com", "HTTP://example.com/");
		checkCanonicalization("ftP://example.com", "ftP://example.com/");
	}

	@Test
	public void testUnicodeEscaping() throws URISyntaxException {
		checkCanonicalization("http://example.org/\u2691", "http://example.org/%E2%9A%91");
		checkCanonicalization("http://example.org/%e2%9a%91", "http://example.org/%E2%9A%91");
		checkCanonicalization("http://example.org/blah?x=\u268b", "http://example.org/blah?x=%E2%9A%8B");
		checkCanonicalization("http://example.org/blah?x=%e2%9a%8b", "http://example.org/blah?x=%E2%9A%8B");
		checkCanonicalization("http://example.org/blah?z\u265fz=z\u4e00z", "http://example.org/blah?z%E2%99%9Fz=z%E4%B8%80z");
		checkCanonicalization("http://example.org/blah?z%e2%99%9Fz=z%e4%b8%80z", "http://example.org/blah?z%E2%99%9Fz=z%E4%B8%80z");
		checkCanonicalization("http://example.org/bl\u2691ah?z\u265fz=z\u4e00z", "http://example.org/bl%E2%9A%91ah?z%E2%99%9Fz=z%E4%B8%80z");
		checkCanonicalization("http://example.org/bl%E2%9A%91ah?z%e2%99%9Fz=z%E4%b8%80z", "http://example.org/bl%E2%9A%91ah?z%E2%99%9Fz=z%E4%B8%80z");
		// character above u+ffff represented in java with surrogate pair
		checkCanonicalization("http://example.org/\ud83c\udca1", "http://example.org/%F0%9F%82%A1");
		// make sure character above u+ffff survives unescaping and re-escaping
		checkCanonicalization("http://example.org/%F0%9F%82%A1", "http://example.org/%F0%9F%82%A1");
	}
	
	private void checkCanonicalization(String in, String want) throws URISyntaxException {
		HandyURL h = URLParser.parse(in);
		guc.canonicalize(h);
		String got = h.getURLString();
		assertEquals(want, got);
	}
}
