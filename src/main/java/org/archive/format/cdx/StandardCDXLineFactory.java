package org.archive.format.cdx;

/**
 * Supports creating CDXLine for certain defined standard CDX formats
 * Current options: a 9-field cdx format (default) and 11 field cdx format
 * 
 * @author ilya
 *
 */
public class StandardCDXLineFactory implements CDXLineFactory, CDXFieldConstants {

	// NEW 11-field format
	// Matches cdx format: CDX N b a m s k r M S V g
	public final static FieldSplitFormat cdx11 = new FieldSplitFormat(urlkey, timestamp, original, mimetype, statuscode, digest, redirect, robotflags, length, offset, filename);
	
	// Matches cdx format: CDX N b a m s k r V g
	public final static FieldSplitFormat cdx09 = new FieldSplitFormat(timestamp, original, mimetype, statuscode, digest, redirect, offset, filename);

	protected final FieldSplitFormat parseFormat;
	
	public StandardCDXLineFactory(String formatName)
	{
		if (formatName == null) {
			parseFormat = cdx09;
		} else if (formatName.equals("cdx11")) {
			parseFormat = cdx11;
		} else {
			parseFormat = cdx09;
		}
	}
	
	protected StandardCDXLineFactory(FieldSplitFormat parseFormat)
	{
		this.parseFormat = parseFormat;
	}
	
	public FieldSplitFormat getParseFormat()
	{
		return parseFormat;
	}
}
