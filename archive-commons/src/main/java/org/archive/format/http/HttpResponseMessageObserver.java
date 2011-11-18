package org.archive.format.http;

public interface HttpResponseMessageObserver extends HttpConstants {
	public void messageParsed(int version, int code, String reason, int bytes);
	public void messageCorrupt();
}
