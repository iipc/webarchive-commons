package org.archive.hadoop.func;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

public class TupleFunc extends EvalFunc<Tuple> {
	
	protected TupleFactory mTupleFactory = TupleFactory.getInstance();
	private ArrayList<Object> mProtoTuple = null;
	
	public TupleFunc() {
		mProtoTuple = new ArrayList<Object>();
	}

	private Tuple makeTuple(String va[]) {
		if(va == null) {
			return null;
		}
		for(String v : va) {
			mProtoTuple.add(v);
		}
		Tuple t = mTupleFactory.newTuple(mProtoTuple);
		mProtoTuple.clear();
		return t;
	}

	@Override
	public Tuple exec(Tuple tup) throws IOException {
		if(tup == null || tup.size() != 2) {
			return null;
		}
		String in = tup.get(0).toString();
		String split = tup.get(1).toString();
		return makeTuple(in.split(split));
	}

}
