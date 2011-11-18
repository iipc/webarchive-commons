package org.archive.hadoop.func;

import junit.framework.TestCase;

public class URLResolverFuncTest extends TestCase {
	public void testResolve() {
		URLResolverFunc f = new URLResolverFunc();
		assertEquals("http://x.com/",
				f.doResolve("", "", "http://x.com/"));

		assertEquals("http://x.com/",
				f.doResolve("http://x.com/", "", "http://x.com/"));

		assertEquals("http://x.com/",
				f.doResolve("http://y.com/", "", "http://x.com/"));

		assertEquals("http://x.com/",
				f.doResolve("http://y.com/", "http://z.com/", "http://x.com/"));

		assertEquals("http://z.com/",
				f.doResolve("http://y.com/", "http://z.com/", "/"));
		
		assertEquals("http://y.com/",
				f.doResolve("http://y.com/", null, "/"));

		assertEquals("http://y.com/images/",
				f.doResolve("http://y.com/", null, "/images/"));

		assertEquals("http://y.com/images/",
				f.doResolve("http://z.com/","http://y.com/", "/images/"));

		assertEquals("http://y.com/foo.gif",
				f.doResolve("http://z.com/","http://y.com/images", "foo.gif"));

		assertEquals("http://z.com/foo.gif",
				f.doResolve("http://z.com/images",null, "foo.gif"));

		assertEquals("http://z.com/images/foo.gif",
				f.doResolve("http://z.com/images/",null, "foo.gif"));

		assertEquals("http://y.com/images/foo.gif",
				f.doResolve("http://z.com/","http://y.com/images/", "foo.gif"));

		assertEquals("http://y.com/z/foo.gif",
				f.doResolve("http://z.com/","http://y.com/images/", "/z/foo.gif"));
	}
}
