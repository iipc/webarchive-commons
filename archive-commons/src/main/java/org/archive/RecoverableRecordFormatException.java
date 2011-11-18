package org.archive;

import java.io.IOException;

public class RecoverableRecordFormatException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2775048979983919630L;
	public RecoverableRecordFormatException() {
		super();
	}
	public RecoverableRecordFormatException(String message) {
		super(message);
	}
	public RecoverableRecordFormatException(Exception e) {
		super(e);
	}
	public RecoverableRecordFormatException(String message, IOException e) {
		super(message,e);
	}

}
