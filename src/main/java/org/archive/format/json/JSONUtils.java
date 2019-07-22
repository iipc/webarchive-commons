package org.archive.format.json;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

public class JSONUtils {
	private static final Logger LOG =
		Logger.getLogger(JSONUtils.class.getName());
	
	private static JSONObject getChild(JSONObject m, String child) {
		try {
			if(m.has(child)) {
				return m.getJSONObject(child);
			}
		} catch (JSONException e) {
			LOG.warning(e.getMessage());
		}
		return null;
	}
	public static JSONObject extractObject(JSONObject json, String path) {
		String parts[] = path.split("\\.");
		JSONObject cur = json;
		for(int i = 0; i < parts.length; i++) {
			cur = getChild(cur,parts[i]);
			if(cur == null) {
				break;
			}
		}
		return cur;
	}

	public static JSONArray extractArray(JSONObject json, String path) {
		String parts[] = path.split("\\.");
		JSONObject cur = json;
		for(int i = 0; i < parts.length - 1; i++) {
			cur = getChild(cur,parts[i]);
			if(cur == null) {
				break;
			}
		}
		if(cur != null) {
			return cur.optJSONArray(parts[parts.length-1]);
		}
		return null;
	}

	public static String extractSingle(JSONObject json, String path) {
		String result = null;
		String parts[] = path.split("\\.");
		JSONObject cur = json;
		for(int i = 0; i < parts.length-1; i++) {
			cur = getChild(cur,parts[i]);
			if(cur == null) {
				break;
			}
		}
		if(cur != null) {
			String last = parts[parts.length-1];
			if(cur.has(last)) {
				try {
					result = cur.get(last).toString();
				} catch (JSONException e) {
					LOG.warning(e.getMessage());
				}
			}
		}
		
		return result;
	}
	public static List<String> extractFancy(JSONObject json, String path) {
		ArrayList<String> matches = new ArrayList<String>();
		String parts[] = path.split("\\.");
		try {
			extractRecursive(json,parts,0,matches);
		} catch (JSONException e) {
			LOG.warning(e.getMessage());
		}

		return matches;
	}
	private static void extractRecursive(JSONObject json, String path[], int idx, List<String> matches) throws JSONException {
		if(json == null) {
			return;
		}
		String part = path[idx];
		if(idx == path.length - 1) {
			// at the end. apply:
			if(json.has(part)) {
				matches.add(json.get(part).toString());
			}
		} else {
			if(part.startsWith("@")) {
				part = part.substring(1);
				// looped recurse for each array element:
				if(json.has(part)) {
					JSONArray a = json.getJSONArray(part);
					for(int i = 0; i < a.length(); i++) {
						extractRecursive(a.getJSONObject(i),path,idx+1,matches);
					}
				}
			} else {
				if(part.startsWith("{") && part.endsWith("}")) {
					
				}
				if(json.has(part)) {
					// recurse
					extractRecursive(json.getJSONObject(part),path,idx+1,matches);
				}
			}
		}
	}
}
