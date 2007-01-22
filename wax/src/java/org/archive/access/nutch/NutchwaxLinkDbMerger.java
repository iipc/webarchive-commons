/* $Id$
 * 
 * Created on January 8, 2007
 *
 * Copyright (C) 2007 Internet Archive.
 * 
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 * 
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 * 
 * Heritrix is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.access.nutch;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.nutch.crawl.LinkDb;
import org.apache.nutch.crawl.LinkDbMerger;

/**
 * Wrapper around LinkDbMerger.
 * Calls NutchwaxLinkDb.createMergeJob instead of LinkDb.createMergeJob.
 * @author stack
 */
public class NutchwaxLinkDbMerger extends LinkDbMerger {
    public void merge(Path output, Path[] dbs, boolean normalize,
            boolean filter)
    throws Exception {
        JobConf job = NutchwaxLinkDb.createMergeJob(getConf(), output,
            normalize, filter);
        for (int i = 0; i < dbs.length; i++) {
          job.addInputPath(new Path(dbs[i], LinkDb.CURRENT_NAME));      
        }
        JobClient.runJob(job);
        FileSystem fs = FileSystem.get(getConf());
        fs.mkdirs(output);
        fs.rename(job.getOutputPath(), new Path(output, LinkDb.CURRENT_NAME));
      }

      public static void main(String[] args) throws Exception {
        int res = new NutchwaxLinkDbMerger().
            doMain(NutchwaxConfiguration.getConfiguration(), args);
        System.exit(res);
      }
}