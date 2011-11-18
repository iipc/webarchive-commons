package org.archive.format.json;

import java.util.ArrayList;

public class JSONPathSpecFactory {
	public static JSONPathSpec get(String spec) {
		if(spec.contains("|")) {
			// compound OR:
			String parts[] = spec.split("\\|");
			ArrayList<JSONPathSpec> subs = new ArrayList<JSONPathSpec>(parts.length);
			for(String part : parts) {
				subs.add(new SimpleJSONPathSpec(part));
			}
			return new CompoundORJSONPathSpec(subs);
		} else {
			// assume "simple":
			return new SimpleJSONPathSpec(spec);
		}
	}
}
