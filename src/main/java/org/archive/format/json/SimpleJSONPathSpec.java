package org.archive.format.json;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

public class SimpleJSONPathSpec implements JSONPathSpec {
	private static final Logger LOG =
		Logger.getLogger(SimpleJSONPathSpec.class.getName());
	private String parts[];
	private List<String> emptyResult;
	
	private final static List<String> EMPTY_MATCH;
	
	static {
		EMPTY_MATCH = new ArrayList<String>();
		EMPTY_MATCH.add(EMPTY);
	}
	public SimpleJSONPathSpec(String pathSpec) {
		parts = pathSpec.split("\\.");
		emptyResult = calculateEmptyResult(parts[parts.length-1]);
	}

	private List<String> calculateEmptyResult(String last) {
		ArrayList<String> empty = new ArrayList<String>();
		if(last.startsWith("{") && last.endsWith("}")) {
			String inner = last.substring(1,last.length()-1);
			String subParts[] = inner.split(",");
			for(int i = 0; i < subParts.length; i++) {
				empty.add(EMPTY);
			}
		} else {
			empty.add(EMPTY);
		}
		return empty;
	}
	
	public List<List<String>> extract(JSONObject json) {
		ArrayList<List<String>> matches = new ArrayList<List<String>>();
		try {
			extractRecursive(json,parts,0,matches);
		} catch (JSONException e) {
			LOG.warning(e.getMessage());
		}

		return matches;
	}
	private void extractRecursive(JSONObject json, String path[], int idx, List<List<String>> matches) throws JSONException {
		if(json == null) {
			return;
		}
		String part = path[idx];
		if(idx == path.length - 1) {
			// at the end. apply:
			List<String> match = applyMatch(json, part);
//			if(match != null) {
			matches.add(match);
//			}
		} else {
			if(part.startsWith("@")) {
				part = part.substring(1);
				// looped recurse for each array element:
				if(json.has(part)) {
					JSONArray a = json.getJSONArray(part);
					for(int i = 0; i < a.length(); i++) {
						extractRecursive(a.getJSONObject(i),path,idx+1,matches);
					}
				} else {
					matches.add(emptyResult);
				}
			} else {
				if(json.has(part)) {
					// recurse
					extractRecursive(json.getJSONObject(part),path,idx+1,matches);
				} else {
					matches.add(emptyResult);
				}
			}
		}
	}

	private List<String> applyMatch(JSONObject json, String part) throws JSONException {
		ArrayList<String> match = new ArrayList<String>();
		if(part.startsWith("{") && part.endsWith("}")) {
			String inner = part.substring(1,part.length()-1);
			String subParts[] = inner.split(",");
			for(String subPart : subParts) {
				if(json.has(subPart)) {
					match.add(json.get(subPart).toString());
				} else {
					match.add(EMPTY);
				}
			}
			
		} else {
			if(json.has(part)) {
				match.add(json.get(part).toString());
			} else {
				return emptyResult;
			}
		}
		return match;
	}

}
