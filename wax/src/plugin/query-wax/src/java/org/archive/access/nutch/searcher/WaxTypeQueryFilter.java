package org.archive.access.nutch.searcher;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.searcher.RawFieldQueryFilter;

/**
 * Type query filter.
 * @author St.Ack
 */
public class WaxTypeQueryFilter extends RawFieldQueryFilter {
    private Configuration conf;
    
    public WaxTypeQueryFilter() {
        super("type", true, 0.1f);
    }

    public Configuration getConf() {
        return this.conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }
}
