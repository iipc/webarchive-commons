package org.archive.format.http;

public interface HttpRequestMessageObserver extends HttpConstants {
	public void messageParsed(int method, String path, int version, int bytes);
	public void messageCorrupt();
}
