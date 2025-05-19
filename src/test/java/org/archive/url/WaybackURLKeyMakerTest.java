package org.archive.url;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WaybackURLKeyMakerTest {

	@Test
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
		assertEquals("192,211,203,34)/robots.txt", km.makeKey("https://34.203.211.192/robots.txt"));
		assertEquals("2600:1f18:200d:fb00:2b74:867c:ab0c:150a)/robots.txt",
				km.makeKey("https://[2600:1f18:200d:fb00:2b74:867c:ab0c:150a]/robots.txt"));
		assertEquals("ua,1kr)/newslist.html?tag=%e4%ee%f8%ea%ee%eb%fc%ed%ee%e5",
				km.makeKey("http://1kr.ua/newslist.html?tag=%E4%EE%F8%EA%EE%EB%FC%ED%EE%E5"));
		assertEquals("com,aluroba)/tags/%c3%ce%ca%c7%d1%e5%c7.htm",
				km.makeKey("http://www.aluroba.com/tags/%C3%CE%CA%C7%D1%E5%C7.htm"));
		assertEquals("ac,insbase)/xoops2/modules/xpwiki?%a4%d5%a4%af%a4%aa%a4%ab%b8%a9%a4%aa%a4%aa%a4%ce%a4%b8%a4%e7%a4%a6%bb%d4",
			km.makeKey("https://www.insbase.ac/xoops2/modules/xpwiki/?%A4%D5%A4%AF%A4%AA%A4%AB%B8%A9%A4%AA%A4%AA%A4%CE%A4%B8%A4%E7%A4%A6%BB%D4"));
	}

}
