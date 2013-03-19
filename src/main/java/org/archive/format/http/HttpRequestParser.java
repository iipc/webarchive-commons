package org.archive.format.http;

import java.io.IOException;
import java.io.InputStream;

public class HttpRequestParser {
	private HttpRequestMessageParser messageParser = 
		new HttpRequestMessageParser();

	private HttpHeaderParser headerParser = new HttpHeaderParser();

	public HttpRequestParser() {}
	public HttpRequest parse(InputStream is) 
	throws HttpParseException, IOException {

		HttpRequestMessage message = new HttpRequestMessage();
		HttpHeaders headers = new HttpHeaders();
		int headerBytes = messageParser.parse(is, message);
		headerBytes += headerParser.doParse(is, headers);
		
		HttpRequest request = new HttpRequest(is, message, headers);
		request.setHeaderBytes(headerBytes);

		return request;
	}
}
