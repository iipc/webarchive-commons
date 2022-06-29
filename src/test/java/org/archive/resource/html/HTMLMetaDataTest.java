package org.archive.resource.html;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

import junit.framework.TestCase;

public class HTMLMetaDataTest extends TestCase {

	public void testHTMLParseData() {
//		fail("Not yet implemented");
	}
	public void testJSON() throws JSONException {
		JSONObject data = new JSONObject();
		JSONObject links = new JSONObject();
		JSONObject header = new JSONObject();
		links.append("link1", "link1.val");
		data.append("links", links);
		links.append("link2", "link2.val");
		data.append("header", header);

		header.put("header1", "header1.val");
		header.put("header1", "header1.va2222l");
		JSONArray ha = new JSONArray();
		JSONObject foo = new JSONObject();
		foo.put("foo1", "fooval");
		ha.put(foo);
		header.put("arr", ha);
		System.out.println(data.toString());

	}
	public void testJSON2() throws JSONException {
		String sa[][] = {{"one","1"},{"two","2"},{"three","3"}};
		JSONObject jo = new JSONObject();
		appendStrArr(jo,sa);
		appendStrArr(jo,sa);
		System.out.println(jo.toString(1));
	}
	public void testJSON3() throws JSONException {
		JSONObject jo = new JSONObject();
		appendStrArr2(jo,"k",new String[] {"1","2","3","4"});
		appendStrArr2(jo,"k","1","2","3","4");
		appendStrArr2(jo,"k2","1","2","3","4","foo","bar");
		appendStrArr2(jo,"k2","1","2","3","4","foo","baz");
		System.out.println(jo.toString(1));
	}
	private void appendStrArr(JSONObject o, String a[][]) throws JSONException {
		JSONObject n = new JSONObject();
		for(String i[] : a) {
			if(i.length != 2) {
				throw new IllegalArgumentException();
			}
			n.put(i[0], i[1]);
		}
		o.append("links", n);
	}
	private void appendStrArr2(JSONObject o, String k, String... a) throws JSONException {
		
		System.out.format("A length(%d)\n", a.length);
		JSONObject n = new JSONObject();
		if((a.length & 1) == 1) {
			throw new IllegalArgumentException();
		}
		for(int i = 0; i < a.length; i+=2) {
			n.put(a[i], a[i+1]);
		}
		o.append(k, n);
//		for(String i[] : a) {
//			if(i.length != 2) {
//				throw new IllegalArgumentException();
//			}
//			n.put(i[0], i[1]);
//		}
//		o.append("links", n);
	}
	
}
