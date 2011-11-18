package org.archive.format.http;

public class HttpMessage implements HttpConstants {
	protected int version = VERSION_0;
	protected int bytes = -1;
	protected boolean isCorrupt;

	public int getVersion() {
		return version;
	}
	public String getVersionString() {
		if(version == VERSION_1) {
			return VERSION_1_STATUS;
		} else if(version == VERSION_9) {
			return VERSION_9_STATUS;
		}
		return VERSION_0_STATUS;
	}
	public int getLength() {
		return bytes;
	}

	public void messageCorrupt() {
		isCorrupt = true;
	}
	public boolean isCorrupt() {
		return isCorrupt;
	}
}
