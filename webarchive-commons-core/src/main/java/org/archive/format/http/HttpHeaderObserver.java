package org.archive.format.http;

public interface HttpHeaderObserver extends HttpConstants {

	public void headerParsed(byte name[], int ns, int nl, 
			byte value[], int vs, int vl);

	public void headersComplete(int totalBytes);
	public void headersCorrupt();
}
