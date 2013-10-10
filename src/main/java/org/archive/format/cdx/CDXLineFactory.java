package org.archive.format.cdx;

public interface CDXLineFactory {
	public FieldSplitFormat getParseFormat();
	public CDXLine createStandardCDXLine(String input, FieldSplitFormat exFormat);
}
