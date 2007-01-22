package org.archive.access.nutch.searcher;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.searcher.RawFieldQueryFilter;

/**
 * Look for explicit match of passed URL.
 * @author St.Ack
 */
public class WaxExacturlQueryFilter extends RawFieldQueryFilter {
    private Configuration conf;
    
    public WaxExacturlQueryFilter() {
        super("exacturl", false, 0.1f);
    }

    public Configuration getConf() {
        return this.conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }
}
