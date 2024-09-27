package org.archive.resource;

import java.io.IOException;

import org.archive.extract.ExtractingResourceFactoryMapper;
import org.archive.extract.ExtractingResourceProducer;
import org.archive.extract.ProducerUtils;
import org.archive.extract.ResourceFactoryMapper;
import org.archive.format.json.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import junit.framework.TestCase;

public class MetaDataTest extends TestCase {

	private static String[] testFilePaths = {
			"src/test/resources/org/archive/format/warc/IAH-urls-wget.warc",
			"src/test/resources/org/archive/format/warc/mutliple-headers.warc"
	};

	private static JSONObject obj = new JSONObject("{\"foo\":\"bar\",\"hello\":\"world\"}");

	private MetaData putMetaData(MetaData m) {
		m.putBoolean("boolean-1", false);
		m.putBoolean("boolean-2", true);
		m.put("boolean-3", true);
		m.put("boolean-1", true); // append

		m.put("double-1", 0.5d);
		m.put("double-2", 2.5d);
		m.put("double-3", 3.5d);
		m.put("double-1", 1.5d); // append

		m.put("int-1", 0);
		m.put("int-2", 2);
		m.put("int-3", 3);
		m.put("int-1", 1); // append

		// choose JSON "numbers" which are forced into a Java long (too big for an integer)
		m.putLong("long-1", 0xffffffffL + 0L);
		m.putLong("long-2", 0xffffffffL + 2L);
		m.put("long-3", 0xffffffffL + 3L);
		m.put("long-1", 0xffffffffL + 1L); // append

		m.putString("string-1", "0");
		m.putString("string-2", "2");
		m.put("string-3", "3");
		m.put("string-1", "1"); // append

		m.putOpt("obj-1", obj);
		m.put("obj-1", obj); // append
		m.put("obj-2", obj);
		m.putOpt("obj-2", null); // do nothing because value is null

		return m;
	}

	private void verifyMultiValuedMetaData(MetaData m) {
		// boolean
		assertEquals(JSONArray.class, m.get("boolean-1").getClass());
		assertEquals(false, ((JSONArray) m.get("boolean-1")).getBoolean(0));
		assertEquals(true, ((JSONArray) m.get("boolean-1")).getBoolean(1));
		assertEquals(true, m.getBoolean("boolean-2"));
		assertEquals(true, m.getBoolean("boolean-3"));
		assertEquals(Boolean.class, m.get("boolean-3").getClass());
		assertEquals(true, m.optBoolean("boolean-3", false));
		assertEquals(false, m.optBoolean("boolean-99", false));

		// double
		assertEquals(JSONArray.class, m.get("double-1").getClass());
		assertEquals(0.5d, ((JSONArray) m.get("double-1")).getDouble(0));
		assertEquals(1.5d, ((JSONArray) m.get("double-1")).getDouble(1));
		assertEquals(2.5d, m.getDouble("double-2"));
		assertEquals(3.5d, m.getDouble("double-3"));
		// could be Double or BigDecimal, depending on the Java version
		// assertEquals(Double.class, m.get("double-3").getClass());
		assertEquals(3.5d, m.optDouble("double-3"));
		assertEquals(99.5d, m.optDouble("double-99", 99.5d));

		// int
		assertEquals(JSONArray.class, m.get("int-1").getClass());
		assertEquals(0, ((JSONArray) m.get("int-1")).getInt(0));
		assertEquals(1, ((JSONArray) m.get("int-1")).getInt(1));
		assertEquals(2, m.getInt("int-2"));
		assertEquals(3, m.getInt("int-3"));
		assertEquals(Integer.class, m.get("int-3").getClass());
		assertEquals(3, m.optInt("int-3"));
		assertEquals(99, m.optInt("int-99", 99));

		// long
		assertEquals(JSONArray.class, m.get("long-1").getClass());
		assertEquals(0xffffffffL + 0L, ((JSONArray) m.get("long-1")).getLong(0));
		assertEquals(0xffffffffL + 1L, ((JSONArray) m.get("long-1")).getLong(1));
		assertEquals(0xffffffffL + 2L, m.getLong("long-2"));
		assertEquals(0xffffffffL + 3L, m.getLong("long-3"));
		assertEquals(Long.class, m.get("long-3").getClass());
		assertEquals(0xffffffffL + 3L, m.optLong("long-3"));
		assertEquals(0xffffffffL + 99L, m.optLong("long-99", 0xffffffffL + 99L));

		// String
		assertEquals(JSONArray.class, m.get("string-1").getClass());
		assertEquals("0", ((JSONArray) m.get("string-1")).getString(0));
		assertEquals("1", ((JSONArray) m.get("string-1")).getString(1));
		assertEquals("2", m.getString("string-2"));
		assertEquals("3", m.getString("string-3"));
		assertEquals(String.class, m.get("string-3").getClass());
		assertEquals("3", m.optString("string-3"));
		assertEquals("99", m.optString("string-99", "99"));

		// Object
		assertEquals(JSONArray.class, m.get("obj-1").getClass());
		assertEquals(JSONObject.class, ((JSONArray) m.get("obj-1")).get(0).getClass());
		assertEquals(JSONObject.class, ((JSONArray) m.get("obj-1")).get(1).getClass());
		assertEquals("bar", ((JSONObject) ((JSONArray) m.get("obj-1")).get(0)).get("foo"));
		assertEquals("world", ((JSONObject) ((JSONArray) m.get("obj-1")).get(0)).get("hello"));
		assertEquals("bar", ((JSONObject) ((JSONArray) m.get("obj-1")).get(1)).get("foo"));
		assertEquals("world", ((JSONObject) ((JSONArray) m.get("obj-1")).get(1)).get("hello"));
		assertEquals(JSONObject.class, m.get("obj-2").getClass());
		assertEquals("bar", ((JSONObject) m.get("obj-2")).get("foo"));
		assertEquals("world", ((JSONObject) m.get("obj-2")).get("hello"));
	}

	public void testMultiValued() {
		MetaData m = new MetaData();
		m = putMetaData(m);
		verifyMultiValuedMetaData(m);

		// test (de)serialization
		m = new MetaData(m.toString(2));
		verifyMultiValuedMetaData(m);
	}

	private MetaData readNextWARCResponseAsMetaData(String filePath) throws IOException, ResourceParseException {
		ResourceProducer producer = ProducerUtils.getProducer(filePath);
		ResourceFactoryMapper mapper = new ExtractingResourceFactoryMapper();
		ExtractingResourceProducer exProducer = new ExtractingResourceProducer(producer, mapper);
		Resource r = exProducer.getNext();
		while (r != null) {
			MetaData top = r.getMetaData().getTopMetaData();
			JSONObject warcHeaders = JSONUtils.extractObject(top, "Envelope.WARC-Header-Metadata");
			if (warcHeaders.has("WARC-Type") && "response".equals(warcHeaders.getString("WARC-Type"))) {
				return top;
			}
			r = exProducer.getNext();
		}
		return null;
	}

	/**
	 * Verify that in the legacy test file all WARC and HTTP headers are
	 * single-valued, i.e. {@linkplain String}s.
	 */
	public void testSingleHeaders() throws IOException, ResourceParseException {
		MetaData m = readNextWARCResponseAsMetaData(testFilePaths[0]);

		JSONObject warcHeaders = JSONUtils.extractObject(m, "Envelope.WARC-Header-Metadata");
		JSONObject httpHeaders = JSONUtils.extractObject(m, "Envelope.Payload-Metadata.HTTP-Response-Metadata.Headers");

		for (Object header : warcHeaders.keySet()) {
			assertEquals(String.class, warcHeaders.get(header.toString()).getClass());
		}

		for (Object header : httpHeaders.keySet()) {
			assertEquals(String.class, httpHeaders.get(header.toString()).getClass());
		}
	}

	public void testMultipleHeaders() throws IOException, ResourceParseException {
		MetaData m = readNextWARCResponseAsMetaData(testFilePaths[1]);

		JSONObject warcHeaders = JSONUtils.extractObject(m, "Envelope.WARC-Header-Metadata");
		JSONObject httpHeaders = JSONUtils.extractObject(m, "Envelope.Payload-Metadata.HTTP-Response-Metadata.Headers");

		assertEquals("https://www.example.com/index.html/", warcHeaders.getString("WARC-Target-URI"));
		assertEquals(JSONArray.class, warcHeaders.get("WARC-Protocol").getClass());
		assertEquals(2, ((JSONArray) warcHeaders.get("WARC-Protocol")).length());
		assertEquals("h2", ((JSONArray) warcHeaders.get("WARC-Protocol")).get(0));

		assertEquals("108", httpHeaders.getString("Content-Length"));
		assertEquals(JSONArray.class, httpHeaders.get("x-powered-by").getClass());
		assertEquals(2, ((JSONArray) httpHeaders.get("x-powered-by")).length());
		assertEquals("PHP/8.3.11", ((JSONArray) httpHeaders.get("x-powered-by")).get(0));
		assertEquals("PleskLin", ((JSONArray) httpHeaders.get("x-powered-by")).get(1));
	}
}
