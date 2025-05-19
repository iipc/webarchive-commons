package org.archive.url;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class URLRegexTransformerTest {

    @Test
	public void testStripPathSessionID() {
		// strip jsessionid
//		String sid1 = "jsessionid=0123456789abcdefghijklemopqrstuv";
//		String sid2 = "PHPSESSID=9682993c8daa2c5497996114facdc805";
//		String sid3 = "sid=9682993c8daa2c5497996114facdc805";
//		String sid4 = "ASPSESSIONIDAQBSDSRT=EOHBLBDDPFCLHKPGGKLILNAM";
//		String sid5 = "CFID=12412453&CFTOKEN=15501799";
//		String sid6 = "CFID=3304324&CFTOKEN=57491900&jsessionid=a63098d96360$B0$D9$A";
//
//		String fore = "http://foo.com/bar?bo=lo&";
//		String aft = "&gum=yum";
//		String want = "foo.com/bar?bo=lo&gum=yum";
//		String fore = "http://www.archive.org/index.html?";
//		String aft = "";
//		String want = "archive.org/index.html";
		


		// Check ASP_SESSIONID2:
		checkStripPathSessionID(
				"/(S(4hqa0555fwsecu455xqckv45))/mileg.aspx",
				"/mileg.aspx");

		// Check ASP_SESSIONID2 (again):
		checkStripPathSessionID(
				"/(4hqa0555fwsecu455xqckv45)/mileg.aspx",
				"/mileg.aspx");

		// Check ASP_SESSIONID3:
		checkStripPathSessionID(
				"/(a(4hqa0555fwsecu455xqckv45)S(4hqa0555fwsecu455xqckv45)f(4hqa0555fwsecu455xqckv45))/mileg.aspx?page=sessionschedules",
				"/mileg.aspx?page=sessionschedules");

		// '@' in path:
		checkStripPathSessionID(
				"/photos/36050182@N05/",
				"/photos/36050182@N05/");
	}

	private static void checkStripPathSessionID(String orig, String want) {
		String got = URLRegexTransformer.stripPathSessionID(orig);
        assertEquals(want, got, String.format("FAIL Orig(%s) Got(%s) Want(%s)", orig, got, want));
	}
	
//    private static final String  BASE = "http://www.archive.org/index.html";
    private static final String  BASE = "";

    @Test
    public void testStripQuerySessionID() throws URIException {
        String str32id = "0123456789abcdefghijklemopqrstuv";
        String url = BASE + "?jsessionid=" + str32id;
        String expectedResult = BASE + "?";
        String result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);
        
        // Test that we don't strip if not 32 chars only.
        url = BASE + "?jsessionid=" + str32id + '0';
        expectedResult = url;
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);
        
        // Test what happens when followed by another key/value pair.
        url = BASE + "?jsessionid=" + str32id + "&x=y";
        expectedResult = BASE + "?x=y";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed (" + result + ")");
        
        // Test what happens when followed by another key/value pair and
        // prefixed by a key/value pair.
        url = BASE + "?one=two&jsessionid=" + str32id + "&x=y";
        expectedResult = BASE + "?one=two&x=y";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);
        
        // Test what happens when prefixed by a key/value pair.
        url = BASE + "?one=two&jsessionid=" + str32id;
        expectedResult = BASE + "?one=two&";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);
        
        // Test aspsession.
        url = BASE + "?aspsessionidABCDEFGH=" + "ABCDEFGHIJKLMNOPQRSTUVWX"
            + "&x=y";
        expectedResult = BASE + "?x=y";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);
        
        // Test archive phpsession.
        url = BASE + "?phpsessid=" + str32id + "&x=y";
        expectedResult = BASE + "?x=y";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);
        
        // With prefix too.
        url = BASE + "?one=two&phpsessid=" + str32id + "&x=y";
        expectedResult = BASE + "?one=two&x=y";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);
        
        // With only prefix
        url = BASE + "?one=two&phpsessid=" + str32id;
        expectedResult = BASE + "?one=two&";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);
        
        // Test sid.
        url = BASE + "?" + "sid=9682993c8daa2c5497996114facdc805" + "&x=y";
        expectedResult = BASE + "?x=y";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);
        
        // Igor test.
        url = BASE + "?" + "sid=9682993c8daa2c5497996114facdc805" + "&" +
            "jsessionid=" + str32id;
        expectedResult = BASE + "?";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);

        
        url = "?CFID=1169580&CFTOKEN=48630702&dtstamp=22%2F08%2F2006%7C06%3A58%3A11";
        expectedResult = "?dtstamp=22%2F08%2F2006%7C06%3A58%3A11";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);

        
        url = "?CFID=12412453&CFTOKEN=15501799&dt=19_08_2006_22_39_28";
        expectedResult = "?dt=19_08_2006_22_39_28";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);

        
        url = "?CFID=14475712&CFTOKEN=2D89F5AF-3048-2957-DA4EE4B6B13661AB&r=468710288378&m=forgotten";
        expectedResult = "?r=468710288378&m=forgotten";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);

        
        url = "?CFID=16603925&CFTOKEN=2AE13EEE-3048-85B0-56CEDAAB0ACA44B8";
        expectedResult = "?";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);

        
        url = "?CFID=4308017&CFTOKEN=63914124&requestID=200608200458360%2E39414378";
        expectedResult = "?requestID=200608200458360%2E39414378";
        result = URLRegexTransformer.stripQuerySessionID(url);
        assertEquals(expectedResult, result, "Failed " + result);

        
    }

    @Test
    public void testSURT() {
    	assertEquals("org,archive,www",URLRegexTransformer.hostToSURT("www.archive.org"));
    }

}
