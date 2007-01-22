package org.archive.access.nutch.searcher;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.searcher.RawFieldQueryFilter;

/**
 * @author St.Ack
 */
public class WaxArcfileQueryFilter extends RawFieldQueryFilter {
    private Configuration conf;
    
    public WaxArcfileQueryFilter() {
        super("arcname", false, 1.0f);
    }

    public void setConf(Configuration c) {
        this.conf = c;
    }

    public Configuration getConf() {
        return this.conf;
    }
}
