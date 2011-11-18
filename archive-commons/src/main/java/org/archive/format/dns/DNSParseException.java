package org.archive.format.dns;

import org.archive.RecoverableRecordFormatException;

public class DNSParseException extends RecoverableRecordFormatException {

	public DNSParseException(String string) {
		super(string);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7946541881940132743L;

}
