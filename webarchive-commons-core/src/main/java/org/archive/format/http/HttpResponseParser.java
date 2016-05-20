package org.archive.format.http;

import java.io.IOException;
import java.io.InputStream;

public class HttpResponseParser {
	private HttpResponseMessageParser messageParser = 
		new HttpResponseMessageParser();

	private HttpHeaderParser headerParser = new HttpHeaderParser();

	public HttpResponseParser() {}
	public HttpResponse parse(InputStream is) 
	throws HttpParseException, IOException {

		HttpResponseMessage message = new HttpResponseMessage();
		HttpHeaders headers = new HttpHeaders();
		int headerBytes = messageParser.parse(is, message);
		headerBytes += headerParser.doParse(is, headers);
		
		HttpResponse response = new HttpResponse(is, message, headers);
		response.setHeaderBytes(headerBytes);
		// TODO: check for chunked transfer encoding
		return response;
	}
}
