package org.archive.hadoop.func;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.archive.format.json.JSONUtils;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

public class JSONViewEvalFunc extends EvalFunc<Tuple> {
	private static final Logger LOG =
		Logger.getLogger(JSONViewEvalFunc.class.getName());

	protected TupleFactory mTupleFactory = TupleFactory.getInstance();
	private ArrayList<Object> mProtoTuple = null;

	public JSONViewEvalFunc() {
		mProtoTuple = new ArrayList<Object>();
	}
	
	@Override
	public Tuple exec(Tuple tup) throws IOException {
		// [0] is the JSON. Remaining elements are Strings describing paths
		// into the JSON to "flatten" into a single tuple:
		if(tup == null || tup.size() == 0) {
			return null;
		}
		try {
			JSONObject json = new JSONObject(tup.get(0).toString());
			for(int i = 1; i < tup.size(); i++) {
				String path = tup.get(i).toString();
				String result = JSONUtils.extractSingle(json, path);
				mProtoTuple.add(result);
			}
		} catch (JSONException e) {
			LOG.warning("Failed to parse JSON:"+e.getMessage());
			return null;
		}
		Tuple t = mTupleFactory.newTuple(mProtoTuple);
		mProtoTuple.clear();
		return t;
	}
}
