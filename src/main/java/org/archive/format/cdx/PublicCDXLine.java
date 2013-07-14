package org.archive.format.cdx;

import org.apache.commons.lang.StringUtils;

/**
 * A filtered cdx line that only shows the following 7 fields:
 * 
 * urlkey, timestamp, original, mimetype, statuscode, digest and length
 * 
 * @author ilya
 *
 */
public class PublicCDXLine extends CDXLine {
	
	// Public cdx line
	public final static String[] PUBLIC_CDX_FIELDS = 
												{urlkey, 
												 timestamp, 
												 original, 
												 mimetype, 
												 statuscode, 
												 digest,
												 length};
	
	@Override
	protected String[] selectNames(String[] fields) {
		return PUBLIC_CDX_FIELDS;
	}

	private PublicCDXLine(String fullLine, String[] fields)
	{
		super(fullLine, fields);
	}
	
	public static PublicCDXLine createCDXLine(String line)
	{
		String[] fields = line.split(" ");
		String length = (fields.length == 11) ? fields[8] : "-";
		String[] filteredFields = new String[]{fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], length};
				
		String fullLine = StringUtils.join(filteredFields, ' ');
		return new PublicCDXLine(fullLine, filteredFields);
	}
}
