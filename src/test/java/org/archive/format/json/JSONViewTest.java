package org.archive.format.json;

import org.archive.util.TestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import org.junit.jupiter.api.Test;

public class JSONViewTest  {
	
	public int getInt(byte b[]) {
		return b[0] & 0xff;
	}

	@Test
	public void testBytes() throws JSONException {
		JSONObject o = new JSONObject();
		o.append("name1", "val\\rue1");
		String json = o.toString();
		System.out.format("once: (%s)\n",json);
		JSONObject o2 = new JSONObject(json);
		System.out.format("twice: (%s)\n",o2.toString());
		
		
		byte b[] = new byte[2];
		for(int i = 0; i < 256; i++) {
			b[0] = (byte) i;
			int gi = getInt(b);
			System.out.format("I(%d) gi(%d)\n",i,gi);
		}
	}

	@Test
	public void testApply() throws JSONException {
		String json1S = "{\"url\":\"a\",\"link\":[{\"zz\":\"1\",\"qq\":\"qa\"},{\"zz2\":\"2\",\"qq\":\"qb\"},{\"zz\":\"3\",\"qq\":\"qc\"},{\"zz\":\"4\"}]}";
		JSONObject json1 = new JSONObject(json1S);

		JSONView view = new JSONView("url","@link.zz");
		TestUtils.assertLoLMatches(new String[][]{{"a","1"},{"a",""},{"a","3"},{"a","4"}},
				view.apply(json1));

		view = new JSONView("url","@link.{zz,qq}");
		TestUtils.assertLoLMatches(new String[][]{{"a","1","qa"},{"a","","qb"},{"a","3","qc"},{"a","4",""}},
				view.apply(json1));
	}

}
