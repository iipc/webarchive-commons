package org.archive.format.http;

import java.io.FilterInputStream;
import java.io.InputStream;

public class HttpRequest extends FilterInputStream {
	private HttpRequestMessage message = null;
	private HttpHeaders headers = null;
	private int headerBytes = 0;

	protected HttpRequest(InputStream in,
			HttpRequestMessage message, HttpHeaders headers) {
		super(in);
		this.message = message;
		this.headers = headers;
	}

	public HttpRequestMessage getMessage() {
		return message;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public int getHeaderBytes() {
		return headerBytes;
	}

	public void setHeaderBytes(int headerBytes) {
		this.headerBytes = headerBytes;
	}
}
