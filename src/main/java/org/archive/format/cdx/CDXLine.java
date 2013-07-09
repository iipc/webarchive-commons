package org.archive.format.cdx;

public class CDXLine extends FieldSplitLine {
	
	 // Matches cdx format: CDX N b a m s k r M S V g
	public final static String[] CDX_11_NAMES = {"urlkey", 
												 "timestamp", 
												 "original", 
												 "mimetype", 
												 "statuscode", 
												 "digest",
												 "redirect",
												 "metaflags", 
												 "length", 
												 "offset", 
												 "filename"}; 
	
	// Old-style
	// Matches cdx format: CDX N b a m s k r V g
	public final static String[] CDX_09_NAMES = {"urlkey", 
												 "timestamp", 
												 "original", 
												 "mimetype", 
												 "statuscode", 
												 "digest",
												 "redirect", 
												 "offset",
												 "filename"};

	public CDXLine(String line) {
		super(line, " ");
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
