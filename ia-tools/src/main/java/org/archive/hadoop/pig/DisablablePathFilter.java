package org.archive.hadoop.pig;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

public abstract class DisablablePathFilter implements Configurable, PathFilter {

	protected boolean enabled = true;
	
	public Configuration getConf() {
		return null;
	}

	public void setConf(Configuration conf) {
		
		if (conf == null) {
			return;
		}
		
		enabled = determineIfEnabled(conf);
		
		if (enabled) {
			setConfWhenEnabled(conf);
		}
	}
	
	protected abstract boolean determineIfEnabled(Configuration conf);
	
	protected abstract void setConfWhenEnabled(Configuration conf);	
	protected abstract boolean acceptWhenEnabled(Path path);
	

	public boolean accept(Path path) {
		if (!enabled) {
			return true;
		}
		
		return acceptWhenEnabled(path);
		
	}
}
