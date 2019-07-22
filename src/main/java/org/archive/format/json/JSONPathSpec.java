package org.archive.format.json;

import java.util.List;

import com.github.openjson.JSONObject;

public interface JSONPathSpec {
	public static final String EMPTY = "";
	public List<List<String>> extract(JSONObject json);
}
