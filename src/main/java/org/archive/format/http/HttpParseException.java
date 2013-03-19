package org.archive.format.http;

import org.archive.RecoverableRecordFormatException;

public class HttpParseException extends RecoverableRecordFormatException {

	/** */
	private static final long serialVersionUID = -2194883519998764425L;

	public HttpParseException(String string) {
		super(string);
	}

}
