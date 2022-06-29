package org.archive.format.json;

import org.archive.util.TestUtils;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

import junit.framework.TestCase;

public class JSONPathSpecFactoryTest extends TestCase {
	String json1S = "{\"a\":\"A\"}";
	String json2S = "{\"b\":\"B\"}";

	String json3S = "{\"b\":{\"a\" : {\"c\":\"Yes\"}}}";

	String json4S = "{\"b\":[{\"x\":\"x1\", \"y\":\"y1\"},{\"x\":\"x2\", \"y\":\"y2\"}]}";

	public void testGet() throws JSONException {
		JSONObject json1 = new JSONObject(json1S);
		JSONObject json2 = new JSONObject(json2S);
		JSONObject json3 = new JSONObject(json3S);
		JSONObject json4 = new JSONObject(json4S);

		TestUtils.assertLoLMatches(new String[][]{{"A"}}, 
				JSONPathSpecFactory.get("a").extract(json1));
		TestUtils.assertLoLMatches(new String[][]{{"B"}}, 
				JSONPathSpecFactory.get("b").extract(json2));
		
		TestUtils.assertLoLMatches(new String[][]{{""}}, 
				JSONPathSpecFactory.get("b").extract(json1));

		TestUtils.assertLoLMatches(new String[][]{{"A"}}, 
				JSONPathSpecFactory.get("a|b").extract(json1));
		TestUtils.assertLoLMatches(new String[][]{{"B"}}, 
				JSONPathSpecFactory.get("a|b").extract(json2));

		TestUtils.assertLoLMatches(new String[][]{{"A"}}, 
				JSONPathSpecFactory.get("b|a").extract(json1));
		TestUtils.assertLoLMatches(new String[][]{{"B"}}, 
				JSONPathSpecFactory.get("b|a").extract(json2));

		TestUtils.assertLoLMatches(new String[][]{{"Yes"}}, 
				JSONPathSpecFactory.get("b.a.a|b.a.c").extract(json3));

		TestUtils.assertLoLMatches(new String[][]{{"x1"},{"x2"}}, 
				JSONPathSpecFactory.get("@b.x").extract(json4));
		TestUtils.assertLoLMatches(new String[][]{{"y1"},{"y2"}}, 
				JSONPathSpecFactory.get("@b.y").extract(json4));

		TestUtils.assertLoLMatches(new String[][]{{"x1","y1"},{"x2","y2"}}, 
				JSONPathSpecFactory.get("@b.{x,y}").extract(json4));
	}

}
