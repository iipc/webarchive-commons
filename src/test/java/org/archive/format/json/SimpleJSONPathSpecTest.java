package org.archive.format.json;

import org.archive.util.TestUtils;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

import junit.framework.TestCase;

public class SimpleJSONPathSpecTest extends TestCase {
	String json1 = "{\"a\": {  \"b\": \"Foo\" }}";
	String json2 = "{\"a\": {  \"b\": [{\"a\":\"1\"},{\"a\":\"2\"}] }}";

	String json3 = "{\"a\": {  \"b\": {\"A\":\"11\",\"B\":\"22\"} }}";
	String json4 = "{\"a\": {  \"b\": [{\"A\":\"11\",\"B\":\"22\"},{\"A\":\"33\",\"B\":\"44\"}] }}";

	public void testExtract() throws JSONException {
		JSONObject json = new JSONObject(json1);
		JSONPathSpec spec = new SimpleJSONPathSpec("a.b");
		TestUtils.dumpMatch("json1", spec.extract(json));
		TestUtils.assertLoLMatches(new String[][]{{"Foo"}}, spec.extract(json));
		
		json = new JSONObject(json2);
		spec = new SimpleJSONPathSpec("a.@b.a");
		TestUtils.dumpMatch("json2", spec.extract(json));
		TestUtils.assertLoLMatches(new String[][]{{"1"},{"2"}}, spec.extract(json));

		json = new JSONObject(json3);
		spec = new SimpleJSONPathSpec("a.b.{A,B}");
		TestUtils.dumpMatch("json3", spec.extract(json));
		TestUtils.assertLoLMatches(new String[][]{{"11","22"}}, spec.extract(json));

		json = new JSONObject(json4);
		spec = new SimpleJSONPathSpec("a.@b.{A,B}");
		TestUtils.dumpMatch("json4", spec.extract(json));
		TestUtils.assertLoLMatches(new String[][]{{"11","22"},{"33","44"}}, spec.extract(json));
	}
}
