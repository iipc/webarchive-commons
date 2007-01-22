package org.archive.access.nutch.searcher;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.searcher.RawFieldQueryFilter;

/**
 * @author St.Ack
 */
public class WaxCollectionQueryFilter extends RawFieldQueryFilter {
    private Configuration conf;
    
    public WaxCollectionQueryFilter() {
        super("collection", true);
    }

    public Configuration getConf() {
        return this.conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }
}
