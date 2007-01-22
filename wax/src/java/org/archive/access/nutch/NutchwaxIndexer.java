package org.archive.access.nutch;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.crawl.LinkDb;
import org.apache.nutch.indexer.Indexer;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.util.NutchJob;

/**
 * Subclass of nutch Indexer that handles keys that are not just URLs.
 * @author stack
 */
public class NutchwaxIndexer extends Indexer {
    public final Log LOG = LogFactory.getLog(this.getClass().getName());
    
    public NutchwaxIndexer() {
        this(null);
    }

    public NutchwaxIndexer(final Configuration c) {
        super(c);
    }

    public void reduce(final WritableComparable key, Iterator values,
        final OutputCollector output, Reporter reporter)
    throws IOException {
        OutputCollector oc = new OutputCollector() {
            public void collect(WritableComparable k, Writable v)
            throws IOException {
                // Substitute original key in place of passed k.  The original
                // is compound key of URL+Collection.  Notice below how we pass
                // just the url to the reduce.  Here we are restoring the
                // original key.
                output.collect(key, v);
            }
            
        };
        // Call parent method but with just url for key and with our collector
        // so we can restore the original key when the reduce goes to write the
        // produce.
        super.reduce(new Text(Nutchwax.getUrlFromWaxKey(key)), values, oc,
            reporter);
    }
    
    public void index(Path indexDir, Path crawlDb, Path linkDb, Path[] segments)
            throws IOException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Indexer: starting");
            LOG.info("Indexer: linkdb: " + linkDb);
        }

        JobConf job = new NutchJob(getConf());
        job.setJobName("index " + indexDir);

        for (int i = 0; i < segments.length; i++) {
            if (LOG.isInfoEnabled()) {
                LOG.info("adding segment: " + segments[i]);
            }
            job.addInputPath(new Path(segments[i], CrawlDatum.FETCH_DIR_NAME));
            job.addInputPath(new Path(segments[i], ParseData.DIR_NAME));
            job.addInputPath(new Path(segments[i], ParseText.DIR_NAME));
        }

        job.addInputPath(new Path(crawlDb, CrawlDb.CURRENT_NAME));
        job.addInputPath(new Path(linkDb, LinkDb.CURRENT_NAME));

        job.setInputFormat(InputFormat.class);
        // job.setInputKeyClass(Text.class);
        // job.setInputValueClass(ObjectWritable.class);

        // job.setCombinerClass(Indexer.class);

        // Only difference from parent class implementation of this method.
        job.setReducerClass(NutchwaxIndexer.class);

        job.setOutputPath(indexDir);
        job.setOutputFormat(OutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(ObjectWritable.class);

        JobClient.runJob(job);
        if (LOG.isInfoEnabled()) {
            LOG.info("Indexer: done");
        }
    }

    public static void main(String[] args) throws Exception {
        int res = new NutchwaxIndexer().
            doMain(NutchwaxConfiguration.getConfiguration(), args);
        System.exit(res);
    }
}
