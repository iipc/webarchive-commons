package org.archive.format.cdx;

public interface CDXFieldConstants {
	public final static String urlkey = "urlkey";
	public final static String timestamp = "timestamp";
	public final static String original = "original";
	public final static String mimetype = "mimetype";
	public final static String statuscode = "statuscode";
	public final static String digest = "digest";
	public final static String redirect = "redirect";
	public final static String robotflags = "robotflags";
	public final static String length = "length";
	public final static String offset = "offset";
	public final static String filename = "filename";

	// A list of *ALL* standard cdx field names
	public final static FieldSplitFormat CDX_ALL_NAMES = new FieldSplitFormat(urlkey, timestamp, original, mimetype, statuscode, digest, redirect, robotflags,
			length, offset, filename);
}
