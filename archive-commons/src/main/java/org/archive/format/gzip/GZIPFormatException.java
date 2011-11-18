package org.archive.format.gzip;

import java.io.IOException;

import org.archive.RecoverableRecordFormatException;


public class GZIPFormatException extends RecoverableRecordFormatException {
	/** */
	private static final long serialVersionUID = -3526676437467483190L;

	public GZIPFormatException() {
		super();
	}
	public GZIPFormatException(String message) {
		super(message);
	}
	public GZIPFormatException(Exception e) {
		super(e);
	}
	public GZIPFormatException(String message, IOException e) {
		super(message,e);
	}
}
