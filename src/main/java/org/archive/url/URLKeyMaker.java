package org.archive.url;

import java.net.URISyntaxException;

public interface URLKeyMaker {
	public String makeKey(String url) throws URISyntaxException;
}
