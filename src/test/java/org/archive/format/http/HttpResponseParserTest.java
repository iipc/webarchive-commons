package org.archive.format.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.archive.util.IAUtils;
import org.archive.util.TestUtils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HttpResponseParserTest {

	@Test
	public void testParse() throws IOException {

		HttpResponseParser parser = new HttpResponseParser();
		String message = "200 OK\r\nContent-Type: text/plain\r\n\r\nHi there";
		try {
			HttpResponse response = 
				parser.parse(new ByteArrayInputStream(message.getBytes(IAUtils.UTF8)));
			assertNotNull(response);
			HttpHeaders headers = response.getHeaders();
			assertNotNull(headers);
			assertEquals(1,headers.size());
			HttpHeader header = headers.get(0);
			assertEquals("Content-Type",header.getName());
			assertEquals("text/plain",header.getValue());
			TestUtils.assertStreamEquals(response, "Hi there".getBytes(IAUtils.UTF8));
			
		} catch (HttpParseException e) {
			e.printStackTrace();
			fail();
		}
		
	}

	@Test
	public void testParseWithLf() throws IOException {

		HttpResponseParser parser = new HttpResponseParser();
		String message = "200 OK\nContent-Type: text/plain\n\nHi there";
		try {
			HttpResponse response = 
				parser.parse(new ByteArrayInputStream(message.getBytes(IAUtils.UTF8)));
			assertNotNull(response);
			HttpHeaders headers = response.getHeaders();
			assertNotNull(headers);
			assertEquals(1,headers.size());
			
		} catch (HttpParseException e) {
			e.printStackTrace();
			fail();
		}
		
	}

	@Test
	public void testParseEmptyHeaderField() throws IOException {

		HttpResponseParser parser = new HttpResponseParser();
		String message = "200 OK\r\nContent-Type: text/plain\r\nServer: \r\n\r\nHi there";
		try {
			HttpResponse response = 
				parser.parse(new ByteArrayInputStream(message.getBytes(IAUtils.UTF8)));
			assertNotNull(response);
			HttpHeaders headers = response.getHeaders();
			assertNotNull(headers);
			assertEquals(2, headers.size());
			HttpHeader header = headers.get(1);
			assertEquals("Server",header.getName());
			System.err.println(header.getValue());
			assertFalse("text/plain".equals(header.getValue()));
			TestUtils.assertStreamEquals(response, "Hi there".getBytes(IAUtils.UTF8));
			
		} catch (HttpParseException e) {
			e.printStackTrace();
			fail();
		}
		
	}

}
