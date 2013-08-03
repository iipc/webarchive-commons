package org.archive.format.cdx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Specify a format for FieldSplitLine with names, and nameToIndex mapping for faster lookup
 * 
 * @author ilya
 *
 */
public class FieldSplitFormat {

	protected final List<String> names;
	protected final HashMap<String, Integer> nameToIndex;
	
	public FieldSplitFormat(String commaSepList)
	{
		this(commaSepList.split(","));
	}

	public FieldSplitFormat(String... names) {
		this(Arrays.asList(names));
	}
	
	public FieldSplitFormat(List<String> names)
	{
		this.names = names;
		this.nameToIndex = new HashMap<String, Integer>();

		for (int i = 0; i < names.size(); i++) {
			this.nameToIndex.put(names.get(i), i);
		}
	}
	
	public FieldSplitFormat addFieldNames(String... newNameArray)
	{
		List<String> newNames = new ArrayList<String>();
		newNames.addAll(this.names);
		for (String name : newNameArray) {
			newNames.add(name);
		}
		return new FieldSplitFormat(newNames);
	}
	
	public FieldSplitFormat createSubset(FieldSplitFormat input)
	{
		List<String> newFields = new ArrayList<String>();
		
		for (int i = 0; i < input.getLength(); i++) {
			String field = input.getName(i);
			if (this.nameToIndex.containsKey(field)) {
				newFields.add(field);
			}
		}
		
		return new FieldSplitFormat(newFields);
	}
	
	public FieldSplitFormat createSubset(String commaSepList)
	{
		String[] fields = commaSepList.split(",");
		List<String> newFields = new ArrayList<String>();
		
		for (String field : fields) {
			if (this.nameToIndex.containsKey(field)) {
				newFields.add(field);
			}
		}
		
		return new FieldSplitFormat(newFields);
	}

	/**
	 * Return field index for given name
	 * 
	 * @param name
	 * @return
	 */
	public int getFieldIndex(String name) {
		Integer val = this.nameToIndex.get(name);
		if (val == null) {
			return -1;
		}
		return val;
	}

	public int getLength() {
		return names.size();
	}

	public String getName(int i) {
		return names.get(i);
	}
}
