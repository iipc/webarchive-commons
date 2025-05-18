package org.archive.format.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.archive.util.IAUtils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpRequestMessageParserTest implements HttpConstants {
	HttpRequestMessageParser parser = new HttpRequestMessageParser();

	@Test
	public void testParse() throws IOException {
		assertParse("GET / HTTP/1.0\r\n", METHOD_GET, "/", VERSION_0);
		assertParse("GET / HTTP/1.1\r\n", METHOD_GET, "/", VERSION_1);
		assertParse("POST / HTTP/1.1\r\n", METHOD_POST, "/", VERSION_1);
		assertParse("POST /POST HTTP/1.1\r\n", METHOD_POST, "/POST", VERSION_1);
		assertParse("POST /POST/ HTTP/1.1\r\n", METHOD_POST, "/POST/", VERSION_1);
		assertParse("HEAD /POST/ HTTP/1.1\r\n", METHOD_HEAD, "/POST/", VERSION_1);

		assertParse("  HEAD /POST/ HTTP/1.1\r\n", METHOD_HEAD, "/POST/", VERSION_1);
		assertParse("HEAD  /POST/ HTTP/1.1\r\n", METHOD_HEAD, "/POST/", VERSION_1);
		assertParse("HEAD /POST/  HTTP/1.1\r\n", METHOD_HEAD, "/POST/", VERSION_1);
		assertParse("HAD /POST/  HTTP/1.1\r\n", METHOD_UNK, "/POST/", VERSION_1);
	}

	private void assertParse(String message, int method, String path, int version) throws HttpParseException, IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes(IAUtils.UTF8));
		HttpRequestMessage m = parser.parse(bais);
		assertEquals(method, m.getMethod());
		assertEquals(path,m.getPath());
		assertEquals(version,m.getVersion());
	}
}
