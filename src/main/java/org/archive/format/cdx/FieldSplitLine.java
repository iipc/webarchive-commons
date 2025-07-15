package org.archive.format.cdx;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Base class for text lines that are split by a delimiter Some examples will be
 * cdx lines, summary lines, etc...
 * 
 * @author ilya
 * 
 */
public class FieldSplitLine {

	protected FieldSplitFormat names;
	protected String fullLine;
	protected List<String> fields;
	
	public static final String EMPTY_VALUE = "-";

	protected FieldSplitLine(String line, List<String> fields, FieldSplitFormat names) {
		this.fullLine = line;
		this.fields = fields;
		this.names = names;
	}

	public FieldSplitLine(List<String> fields, FieldSplitFormat names) {
		this(null, fields, names);
	}

	public FieldSplitLine(String line, char splitchar, FieldSplitFormat names) {
		
		this.fullLine = line;
		this.names = names;
		
		if (line == null) {
			this.fields = null;
			return;
		}
		
		int initSize = (names != null ? names.getLength() : 0);
		
		// Ensure enough space for all the fields expected
		this.fields = new ArrayList<String>(initSize);
		
		int lastIndex = 0;
		int currIndex = line.indexOf(splitchar);
		
		do {
			currIndex = line.indexOf(splitchar, lastIndex);
			if (currIndex > 0) {
				this.fields.add(line.substring(lastIndex, currIndex));
			} else {
				this.fields.add(line.substring(lastIndex));
				break;
			}
			lastIndex = currIndex + 1;
		} while (lastIndex > 0);
		
		
		if (names != null) {
			while (fields.size() < initSize) {
				fields.add("-");
			}
		}
	}

	/**
	 * Return field index for given name
	 * 
	 * @param name
	 * @return
	 */
	public int getFieldIndex(String name) {
		if (names == null) {
			return -1;
		}

		return names.getFieldIndex(name);
	}

	public boolean isInRange(int index) {
		return ((index >= 0) && (index < fields.size()));
	}

	/**
	 * Return field for given name, or defaultVal if not found
	 * 
	 * @param name
	 * @return
	 */
	public String getField(String name, String defaultVal) {
		int index = getFieldIndex(name);
		return (isInRange(index) ? this.fields.get(index) : defaultVal);
	}
	
	public String getField(String name) {
		return getField(name, EMPTY_VALUE);
	}

	@Override
	public String toString() {
		if (fullLine == null && fields != null) {
			fullLine = StringUtils.join(fields, " ");
		}
		return fullLine;
	}

	public String getField(int index) {
		return fields.get(index);
	}
	
	public String setField(int index, String value) {
		return fields.set(index, value);
	}	

	public int getNumFields() {
		return fields.size();
	}

	public List<String> selectValues(FieldSplitFormat otherNames) {
		List<String> values = new ArrayList<String>(otherNames.getLength());

		for (int i = 0; i < otherNames.getLength(); i++) {
			int index = names.getFieldIndex(otherNames.getName(i));
			if (isInRange(index)) {
				values.add(fields.get(index));
			} else {
				values.add("-");
			}
		}

		return values;
	}

	public FieldSplitFormat getNames() {
		return names;
	}
	
	public void setField(String fieldName, String value) {
		int index = this.getFieldIndex(fieldName);
		if (this.isInRange(index)) {
			fields.set(index, value);
			fullLine = null;
		}
	}
}
