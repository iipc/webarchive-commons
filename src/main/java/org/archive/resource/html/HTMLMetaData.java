package org.archive.resource.html;

import java.util.List;
import java.util.logging.Logger;

import org.archive.resource.MetaData;
import org.archive.resource.ResourceConstants;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

public class HTMLMetaData extends MetaData implements ResourceConstants {

	private final static Logger LOG = 
		Logger.getLogger(HTMLMetaData.class.getName()); 

	private JSONObject header;

	public HTMLMetaData(MetaData parentMetaData) {
		super(parentMetaData,HTML_METADATA);
	}

	private JSONObject getHeader() {
		if(header == null) {
			header = new JSONObject();
			putChild(HTML_HEAD, header);
		}
		return header;
	}

	public void setBaseHref(String href) {
		putUnlessNull(getHeader(),HTML_BASE, href);
	}
	public void setTitle(String title) {
		putUnlessNull(getHeader(),HTML_TITLE, title);
	}
	private void putUnlessNull(JSONObject o, String k, String v) {
		if(o != null) {
			try {
				o.put(k, v);
			} catch(JSONException e) {
				LOG.warning(e.getMessage());
			}
		}
	}
	public String[] LtoA(List<String> l) {
		String[] a = new String[l.size()];
		l.toArray(a);
		return a;
	}

	public void addMeta(List<String> l) { addMeta(LtoA(l)); }
	public void addMeta(String...a) {
		appendObj2(getHeader(),HTML_META_TAGS,a);
	}

	public void addLink(List<String> l) { addLink(LtoA(l)); }
	public void addLink(String...a) {
		appendObj2(getHeader(),HTML_LINK_TAGS,a);
	}

	public void addScript(List<String> l) { addScript(LtoA(l)); }
	public void addScript(String...a) {
		appendObj2(getHeader(),HTML_SCRIPT_TAGS,a);
	}

	public void addHref(List<String> l) { addHref(LtoA(l)); }
	public void addHref(String...a) {
		appendObj2(this,HTML_LINKS,a);
	}

	private void appendObj2(JSONObject o, String arr, String... a) {
		if(o == null) {
			return;
		}
		JSONObject n = new JSONObject();
		if((a.length & 1) == 1) {
			throw new IllegalArgumentException();
		}
		try {
			
			for(int i = 0; i < a.length; i+=2) {
				n.put(a[i], a[i+1]);
			}
			JSONArray jarr = o.optJSONArray(arr);
			if(jarr == null) {
				Object ob = o.remove(arr);
				if(ob != null) {
					LOG.warning("Removed(" + arr +") containing:" + ob.toString());
				}
				jarr = new JSONArray();
				jarr.put(n);
				o.put(arr, jarr);
			} else {
				jarr.put(n);
			}


		} catch(JSONException e) {
			try {
				System.err.format("GotErr(%s) JSON(%s)(%s)", e.getMessage(),
						o.toString(1),a.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				System.err.println("JSONERRinJSONErr:"+e1.getMessage());
				e1.printStackTrace();
			}
			LOG.warning(e.getMessage());
		}

	}

	public String getJSONString() {
		return toString();
	}
}
