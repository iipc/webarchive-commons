package org.archive.hadoop.pig;

import org.apache.hadoop.conf.Configuration;

public abstract class FirstPigJobOnlyFilter extends DisablablePathFilter {

	@Override
	protected boolean determineIfEnabled(Configuration conf) {
		
		// If first job, then parentId is not set
		String parentId = conf.get("pig.parent.jobid");
		
		if (parentId == null) {
			return true;
		} else {
			//System.out.println("FILTER DISABLED");
			return false;
		}
	}
}
