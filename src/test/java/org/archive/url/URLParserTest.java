package org.archive.url;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import junit.framework.TestCase;

import org.apache.commons.httpclient.URIException;

import com.google.common.net.InetAddresses;

public class URLParserTest extends TestCase {
	public void testGuava() throws URIException, UnsupportedEncodingException {
		Long l = Long.parseLong("3279880203");
		int i2 = l.intValue();
		//		int i = Integer.decode("3279880203");
		System.err.format("FromNum(%s)\n", InetAddresses.fromInteger(i2).getHostAddress());
	}

	public void testAddDefaultSchemeIfNeeded() {
		assertEquals(null,URLParser.addDefaultSchemeIfNeeded(null));
		assertEquals("http://",URLParser.addDefaultSchemeIfNeeded(""));
		assertEquals("http://www.fool.com",URLParser.addDefaultSchemeIfNeeded("http://www.fool.com"));
		assertEquals("http://www.fool.com/",URLParser.addDefaultSchemeIfNeeded("http://www.fool.com/"));
		assertEquals("http://www.fool.com",URLParser.addDefaultSchemeIfNeeded("www.fool.com"));
		assertEquals("http://www.fool.com/",URLParser.addDefaultSchemeIfNeeded("www.fool.com/"));
	}


	public void testParse() throws UnsupportedEncodingException, URISyntaxException {
		System.out.format("O(%s) E(%s)\n","%66",URLDecoder.decode("%66","UTF-8"));
		checkParse("http://www.archive.org/index.html#foo", 
				null, "http", null, null, "www.archive.org", -1, "/index.html", null, "foo",
				"http://www.archive.org/index.html#foo", "/index.html");
		checkParse("http://www.archive.org/", 
				null, "http", null, null, "www.archive.org", -1, "/", null, null,
				"http://www.archive.org/", "/");
		checkParse("http://www.archive.org", 
				null, "http", null, null, "www.archive.org", -1, "", null, null,
				"http://www.archive.org", "");
		checkParse("http://www.archive.org?",
				null, "http", null, null, "www.archive.org", -1, "", "", null,
				"http://www.archive.org/?", "/?");
		checkParse("http://www.archive.org#",
				null, "http", null, null, "www.archive.org", -1, "", null, "",
				"http://www.archive.org/#", "/");
		checkParse("http://www.archive.org#foo#bar#baz",
				null, "http", null, null, "www.archive.org", -1, "", null, "foo#bar#baz",
				"http://www.archive.org/#foo#bar#baz", "/");
		checkParse("http://www.archive.org:8080/index.html?query#foo",
				null, "http", null, null, "www.archive.org", 8080, "/index.html", "query", "foo",
				"http://www.archive.org:8080/index.html?query#foo", "/index.html?query");
		checkParse("http://www.archive.org:8080/index.html?#foo",
				null, "http", null, null, "www.archive.org", 8080, "/index.html", "", "foo",
				"http://www.archive.org:8080/index.html?#foo", "/index.html?");
		checkParse("http://www.archive.org:8080?#foo",
				null, "http", null, null, "www.archive.org", 8080, "", "", "foo",
				"http://www.archive.org:8080/?#foo", "/?");
		checkParse("http://bücher.ch:8080?#foo",
				null, "http", null, null, "bücher.ch", 8080, "", "", "foo",
				"http://bücher.ch:8080/?#foo", "/?");

		checkParse("dns:bücher.ch",
				"dns:bücher.ch", null, null, null, null, -1, null, null, null,
				"dns:bücher.ch", "");

		checkParse("http://www.archive.org/?foo?what", 
				null, "http", null, null, "www.archive.org", -1, "/", "foo?what", null,
				"http://www.archive.org/?foo?what", "/?foo?what");
		checkParse("http://www.archive.org/?foo?what#spuz?baz?", 
				null, "http", null, null, "www.archive.org", -1, "/", "foo?what", "spuz?baz?",
				"http://www.archive.org/?foo?what#spuz?baz?", "/?foo?what");
		checkParse("http://www.archive.org/?foo?what#spuz?baz?#fooo", 
				null, "http", null, null, "www.archive.org", -1, "/", "foo?what", "spuz?baz?#fooo",
				"http://www.archive.org/?foo?what#spuz?baz?#fooo", "/?foo?what");
		checkParse("http://jdoe@www.archive.org:8080/index.html?query#foo",
				null, "http", "jdoe", null, "www.archive.org", 8080, "/index.html", "query", "foo",
				"http://jdoe@www.archive.org:8080/index.html?query#foo", "/index.html?query");
		checkParse("http://jdoe:****@www.archive.org:8080/index.html?query#foo",
				null, "http", "jdoe", "****", "www.archive.org", 8080, "/index.html", "query", "foo",
				"http://jdoe:****@www.archive.org:8080/index.html?query#foo", "/index.html?query");
		checkParse("http://:****@www.archive.org:8080/index.html?query#foo",
				null, "http", "", "****", "www.archive.org", 8080, "/index.html", "query", "foo",
				"http://:****@www.archive.org:8080/index.html?query#foo", "/index.html?query");
		checkParse(" \n http://:****@www.archive.org:8080/inde\rx.html?query#foo \r\n \t ",
				null, "http", "", "****", "www.archive.org", 8080, "/index.html", "query", "foo",
				"http://:****@www.archive.org:8080/index.html?query#foo", "/index.html?query");
		checkParse("https://[2600:1f18:200d:fb00:2b74:867c:ab0c:150a]/robots.txt", null, "https", null, null,
				"[2600:1f18:200d:fb00:2b74:867c:ab0c:150a]", -1, "/robots.txt", null, null,
				"https://[2600:1f18:200d:fb00:2b74:867c:ab0c:150a]/robots.txt", "/robots.txt");
	}

	private void checkParse(String s, String opaque, String scheme, String authUser,
			String authPass, String host, int port, String path,
			String query, String fragment, String urlString, String pathQuery) throws URISyntaxException {
		HandyURL h = URLParser.parse(s);
		System.out.format("Input:(%s)\nHandyURL\t%s\n",s,h.toDebugString());
		assertEquals(scheme, h.getScheme());
		assertEquals(authUser, h.getAuthUser());
		assertEquals(authPass, h.getAuthPass());
		assertEquals(host, h.getHost());
		assertEquals(port, h.getPort());
		assertEquals(path, h.getPath());
		assertEquals(query, h.getQuery());
		assertEquals(fragment, h.getHash());

		assertEquals(urlString, h.getURLString());
		assertEquals(pathQuery, h.getPathQuery());
	}

}
