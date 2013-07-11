package org.archive.format.cdx;

/**
 * Base class for text lines that are split by a delimiter
 * Some examples will be cdx lines, summary lines, etc...
 * @author ilya
 *
 */
public class FieldSplitLine {
	
	final public String names[];
	final public String fullLine;
	final public String fields[];
	
	protected FieldSplitLine(String line, String[] fields)
	{
		this.fullLine = line;
		this.fields = fields;
		this.names = selectNames(this.fields);
	}
	
	public FieldSplitLine(String line)
	{
		this(line, "\t");
	}
		
	public FieldSplitLine(String line, String splitstr)
	{
		this.fullLine = line;
		if (line != null) {
			this.fields = line.split(splitstr);
		} else {
			this.fields = null;
		}
		this.names = selectNames(this.fields);
	}
	
	/**
	 * Select the names
	 * 
	 * @param fields
	 * @return
	 */
	protected String[] selectNames(String[] fields)
	{
		return null;
	}
	
	/**
	 * Return field index for given name
	 * 
	 * @param name
	 * @return
	 */
	public int getFieldIndex(String name)
	{
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(name)) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Return field for given name, or null if not found
	 * 
	 * @param name
	 * @return
	 */
	public String getField(String name)
	{
		int index = getFieldIndex(name);
		return ((index >= 0) && (index < fields.length) ? this.fields[index] : null);
	}
	
	public String getNamesAsJson()
	{
		if (names == null || names.length == 0) {
			return "[]";
		}
		
        StringBuilder b = new StringBuilder();
        
        b.append('[');
        
        for (int i = 0; i < names.length; i++) {
        	if (i > 0) {
        		b.append(',');
        	}
        	b.append('\"');
            b.append(names[i]);
        	b.append('\"');
        }
        
        b.append(']');
        return b.toString();
	}
	
	@Override
	public String toString()
	{
		return fullLine;
	}
}
