package org.archive.format.json;

import java.util.List;

import org.json.JSONObject;

public interface JSONPathSpec {
	public static final String EMPTY = "";
	public List<List<String>> extract(JSONObject json);
}
