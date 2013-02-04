package org.archive.hadoop.pig.udf;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.archive.hadoop.mapreduce.CDXMapper;

public class toSURT extends EvalFunc<String> {
	
	CDXMapper converter;
	
	public toSURT()
	{
		converter = new CDXMapper();
		converter.setNoRedirect(true);
		converter.setSkipOnCanonFail(true);
	}

	@Override
	public String exec(Tuple tuple) throws IOException {
		
		if (tuple == null || tuple.isNull()) {
			return null;
		}
		
		if (tuple.size() == 1) {
			String line = (String)tuple.get(0);
			return converter.convertLine(line);
		} else if (tuple.size() == 2) {
			String key = (String)tuple.get(0);
			String value = (String)tuple.get(1);
			return converter.convertLine(key + " " + value);			
		} else {
			throw new IOException("CDX tuple must be length 1 or 2");
		}
	}
}
