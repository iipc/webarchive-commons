package org.archive.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.archive.format.json.JSONView;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

public class ArchiveJSONViewLoader extends ArchiveMetadataLoader {
	private final static Logger LOG = 
		Logger.getLogger(ArchiveJSONViewLoader.class.getName());

	protected TupleFactory mCacheTupleFactory = TupleFactory.getInstance();
	private ArrayList<Object> mCacheProtoTuple = null;
	private JSONView view;
//	private static final List<String> EMPTY;
//	static {
//		EMPTY = new ArrayList<String>();
//		EMPTY.add("");
//	}
//	ArrayList<String> fields;
	ArrayList<Tuple> cached;

	public ArchiveJSONViewLoader(String... fieldArgs) {
		super();
		// TODO: fix this logging...
	    Logger.getLogger("org.archive").setLevel(Level.WARNING);
		mCacheProtoTuple = new ArrayList<Object>();
		cached = null;
		if(fieldArgs.length == 0) {
			LOG.info("Constructed with NO foo");
			throw new RuntimeException("No field definition");
		} else {
			if(LOG.isLoggable(Level.INFO)) {
				LOG.info("ArchiveJSONViewLoader:(" +
						StringUtils.join(fieldArgs,",") +
						")");
			}
			view = new JSONView(fieldArgs);
		}
	}

	@Override
	public Tuple getNext() throws IOException {
		if(cached == null) {
			// try to load some more:
			Tuple inner = super.getNext();
			if(inner != null) {
				cached = applyView(inner);
			}
		}
		if(cached != null) {
			Tuple n = cached.remove(0);
			if(cached.size() == 0) {
				cached = null;
			}
			return n;
		}
		// all done
		return null;
	}

	private ArrayList<Tuple> applyView(Tuple inner) {
		// [0] is the JSON. Remaining elements are Strings describing paths
		// into the JSON to "flatten" into a single tuple:
		if(inner == null || inner.size() == 0) {
			return null;
		}
		try {
			JSONObject json = new JSONObject(inner.get(2).toString());
			List<List<String>> matches = view.apply(json);
			if(matches.size() == 0) {
				return null;
			}
			ArrayList<Tuple> results = new ArrayList<Tuple>();
			for(List<String> t : matches) {
				mCacheProtoTuple.addAll(t);
				Tuple tup = mCacheTupleFactory.newTuple(mCacheProtoTuple);
				mCacheProtoTuple.clear();
				results.add(tup);
			}
			return results;
		} catch (JSONException e) {
			LOG.warning("Failed to parse JSON:"+e.getMessage());
		} catch (ExecException e) {
			LOG.warning("ExecException:"+e.getMessage());
		}
		
		return null;
	}

}
