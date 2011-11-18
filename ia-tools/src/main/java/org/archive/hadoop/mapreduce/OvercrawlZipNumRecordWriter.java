package org.archive.hadoop.mapreduce;

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.io.Text;

public class OvercrawlZipNumRecordWriter extends ZipNumRecordWriter {
	private int dayLimit = -1;
	private int curDayCount = 0;
	private String lastDay = null;
	public OvercrawlZipNumRecordWriter(int limit, int dayLimit,
			DataOutputStream outMain, DataOutputStream outSummary) {
		super(limit, outMain, outSummary);
		this.dayLimit = dayLimit;
		lastDay = null;
		curDayCount = 0;
	}

	public void write(Text key, Text val) throws IOException,
			InterruptedException {
		String urlPlusDate = key.toString();
		int spaceIdx = urlPlusDate.indexOf(delim);
		if(spaceIdx > 0) {
			if(spaceIdx + 8 < urlPlusDate.length()) {
				String tmp = urlPlusDate.substring(0,spaceIdx + 8);
				boolean filter = false;
				if(lastDay == null) {
					lastDay = tmp;
					curDayCount = 1;
				} else {
					if(lastDay.compareTo(tmp) == 0) {
						// same day.. how many so far?
						if(curDayCount > dayLimit) {
							filter = true;
						}
					} else {
						// a new day..
						lastDay = tmp;
						curDayCount = 0;
					}
				}
				if(!filter) {
					super.write(key, val);
				}
			}
		}
	}

}
