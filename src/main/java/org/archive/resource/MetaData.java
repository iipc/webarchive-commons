package org.archive.resource;

import java.util.logging.Logger;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import com.github.openjson.JSONTokener;

public class MetaData extends JSONObject {

	private static final Logger LOG = 
		Logger.getLogger(MetaData.class.getName());

	private MetaData topMetaData;

	public MetaData(MetaData parentMetaData, String name) {
		if(parentMetaData == null) {
			this.topMetaData = this;
		} else {
			topMetaData = parentMetaData.topMetaData;
		}
		parentMetaData.putChild(name, this);
	}

	public MetaData(String jsonString) throws JSONException {
		super(jsonString);
		this.topMetaData = this;
	}
	public MetaData(JSONTokener jsonTokener) throws JSONException {
		super(jsonTokener);
		this.topMetaData = this;
	}

	public MetaData() {
		this.topMetaData = this;
	}


	@Override
	public Object get(String key) {
		try {
			return super.get(key);
		} catch(JSONException e) {
			LOG.severe(e.getMessage());
			return null;
		}
	}

	@Override
	public boolean getBoolean(String key) {
		try {
			return super.getBoolean(key);
		} catch(JSONException e) {
			LOG.severe(e.getMessage());
			return false;
		}
	}

	@Override
	public int getInt(String key) {
		try {
			return super.getInt(key);
		} catch(JSONException e) {
			LOG.severe(e.getMessage());
			return -1;
		}
	}

	@Override
	public long getLong(String key) {
		try {
			return super.getLong(key);
		} catch(JSONException e) {
			LOG.severe(e.getMessage());
			return -1;
		}
	}

	@Override
	public String getString(String key) {
		try {
			return super.getString(key);
		} catch(JSONException e) {
			LOG.severe(e.getMessage());
			return null;
		}
	}

	public MetaData createChild(String name) {
		return new MetaData(this,name);
	}

	public MetaData getTopMetaData() {
		if(topMetaData == null) {
			return this;
		}
		return topMetaData;
	}

	public void setTopMetaData(MetaData topMetaData) {
		this.topMetaData = topMetaData;
	}

	public JSONObject putString(String key, String val) {
		try {
			return super.put(key,val);
		} catch(JSONException e) {
			LOG.severe(e.getMessage());
			return null;
		}
	}

	public JSONObject putLong(String key, long val) {
		try {
			return super.put(key,String.valueOf(val));
		} catch(JSONException e) {
			LOG.severe(e.getMessage());
			return null;
		}
	}

	public JSONObject putBoolean(String key, boolean val) {
		try {
			return super.put(key,val);
		} catch(JSONException e) {
			LOG.severe(e.getMessage());
			return null;
		}
	}

	public JSONObject putChild(String key, JSONObject child) {
		try {
			return super.put(key,child);
		} catch(JSONException e) {
			LOG.severe(e.getMessage());
			return null;
		}
	}

	public JSONObject appendChild(String key, JSONObject child) {
		try {
			JSONArray jarr = optJSONArray(key);
			if(jarr == null) {
				Object ob = remove(key);
				if(ob != null) {
					LOG.warning("Removed(" + key +") containing:" + ob.toString());
				}
				jarr = new JSONArray();
				jarr.put(child);
				put(key,jarr);
			} else {
				jarr.put(child);
			}
			return this;
		} catch(JSONException e) {
			LOG.severe(e.getMessage());
			return null;
		}
	}

	public void appendObj(String key, String... a) {
		JSONObject n = new JSONObject();
		if((a.length & 1) == 1) {
			throw new IllegalArgumentException();
		}
		try {
			
			for(int i = 0; i < a.length; i+=2) {
				n.put(a[i], a[i+1]);
			}
			appendChild(key,n);

		} catch(JSONException e) {
			LOG.severe(e.getMessage());
		}
	}
}
