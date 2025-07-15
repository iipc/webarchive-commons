/*
 *  This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.archive.url;

import java.util.TreeMap;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test UURIFactory for proper UURI creation across variety of
 * important/tricky cases.
 * 
 * Be careful writing this file.  Make sure you write it with UTF-8 encoding.
 *
 * @author igor stack gojomo
 */
public class UsableURIFactoryTest {

	@Test
	public final void testEscaping() throws URIException {
		// Note: single quote is not being escaped by URI class.
		final String ESCAPED_URISTR = "http://archive.org/" +
		    UsableURIFactory.ESCAPED_SPACE +
			UsableURIFactory.ESCAPED_SPACE +
			UsableURIFactory.ESCAPED_CIRCUMFLEX +
			UsableURIFactory.ESCAPED_QUOT +
			UsableURIFactory.SQUOT +
			UsableURIFactory.ESCAPED_APOSTROPH +
			UsableURIFactory.ESCAPED_LSQRBRACKET +
			UsableURIFactory.ESCAPED_RSQRBRACKET +
			UsableURIFactory.ESCAPED_LCURBRACKET +
			UsableURIFactory.ESCAPED_RCURBRACKET +
			UsableURIFactory.SLASH + "a.gif"; // NBSP and SPACE should be trimmed;
		
		final String URISTR = "http://archive.org/.././" + "\u00A0" +
		    UsableURIFactory.SPACE + UsableURIFactory.CIRCUMFLEX +
			UsableURIFactory.QUOT + UsableURIFactory.SQUOT +
			UsableURIFactory.APOSTROPH + UsableURIFactory.LSQRBRACKET +
			UsableURIFactory.RSQRBRACKET + UsableURIFactory.LCURBRACKET +
			UsableURIFactory.RCURBRACKET + UsableURIFactory.BACKSLASH +
			"test/../a.gif" + "\u00A0" + UsableURIFactory.SPACE;
		
		UsableURI uuri = UsableURIFactory.getInstance(URISTR);
		final String uuriStr = uuri.toString();
		assertEquals(ESCAPED_URISTR, uuriStr, "expected escaping");
	}

	@Test
    public final void testUnderscoreMakesPortParseFail() throws URIException {
        UsableURI uuri = UsableURIFactory.getInstance("http://one-two_three:8080/index.html");
        int port = uuri.getPort();
        assertEquals(8080, port, "Failed find of port " + uuri);
    }

	@Test
    public final void testRelativeURIWithTwoSlashes() throws URIException {
        UsableURI base = UsableURIFactory.getInstance("http://www.archive.org");
        UsableURI uuri = UsableURIFactory.getInstance(base, "one//index.html");
        assertEquals("http://www.archive.org/one//index.html", uuri.toString(),
				"Doesn't do right thing with two slashes " + uuri);
    }

	@Test
    public final void testSchemelessURI() throws URIException {
        UsableURI base = UsableURIFactory.getInstance("https://www.archive.org");
        UsableURI uuri = UsableURIFactory.getInstance(base, "//example.com/monkey?this:uri:has:colons");
        assertEquals("https://example.com/monkey?this:uri:has:colons", uuri.toString(),
				"Doesn't do right thing with a schemeless URI " + uuri);
    }

	@Test
    public final void testTrailingEncodedSpace() throws URIException {
        UsableURI uuri = UsableURIFactory.getInstance("http://www.nps-shoes.co.uk%20");
        assertEquals("http://www.nps-shoes.co.uk/", uuri.toString(),
				"Doesn't strip trailing encoded space 1 " + uuri);
        uuri = UsableURIFactory.getInstance("http://www.nps-shoes.co.uk%20%20%20");
        assertEquals("http://www.nps-shoes.co.uk/", uuri.toString(),
				"Doesn't strip trailing encoded space 2 " + uuri);
    }

	@Test
    public final void testPort0080is80() throws URIException {
        UsableURI uuri = UsableURIFactory.getInstance("http://archive.org:0080");
        assertEquals("http://archive.org/", uuri.toString(), "Doesn't strip leading zeros " + uuri);
    }
    
// DISABLING TEST AS PRECURSOR TO ELIMINATION
// the problematic input given -- specifically the "%6s" incomplete uri-escape,
// shouldn't necessarily be rejected as a bad URI. IE and Firefox, at least, 
// will  attempt to fetch such an URL (getting, in this case against that ad 
// server, a bad-request error). Ideally, we'd generate exactly the same 
// request against the server as they do. However, with the most recent 
// fixup for stray '%' signs, we come close, but not exactly. That's enough
// to cause this test to fail (it's not getting the expected exception) but
// our almost-URI, which might be what was intended, is better than trying 
// nothing.
//    public final void testBadPath() {
//        String message = null;
//        try {
//            UURIFactory.getInstance("http://ads.as4x.tmcs.net/" +
//                "html.ng/site=cs&pagepos=102&page=home&adsize=1x1&context=" +
//                "generic&Params.richmedia=yes%26city%3Dseattle%26" +
//                "rstid%3D2415%26market_id%3D86%26brand%3Dcitysearch" +
//                "%6state%3DWA");
//        } catch (URIException e) {
//            message = e.getMessage();
//        }
//        assertNotNull("Didn't get expected exception.", message);
//    }   

	@Test
    public final void testEscapeEncoding() throws URIException {
        UsableURI uuri = UsableURIFactory.getInstance("http://www.y1y1.com/" +
            "albums/userpics/11111/normal_%E3%E4%EC%EC%EC.jpg", "windows-1256");
        uuri.getPath();
    }   

	@Test
    public final void testTooLongAfterEscaping() {
        StringBuffer buffer = new StringBuffer("http://www.archive.org/a/");
        // Append bunch of spaces.  When escaped, they'll triple in size.
        for (int i = 0; i < 1024; i++) {
        	buffer.append(" ");
        }
        buffer.append("/index.html");
        String message = null;
        try {
        	UsableURIFactory.getInstance(buffer.toString());
        } catch (URIException e) {
            message = e.getMessage();
        }
        assertTrue((message != null) && message.startsWith("Created (escaped) uuri >"),
				"Wrong or no exception: " + message);
    }

	@Test
	public final void testFtpUris() throws URIException {
		final String FTP = "ftp";
		final String AUTHORITY = "pfbuser:pfbuser@mprsrv.agri.gov.cn";
		final String PATH = "/clzreceive/";
		final String uri = FTP + "://" + AUTHORITY + PATH;
		UsableURI uuri = UsableURIFactory.getInstance(uri);
        assertEquals(FTP, (uuri.getScheme()), "Failed to get matching scheme: " + uuri.getScheme());
        assertEquals(AUTHORITY, (uuri.getAuthority()), "Failed to get matching authority: " +
                uuri.getAuthority());
        assertEquals(PATH, (uuri.getPath()), "Failed to get matching path: " +
                uuri.getPath());
	}

	@Test
    public final void testWhitespaceEscaped() throws URIException {
        // Test that we get all whitespace even if the uri is
        // already escaped.
        String uri = "http://archive.org/index%25 .html";
        String tgtUri = "http://archive.org/index%25%20.html";
        UsableURI uuri = UsableURIFactory.getInstance(uri);
        assertEquals(tgtUri, uuri.toString(), "Not equal " + uuri);
        uri = "http://archive.org/index%25\u001D.html";
        tgtUri = "http://archive.org/index%25%1D.html";
        uuri = UsableURIFactory.getInstance(uri);
        assertEquals(tgtUri, uuri.toString(), "whitespace escaping");
        uri = "http://gemini.info.usaid.gov/directory/" +
            "pbResults.cfm?&urlNameLast=Rumplestiltskin";
        tgtUri = "http://gemini.info.usaid.gov/directory/faxResults.cfm?" +
            "name=Ebenezer%20+Rumplestiltskin,&location=RRB%20%20%20%205%2E08%2D006";
        uuri = UsableURIFactory.getInstance(UsableURIFactory.getInstance(uri),
            "faxResults.cfm?name=Ebenezer +Rumplestiltskin,&location=" +
            "RRB%20%20%20%205%2E08%2D006");
        assertEquals(tgtUri, uuri.toString(), "whitespace escaping");

        // https://webarchive.jira.com/browse/HER-2089
        uri = "http://archive.org/index%25\u3000.html";
        tgtUri = "http://archive.org/index%25%E3%80%80.html";
        uuri = UsableURIFactory.getInstance(uri);
        assertEquals(tgtUri, uuri.toString(), "U+3000 ideographic space escaping");
    }
    
//	public final void testFailedGetPath() throws URIException {
//		final String path = "/RealMedia/ads/" +
//		"click_lx.ads/%%PAGE%%/%%RAND%%/%%POS%%/%%CAMP%%/empty";
//        // decoding in getPath will interpret %CA as 8-bit escaped char,
//        // possibly incomplete
//		final String uri = "http://ads.nandomedia.com" + path;
//		final UURI uuri = UURIFactory.getInstance(uri);
//		String foundPath = uuri.getPath();
//		assertEquals("unexpected path", path, foundPath);
//	}

	@Test
    public final void testDnsHost() throws URIException {
        String uri = "dns://ads.nandomedia.com:81/one.html";
        UsableURI uuri = UsableURIFactory.getInstance(uri);
        String host = uuri.getReferencedHost();
        assertEquals("ads.nandomedia.com", host, "Host is wrong " + host);
        uri = "dns:ads.nandomedia.com";
        uuri = UsableURIFactory.getInstance(uri);
        host = uuri.getReferencedHost();
        assertEquals("ads.nandomedia.com", host, "Host is wrong " + host);
        uri = "dns:ads.nandomedia.com?a=b";
        uuri = UsableURIFactory.getInstance(uri);
        host = uuri.getReferencedHost();
        assertEquals("ads.nandomedia.com", host, "Host is wrong " + host);
    }

	@Test
	public final void testPercentEscaping() throws URIException {
		final String uri = "http://archive.org/%a%%%%%.html";
        // tests indicate firefox (1.0.6) does not encode '%' at all
        final String tgtUri = "http://archive.org/%a%%%%%.html";
		UsableURI uuri = UsableURIFactory.getInstance(uri);
		assertEquals(tgtUri,uuri.toString(), "Not equal");
	}

	@Test
	public final void testRelativeDblPathSlashes() throws URIException {
		UsableURI base = UsableURIFactory.getInstance("http://www.archive.org/index.html");
		UsableURI uuri = UsableURIFactory.getInstance(base, "JIGOU//KYC//INDEX.HTM");
        assertEquals("/JIGOU//KYC//INDEX.HTM", uuri.getPath(), "Double slash not working " + uuri);
	}

	@Test
    public final void testRelativeWithScheme() throws URIException {
        UsableURI base = UsableURIFactory.getInstance("http://www.example.com/some/page");
        UsableURI uuri = UsableURIFactory.getInstance(base, "http:boo");
        assertEquals("http://www.example.com/some/boo", uuri.toString(),
				"Relative with scheme not working " + uuri);
    }

	@Test
    public final void testBadBaseResolve() throws URIException {
        UsableURI base = UsableURIFactory.getInstance("http://license.joins.com/board/" +
            "etc_board_list.asp?board_name=new_main&b_type=&nPage=" +
            "2&category=G&lic_id=70&site=changeup&g_page=changeup&g_sPage=" +
            "notice&gate=02");
        UsableURIFactory.getInstance(base, "http://www.changeup.com/...</a");
    }

	@Test
    public final void testTilde() throws URIException {
        noChangeExpected("http://license.joins.com/~igor");
    }

	@Test
    public final void testCurlies() throws URIException {
        // Firefox allows curlies in the query string portion of a URL only
        // (converts curlies if they are in the path portion ahead of the
        // query string).
        UsableURI uuri =
            noChangeExpected("http://license.joins.com/igor?one={curly}");
        assertEquals("one={curly}", uuri.getQuery());
        assertEquals("http://license.joins.com/igor%7Bcurly%7D.html",
                UsableURIFactory.getInstance("http://license.joins.com/igor{curly}.html").toString());
        boolean exception = false;
        try {
            UsableURIFactory.getInstance("http://license.{curly}.com/igor.html");
        } catch (URIException u) {
            exception = true;
        }
        assertTrue(exception, "Did not get exception.");
    }
    
    protected UsableURI noChangeExpected(final String original)
    throws URIException {
        UsableURI uuri = UsableURIFactory.getInstance(original);
        assertEquals(original, uuri.toString());
        return uuri;
    }

	@Test
	public final void testTrimSpaceNBSP() throws URIException {
		final String uri = "   http://archive.org/DIR WITH SPACES/" +
		UsableURIFactory.NBSP + "home.html    " + UsableURIFactory.NBSP + "   ";
		final String tgtUri =
			"http://archive.org/DIR%20WITH%20SPACES/%20home.html";
		UsableURI uuri = UsableURIFactory.getInstance(uri);
        assertEquals(tgtUri, uuri.toString(), "Not equal " + uuri);
	}
	
	/**
	 * Test space plus encoding ([ 1010966 ] crawl.log has URIs with spaces in them).
	 * See <a href="http://sourceforge.net/tracker/index.php?func=detail&aid=1010966&group_id=73833&atid=539099">[ 1010966 ] crawl.log has URIs with spaces in them</a>.
	 */
	@Test
	public final void testSpaceDoubleEncoding() throws URIException {
		final String uri = "http://www.brook.edu/i.html? %20taxonomy=Politics";
		final String encodedUri =
			"http://www.brook.edu/i.html?%20%20taxonomy=Politics";
		UsableURI uuri = UsableURIFactory.getInstance(uri, "ISO-8859-1");
        assertEquals(encodedUri, uuri.toString(), "Not equal " + uuri.toString());
	}
	
	/**
	 * Test for doubly-encoded sequences.
	 * See <a href="https://sourceforge.net/tracker/index.php?func=detail&aid=966219&group_id=73833&atid=539099">[ 966219 ] UURI doubly-encodes %XX sequences</a>.
	 */
	@Test
	public final void testDoubleEncoding() throws URIException {
		final char ae = '\u00E6';
		final String uri = "http://archive.org/DIR WITH SPACES/home" +
		    ae + ".html";
		final String encodedUri =
			"http://archive.org/DIR%20WITH%20SPACES/home%E6.html";
		UsableURI uuri = UsableURIFactory.getInstance(uri, "ISO-8859-1");
		assertEquals(encodedUri, uuri.toString(), "single encoding");
		// Dbl-encodes.
		uuri = UsableURIFactory.getInstance(uuri.toString(), "ISO-8859-1");
		uuri = UsableURIFactory.getInstance(uuri.toString(), "ISO-8859-1");
		assertEquals(encodedUri, uuri.toString(), "double encoding");
		// Do default utf-8 test.
		uuri = UsableURIFactory.getInstance(uri);
		final String encodedUtf8Uri =
			"http://archive.org/DIR%20WITH%20SPACES/home%C3%A6.html";
		assertEquals(encodedUtf8Uri, uuri.toString(), "Not equal utf8");
		// Now dbl-encode.
		uuri = UsableURIFactory.getInstance(uuri.toString());
		uuri = UsableURIFactory.getInstance(uuri.toString());
		assertEquals(encodedUtf8Uri, uuri.toString(), "Not equal (dbl-encoding) utf8");
	}
	
	/**
	 * Test for syntax errors stop page parsing.
	 * @see <a href="https://sourceforge.net/tracker/?func=detail&aid=788219&group_id=73833&atid=539099">[ 788219 ] URI Syntax Errors stop page parsing</a>
	 * @throws URIException
	 */
	@Test
	public final void testThreeSlashes() throws URIException {
		UsableURI goodURI = UsableURIFactory.
		getInstance("http://lcweb.loc.gov/rr/goodtwo.html");
		String uuri = "http:///lcweb.loc.gov/rr/goodtwo.html";
		UsableURI rewrittenURI = UsableURIFactory.getInstance(uuri);
        assertEquals(goodURI.toString(), rewrittenURI.toString(), "Not equal " + goodURI + ", " + uuri);
		uuri = "http:////lcweb.loc.gov/rr/goodtwo.html";
		rewrittenURI = UsableURIFactory.getInstance(uuri);
        assertEquals(goodURI.toString(), rewrittenURI.toString(), "Not equal " + goodURI + ", " + uuri);
		// Check https.
		goodURI = UsableURIFactory.
		getInstance("https://lcweb.loc.gov/rr/goodtwo.html");
		uuri = "https:////lcweb.loc.gov/rr/goodtwo.html";
		rewrittenURI = UsableURIFactory.getInstance(uuri);
        assertEquals(goodURI.toString(), rewrittenURI.toString(), "Not equal " + goodURI + ", " + uuri);
	}

	@Test
	public final void testNoScheme() {
		boolean expectedException = false;
		String uuri = "www.loc.gov/rr/european/egw/polishex.html";
		try {
			UsableURIFactory.getInstance(uuri);
		} catch (URIException e) {
			// Expected exception.
			expectedException = true;
		}
		assertTrue(expectedException, "Didn't get expected exception: " + uuri);
	}

	@Test
	public final void testRelative() throws URIException {
		UsableURI uuriTgt = UsableURIFactory.
		getInstance("http://archive.org:83/home.html");
		UsableURI uri = UsableURIFactory.
		getInstance("http://archive.org:83/one/two/three.html");
		UsableURI uuri = UsableURIFactory.
		getInstance(uri, "/home.html");
        assertEquals(uuriTgt.toString(), uuri.toString(), "Not equal");
	}

	@Test
	public void testSchemelessRelative() throws URIException {
	    UsableURI base = UsableURIFactory.getInstance("http://www.itsnicethat.com/articles/laura-hobson");
	    UsableURI test1 = UsableURIFactory.getInstance(base, "//www.facebook.com/plugins/like.php");
	    assertEquals("http://www.facebook.com/plugins/like.php", test1.toString(), "schemaless relative 1");
	    // reported by Erin Staniland
	    UsableURI test2 = UsableURIFactory.getInstance(base, "//www.facebook.com/plugins/like.php?href=http://www.itsnicethat.com/articles/laura-hobson");
	    assertEquals("http://www.facebook.com/plugins/like.php?href=http://www.itsnicethat.com/articles/laura-hobson", test2.toString(),
	            "schemeless relative 2");
	}
	
	/**
	 * Test that an empty uuri does the right thing -- that we get back the
	 * base.
	 */
	@Test
	public final void testRelativeEmpty() throws URIException {
		UsableURI uuriTgt = UsableURIFactory.
		getInstance("http://archive.org:83/one/two/three.html");
		UsableURI uri = UsableURIFactory.
		getInstance("http://archive.org:83/one/two/three.html");
		UsableURI uuri = UsableURIFactory.
		getInstance(uri, "");
        assertEquals(uuriTgt.toString(), uuri.toString(), "Empty length don't work");
	}

	@Test
	public final void testAbsolute() throws URIException {
		UsableURI uuriTgt = UsableURIFactory.
		getInstance("http://archive.org:83/home.html");
		UsableURI uri = UsableURIFactory.
		getInstance("http://archive.org:83/one/two/three.html");
		UsableURI uuri = UsableURIFactory.
		getInstance(uri, "http://archive.org:83/home.html");
        assertEquals(uuriTgt.toString(), uuri.toString(), "Not equal");
	}
	
	/**
	 * Test for [ 962892 ] UURI accepting/creating unUsable URIs (bad hosts).
	 * @see <a href="https://sourceforge.net/tracker/?func=detail&atid=539099&aid=962892&group_id=73833">[ 962892 ] UURI accepting/creating unUsable URIs (bad hosts)</a>
	 */
	@Test
	public final void testHostWithLessThan() {
		checkExceptionOnIllegalDomainlabel("http://www.betamobile.com</A");
		checkExceptionOnIllegalDomainlabel(
		"http://C|/unzipped/426/spacer.gif");
		checkExceptionOnIllegalDomainlabel("http://www.lycos.co.uk\"/l/b/\"");
	}    
	
	/**
	 * Test for [ 1012520 ] UURI.length() &gt; 2k.
	 * @see <a href="http://sourceforge.net/tracker/index.php?func=detail&aid=1012520&group_id=73833&atid=539099">[ 1012520 ] UURI.length() &gt; 2k</a>
	 */
	@Test
	public final void test2kURI() throws URIException {
		final StringBuffer buffer = new StringBuffer("http://a.b");
		final String subPath = "/123456789";
		for (int i = 0; i < 207; i++) {
			buffer.append(subPath);
		}
		// String should be 2080 characters long.  Legal.
		UsableURIFactory.getInstance(buffer.toString());
		boolean gotException = false;
		// Add ten more characters and make size illegal.
		buffer.append(subPath);
		try {
			UsableURIFactory.getInstance(buffer.toString()); 
		} catch (URIException e) {
			gotException = true;
		}
		assertTrue(gotException, "No expected exception complaining about long URI");
	} 
	
	private void checkExceptionOnIllegalDomainlabel(String uuri) {
		boolean expectedException = false;
        try {
			UsableURIFactory.getInstance(uuri);
		} catch (URIException e) {
			// Expected exception.
			expectedException = true;
		}
		assertTrue(expectedException, "Didn't get expected exception: " + uuri);
	}
	
	/**
	 * Test for doing separate DNS lookup for same host
	 *
	 * @see <a href="https://sourceforge.net/tracker/?func=detail&aid=788277&group_id=73833&atid=539099">[ 788277 ] Doing separate DNS lookup for same host</a>
	 */
	@Test
	public final void testHostWithPeriod() throws URIException {
		UsableURI uuri1 = UsableURIFactory.
		getInstance("http://www.loc.gov./index.html");
		UsableURI uuri2 = UsableURIFactory.
		getInstance("http://www.loc.gov/index.html");
		assertEquals(uuri1.getHost(), uuri2.getHost(), "Failed equating hosts with dot");
	}
	
	/**
	 * Test for NPE in java.net.URI.encode
	 *
	 * @see <a href="https://sourceforge.net/tracker/?func=detail&aid=874220&group_id=73833&atid=539099">[ 874220 ] NPE in java.net.URI.encode</a>
	 * @throws URIException
	 */
	@Test
	public final void testHostEncodedChars() throws URIException {
		String s = "http://g.msn.co.kr/0nwkokr0/00/19??" +
		"PS=10274&NC=10009&CE=42&CP=949&HL=" +
		"&#65533;&#65533;&#65533;?&#65533;&#65533;";
		assertNotNull(UsableURIFactory.getInstance(s), "Encoded chars " + s);
	}
	
	/**
	 * Test for java.net.URI parses %20 but getHost null
	 *
	 * See <a href="https://sourceforge.net/tracker/?func=detail&aid=927940&group_id=73833&atid=539099">[ 927940 ] java.net.URI parses %20 but getHost null</a>
	 */
	@Test
	public final void testSpaceInHost() {
		boolean expectedException = false;
		try {
			UsableURIFactory.getInstance(
					"http://www.local-regions.odpm%20.gov.uk" +
			"/lpsa/challenge/pdf/propect.pdf");
		} catch (URIException e) {
			expectedException = true;
		}
		assertTrue(expectedException, "Did not fail with escaped space.");
		
		expectedException = false;
		try {
			UsableURIFactory.getInstance(
					"http://www.local-regions.odpm .gov.uk" +
			"/lpsa/challenge/pdf/propect.pdf");
		} catch (URIException e) {
			expectedException = true;
		}
		assertTrue(expectedException, "Did not fail with real space.");
	}
	
	/**
	 * Test for java.net.URI chokes on hosts_with_underscores.
	 *
	 * @see  <a href="https://sourceforge.net/tracker/?func=detail&aid=808270&group_id=73833&atid=539099">[ 808270 ] java.net.URI chokes on hosts_with_underscores</a>
     */
	@Test
	public final void testHostWithUnderscores() throws URIException {
		UsableURI uuri = UsableURIFactory.getInstance(
		"http://x_underscore_underscore.2u.com.tw/nonexistent_page.html");
		assertEquals("x_underscore_underscore.2u.com.tw",
				uuri.getHost(), "Failed get of host with underscore");
	}
	
	
	/**
	 * Two dots for igor.
	 */
	@Test
	public final void testTwoDots() {
		boolean expectedException = false;
		try {
			UsableURIFactory.getInstance(
			"http://x_underscore_underscore..2u.com/nonexistent_page.html");
		} catch (URIException e) {
			expectedException = true;
		}
		assertTrue(expectedException, "Two dots did not throw exception");
	}
	
	/**
	 * Test for java.net.URI#getHost fails when leading digit.
	 *
	 * @see <a href="https://sourceforge.net/tracker/?func=detail&aid=910120&group_id=73833&atid=539099">[ 910120 ] java.net.URI#getHost fails when leading digit.</a>
	 */
	@Test
	public final void testHostWithDigit() throws URIException {
		UsableURI uuri = UsableURIFactory.
		getInstance("http://0204chat.2u.com.tw/nonexistent_page.html");
		assertEquals("0204chat.2u.com.tw", uuri.getHost(), "Failed get of host with digit");
	}
	
	/**
	 * Test for Constraining java URI class.
	 *
	 * @see <a href="https://sourceforge.net/tracker/?func=detail&aid=949548&group_id=73833&atid=539099">[ 949548 ] Constraining java URI class</a>
	 */
	@Test
	public final void testPort() {
		checkBadPort("http://www.tyopaikat.com:a/robots.txt");
		checkBadPort("http://158.144.21.3:80808/robots.txt");
		checkBadPort("http://pdb.rutgers.edu:81.rutgers.edu/robots.txt");
		checkBadPort(
		    "https://webmail.gse.harvard.edu:9100robots.txt/robots.txt");
		checkBadPort(
		    "https://webmail.gse.harvard.edu:0/robots.txt/robots.txt");
	}
	
	/**
	 * Test bad port throws exception.
	 * @param uri URI with bad port to check.
	 */
	private void checkBadPort(String uri) {
		boolean exception = false;
		try {
			UsableURIFactory.getInstance(uri);
		}
		catch (URIException e) {
			exception = true;
		}
		assertTrue(exception, "Didn't throw exception: " + uri);
	}
	
	/**
	 * Preserve userinfo capitalization.
	 */
	@Test
	public final void testUserinfo() throws URIException {
        final String authority = "stack:StAcK@www.tyopaikat.com";
        final String uri = "http://" + authority + "/robots.txt";
		UsableURI uuri = UsableURIFactory.getInstance(uri);
		assertEquals(authority, uuri.getAuthority(), "Authority not equal");
        /*
        String tmp = uuri.toString();
        assertTrue("URI not equal", tmp.equals(uri));
        */
	}

	/**
	 * Test user info + port
	 */
	@Test
	public final void testUserinfoPlusPort() throws URIException {
		final String userInfo = "stack:StAcK";
        final String authority = "www.tyopaikat.com";
        final int port = 8080;
        final String uri = "http://" + userInfo + "@" + authority + ":" + port 
        	+ "/robots.txt";
		UsableURI uuri = UsableURIFactory.getInstance(uri);
		assertEquals(authority, uuri.getHost(),"Host not equal");
		assertEquals(userInfo,uuri.getUserinfo(),"Userinfo Not equal");
		assertEquals(port,uuri.getPort(),"Port not equal");
		assertEquals("stack:StAcK@www.tyopaikat.com:8080",uuri.getAuthority(),
				"Authority wrong");
		assertEquals("www.tyopaikat.com:8080",uuri.getAuthorityMinusUserinfo(),
				"AuthorityMinusUserinfo wrong");
	}
	
    public final void testRFC3986RelativeChange() throws URIException {
         UsableURI base = UsableURIFactory.getInstance("http://a/b/c/d;p?q");
         tryRelative(base, "?y", "http://a/b/c/d;p?y");
    }
	        
    /**
     * Tests from rfc3986
     *
     * <pre>
     *       "g:h"           =  "g:h"
     *       "g"             =  "http://a/b/c/g"
     *       "./g"           =  "http://a/b/c/g"
     *       "g/"            =  "http://a/b/c/g/"
     *       "/g"            =  "http://a/g"
     *       "//g"           =  "http://g"
     *       "?y"            =  "http://a/b/c/d;p?y"
     *       "g?y"           =  "http://a/b/c/g?y"
     *       "#s"            =  "http://a/b/c/d;p?q#s"
     *       "g#s"           =  "http://a/b/c/g#s"
     *       "g?y#s"         =  "http://a/b/c/g?y#s"
     *       ";x"            =  "http://a/b/c/;x"
     *       "g;x"           =  "http://a/b/c/g;x"
     *       "g;x?y#s"       =  "http://a/b/c/g;x?y#s"
     *       ""              =  "http://a/b/c/d;p?q"
     *       "."             =  "http://a/b/c/"
     *       "./"            =  "http://a/b/c/"
     *       ".."            =  "http://a/b/"
     *       "../"           =  "http://a/b/"
     *       "../g"          =  "http://a/b/g"
     *       "../.."         =  "http://a/"
     *       "../../"        =  "http://a/"
     *       "../../g"       =  "http://a/g"
     * </pre>
     */
	@Test
    public final void testRFC3986Relative() throws URIException {
        UsableURI base = UsableURIFactory.getInstance("http://a/b/c/d;p?q");
        tryRelative(base, "g:h",    "g:h");
        tryRelative(base, "g",      "http://a/b/c/g");
        tryRelative(base, "./g",    "http://a/b/c/g");
        tryRelative(base, "g/",     "http://a/b/c/g/");
        tryRelative(base, "/g",     "http://a/g");
        tryRelative(base, "//g",    "http://g");
        tryRelative(base, "?y",     "http://a/b/c/d;p?y");
        tryRelative(base, "g?y",    "http://a/b/c/g?y");
        tryRelative(base, "#s",     "http://a/b/c/d;p?q#s");
        tryRelative(base, "g#s",    "http://a/b/c/g#s");
        tryRelative(base, "g?y#s",  "http://a/b/c/g?y#s");
        tryRelative(base, ";x",     "http://a/b/c/;x");
        tryRelative(base, "g;x",    "http://a/b/c/g;x");
        tryRelative(base, "g;x?y#s","http://a/b/c/g;x?y#s");
        tryRelative(base, "",       "http://a/b/c/d;p?q");
        tryRelative(base, ".",      "http://a/b/c/");
        tryRelative(base, "./",     "http://a/b/c/");
        tryRelative(base, "..",     "http://a/b/");
        tryRelative(base, "../",    "http://a/b/");
        tryRelative(base, "../g",   "http://a/b/g");
        tryRelative(base, "../..",  "http://a/");
        tryRelative(base, "../../", "http://a/");
        tryRelative(base, "../../g","http://a/g");
    }
	    
    protected void tryRelative(UsableURI base, String relative, String expected) 
    throws URIException {
        UsableURI uuri = UsableURIFactory.getInstance(base, relative);
        assertEquals(UsableURIFactory.getInstance(expected), uuri,"Derelativized " + relative + " gave "
              + uuri + " not " + expected);
    }
	
	/**
	 * Tests from rfc2396 with amendments to accomodate differences
	 * intentionally added to make our URI handling like IEs.
	 *
	 * <pre>
	 *       g:h           =  g:h
	 *       g             =  http://a/b/c/g
	 *       ./g           =  http://a/b/c/g
	 *       g/            =  http://a/b/c/g/
	 *       /g            =  http://a/g
	 *       //g           =  http://g
	 *       ?y            =  http://a/b/c/?y
	 *       g?y           =  http://a/b/c/g?y
	 *       #s            =  (current document)#s
	 *       g#s           =  http://a/b/c/g#s
	 *       g?y#s         =  http://a/b/c/g?y#s
	 *       ;x            =  http://a/b/c/;x
	 *       g;x           =  http://a/b/c/g;x
	 *       g;x?y#s       =  http://a/b/c/g;x?y#s
	 *       .             =  http://a/b/c/
	 *       ./            =  http://a/b/c/
	 *       ..            =  http://a/b/
	 *       ../           =  http://a/b/
	 *       ../g          =  http://a/b/g
	 *       ../..         =  http://a/
	 *       ../../        =  http://a/
	 *       ../../g       =  http://a/g
	 * </pre>
	 */
	@Test
	public final void testRFC2396Relative() throws URIException {
		UsableURI base = UsableURIFactory.
		getInstance("http://a/b/c/d;p?q");
		TreeMap<String,String> m = new TreeMap<String,String>();
		m.put("..", "http://a/b/");
		m.put("../", "http://a/b/");
		m.put("../g", "http://a/b/g");
		m.put("../..", "http://a/");
		m.put("../../", "http://a/");
		m.put("../../g", "http://a/g");
		m.put("g#s", "http://a/b/c/g#s");
		m.put("g?y#s ", "http://a/b/c/g?y#s");
		m.put(";x", "http://a/b/c/;x");
		m.put("g;x", "http://a/b/c/g;x");
		m.put("g;x?y#s", "http://a/b/c/g;x?y#s");
		m.put(".", "http://a/b/c/");
		m.put("./", "http://a/b/c/");
		m.put("g", "http://a/b/c/g");
		m.put("./g", "http://a/b/c/g");
		m.put("g/", "http://a/b/c/g/");
		m.put("/g", "http://a/g");
		m.put("//g", "http://g");
	   // CHANGED BY RFC3986
                // m.put("?y", "http://a/b/c/?y");
		m.put("g?y", "http://a/b/c/g?y");
		// EXTRAS beyond the RFC set.
		// TODO: That these resolve to a path of /a/g might be wrong.  Perhaps
		// it should be '/g'?.
		m.put("/../../../../../../../../g", "http://a/g");
		m.put("../../../../../../../../g", "http://a/g");
		m.put("../G", "http://a/b/G");
        for (String key : m.keySet()) {
            String value = m.get(key);
            UsableURI uuri = UsableURIFactory.getInstance(base, key);
            assertEquals(uuri, UsableURIFactory.getInstance(value), "Unexpected " + key + " " + value + " " + uuri);
        }
	}
	
	/**
	 * A UURI should always be without a 'fragment' segment, which is
	 * unused and irrelevant for network fetches. 
	 *  
	 * See [ 970666 ] #anchor links not trimmed, and thus recrawled 
	 */
	@Test
	public final void testAnchors() throws URIException {
		UsableURI uuri = UsableURIFactory.
		getInstance("http://www.example.com/path?query#anchor");
		assertEquals("http://www.example.com/path?query", uuri.toString(),
				"Not equal");
	}
    

    /**
     * Ensure that URI strings beginning with a colon are treated
     * the same as browsers do (as relative, rather than as absolute
     * with zero-length scheme). 
     */
	@Test
    public void testStartsWithColon() throws URIException {
        UsableURI base = UsableURIFactory.getInstance("http://www.example.com/path/page");
        UsableURI uuri = UsableURIFactory.getInstance(base,":foo");
        assertEquals("http://www.example.com/path/:foo",
                uuri.getURI(),
                "derelativize starsWithColon");
    }
    
    /**
     * Ensure that relative URIs with colons in late positions 
     * aren't mistakenly interpreted as absolute URIs with long, 
     * illegal schemes. 
     */
	@Test
    public void testLateColon() throws URIException {
        UsableURI base = UsableURIFactory.getInstance("http://www.example.com/path/page");
        UsableURI uuri1 = UsableURIFactory.getInstance(base,"example.html;jsessionid=deadbeef:deadbeed?parameter=this:value");
        assertEquals("http://www.example.com/path/example.html;jsessionid=deadbeef:deadbeed?parameter=this:value",
                uuri1.getURI(),
                "derelativize lateColon");
        UsableURI uuri2 = UsableURIFactory.getInstance(base,"example.html?parameter=this:value");
        assertEquals("http://www.example.com/path/example.html?parameter=this:value",
                uuri2.getURI(),
                "derelativize lateColon");
    }
    
    /**
     * Ensure that stray trailing '%' characters do not prevent
     * UURI instances from being created, and are reasonably 
     * escaped when encountered. 
     */
	@Test
    public void testTrailingPercents() throws URIException {
        String plainPath = "http://www.example.com/path%";
        UsableURI plainPathUuri = UsableURIFactory.getInstance(plainPath);
        assertEquals(plainPath, plainPathUuri.getURI(), "plainPath getURI");
        assertEquals("http://www.example.com/path%",
                plainPathUuri.getEscapedURI(), // browsers don't escape '%'
                "plainPath getEscapedURI");
        
        String partiallyEscapedPath = "http://www.example.com/pa%20th%";
        UsableURI partiallyEscapedPathUuri = UsableURIFactory.getInstance(
                partiallyEscapedPath);
//        assertEquals("partiallyEscapedPath getURI", 
//                "http://www.example.com/pa th%", // TODO: is this desirable?
////              partiallyEscapedPath,
//                partiallyEscapedPathUuri.getURI());
        assertEquals("http://www.example.com/pa%20th%",
                partiallyEscapedPathUuri.getEscapedURI(),
                "partiallyEscapedPath getEscapedURI");
        
        String plainQueryString = "http://www.example.com/path?q=foo%";
        UsableURI plainQueryStringUuri = UsableURIFactory.getInstance(
                plainQueryString);
//        assertEquals("plainQueryString getURI", 
//                plainQueryString,
//                plainQueryStringUuri.getURI());
        assertEquals("http://www.example.com/path?q=foo%",
                plainQueryStringUuri.getEscapedURI(),
                "plainQueryString getEscapedURI");
        
        String partiallyEscapedQueryString = 
            "http://www.example.com/pa%20th?q=foo%";
        UsableURI partiallyEscapedQueryStringUuri = UsableURIFactory.getInstance(
                partiallyEscapedQueryString);
        assertEquals("http://www.example.com/pa th?q=foo%",
                partiallyEscapedQueryStringUuri.getURI(),
                "partiallyEscapedQueryString getURI");
        assertEquals("http://www.example.com/pa%20th?q=foo%",
                partiallyEscapedQueryStringUuri.getEscapedURI(),
                "partiallyEscapedQueryString getEscapedURI");
    }
    
    /**
     * Ensure that stray '%' characters do not prevent
     * UURI instances from being created, and are reasonably 
     * escaped when encountered. 
     */
	@Test
    public void testStrayPercents() throws URIException {
        String oneStray = "http://www.example.com/pa%th";
        UsableURI oneStrayUuri = UsableURIFactory.getInstance(oneStray);
        assertEquals(oneStray, oneStrayUuri.getURI(), "oneStray getURI");
        assertEquals("http://www.example.com/pa%th",
                oneStrayUuri.getEscapedURI(), // browsers don't escape '%'
                "oneStray getEscapedURI");
        
        String precededByValidEscape = "http://www.example.com/pa%20th%way";
        UsableURI precededByValidEscapeUuri = UsableURIFactory.getInstance(
                precededByValidEscape);
        assertEquals("http://www.example.com/pa th%way",
                precededByValidEscapeUuri.getURI(), // getURI interprets escapes
                "precededByValidEscape getURI");
        assertEquals("http://www.example.com/pa%20th%way",
                precededByValidEscapeUuri.getEscapedURI(),
                "precededByValidEscape getEscapedURI");
        
        String followedByValidEscape = "http://www.example.com/pa%th%20way";
        UsableURI followedByValidEscapeUuri = UsableURIFactory.getInstance(
                followedByValidEscape);
        assertEquals("http://www.example.com/pa%th way",
                followedByValidEscapeUuri.getURI(), // getURI interprets escapes
                "followedByValidEscape getURI");
        assertEquals("http://www.example.com/pa%th%20way",
                followedByValidEscapeUuri.getEscapedURI(),
                "followedByValidEscape getEscapedURI");
    }

	@Test
    public void testEscapingNotNecessary() throws URIException {
        String escapesUnnecessary = 
            "http://www.example.com/misc;reserved:chars@that&don't=need"
            +"+escaping$even,though!you(might)initially?think#so";
        // expect everything but the #fragment
        String expected = escapesUnnecessary.substring(0, escapesUnnecessary
                .length() - 3);
        assertEquals(expected,
                UsableURIFactory.getInstance(escapesUnnecessary).toString(),
                "escapes unnecessary");
    }

	@Test
    public void testIdn() throws URIException {
        // See http://www.josefsson.org/idn.php.
        // http://räksmörgås.josefßon.org/
        String idn1 = "http://r\u00e4ksm\u00f6rg\u00e5s.josef\u00dfon.org/";
        String puny1 = "http://xn--rksmrgs-5wao1o.josefsson.org/";
        assertEquals(puny1, UsableURIFactory
                .getInstance(idn1).toString(), "encoding of " + idn1);
        // http://www.pølse.dk/
        String idn2 = "http://www.p\u00f8lse.dk/";
        String puny2 = "http://www.xn--plse-gra.dk/";
        assertEquals(puny2, UsableURIFactory
                .getInstance(idn2).toString(), "encoding of " + idn2);
        // http://例子.測試
        String idn3 = "http://\u4F8B\u5B50.\u6E2C\u8A66";
        String puny3 = "http://xn--fsqu00a.xn--g6w251d/";
        assertEquals(puny3, UsableURIFactory
                .getInstance(idn3).toString(), "encoding of " + idn3);
    }

	@Test
    public void testNewLineInURL() throws URIException {
    	UsableURI uuri = UsableURIFactory.getInstance("http://www.ar\rchive\n." +
    	    "org/i\n\n\r\rndex.html");
    	assertEquals("http://www.archive.org/index.html", uuri.toString());
    }

	@Test
    public void testTabsInURL() throws URIException {
        UsableURI uuri = UsableURIFactory.getInstance("http://www.ar\tchive\t." +
            "org/i\t\r\n\tndex.html");
        assertEquals("http://www.archive.org/index.html", uuri.toString());
    }

	@Test
    public void testQueryEscaping() throws URIException {
        UsableURI uuri = UsableURIFactory.getInstance(
            "http://www.yahoo.com/foo?somechars!@$%^&*()_-+={[}]|\'\";:/?.>,<");
        assertEquals(
            // tests in FF1.5 indicate it only escapes " < > 
            "http://www.yahoo.com/foo?somechars!@$%^&*()_-+={[}]|\'%22;:/?.%3E,%3C",
            uuri.toString());
    }
    
    /**
     * Check that our 'normalization' does same as Nutch's
     * Below before-and-afters were taken from the nutch urlnormalizer-basic
     * TestBasicURLNormalizer class  (December 2006, Nutch 0.9-dev).
     */
	@Test
    public void testSameAsNutchURLFilterBasic() throws URIException {
        assertEquals("http://foo.com/",
                UsableURIFactory.getInstance(" http://foo.com/ ").toString());

        // check that protocol is lower cased
        assertEquals("http://foo.com/",
                UsableURIFactory.getInstance("HTTP://foo.com/").toString());
        
        // check that host is lower cased
        assertEquals("http://foo.com/index.html",
                UsableURIFactory.getInstance("http://Foo.Com/index.html").toString());
        assertEquals("http://foo.com/index.html",
                UsableURIFactory.getInstance("http://Foo.Com/index.html").toString());

        // check that port number is normalized
        assertEquals("http://foo.com/index.html",
                UsableURIFactory.getInstance("http://foo.com:80/index.html").toString());
        assertEquals("http://foo.com:81/",
                UsableURIFactory.getInstance("http://foo.com:81/").toString());

        // check that null path is normalized
        assertEquals("http://foo.com/",
                UsableURIFactory.getInstance("http://foo.com").toString());

        // check that references are removed
        assertEquals("http://foo.com/foo.html",
                UsableURIFactory.getInstance("http://foo.com/foo.html#ref").toString());

        //     // check that encoding is normalized
        //     normalizeTest("http://foo.com/%66oo.html", "http://foo.com/foo.html");

        // check that unnecessary "../" are removed
        assertEquals("http://foo.com/",
                UsableURIFactory.getInstance("http://foo.com/aa/../").toString());
        assertEquals("http://foo.com/aa/",
                UsableURIFactory.getInstance("http://foo.com/aa/bb/../").toString());

        /* We fail this one.  Here we produce: 'http://foo.com/'.
        assertEquals(UURIFactory.
                getInstance("http://foo.com/aa/..").toString(),
            "http://foo.com/aa/..");
         */
        
        assertEquals("http://foo.com/aa/foo.html",
                UsableURIFactory.getInstance("http://foo.com/aa/bb/cc/../../foo.html").toString());
        assertEquals("http://foo.com/aa/cc/ee/foo.html",
                UsableURIFactory.getInstance("http://foo.com/aa/bb/../cc/dd/../ee/foo.html").toString());
        assertEquals("http://foo.com/foo.html",
                UsableURIFactory.getInstance("http://foo.com/../foo.html").toString());
        assertEquals("http://foo.com/foo.html",
                UsableURIFactory.getInstance("http://foo.com/../../foo.html").toString());
        assertEquals("http://foo.com/foo.html",
                UsableURIFactory.getInstance("http://foo.com/../aa/../foo.html").toString());
        assertEquals("http://foo.com/foo.html",
                UsableURIFactory.getInstance("http://foo.com/aa/../../foo.html").toString());
        assertEquals("http://foo.com/",
                UsableURIFactory.getInstance("http://foo.com/aa/../bb/../foo.html/../../").toString());
        assertEquals("http://foo.com/aa/foo.html",
				UsableURIFactory.getInstance("http://foo.com/../aa/foo.html").toString());
        assertEquals("http://foo.com/foo.html",
                UsableURIFactory.getInstance("http://foo.com/../aa/../foo.html").toString());
        assertEquals("http://foo.com/a..a/foo.html",
                UsableURIFactory.getInstance("http://foo.com/a..a/foo.html").toString());
        assertEquals("http://foo.com/foo.html",
                UsableURIFactory.getInstance("http://foo.com/a..a/../foo.html").toString());
        assertEquals("http://foo.com/foo.html",
                UsableURIFactory.getInstance("http://foo.com/foo.foo/../foo.html").toString());
    }

	@Test
    public void testHttpSchemeColonSlash() {
    	boolean exception = false;
    	try {
    		UsableURIFactory.getInstance("https:/");
    	} catch (URIException e) {
    		exception = true;
    	}
    	assertTrue(exception, "Didn't throw exception when one expected");
    	exception = false;
    	try {
    		UsableURIFactory.getInstance("http://");
    	} catch (URIException e) {
    		exception = true;
    	}
    	assertTrue(exception, "Didn't throw exception when one expected");
    }

	@Test
    public void testNakedHttpsSchemeColon() {
        boolean exception = false;
        try {
            UsableURIFactory.getInstance("https:");
        } catch (URIException e) {
            exception = true;
        }
        assertTrue(exception, "Didn't throw exception when one expected");
        exception = false;
        try {
            UsableURI base = UsableURIFactory.getInstance("http://www.example.com");
            UsableURIFactory.getInstance(base, "https:");
        } catch (URIException e) {
            exception = true;
        }
        assertTrue(exception, "Didn't throw exception when one expected");
    }
    
    /**
     * Test motivated by [#HER-616] The UURI class may throw 
     * NullPointerException in getReferencedHost()
     */
	@Test
    public void testMissingHttpColon() throws URIException {
        String suspectUri = "http//www.test.foo";
        UsableURI base = UsableURIFactory.getInstance("http://www.example.com");
        boolean exceptionThrown = false; 
        try {
            UsableURI badUuri = UsableURIFactory.getInstance(suspectUri);
            badUuri.getReferencedHost(); // not reached
        } catch (URIException e) {
            // should get relative-uri-no-base exception
            exceptionThrown = true;
        } finally {
            assertTrue(exceptionThrown,"expected exception not thrown");
        }
        UsableURI goodUuri = UsableURIFactory.getInstance(base,suspectUri);
        goodUuri.getReferencedHost();
    }
    
    /**
     * A UURI's string representation should be same after a 
     * serialization roundtrip. 
     */
	@Test
    public final void testSerializationRoundtrip() throws URIException {
        UsableURI uuri = UsableURIFactory.
            getInstance("http://www.example.com/path?query#anchor");
        UsableURI uuri2 = (UsableURI) SerializationUtils.deserialize(
                SerializationUtils.serialize(uuri));
        assertEquals(uuri.toString(), uuri2.toString(), "Not equal");
        uuri = UsableURIFactory.
            getInstance("file://///boo_hoo/wwwroot/CMS/Images1/Banner.gif");
        uuri2 = (UsableURI) SerializationUtils.deserialize(
            SerializationUtils.serialize(uuri));
        assertEquals(uuri.toString(), uuri2.toString(), "Not equal");
    }
    
    /**
     * A UURI's string representation should be same after a 
     * toCustomString-getInstance roundtrip. 
     */
	@Test
    public final void testToCustomStringRoundtrip() throws URIException {
        UsableURI uuri = UsableURIFactory.
            getInstance("http://www.example.com/path?query#anchor");
        UsableURI uuri2 = UsableURIFactory.getInstance(uuri.toCustomString());
        assertEquals(uuri.toString(), uuri2.toString(), "Not equal");
        // TODO: fix
        // see [HER-1470] UURI String roundtrip (UURIFactory.getInstance(uuri.toString()) results in different URI for file: (and perhaps other) URIs
        // http://webteam.archive.org/jira/browse/HER-1470
//        uuri = UURIFactory.
//            getInstance("file://///boo_hoo/wwwroot/CMS/Images1/Banner.gif");
//        uuri2 = UURIFactory.getInstance(uuri.toCustomString());
//        assertEquals("Not equal", uuri.toString(), uuri2.toString());
    }
    
    /**
     * A UURI's string representation should be same after a 
     * toCustomString-getInstance roundtrip. 
     */
	@Test
    public final void testHostnamePortRoundtrip() throws URIException {
        UsableURI base = UsableURIFactory.
            getInstance("http://www.example.com/path?query#anchor");
        UsableURI test = UsableURIFactory.getInstance(base,"boom1.hostname.com:9999");
        System.out.println("scheme:"+test.getScheme());
        System.out.println(test.toCustomString());
        UsableURI roundtrip = UsableURIFactory.getInstance(test.toCustomString());
        assertEquals(test.toString(), roundtrip.toString(), "Not equal");
    }
    
    
    /**
     * Test bad port throws URIException not NumberFormatException
     */
	@Test
    public void testExtremePort() {
        try {
            UsableURI uuri = UsableURIFactory.getInstance("http://Tel.:010101010101");
            System.out.println(uuri); 
            fail("expected exception not thrown");
        } catch (URIException ue){
            // expected
        }
    }
    
    /**
     * Bars ('|') in path-segments aren't encoded by FF, preferred by some
     * RESTful-URI-ideas guides, so should work without error.
     */
	@Test
    public void testBarsInRelativePath() throws URIException {
        UsableURI base = UsableURIFactory.getInstance("http://www.example.com");
        String relative = "foo/bar|baz|yorple";
        base.resolve(relative);
        UsableURIFactory.getInstance(base,relative); 
    }

    /**
     * To match IE behavior, backslashes in path-info (really, anywhere before
     * query string) assumed to be slashes, to match IE behavior. In
     * query-string, they are escaped to %5C.
     */
	@Test
    public void testBackslashes() throws URIException {
        UsableURI uuri = UsableURIFactory.getInstance("http:\\/www.example.com\\a/b\\c/d?q\\r\\|s/t\\v");
        String expected = "http://www.example.com/a/b/c/d?q%5Cr%5C|s/t%5Cv";
        assertEquals(expected, uuri.toString());
    }
}
