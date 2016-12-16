/*
 * Copyright 2016 The International Internet Preservation Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netpreserve.commons.uri;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Test UriBuilder for proper Uri creation across variety of important/tricky cases.
 */
public class UsableUriTest {

    /**
     * Test that parsing of escaped and unescaped uri gives expected result.
     */
    @Test
    public final void testEscaping() {
        final String escapedUriString = "http://archive.org/%20%20%5E%22'%60%5B%5D%7B%7D/a.gif";
        final String uriString = "http://archive.org/.././\u00A0 ^\"'`[]{}\\test/../a.gif\u00A0 ";

        Uri parsedEscapedUri = UriConfigs.HERITRIX.buildUri(escapedUriString);
        Uri parsedUri = UriConfigs.HERITRIX.buildUri(uriString);

        assertThat(parsedUri).hasToString(escapedUriString);

        assertThat(parsedUri).isEqualTo(parsedEscapedUri);
    }

    @Test
    public final void testUnderscoreMakesPortParseFail() {
        Uri uuri = UriConfigs.HERITRIX.buildUri("http://one-two_three:8080/index.html");
        assertThat(uuri.port).isEqualTo(8080);
    }

    @Test
    public final void testRelativeURIWithTwoSlashes() {
        Uri uuri = UriConfigs.HERITRIX.resolve("http://www.archive.org", "one//index.html");
        assertThat(uuri).hasToString("http://www.archive.org/one//index.html");
    }

    @Test
    public final void testSchemelessURI() {
        Uri base = UriConfigs.HERITRIX.buildUri("https://www.archive.org");
        Uri uuri = UriConfigs.HERITRIX.resolve(base, "//example.com/monkey?this:uri:has:colons");

        assertThat(uuri).hasToString("https://example.com/monkey?this:uri:has:colons");
    }

    @Test
    public final void testTrailingEncodedSpace() {
        Uri uuri = UriConfigs.HERITRIX.buildUri("http://www.nps-shoes.co.uk%20");
        assertThat(uuri).hasToString("http://www.nps-shoes.co.uk/");

        uuri = UriConfigs.HERITRIX.buildUri("http://www.nps-shoes.co.uk%20%20%20");
        assertThat(uuri).hasToString("http://www.nps-shoes.co.uk/");
    }

    @Test
    public final void testPort0080is80() {
        Uri uuri = UriConfigs.HERITRIX.buildUri("http://archive.org:0080");
        assertThat(uuri).hasToString("http://archive.org/");
    }

    @Test
    public final void testEscapeEncoding() {
        String uriString = "http://www.y1y1.com/albums/userpics/11111/normal_\u0645\u0646\u0649\u0649\u0649.jpg";
        String windowsUriString = "http://www.y1y1.com/albums/userpics/11111/normal_%E3%E4%EC%EC%EC.jpg";
        String utf8UriString = "http://www.y1y1.com/albums/userpics/11111/normal_%D9%85%D9%86%D9%89%D9%89%D9%89.jpg";

        Uri uri = UriConfigs.HERITRIX.buildUri(uriString);
        Uri windowsUri = UriConfigs.HERITRIX.charset(Charset.forName("windows-1256")).buildUri(uriString);
        Uri utf8Uri = UriConfigs.HERITRIX.buildUri(uriString);

        assertThat(uri.toDecodedString()).isEqualTo(windowsUri.toDecodedString());
        assertThat(uri.toDecodedString()).isEqualTo(utf8Uri.toDecodedString());
    }

    @Test
    public final void testTooLongAfterEscaping() {
        StringBuilder buffer = new StringBuilder("http://www.archive.org/a/");
        // Append bunch of spaces.  When escaped, they'll triple in size.
        for (int i = 0; i < 1024; i++) {
            buffer.append(" ");
        }
        buffer.append("/index.html");
        try {
            UriConfigs.HERITRIX.buildUri(buffer.toString());
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class)
                    .hasMessageStartingWith("Created (escaped) uuri >");
        }
    }

    @Test
    public final void testFtpUris() {
        final String ftp = "ftp";
        final String authority = "pfbuser:pfbuser@mprsrv.agri.gov.cn";
        final String path = "/clzreceive/";
        final String uri = ftp + "://" + authority + path;
        Uri uuri = UriConfigs.HERITRIX.buildUri(uri);
        assertThat(uuri.getScheme()).isEqualTo(ftp);
        assertThat(uuri.getAuthority()).isEqualTo(authority);
        assertThat(uuri.getPath()).isEqualTo(path);
    }

    @Test
    public final void testWhitespaceEscaped() {
        // Test that we get all whitespace even if the uri is
        // already escaped.
        String uri = "http://archive.org/index%25 .html";
        String tgtUri = "http://archive.org/index%25%20.html";
        Uri uuri = UriConfigs.HERITRIX.buildUri(uri);
        assertThat(uuri).hasToString(tgtUri);

        uri = "http://archive.org/index%25\u001D.html";
        tgtUri = "http://archive.org/index%25%1D.html";
        uuri = UriConfigs.HERITRIX.buildUri(uri);
        assertThat(uuri).hasToString(tgtUri);

        uri = "http://gemini.info.usaid.gov/directory/pbResults.cfm?&urlNameLast=Rumplestiltskin";
        tgtUri = "http://gemini.info.usaid.gov/directory/faxResults.cfm?"
                + "name=Ebenezer%20+Rumplestiltskin,&location=RRB%20%20%20%205.08-006";

        Uri base = UriConfigs.HERITRIX.buildUri(uri);
        uuri = UriConfigs.HERITRIX.resolve(base,
                "faxResults.cfm?name=Ebenezer +Rumplestiltskin,&location=RRB%20%20%20%205%2E08%2D006");
        assertThat(uuri).hasToString(tgtUri);

        uri = "http://archive.org/index%25\u3000.html";
        tgtUri = "http://archive.org/index%25%E3%80%80.html";
        uuri = UriConfigs.HERITRIX.buildUri(uri);
        assertThat(uuri).hasToString(tgtUri);
    }

    @Test
    public final void testFailedGetPath() {
        final String path = "/RealMedia/ads/click_lx.ads/%%PAGE%%/%%RAND%%/%%POS%%/%%CAMP%%/empty";
        // decoding in getPath will interpret %CA as 8-bit escaped char,
        // possibly incomplete
        final String uri = "http://ads.nandomedia.com" + path;
        Uri uuri = UriConfigs.HERITRIX.buildUri(uri);
        assertThat(uuri.getPath()).isEqualTo(path);
    }

    @Test
    public final void testDnsHost() {
        String uri = "dns://ads.nandomedia.com:81/one.html";
        Uri uuri = UriConfigs.HERITRIX.buildUri(uri);
        assertThat(uuri.getPath()).isEqualTo("ads.nandomedia.com");

        uri = "dns:ads.nandomedia.com";
        uuri = UriConfigs.HERITRIX.buildUri(uri);
        assertThat(uuri.getPath()).isEqualTo("ads.nandomedia.com");

        uri = "dns:ads.nandomedia.com?a=b";
        uuri = UriConfigs.HERITRIX.buildUri(uri);
        assertThat(uuri.getPath()).isEqualTo("ads.nandomedia.com");
    }

    @Test
    public final void testPercentEscaping() {
        final String uri = "http://archive.org/%a%%%%%.html";
        // tests indicate firefox (1.0.6) does not encode '%' at all
        final String tgtUri = "http://archive.org/%a%%%%%.html";
        Uri uuri = UriConfigs.HERITRIX.buildUri(uri);
        assertThat(uuri).hasToString(tgtUri);
    }

    @Test
    public final void testRelativeDblPathSlashes() {
        Uri uuri = UriConfigs.HERITRIX.resolve("http://www.archive.org/index.html", "JIGOU//KYC//INDEX.HTM");
        assertThat(uuri.getPath()).isEqualTo("/JIGOU//KYC//INDEX.HTM");
    }

    @Test
    public final void testRelativeWithScheme() {
        Uri base = UriConfigs.HERITRIX.buildUri("http://www.example.com/some/page");
        Uri uuri = UriConfigs.HERITRIX.resolve(base, "http:boo");
        assertThat(uuri).hasToString("http://www.example.com/some/boo");
    }

    @Test
    public final void testBadBaseResolve() {
        Uri base = UriConfigs.HERITRIX.buildUri("http://license.joins.com/board/"
                + "etc_board_list.asp?board_name=new_main&b_type=&nPage="
                + "2&category=G&lic_id=70&site=changeup&g_page=changeup&g_sPage="
                + "notice&gate=02");
        Uri uuri = UriConfigs.HERITRIX.resolve(base, "http://www.changeup.com/...</a");
        assertThat(uuri).hasToString("http://www.changeup.com/...</a");
    }

    @Test
    public final void testTilde() {
        noChangeExpected("http://license.joins.com/~igor");
    }

    @Test
    public final void testCurlies() {
        // Firefox allows curlies in the getQuery string portion of a URL only
        // (converts curlies if they are in the getPath portion ahead of the
        // getQuery string).
        Uri uuri = noChangeExpected("http://license.joins.com/igor?one={curly}");
        assertThat(uuri.getQuery()).isEqualTo("one={curly}");
        assertThat(UriConfigs.HERITRIX.buildUri("http://license.joins.com/igor{curly}.html"))
                .hasToString("http://license.joins.com/igor%7Bcurly%7D.html");
        try {
            UriConfigs.HERITRIX.buildUri("http://license.{curly}.com/igor.html");
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }
    }

    protected Uri noChangeExpected(final String original) {
        Uri uuri = UriConfigs.HERITRIX.buildUri(original);
        assertThat(uuri).hasToString(original);
        return uuri;
    }

    @Test
    public final void testTrimSpaceNBSP() {
        final String uri = "   http://archive.org/DIR WITH SPACES/"
                + UriBuilder.NBSP + "home.html    " + UriBuilder.NBSP + "   ";
        final String tgtUri = "http://archive.org/DIR%20WITH%20SPACES/%20home.html";
        Uri uuri = UriConfigs.HERITRIX.buildUri(uri);
        assertThat(uuri).hasToString(tgtUri);
    }

    /**
     * Test space plus encoding ([ 1010966 ] crawl.log has URIs with spaces in them). See
     * <a href="http://sourceforge.net/tracker/index.php?func=detail&aid=1010966&group_id=73833&atid=539099">[ 1010966 ]
     * crawl.log has URIs with spaces in them</a>.
     */
    @Test
    public final void testSpaceDoubleEncoding() {
        final String uri = "http://www.brook.edu/i.html? %20taxonomy=Politics";
        final String encodedUri = "http://www.brook.edu/i.html?%20%20taxonomy=Politics";
        Uri uuri = UriConfigs.HERITRIX.buildUri(uri);
        assertThat(uuri).hasToString(encodedUri);
    }

    /**
     * Test for doubly-encoded sequences. See
     * <a href="https://sourceforge.net/tracker/index.php?func=detail&aid=966219&group_id=73833&atid=539099">[ 966219 ]
     * UURI doubly-encodes %XX sequences</a>.
     */
    @Test
    public final void testDoubleEncoding() {
        final char ae = '\u00E6';
        final String uri = "http://archive.org/DIR WITH SPACES/home" + ae + ".html";
        final String encodedUri = "http://archive.org/DIR%20WITH%20SPACES/home%E6.html";

        Uri uuri = UriConfigs.HERITRIX.charset(StandardCharsets.ISO_8859_1).buildUri(uri);
        assertThat(uuri).hasToString(encodedUri);

        // Dbl-encodes.
        uuri = UriConfigs.HERITRIX.charset(StandardCharsets.ISO_8859_1).buildUri(uuri.toString());
        uuri = UriConfigs.HERITRIX.charset(StandardCharsets.ISO_8859_1).buildUri(uuri.toString());
        assertThat(uuri).hasToString(encodedUri);

        // Do default utf-8 test.
        uuri = UriConfigs.HERITRIX.buildUri(uri);
        final String encodedUtf8Uri = "http://archive.org/DIR%20WITH%20SPACES/home%C3%A6.html";
        assertThat(uuri).hasToString(encodedUtf8Uri);

        // Now dbl-encode.
        uuri = UriConfigs.HERITRIX.buildUri(uuri.toString());
        uuri = UriConfigs.HERITRIX.buildUri(uuri.toString());
        assertThat(uuri).hasToString(encodedUtf8Uri);
    }

    /**
     * Test for syntax errors stop page parsing.
     * <p>
     * @see
     * <a href="https://sourceforge.net/tracker/?func=detail&aid=788219&group_id=73833&atid=539099">[ 788219 ] URI
     * Syntax Errors stop page parsing</a>
     */
    @Test
    public final void testThreeSlashes() {
        Uri goodURI = UriConfigs.HERITRIX.buildUri("http://lcweb.loc.gov/rr/goodtwo.html");
        String uuri = "http:///lcweb.loc.gov/rr/goodtwo.html";
        Uri rewrittenURI = UriConfigs.HERITRIX.buildUri(uuri);
        assertThat(rewrittenURI).isEqualTo(goodURI);

        uuri = "http:////lcweb.loc.gov/rr/goodtwo.html";
        rewrittenURI = UriConfigs.HERITRIX.buildUri(uuri);
        assertThat(rewrittenURI).isEqualTo(goodURI);

        // Check https.
        goodURI = UriConfigs.HERITRIX.buildUri("https://lcweb.loc.gov/rr/goodtwo.html");
        uuri = "https:////lcweb.loc.gov/rr/goodtwo.html";
        rewrittenURI = UriConfigs.HERITRIX.buildUri(uuri);
        assertThat(rewrittenURI).isEqualTo(goodURI);
    }

    @Test
    public final void testNoScheme() {
        String uuri = "www.loc.gov/rr/european/egw/polishex.html";
        try {
            UriConfigs.HERITRIX.buildUri(uuri);
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }
    }

    @Test
    public final void testRelative() {
        Uri uuriTgt = UriConfigs.HERITRIX.buildUri("http://archive.org:83/home.html");
        Uri uri = UriConfigs.HERITRIX.buildUri("http://archive.org:83/one/two/three.html");
        Uri uuri = UriConfigs.HERITRIX.resolve(uri, "/home.html");

        assertThat(uuri).isEqualTo(uuriTgt);

        Uri resolvedAbsolutePath = UriConfigs.HERITRIX.resolve(uri, "/home.html");
        assertThat(resolvedAbsolutePath).isEqualTo(uuriTgt);

        Uri resolvedRelativePath = UriConfigs.HERITRIX.resolve("http://archive.org:83/one/two/three.html", "home.html");
        assertThat(resolvedRelativePath).hasToString("http://archive.org:83/one/two/home.html");
    }

    @Test
    public void testSchemelessRelative() {
        Uri base = UriConfigs.HERITRIX.buildUri("http://www.itsnicethat.com/articles/laura-hobson");

        Uri test1 = UriConfigs.HERITRIX.resolve(base, "//www.facebook.com/plugins/like.php");
        assertThat(test1).hasToString("http://www.facebook.com/plugins/like.php");

        // reported by Erin Staniland
        Uri test2 = UriConfigs.HERITRIX.resolve(base,
                "//www.facebook.com/plugins/like.php?href=http://www.itsnicethat.com/articles/laura-hobson");
        assertThat(test2).hasToString(
                "http://www.facebook.com/plugins/like.php?href=http://www.itsnicethat.com/articles/laura-hobson");
    }

    /**
     * Test that an empty uuri does the right thing -- that we get back the base.
     */
    @Test
    public final void testRelativeEmpty() throws UriException {
        Uri uuriTgt = UriConfigs.HERITRIX.buildUri("http://archive.org:83/one/two/three.html");
        Uri uri = UriConfigs.HERITRIX.buildUri("http://archive.org:83/one/two/three.html");
        Uri uuri = UriConfigs.HERITRIX.resolve(uri, "");

        assertThat(uuri).hasToString(uuriTgt.toString());
    }

    @Test
    public final void testAbsolute() {
        Uri uuriTgt = UriConfigs.HERITRIX.buildUri("http://archive.org:83/home.html");
        Uri uri = UriConfigs.HERITRIX.buildUri("http://archive.org:83/one/two/three.html");
        Uri uuri = UriConfigs.HERITRIX.resolve(uri, "http://archive.org:83/home.html");

        assertThat(uuri).hasToString(uuriTgt.toString());
    }

    /**
     * Test for [ 962892 ] UURI accepting/creating unUsable URIs (bad hosts).
     * <p>
     * @see
     * <a href="https://sourceforge.net/tracker/?func=detail&atid=539099&aid=962892&group_id=73833">[ 962892 ] UURI
     * accepting/creating unUsable URIs (bad hosts)</a>
     */
    @Test
    public final void testHostWithLessThan() {
        checkExceptionOnIllegalDomainlabel("http://www.betamobile.com</A");
        checkExceptionOnIllegalDomainlabel("http://C|/unzipped/426/spacer.gif");
        checkExceptionOnIllegalDomainlabel("http://www.lycos.co.uk\"/l/b/\"");
    }

    private void checkExceptionOnIllegalDomainlabel(String uuri) {
        try {
            UriConfigs.HERITRIX.buildUri(uuri);
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }
    }

    /**
     * Test for [ 1012520 ] UURI.length() &gt; 2k.
     * <p>
     * @throws UriException
     * @see
     * <a href="http://sourceforge.net/tracker/index.php?func=detail&aid=1012520&group_id=73833&atid=539099">[ 1012520 ]
     * UURI.length() &gt; 2k</a>
     */
    @Test
    public final void test2kURI() throws UriException {
        final StringBuffer buffer = new StringBuffer("http://a.b");
        final String subPath = "/123456789";
        for (int i = 0; i < 207; i++) {
            buffer.append(subPath);
        }
        // String should be 2080 characters long.  Legal.
        Uri uri = UriConfigs.HERITRIX.buildUri(buffer.toString());
        assertThat(uri.toString()).hasSize(2080);

        // Add ten more characters and make size illegal.
        buffer.append(subPath);

        try {
            UriConfigs.HERITRIX.buildUri(buffer.toString());
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }
    }

    /**
     * Test for doing separate DNS lookup for same getHost
     * <p>
     * @see
     * <a href="https://sourceforge.net/tracker/?func=detail&aid=788277&group_id=73833&atid=539099">[ 788277 ] Doing
     * separate DNS lookup for same getHost</a>
     */
    @Test
    public final void testHostWithPeriod() {
        Uri uuri1 = UriConfigs.HERITRIX.buildUri("http://www.loc.gov./index.html");
        Uri uuri2 = UriConfigs.HERITRIX.buildUri("http://www.loc.gov/index.html");

        assertThat(uuri1.getHost()).isEqualTo(uuri2.getHost());
    }

    /**
     * Test for NPE in java.net.URI.encode
     * <p>
     * @see
     * <a href="https://sourceforge.net/tracker/?func=detail&aid=874220&group_id=73833&atid=539099">[ 874220 ] NPE in
     * java.net.URI.encode</a>
     */
    @Test
    public final void testHostEncodedChars() {
        String s = "http://g.msn.co.kr/0nwkokr0/00/19??"
                + "PS=10274&NC=10009&CE=42&CP=949&HL="
                + "&#65533;&#65533;&#65533;?&#65533;&#65533;";

        Uri uri = UriConfigs.HERITRIX.buildUri(s);
        assertThat(uri).hasToString("http://g.msn.co.kr/0nwkokr0/00/19??PS=10274&NC=10009&CE=42&CP=949&HL=&");
    }

    /**
     * Test for java.net.URI parses %20 but getHost null
     * <p>
     * See
     * <a href="https://sourceforge.net/tracker/?func=detail&aid=927940&group_id=73833&atid=539099">[ 927940 ]
     * java.net.URI parses %20 but getHost null</a>
     */
    @Test
    public final void testSpaceInHost() {
        // Check that parsing fails with escaped space
        try {
            UriConfigs.HERITRIX.buildUri("http://www.local-regions.odpm%20.gov.uk/lpsa/challenge/pdf/propect.pdf");
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }

        // Check that parsing fails with real space
        try {
            UriConfigs.HERITRIX.buildUri("http://www.local-regions.odpm .gov.uk/lpsa/challenge/pdf/propect.pdf");
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }
    }

    /**
     * Test for java.net.URI chokes on hosts_with_underscores.
     * <p>
     * @see
     * <a href="https://sourceforge.net/tracker/?func=detail&aid=808270&group_id=73833&atid=539099">[ 808270 ]
     * java.net.URI chokes on hosts_with_underscores</a>
     */
    @Test
    public final void testHostWithUnderscores() {
        Uri uuri = UriConfigs.HERITRIX.buildUri("http://x_underscore_underscore.2u.com.tw/nonexistent_page.html");
        assertThat(uuri.getHost())
                .as("Failed get of host with underscore")
                .isEqualTo("x_underscore_underscore.2u.com.tw");
    }

    /**
     * Two dots for igor.
     */
    @Test
    public final void testTwoDots() {
        try {
            UriConfigs.HERITRIX.buildUri("http://x_underscore_underscore..2u.com/nonexistent_page.html");
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }
    }

    /**
     * Test for java.net.URI#getHost fails when leading digit.
     * <p>
     * @see
     * <a href="https://sourceforge.net/tracker/?func=detail&aid=910120&group_id=73833&atid=539099">[ 910120 ]
     * java.net.URI#getHost fails when leading digit.</a>
     */
    @Test
    public final void testHostWithDigit() {
        Uri uuri = UriConfigs.HERITRIX.buildUri("http://0204chat.2u.com.tw/nonexistent_page.html");

        assertThat(uuri.getHost()).as("Failed get of host with digit").isEqualTo("0204chat.2u.com.tw");
    }

    /**
     * Test for Constraining java URI class.
     * <p>
     * @see
     * <a href="https://sourceforge.net/tracker/?func=detail&aid=949548&group_id=73833&atid=539099">[ 949548 ]
     * Constraining java URI class</a>
     */
    @Test
    public final void testPort() {
        checkBadPort("http://www.tyopaikat.com:a/robots.txt");
        checkBadPort("http://158.144.21.3:80808/robots.txt");
        checkBadPort("http://pdb.rutgers.edu:81.rutgers.edu/robots.txt");
        checkBadPort("https://webmail.gse.harvard.edu:9100robots.txt/robots.txt");
        checkBadPort("https://webmail.gse.harvard.edu:0/robots.txt/robots.txt");
    }

    /**
     * Test bad getPort throws exception.
     * <p>
     * @param uri URI with bad getPort to check.
     */
    private void checkBadPort(String uri) {
        try {
            UriConfigs.HERITRIX.buildUri(uri);
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }
    }

    /**
     * Preserve getUserinfo capitalization.
     */
    @Test
    public final void testUserinfo() {
        final String authority = "stack:StAcK@www.tyopaikat.com";
        final String uri = "http://" + authority + "/robots.txt";
        Uri uuri = UriConfigs.HERITRIX.buildUri(uri);

        assertThat(uuri.getAuthority())
                .as("Authority did not preserve userinfo capitalization")
                .isEqualTo(authority);
    }

    /**
     * Test user info + getPort
     */
    @Test
    public final void testUserinfoPlusPort() {
        final String userInfo = "stack:StAcK";
        final String host = "www.tyopaikat.com";
        final int port = 8080;
        final String authority = userInfo + "@" + host + ":" + port;
        final String uri = "http://" + authority + "/robots.txt";
        Uri uuri = UriConfigs.HERITRIX.buildUri(uri);

        assertThat(uuri.getHost()).isEqualTo(host);
        assertThat(uuri.getUserinfo()).isEqualTo(userInfo);
        assertThat(uuri.getPort()).isEqualTo(port);
        assertThat(uuri.getAuthority()).isEqualTo(authority);
    }

    @Test
    public final void testRFC3986RelativeChange() {
        Uri base = UriConfigs.HERITRIX.buildUri("http://a/b/c/d;p?q");
        tryRelative(base, "?y", "http://a/b/c/d;p?y");
    }

    /**
     * Tests from rfc3986.
     * <p>
     * <
     * pre>
     * "g:h" = "g:h" "g" = "http://a/b/c/g" "./g" = "http://a/b/c/g" "g/" = "http://a/b/c/g/" "/g" = "http://a/g" "//g"
     * = "http://g" "?y" = "http://a/b/c/d;p?y" "g?y" = "http://a/b/c/g?y" "#s" = "http://a/b/c/d;p?q#s" "g#s" =
     * "http://a/b/c/g#s" "g?y#s" = "http://a/b/c/g?y#s" ";x" = "http://a/b/c/;x" "g;x" = "http://a/b/c/g;x" "g;x?y#s" =
     * "http://a/b/c/g;x?y#s" "" = "http://a/b/c/d;p?q" "." = "http://a/b/c/" "./" = "http://a/b/c/" ".." =
     * "http://a/b/" "../" = "http://a/b/" "../g" = "http://a/b/g" "../.." = "http://a/" "../../" = "http://a/"
     * "../../g" = "http://a/g"
     * </pre>
     */
    @Test
    public final void testRFC3986Relative() {
        Uri base = UriConfigs.HERITRIX.buildUri("http://a/b/c/d;p?q");
        tryRelative(base, "g:h", "g:h");
        tryRelative(base, "g", "http://a/b/c/g");
        tryRelative(base, "./g", "http://a/b/c/g");
        tryRelative(base, "g/", "http://a/b/c/g/");
        tryRelative(base, "/g", "http://a/g");
        tryRelative(base, "//g", "http://g");
        tryRelative(base, "?y", "http://a/b/c/d;p?y");
        tryRelative(base, "g?y", "http://a/b/c/g?y");
        tryRelative(base, "#s", "http://a/b/c/d;p?q#s");
        tryRelative(base, "g#s", "http://a/b/c/g#s");
        tryRelative(base, "g?y#s", "http://a/b/c/g?y#s");
        tryRelative(base, ";x", "http://a/b/c/;x");
        tryRelative(base, "g;x", "http://a/b/c/g;x");
        tryRelative(base, "g;x?y#s", "http://a/b/c/g;x?y#s");
        tryRelative(base, "", "http://a/b/c/d;p?q");
        tryRelative(base, ".", "http://a/b/c/");
        tryRelative(base, "./", "http://a/b/c/");
        tryRelative(base, "..", "http://a/b/");
        tryRelative(base, "../", "http://a/b/");
        tryRelative(base, "../g", "http://a/b/g");
        tryRelative(base, "../..", "http://a/");
        tryRelative(base, "../../", "http://a/");
        tryRelative(base, "../../g", "http://a/g");
    }

    /**
     * Tests from rfc2396 with amendments to accomodate differences intentionally added to make our URI handling like
     * IEs.
     * <p>
     * <
     * pre>
     * g:h = g:h g = http://a/b/c/g ./g = http://a/b/c/g g/ = http://a/b/c/g/ /g = http://a/g //g = http://g ?y =
     * http://a/b/c/?y g?y = http://a/b/c/g?y #s = (current document)#s g#s = http://a/b/c/g#s g?y#s =
     * http://a/b/c/g?y#s ;x = http://a/b/c/;x g;x = http://a/b/c/g;x g;x?y#s = http://a/b/c/g;x?y#s . = http://a/b/c/
     * ./ = http://a/b/c/ .. = http://a/b/ ../ = http://a/b/ ../g = http://a/b/g ../.. = http://a/ ../../ = http://a/
     * ../../g = http://a/g
     * </pre>
     */
    @Test
    public final void testRFC2396Relative() {
        Uri base = UriConfigs.HERITRIX.buildUri("http://a/b/c/d;p?q");

        tryRelative(base, "..", "http://a/b/");
        tryRelative(base, "../", "http://a/b/");
        tryRelative(base, "../g", "http://a/b/g");
        tryRelative(base, "../..", "http://a/");
        tryRelative(base, "../../", "http://a/");
        tryRelative(base, "../../g", "http://a/g");
        tryRelative(base, "g#s", "http://a/b/c/g#s");
        tryRelative(base, "g?y#s ", "http://a/b/c/g?y#s");
        tryRelative(base, ";x", "http://a/b/c/;x");
        tryRelative(base, "g;x", "http://a/b/c/g;x");
        tryRelative(base, "g;x?y#s", "http://a/b/c/g;x?y#s");
        tryRelative(base, ".", "http://a/b/c/");
        tryRelative(base, "./", "http://a/b/c/");
        tryRelative(base, "g", "http://a/b/c/g");
        tryRelative(base, "./g", "http://a/b/c/g");
        tryRelative(base, "g/", "http://a/b/c/g/");
        tryRelative(base, "/g", "http://a/g");
        tryRelative(base, "//g", "http://g");
        // CHANGED BY RFC3986
        // tryRelative(base, "?y", "http://a/b/c/?y");
        tryRelative(base, "g?y", "http://a/b/c/g?y");
        // EXTRAS beyond the RFC set.
        // TODO: That these resolve to a getPath of /a/g might be wrong. Perhaps it should be '/g'?.
        tryRelative(base, "/../../../../../../../../g", "http://a/g");
        tryRelative(base, "../../../../../../../../g", "http://a/g");
        tryRelative(base, "../G", "http://a/b/G");
    }

    protected void tryRelative(Uri base, String relative, String expected) {
        Uri uri = UriConfigs.HERITRIX.resolve(base, relative);

        Uri exp = UriConfigs.HERITRIX.buildUri(expected);
        assertThat(uri).as("Failed derelativizing %s", relative).isEqualTo(exp);
    }

    /**
     * A UURI should always be without a 'getFragment' segment, which is unused and irrelevant for network fetches.
     * <p>
     * See [ 970666 ] #anchor links not trimmed, and thus recrawled
     */
    @Test
    public final void testAnchors() {
        Uri uuri = UriConfigs.HERITRIX.buildUri("http://www.example.com/path?query#anchor");

        assertThat(uuri).hasToString("http://www.example.com/path?query");
    }

    /**
     * Ensure that URI strings beginning with a colon are treated the same as browsers do (as relative, rather than as
     * absolute with zero-length getScheme).
     */
    @Test
    public void testStartsWithColon() {
        Uri base = UriConfigs.HERITRIX.buildUri("http://www.example.com/path/page");
        Uri uuri = UriConfigs.HERITRIX.resolve("http://www.example.com/path/page", ":foo");

        assertThat(uuri).hasToString("http://www.example.com/path/:foo");
    }

    /**
     * Ensure that relative URIs with colons in late positions aren't mistakenly interpreted as absolute URIs with long,
     * illegal schemes.
     */
    @Test
    public void testLateColon() {
        Uri base = UriConfigs.HERITRIX.buildUri("http://www.example.com/path/page");

        Uri uuri1 = UriConfigs.HERITRIX.resolve(base, "example.html;jsessionid=deadbeef:deadbeed?parameter=this:value");
        assertThat(uuri1)
                .as("Failed derelativizing late colon")
                .hasToString(
                        "http://www.example.com/path/example.html;jsessionid=deadbeef:deadbeed?parameter=this:value");

        Uri uuri2 = UriConfigs.HERITRIX.resolve(base, "example.html?parameter=this:value");
        assertThat(uuri2.toString())
                .as("Failed derelativizing late colon")
                .isEqualTo("http://www.example.com/path/example.html?parameter=this:value");
    }

    /**
     * Ensure that stray trailing '%' characters do not prevent UURI instances from being created, and are reasonably
     * escaped when encountered.
     */
    @Test
    public void testTrailingPercents() {
        String plainPath = "http://www.example.com/path%";
        Uri plainPathUuri = UriConfigs.HERITRIX.buildUri(plainPath);
        assertThat(plainPathUuri).hasToString(plainPath);

        String partiallyEscapedPath = "http://www.example.com/pa%20th%";
        Uri partiallyEscapedPathUuri = UriConfigs.HERITRIX.buildUri(partiallyEscapedPath);
        assertThat(partiallyEscapedPathUuri).hasToString(partiallyEscapedPath);

        String plainQueryString = "http://www.example.com/path?q=foo%";
        Uri plainQueryStringUuri = UriConfigs.HERITRIX.buildUri(plainQueryString);
        assertThat(plainQueryStringUuri).hasToString(plainQueryString);

        String partiallyEscapedQueryString = "http://www.example.com/pa%20th?q=foo%";
        Uri partiallyEscapedQueryStringUuri = UriConfigs.HERITRIX.buildUri(partiallyEscapedQueryString);
        assertThat(partiallyEscapedQueryStringUuri).hasToString(partiallyEscapedQueryString);
    }

    /**
     * Ensure that stray '%' characters do not prevent UURI instances from being created, and are reasonably escaped
     * when encountered.
     */
    @Test
    public void testStrayPercents() {
        String oneStray = "http://www.example.com/pa%th";
        Uri oneStrayUuri = UriConfigs.HERITRIX.buildUri(oneStray);
        assertThat(oneStrayUuri)
                .as("Failed parsing one stray %")
                .hasToString(oneStray);

        String precededByValidEscape = "http://www.example.com/pa%20th%way";
        Uri precededByValidEscapeUuri = UriConfigs.HERITRIX.buildUri(precededByValidEscape);
        assertThat(precededByValidEscapeUuri)
                .as("Failed parsing '%' preceeded by valid escape")
                .hasToString(precededByValidEscape);
        assertThat(precededByValidEscapeUuri.getDecodedPath()).isEqualTo("/pa th%way");

        String followedByValidEscape = "http://www.example.com/pa%th%20way";
        Uri followedByValidEscapeUuri = UriConfigs.HERITRIX.buildUri(followedByValidEscape);
        assertThat(followedByValidEscapeUuri)
                .as("Failed parsing '%' followed by valid escape")
                .hasToString(followedByValidEscape);
        assertThat(followedByValidEscapeUuri.getDecodedPath()).isEqualTo("/pa%th way");

    }

    @Test
    public void testEscapingNotNecessary() {
        String escapesUnnecessary
                = "http://www.example.com/misc;reserved:chars@that&don't=need"
                + "+escaping$even,though!you(might)initially?think#so";

        // expect everything but the #getFragment
        String expected = escapesUnnecessary.substring(0, escapesUnnecessary.length() - 3);
        assertThat(UriConfigs.HERITRIX.buildUri(escapesUnnecessary)).hasToString(expected);
    }

    @Test
    public void testIdn() {
        // See http://www.josefsson.org/idn.php.
        // http://räksmörgås.josefßon.org/
        String idn1 = "http://r\u00e4ksm\u00f6rg\u00e5s.josef\u00dfon.org/";
        String puny1 = "http://xn--rksmrgs-5wao1o.josefsson.org/";
        Uri uri1 = UriConfigs.HERITRIX.buildUri(idn1);
        assertThat(uri1)
                .as("Failed encoding %s", idn1)
                .hasToString(puny1);
        assertThat(uri1.getDecodedHost()).isEqualTo("r\u00e4ksm\u00f6rg\u00e5s.josefsson.org");

        // http://www.pølse.dk/
        String idn2 = "http://www.p\u00f8lse.dk/";
        String puny2 = "http://www.xn--plse-gra.dk/";
        Uri uri2 = UriConfigs.HERITRIX.buildUri(idn2);
        assertThat(uri2)
                .as("Failed encoding %s", idn2)
                .hasToString(puny2);
        assertThat(uri2.getDecodedHost()).isEqualTo("www.p\u00f8lse.dk");

        // http://例子.測試
        String idn3 = "http://\u4F8B\u5B50.\u6E2C\u8A66";
        String puny3 = "http://xn--fsqu00a.xn--g6w251d/";
        Uri uri3 = UriConfigs.HERITRIX.buildUri(idn3);
        assertThat(uri3)
                .as("Failed encoding %s", idn3)
                .hasToString(puny3);
        assertThat(uri3.getDecodedHost()).isEqualTo("\u4F8B\u5B50.\u6E2C\u8A66");
    }

    @Test
    public void testNewLineInURL() {
        Uri uuri = UriConfigs.HERITRIX.buildUri("http://www.ar\rchive\n.org/i\n\n\r\rndex.html");

        assertThat(uuri).hasToString("http://www.archive.org/index.html");
    }

    @Test
    public void testTabsInURL() {
        Uri uuri = UriConfigs.HERITRIX.buildUri("http://www.ar\tchive\t.org/i\t\r\n\tndex.html");

        assertThat(uuri).hasToString("http://www.archive.org/index.html");
    }

    @Test
    public void testQueryEscaping() {
        Uri uuri = UriConfigs.HERITRIX
                .buildUri("http://www.yahoo.com/foo?somechars!@$%^&*()_-+={[}]|\'\";:/?.>,<");

        // tests in FF1.5 indicate it only escapes " < >
        assertThat(uuri).hasToString("http://www.yahoo.com/foo?somechars!@$%^&*()_-+={[}]|\'%22;:/?.%3E,%3C");
    }

    /**
     * Check that our 'normalization' does same as Nutch's Below before-and-afters were taken from the nutch
     * urlnormalizer-basic TestBasicURLNormalizer class (December 2006, Nutch 0.9-dev).
     * <p>
     * @throws UriException
     */
    @Test
    public void testSameAsNutchURLFilterBasic() {
        Uri uuri = UriConfigs.HERITRIX.buildUri(" http://foo.com/ ");
        assertThat(uuri).hasToString("http://foo.com/");

        // check that protocol is lower cased
        uuri = UriConfigs.HERITRIX.buildUri("HTTP://foo.com/");
        assertThat(uuri).hasToString("http://foo.com/");

        // check that getHost is lower cased
        uuri = UriConfigs.HERITRIX.buildUri("http://Foo.Com/index.html");
        assertThat(uuri).hasToString("http://foo.com/index.html");

        // check that getPort number is normalized
        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com:80/index.html");
        assertThat(uuri).hasToString("http://foo.com/index.html");
        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com:81/");
        assertThat(uuri).hasToString("http://foo.com:81/");

        // check that null getPath is normalized
        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com");
        assertThat(uuri).hasToString("http://foo.com/");

        // check that references are removed
        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/foo.html#ref");
        assertThat(uuri).hasToString("http://foo.com/foo.html");

        // check that encoding is normalized
        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/%66oo.html");
        assertThat(uuri).hasToString("http://foo.com/foo.html");

        // check that unnecessary "../" are removed
        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/aa/../");
        assertThat(uuri).hasToString("http://foo.com/");

        // Default normalization is to remove trailing slash. To get this one working we must change config.
        uuri = UriConfigs.HERITRIX.schemeBasedNormalization(false).buildUri("http://foo.com/aa/bb/../");
        assertThat(uuri).hasToString("http://foo.com/aa/");
        // Check for default behavior of above
        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/aa/bb/../");
        assertThat(uuri).hasToString("http://foo.com/aa");

        // We fail this one.  Here we produce: 'http://foo.com/'.
        // uuri = Configurations.HERITRIX_USABLE_URI.buildUri("http://foo.com/aa/..");
        //assertThat(uuri).hasToString("http://foo.com/aa/..");
        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/aa/bb/cc/../../foo.html");
        assertThat(uuri).hasToString("http://foo.com/aa/foo.html");

        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/aa/bb/../cc/dd/../ee/foo.html");
        assertThat(uuri).hasToString("http://foo.com/aa/cc/ee/foo.html");

        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/../foo.html");
        assertThat(uuri).hasToString("http://foo.com/foo.html");

        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/../../foo.html");
        assertThat(uuri).hasToString("http://foo.com/foo.html");

        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/../aa/../foo.html");
        assertThat(uuri).hasToString("http://foo.com/foo.html");

        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/aa/../../foo.html");
        assertThat(uuri).hasToString("http://foo.com/foo.html");

        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/aa/../bb/../foo.html/../../");
        assertThat(uuri).hasToString("http://foo.com/");

        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/../aa/foo.html");
        assertThat(uuri).hasToString("http://foo.com/aa/foo.html");

        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/../aa/../foo.html");
        assertThat(uuri).hasToString("http://foo.com/foo.html");

        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/a..a/foo.html");
        assertThat(uuri).hasToString("http://foo.com/a..a/foo.html");

        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/a..a/../foo.html");
        assertThat(uuri).hasToString("http://foo.com/foo.html");

        uuri = UriConfigs.HERITRIX.buildUri("http://foo.com/foo.foo/../foo.html");
        assertThat(uuri).hasToString("http://foo.com/foo.html");
    }

    @Test
    public void testHttpSchemeColonSlash() {
        try {
            UriConfigs.HERITRIX.buildUri("https:/");
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }

        try {
            UriConfigs.HERITRIX.buildUri("http://");
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }
    }

    @Test
    public void testNakedHttpsSchemeColon() {
        try {
            UriConfigs.HERITRIX.buildUri("https:");
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }

        try {
            Uri base = UriConfigs.HERITRIX.buildUri("http://www.example.com");
            UriConfigs.HERITRIX.resolve(base, "https:");
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }
    }

    /**
     * Test motivated by [#HER-616] The UURI class may throw NullPointerException in getReferencedHost()
     */
    @Test
    public void testMissingHttpColon() {
        String suspectUri = "http//www.test.foo";
        try {
            Uri badUuri = UriConfigs.HERITRIX.buildUri(suspectUri);
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            // should get uri is not absolute exception
            assertThat(e).isInstanceOf(UriException.class);
        }

        Uri goodUuri = UriConfigs.HERITRIX.resolve("http://www.example.com", suspectUri);
        assertThat(goodUuri.getHost()).isEqualTo("www.example.com");
        assertThat(goodUuri.getPath()).isEqualTo("/http//www.test.foo");
    }

    /**
     * A UURI's string representation should be same after a serialization roundtrip.
     */
    public final void testSerializationRoundtrip() throws IOException, ClassNotFoundException {
        Uri uuri = UriConfigs.HERITRIX.buildUri("http://www.example.com/path?query#anchor");
        Object uuri2 = serializeDeserialize(uuri);
        assertThat(uuri2).isInstanceOf(Uri.class);
        assertThat(uuri2).isEqualTo(uuri);
        assertThat(((Uri) uuri2)).hasToString(uuri.toString());

        uuri = UriConfigs.HERITRIX.buildUri("file://///boo_hoo/wwwroot/CMS/Images1/Banner.gif");
        uuri2 = serializeDeserialize(uuri);
        assertThat(uuri2).isInstanceOf(Uri.class);
        assertThat(uuri2).isEqualTo(uuri);
        assertThat(((Uri) uuri2)).hasToString(uuri.toString());
    }

    private Object serializeDeserialize(Object obj) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }

    /**
     * A UURI's string representation should be same after a toCustomString-getInstance roundtrip.
     * <p>
     * @throws UriException
     */
    @Test
    public final void testToCustomStringRoundtrip() throws UriException {
        Uri uuri = UriConfigs.HERITRIX.buildUri("http://www.example.com/path?query#anchor");
        Uri uuri2 = UriConfigs.HERITRIX.buildUri(uuri.toString());
        assertThat(uuri2).hasToString(uuri.toString());

        // see [HER-1470] UURI String roundtrip (UURIFactory.getInstance(uuri.toString()) results
        // in different URI for file: (and perhaps other) URIs
        // http://webteam.archive.org/jira/browse/HER-1470
        uuri = UriConfigs.HERITRIX.buildUri("file://///boo_hoo/wwwroot/CMS/Images1/Banner.gif");
        uuri2 = UriConfigs.HERITRIX.buildUri(uuri.toString());
        assertThat(uuri2).hasToString(uuri.toString());
    }

    /**
     * Test bad getPort throws UriException not NumberFormatException.
     */
    @Test
    public void testExtremePort() {
        try {
            Uri uuri = UriConfigs.HERITRIX.buildUri("http://Tel.:010101010101");
            shouldHaveThrown(UriException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UriException.class);
        }
    }

    /**
     * Bars ('|') in getPath-segments aren't encoded by FF, preferred by some RESTful-URI-ideas guides, so should work
     * without error.
     */
    @Test
    public void testBarsInRelativePath() {
        Uri uuri = UriConfigs.HERITRIX.resolve("http://www.example.com", "foo/bar|baz|yorple");

        assertThat(uuri).hasToString("http://www.example.com/foo/bar|baz|yorple");
    }

    /**
     * To match IE behavior, backslashes in getPath-info (really, anywhere before getQuery string) assumed to be
     * slashes, to match IE behavior. In getQuery-string, they are escaped to %5C.
     */
    @Test
    public void testBackslashes() {
        Uri uuri = UriConfigs.HERITRIX.buildUri("http:\\/www.example.com\\a/b\\c/d?q\\r\\|s/t\\v");
        String expected = "http://www.example.com/a/b/c/d?q%5Cr%5C|s/t%5Cv";
        assertThat(uuri).hasToString(expected);
    }

    @Test
    public void testIpAddress() {
        Uri ipv4 = UriConfigs.HERITRIX.buildUri("http://127.0.0.1/a/b/c");
        String expectedIpv4 = "http://127.0.0.1/a/b/c";
        assertThat(ipv4).hasToString(expectedIpv4);
        assertThat(ipv4.isIPv4address()).isTrue();

        Uri ipv6 = UriConfigs.HERITRIX.buildUri("http://[::1]/a/b/c");
        String expectedIpv6 = "http://[::1]/a/b/c";
        assertThat(ipv6).hasToString(expectedIpv6);
        assertThat(ipv6.isIPv6reference()).isTrue();

        ipv6 = UriConfigs.HERITRIX.buildUri("http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]:81/index.html");
        expectedIpv6 = "http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]:81/index.html";
        assertThat(ipv6).hasToString(expectedIpv6);
        assertThat(ipv6.isIPv6reference()).isTrue();
    }

    @Test
    public void testMetadata() {
        UriBuilderConfig config = UriConfigs.HERITRIX.requireAbsoluteUri(false);

        Uri uri;

        // Absolute Opaque Uris
        uri = UriConfigs.HERITRIX.buildUri("mailto:java-net@java.sun.com");
        assertThat(uri.isAbsolute()).isTrue();
        assertThat(uri.isAbsolutePath()).isFalse();

        uri = UriConfigs.HERITRIX.buildUri("news:comp.lang.java#fragment");
        assertThat(uri.isAbsolute()).isTrue();
        assertThat(uri.isAbsolutePath()).isFalse();

        uri = UriConfigs.HERITRIX.buildUri("urn:isbn:096139210x");
        assertThat(uri.isAbsolute()).isTrue();
        assertThat(uri.isAbsolutePath()).isFalse();

        // Hierarchical Uris
        uri = UriConfigs.HERITRIX.buildUri("http://java.sun.com/j2se/1.3/");
        assertThat(uri.isAbsolute()).isTrue();
        assertThat(uri.isAbsolutePath()).isTrue();

        uri = config.buildUri("docs/guide/collections/designfaq.html#28");
        assertThat(uri.isAbsolute()).isFalse();
        assertThat(uri.isAbsolutePath()).isFalse();

        uri = config.buildUri("../../../demo/jfc/SwingSet2/src/SwingSet2.java");
        assertThat(uri.isAbsolute()).isFalse();
        assertThat(uri.isAbsolutePath()).isFalse();

        uri = UriConfigs.HERITRIX.buildUri("file:///~/calendar");
        assertThat(uri.isAbsolute()).isTrue();
        assertThat(uri.isAbsolutePath()).isTrue();

        uri = config.buildUri(":foo");
        assertThat(uri.isAbsolute()).isFalse();
        assertThat(uri.isAbsolutePath()).isFalse();
    }

}
