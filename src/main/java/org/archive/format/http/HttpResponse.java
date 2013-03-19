package org.archive.format.http;

import java.io.FilterInputStream;
import java.io.InputStream;

public class HttpResponse extends FilterInputStream {
	private HttpResponseMessage message = null;
	private HttpHeaders headers = null;
	private InputStream inner;
	private int headerBytes = 0;

	protected HttpResponse(InputStream in,
			HttpResponseMessage message, HttpHeaders headers) {
		super(in);
		inner = in;
		this.message = message;
		this.headers = headers;
	}
	public InputStream getInner() {
		return inner;
	}
	public HttpResponseMessage getMessage() {
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
