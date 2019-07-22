package org.archive.format.arc;

import java.util.logging.Logger;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

public class FiledescRecord {
	private static final Logger LOG = 
		Logger.getLogger(FiledescRecord.class.getName());
	private int majorVersion;
	private int minorVersion;
	private String organization;
	private String format;
	private JSONObject metadata;

	public boolean hasMetaData() {
		return metadata != null;
	}
	public int getMetaDataCount() {
		try {
			return metadata == null ? 0 : metadata.getJSONArray("MetaData").length();
		} catch (JSONException e) {
			LOG.warning(e.getMessage());
		}
		return 0;
	}
	public void addMetaData(String name, String value) {
		if(metadata == null) {
			metadata = new JSONObject();
		}
		try {
			JSONObject jo = new JSONObject();
			jo.put("name", name);
			jo.put("value", value);
			metadata.append("MetaData",jo);
		} catch(JSONException e) {
			LOG.warning(e.getMessage());			
		}
	}
	public String getMetaDataName(int i) {
		return getMetaDataField(i,"name");
	}
	public String getMetaDataValue(int i) {
		return getMetaDataField(i,"value");
	}

	public String getMetaDataField(int i, String field) {
		try {
			if(metadata != null) {
				JSONArray a = metadata.getJSONArray("MetaData");
				if(i < a.length()) {
					JSONObject jo = a.getJSONObject(i);
					if(jo != null) {
						return jo.getString(field);
					}
				}
			}
		} catch (JSONException e) {
			LOG.warning(e.getMessage());
		}
		return null;
		
	}
	public int getMajorVersion() {
		return majorVersion;
	}
	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}
	public int getMinorVersion() {
		return minorVersion;
	}
	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
}
