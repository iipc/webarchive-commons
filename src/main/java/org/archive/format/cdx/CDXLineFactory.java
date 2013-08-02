package org.archive.format.cdx;

public interface CDXLineFactory {
	public CDXLine createCdxLine(String line);
	
	CDXLine createCdxLine(String line, FieldSplitFormat format);
	
	public FieldSplitFormat getParseFormat();
}
