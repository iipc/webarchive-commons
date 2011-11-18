package org.archive.format.arc;

import org.archive.RecoverableRecordFormatException;

public class ARCFormatException extends RecoverableRecordFormatException {

	public ARCFormatException(String string) {
		super(string);
	}
	public ARCFormatException(Exception e) {
		super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
