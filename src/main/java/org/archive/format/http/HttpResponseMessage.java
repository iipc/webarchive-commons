package org.archive.format.http;

import java.util.Locale;

public class HttpResponseMessage extends HttpMessage implements HttpResponseMessageObserver {
	private int status = 0;
	private String reason = null;
	
	public HttpResponseMessage(){}

	public HttpResponseMessage(int version, int status, String reason) {
		this.version = version;
		this.status = status;
		this.reason = reason;
	}

	public int getStatus() {
		return status;
	}
	
	public String getReason() {
		return reason;
	}
	public String toString() {
		return String.format(Locale.ROOT, "%s %d %s%s", getVersionString(), status, reason, CRLF);
	}
	public String toDebugString() {
		return String.format(Locale.ROOT, "Message(%d):(%s) (%d) (%s)\n",
				reason.length(),getVersionString(),status,reason,CRLF);
	}

	public void messageParsed(int version, int status, String reason, int bytes) {
		this.version = version;
		this.status = status;
		this.reason = reason;
		this.bytes = bytes;
	}
}
