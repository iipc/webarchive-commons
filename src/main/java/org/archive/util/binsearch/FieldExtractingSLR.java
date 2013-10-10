package org.archive.util.binsearch;

import java.io.IOException;

/**
 * Special SLR wrapper (SeekableLineReader) that extracts a certain field
 * from the reader and only returns that field
 * @author ilya
 *
 */

public class FieldExtractingSLR extends WrappedSeekableLineReader {
	protected String sep;
	protected int fieldIndex;
	
	public FieldExtractingSLR(SeekableLineReader slr, int fieldIndex, String sep) {
		super(slr);
		this.fieldIndex = fieldIndex;
		this.sep = sep;
	}

	@Override
	public String readLine() throws IOException {
		String line = super.readLine();
		String[] fields = line.split(sep);
		return fields[fieldIndex];
	}
	
	@Override
	public void skipLine() throws IOException {
		super.readLine();
	}
}
