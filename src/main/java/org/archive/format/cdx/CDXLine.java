package org.archive.format.cdx;


public class CDXLine extends FieldSplitLine implements CDXFieldConstants {

	public CDXLine(String line, FieldSplitFormat names) {
		super(line, ' ', names);
	}
	
	public CDXLine(CDXLine line, FieldSplitFormat selectNames)
	{
		super(line.selectValues(selectNames), selectNames);
	}

	public String getUrlKey() {
		return super.getField(0);
	}

	public String getTimestamp() {
		return super.getField(1);
	}

	public String getOriginalUrl() {
		return super.getField(2);
	}

	public String getMimeType() {
		return super.getField(3);
	}
	
	public void setMimeType(String newMime) {
		setField(CDXLine.mimetype, newMime);
	}

	public String getStatusCode() {
		return super.getField(4);
	}

	public String getDigest() {
		return super.getField(5);
	}

	public String getLength() {
		return super.getField(CDXLine.length, "-");
	}

	protected String getOffset() {
		return super.getField(CDXLine.length, "-");
	}

	protected String getFilename() {
		return super.getField(CDXLine.filename, "-");
	}
}
