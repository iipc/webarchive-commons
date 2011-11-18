package org.archive.format.http;

public class HttpRequestMessage extends HttpMessage implements HttpRequestMessageObserver {
	private int method = 0;
	private String path = null;

	public void messageParsed(int method, String path, int version, int bytes) {
		this.method = method;
		this.path = path;
		this.version = version;
		this.bytes = bytes;
	}

	public String getPath() {
		return path;
	}
	public int getMethod() {
		return method;
	}

	public String getMethodString() {
		switch(method) {
		case METHOD_GET     : return METHOD_GET_STRING;
		case METHOD_HEAD    : return METHOD_HEAD_STRING;
		case METHOD_POST    : return METHOD_POST_STRING;
		case METHOD_PUT     : return METHOD_PUT_STRING;
		case METHOD_TRACE   : return METHOD_TRACE_STRING;
		case METHOD_DELETE  : return METHOD_DELETE_STRING;
		case METHOD_CONNECT : return METHOD_CONNECT_STRING;
		}
		return METHOD_UNK_STRING;
	}
	
}
