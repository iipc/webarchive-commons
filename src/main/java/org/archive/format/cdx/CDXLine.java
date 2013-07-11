package org.archive.format.cdx;

public class CDXLine extends FieldSplitLine {
	
	
	public final static String urlkey = "urlkey";
	public final static String timestamp = "timestamp";
	public final static String original = "original";	
	public final static String mimetype = "mimetype";
	public final static String statuscode = "statuscode";
	public final static String digest = "digest";
	public final static String redirect = "redirect";
	public final static String metaflags = "metaflags";
	public final static String length = "length";
	public final static String offset = "offset";
	public final static String filename = "filename";
	
	
	 // Matches cdx format: CDX N b a m s k r M S V g
	public final static String[] CDX_11_NAMES = {urlkey, 
												 timestamp, 
												 original, 
												 mimetype, 
												 statuscode, 
												 digest,
												 redirect,
												 metaflags, 
												 length, 
												 offset, 
												 filename}; 

	// A list of *ALL* cdx field names
	public final static String[] CDX_ALL_NAMES = CDX_11_NAMES;
	
	
	// Old-style
	// Matches cdx format: CDX N b a m s k r V g
	public final static String[] CDX_09_NAMES = {urlkey, 
												 timestamp, 
												 original, 
												 mimetype, 
												 statuscode, 
												 digest,
												 redirect, 
												 offset,
												 filename};

	public CDXLine(String line) {
		super(line, " ");
	}
	
	protected CDXLine(String line, String[] fields)
	{
		super(line, fields);
	}
	
	protected String[] selectNames(String[] fields)
	{
		if (fields.length == CDX_11_NAMES.length) {
			return CDX_11_NAMES;
		} else if (fields.length == CDX_09_NAMES.length) {
			return CDX_09_NAMES;		
		} else {
			return null;
		}
	}
}
