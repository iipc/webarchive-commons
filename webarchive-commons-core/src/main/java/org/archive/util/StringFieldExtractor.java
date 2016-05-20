package org.archive.util;

public class StringFieldExtractor {
	private char delim;
	private int field;
	public StringFieldExtractor(char delim, int field) {
		this.delim = delim;
		this.field = field;
	}

	public String extract(String text) {
		if(text == null) {
			return null;
		}
		int start = 0;
//		int end = text.length();
		for(int i = 0; i < field; i++) {
			if(start > text.length()) {
				return null;
			}
			int newStart = text.indexOf(delim, start);
			if(newStart == -1) {
				return null;
			}
			start = newStart + 1;
		}
		if(start == text.length()) {
			return "";
		}
		int end = text.indexOf(delim,start);
		if(end == -1) {
			return text.substring(start);
		} else {
			return text.substring(start,end);
		}
	}

	public StringTuple split(String s) {
		int keyEnd = 0;
		for (int i = 0; i < field; i++) {
			int dIdx = s.indexOf(delim, keyEnd);
			if (dIdx == -1) {
				return new StringTuple(s,null);
			}
			keyEnd = dIdx + 1;
		}
		return new StringTuple(s.substring(0, keyEnd - 1),s.substring(keyEnd));
	}
	
	public class StringTuple {
		public String first;
		public String second;
		public StringTuple(String first, String second) {
			this.first = first;
			this.second = second;
		}
	}

	/**
	 * @return the field
	 */
	public int getField() {
		return field;
	}

	/**
	 * @param field the field to set
	 */
	public void setField(int field) {
		this.field = field;
	}

	/**
	 * @return the delim
	 */
	public char getDelim() {
		return delim;
	}

	/**
	 * @param delim the delim to set
	 */
	public void setDelim(char delim) {
		this.delim = delim;
	}
	
}
