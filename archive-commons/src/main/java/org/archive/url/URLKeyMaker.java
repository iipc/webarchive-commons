package org.archive.url;

import org.apache.commons.httpclient.URIException;

public interface URLKeyMaker {
	public String makeKey(String url) throws URIException;
}
