package org.archive.resource.html;

import org.archive.resource.MetaData;
import org.htmlparser.nodes.TextNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import junit.framework.TestCase;

public class ExtractingParseObserverTest extends TestCase {

	public void testHandleStyleNodeExceptions() throws Exception {
		String[] tests = {
				"some css",
				"url()",
				"url () ",
				"url ('')",
				"url (' ')",
				"url('\")",
				"url(')",
				"url('\"')",
				"url('\\\"\"')",
				"url(''''')"
		};
		boolean except = false;
		HTMLMetaData md = new HTMLMetaData(new MetaData());
		ExtractingParseObserver epo = new ExtractingParseObserver(md);
		for(String css : tests) {
			try {
				TextNode tn = new TextNode(css);
				epo.handleStyleNode(tn);
			} catch(Exception e) {
				System.err.format("And the winner is....(%s)\n", css);
				e.printStackTrace();
				except = true;
				throw e;
			}
			assertFalse(except);
		}
	}

	public void testHandleStyleNode() throws Exception {
		String[][] tests = {
				{""},
				{"url(foo.gif)","foo.gif"},
				{"url('foo.gif')","foo.gif"},
				{"url(\"foo.gif\")","foo.gif"},
				{"url(\\\"foo.gif\\\")","foo.gif"},
				{"url(\\'foo.gif\\')","foo.gif"},
				{"url(''foo.gif'')","foo.gif"},
				{"url(  foo.gif  )","foo.gif"},
				{"url('''')"},
				{"url('foo.gif'')","foo.gif"},
				};
		for(String[] testa : tests) {
			checkExtract(testa);
		}
	}

	/**
	 * Test whether the pattern matcher does extract nothing and also does not
	 * not hang-up if an overlong CSS link is truncated.
	 */
	public void testHandleStyleNodeNoHangupTruncated() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("url(");
		for (int i = 0; i < 500000; i++)
			sb.append('\'');
		sb.append("foo.gif");
		for (int i = 0; i < 499000; i++)
			sb.append('\'');
		String[] test = new String[1];
		test[0] = sb.toString();
		checkExtract(test);
	}

	private void checkExtract(String[] data) throws JSONException {
//		System.err.format("CSS(%s) want[0](%s)\n",css,want[0]);
		String css = data[0];
		HTMLMetaData md = new HTMLMetaData(new MetaData());
		ExtractingParseObserver epo = new ExtractingParseObserver(md);
		try {
			TextNode tn = new TextNode(css);
			epo.handleStyleNode(tn);
		} catch(Exception e) {
			fail("Exception with CSS:" + css);
		}
		JSONArray a = md.optJSONArray("Links");
		if(data.length > 1) {
			assertNotNull(a);
			assertEquals(data.length-1,a.length());
			for(int i = 1; i < data.length; i++) {
				Object o = a.optJSONObject(i-1);
				
				assertTrue(o instanceof JSONObject);
				JSONObject jo = (JSONObject) o;
				assertEquals("CSS link extraction failed for <" + css + ">",
						data[i], jo.getString("href"));
			}
		} else {
			assertNull("Expected no extracted link for <" + css + ">", a);
		}
	}
	

}
