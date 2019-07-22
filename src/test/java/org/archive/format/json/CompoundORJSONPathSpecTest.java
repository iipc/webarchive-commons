package org.archive.format.json;

import java.util.ArrayList;

import org.archive.util.TestUtils;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

import junit.framework.TestCase;

public class CompoundORJSONPathSpecTest extends TestCase {
	String json1S = "{\"a\":\"A\"}";
	String json2S = "{\"b\":\"B\"}";
	public void testExtract() throws JSONException {
		JSONObject json1 = new JSONObject(json1S);
		JSONObject json2 = new JSONObject(json2S);
		ArrayList<JSONPathSpec> parts = new ArrayList<JSONPathSpec>();
		parts.add(new SimpleJSONPathSpec("a"));
		parts.add(new SimpleJSONPathSpec("b"));
		
		JSONPathSpec comp = new CompoundORJSONPathSpec(parts);
		TestUtils.dumpMatch("json1", comp.extract(json1));
		TestUtils.assertLoLMatches(new String[][]{{"A"}}, comp.extract(json1));
		TestUtils.assertLoLMatches(new String[][]{{"B"}}, comp.extract(json2));
	}

}
