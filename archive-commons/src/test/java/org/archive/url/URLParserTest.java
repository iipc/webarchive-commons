package org.archive.url;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.httpclient.URIException;

import com.google.common.net.InetAddresses;

import junit.framework.TestCase;

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

	
	public void testParse() throws URIException, UnsupportedEncodingException {
		System.out.format("O(%s) E(%s)\n","%66",URLDecoder.decode("%66","UTF-8"));
		dumpParse("http://www.archive.org/index.html#foo");
		dumpParse("http://www.archive.org/");
		dumpParse("http://www.archive.org");
		dumpParse("http://www.archive.org?");
		dumpParse("http://www.archive.org:8080/index.html?query#foo");
		dumpParse("http://www.archive.org:8080/index.html?#foo");
		dumpParse("http://www.archive.org:8080?#foo");
		dumpParse("http://bücher.ch:8080?#foo");
		
		dumpParse("dns:bücher.ch");

	}
	
	private void dumpParse(String s) throws URIException {
		HandyURL h = URLParser.parse(s);
		System.out.format("Input:(%s)\nHandyURL\t%s\n",s,h.toDebugString());
	}

}
