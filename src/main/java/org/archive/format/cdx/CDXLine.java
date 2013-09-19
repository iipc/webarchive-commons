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
		return super.getField(CDXLine.urlkey);
	}

	public String getTimestamp() {
		return super.getField(CDXLine.timestamp);
	}

	public String getOriginalUrl() {
		return super.getField(CDXLine.original);
	}

	public String getMimeType() {
		return super.getField(CDXLine.mimetype);
	}
	
	public void setMimeType(String newMime) {
		setField(CDXLine.mimetype, newMime);
	}

	public String getStatusCode() {
		return super.getField(CDXLine.statuscode);
	}
	
	public void setStatusCode(String newStatus) {
		setField(CDXLine.statuscode, newStatus);
	}

	public String getDigest() {
		return super.getField(CDXLine.digest);
	}

	public String getLength() {
		return super.getField(CDXLine.length);
	}

	public String getOffset() {
		return super.getField(CDXLine.offset);
	}

	public String getFilename() {
		return super.getField(CDXLine.filename);
	}
	
	public String getRedirect() {
		return super.getField(CDXLine.redirect);
	}
	
	public String getRobotFlags() {
		return super.getField(CDXLine.robotflags);
	}
}
