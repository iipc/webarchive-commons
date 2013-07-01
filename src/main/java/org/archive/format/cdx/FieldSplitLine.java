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
	
	public FieldSplitLine(String line)
	{
		this(line, "\t");
	}
	
	public FieldSplitLine(String line, String splitstr)
	{
		this(line, splitstr, null);
	}
	
	public FieldSplitLine(String line, String splitstr, String[] names)
	{
		this.fullLine = line;
		if (line != null) {
			this.fields = line.split(splitstr);
		} else {
			this.fields = null;
		}
		this.names = names;
	}
	
	/**
	 * Return field for given name
	 * Note: no bounds checking performed here
	 * 
	 * @param name
	 * @return
	 */
	public String getField(String name)
	{
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(name)) {
				return fields[i];
			}
		}
		
		return null;
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
