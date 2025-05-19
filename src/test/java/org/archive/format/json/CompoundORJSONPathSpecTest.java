package org.archive.format.json;

import java.util.ArrayList;

import org.archive.util.TestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import org.junit.jupiter.api.Test;

public class CompoundORJSONPathSpecTest {
	String json1S = "{\"a\":\"A\"}";
	String json2S = "{\"b\":\"B\"}";
	@Test
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
