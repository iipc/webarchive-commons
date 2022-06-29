package org.archive.format.json;

import java.util.ArrayList;
import java.util.List;

import com.github.openjson.JSONObject;

public class CompoundORJSONPathSpec implements JSONPathSpec {
	ArrayList<JSONPathSpec> parts;
	public CompoundORJSONPathSpec(List<JSONPathSpec> parts) {
		this.parts = new ArrayList<JSONPathSpec>();
		for(JSONPathSpec part : parts) {
			this.parts.add(part);
		}
	}

	public List<List<String>> extract(JSONObject json) {
		List<List<String>> matches;
		for(JSONPathSpec spec : parts) {
			matches = spec.extract(json);
			// check if empty:
			if(matches.size() == 1) {
				if(matches.get(0).size() == 1) {
					if(matches.get(0).get(0).length() > 0) {
						return matches;
					}
				}
			}
//			if(matches.size() > 0) {
//				if(matches.get(0).size() > 0) {
//					return matches;
//				}
//			}
		}
		return null;
	}

}
