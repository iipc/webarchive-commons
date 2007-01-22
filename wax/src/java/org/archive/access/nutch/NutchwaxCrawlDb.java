/* $Id$
 * 
 * Created on December 18, 2006
 *
 * Copyright (C) 2006 Internet Archive.
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

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.util.LockUtil;
import org.apache.nutch.util.NutchConfiguration;


/**
 * Adds setting of the NutchwaxCrawlDbFilter.
 * @author stack
 */
public class NutchwaxCrawlDb extends CrawlDb {
    public static final Log LOG = LogFactory.getLog(NutchwaxCrawlDb.class);
    
    public NutchwaxCrawlDb() {
        super();
    }
    
    public NutchwaxCrawlDb(Configuration conf) {
        super(conf);
    }
    
    public void update(Path crawlDb, Path [] segments, boolean normalize,
            boolean filter, boolean additionsAllowed, boolean force)
    throws IOException {
        FileSystem fs = FileSystem.get(getConf());
        Path lock = new Path(crawlDb, LOCK_NAME);
        LockUtil.createLockFile(fs, lock, force);
        if (LOG.isInfoEnabled()) {
            LOG.info("NutchwaxCrawlDb update: starting");
            LOG.info("NutchwaxCrawlDb update: db: " + crawlDb);
            LOG.info("NutchwaxCrawlDb update: segment: " +
                Arrays.asList(segments));
            LOG.info("NutchwaxCrawlDb update: additions allowed: " +
                additionsAllowed);
            LOG.info("NutchwaxCrawlDb update: URL normalizing: " + normalize);
            LOG.info("NutchwaxCrawlDb update: URL filtering: " + filter);
        }

        JobConf job = CrawlDb.createJob(getConf(), crawlDb);

        // Now, change the map and reduce to run.  Use ours instead.
        job.setMapperClass(NutchwaxCrawlDbFilter.class);
        // Use nutch native reducer.  It passes the key via the scoring
        // plugins but as currently implemented, they don't expect the key to
        // be an URL.
        // job.setReducerClass(CrawlDbReducer.class);
        job.setJobName("nutchwaxcrawldb " + crawlDb + " " +
            Arrays.asList(segments));
        
        
        job.setBoolean(CRAWLDB_ADDITIONS_ALLOWED, additionsAllowed);
        job.setBoolean(NutchwaxCrawlDbFilter.URL_FILTERING, filter);
        job.setBoolean(NutchwaxCrawlDbFilter.URL_NORMALIZING, normalize);
        
        for (int i = 0; i < segments.length; i++) {
            Path fetch = new Path(segments[i], CrawlDatum.FETCH_DIR_NAME);
            Path parse = new Path(segments[i], CrawlDatum.PARSE_DIR_NAME);
            if (fs.exists(fetch) && fs.exists(parse)) {
                job.addInputPath(fetch);
                job.addInputPath(parse);
            } else {
                LOG.info(" - skipping invalid segment " + segments[i]);
            }
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("NutchwaxCrawlDb update: Merging segment data " +
                Arrays.asList(segments) + " into db.");
        }
        try {
            JobClient.runJob(job);
        } catch (IOException e) {
            LockUtil.removeLockFile(fs, lock);
            if (fs.exists(job.getOutputPath()))
                fs.delete(job.getOutputPath());
            throw e;
        }

        NutchwaxCrawlDb.install(job, crawlDb);
        if (LOG.isInfoEnabled()) {
            LOG.info("NutchwaxCrawlDb update: done");
        }
    }

    public static void main(String[] args) throws Exception {
        int res = new NutchwaxCrawlDb().doMain(NutchConfiguration.create(),
            args);
        System.exit(res);
    }
}