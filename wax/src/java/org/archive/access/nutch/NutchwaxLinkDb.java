package org.archive.access.nutch;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.nutch.crawl.Inlink;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.crawl.LinkDb;
import org.apache.nutch.crawl.LinkDbFilter;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.util.NutchJob;

/**
 * Subclass of nutch indexer that writes out LinkDb keys that include the
 * collection name.
 * Bulk of code is a copy and paste from LinkDb. LinkDb is not amenable to
 * subclassing.
 * @author stack
 */
public class NutchwaxLinkDb extends LinkDb {
    private int nwMaxAnchorLength;
    private boolean nwIgnoreInternalLinks;
    private URLFilters nwUrlFilters;
    private URLNormalizers nwUrlNormalizers;

    
    public NutchwaxLinkDb() {
        super(null);
    }

    /** Construct an LinkDb. */
    public NutchwaxLinkDb(Configuration conf) {
        super(conf);
    }
    
    public void configure(JobConf job) {
        super.configure(job);
        // These config. are private in parent class.  Make copy here in this
        // class with a 'nw' prefix.  St.Ack.
        this.nwMaxAnchorLength = job.getInt("db.max.anchor.length", 100);
        this.nwIgnoreInternalLinks =
            job.getBoolean("db.ignore.internal.links", true);
        if (job.getBoolean(LinkDbFilter.URL_FILTERING, false)) {
            this.nwUrlFilters = new URLFilters(job);
        }
        if (job.getBoolean(LinkDbFilter.URL_NORMALIZING, false)) {
            this.nwUrlNormalizers =
                new URLNormalizers(job, URLNormalizers.SCOPE_LINKDB);
        }
    }

    public void map(WritableComparable key, Writable value,
            OutputCollector output, Reporter reporter)
    throws IOException {
        String collection = Nutchwax.getCollectionFromWaxKey(key);
        if (collection == null) {
            LOG.info("Collection is null in key -- skipping " + key);
        }

        String fromUrl = Nutchwax.getUrlFromWaxKey(key);
        String fromHost = getHost(fromUrl);

        if (this.nwUrlNormalizers != null) {
            try {
                fromUrl = this.nwUrlNormalizers.
                    normalize(fromUrl, URLNormalizers.SCOPE_LINKDB);
            } catch (Exception e) {
                LOG.warn("Skipping " + fromUrl + ":" + e);
                fromUrl = null;
            }
        }
        if (fromUrl != null && this.nwUrlFilters != null) {
            try {
                fromUrl = this.nwUrlFilters.filter(fromUrl);
            } catch (Exception e) {
                LOG.warn("Skipping " + fromUrl + ":" + e);
                fromUrl = null;
            }
        }
        if (fromUrl == null) return; // discard all outlinks


        ParseData parseData = (ParseData)value;
        Outlink[] outlinks = parseData.getOutlinks();
        Inlinks inlinks = new Inlinks();
        for (int i = 0; i < outlinks.length; i++) {
            Outlink outlink = outlinks[i];
            String toUrl = outlink.getToUrl();
            if (this.nwIgnoreInternalLinks) {
                String toHost = getHost(toUrl);
                if (toHost == null || toHost.equals(fromHost)) { // internal link
                    continue;                               // skip it
                }
            }

            if (this.nwUrlNormalizers != null) {
                try {
                    toUrl = this.nwUrlNormalizers.
                        normalize(toUrl, URLNormalizers.SCOPE_LINKDB);
                } catch (Exception e) {
                    LOG.warn("Skipping " + toUrl + ":" + e);
                    toUrl = null;
                }
            }
            if (toUrl != null && this.nwUrlFilters != null) {
                try {
                toUrl = this.nwUrlFilters.filter(toUrl); // filter the url
                } catch (Exception e) {
                    LOG.warn("Skipping " + toUrl + ":" + e);
                    toUrl = null;
                }
            }
            if (toUrl == null) continue;

            inlinks.clear();
            String anchor = outlink.getAnchor();        // truncate long anchors
            if (anchor.length() > this.nwMaxAnchorLength) {
                anchor = anchor.substring(0, this.nwMaxAnchorLength);
            }
            inlinks.add(new Inlink(fromUrl, anchor));   // collect inverted link
            output.collect(new Text(Nutchwax.generateWaxKey(toUrl, collection)),
                inlinks);
        }
    }

    private String getHost(String url) {
        try {
            return new URL(url).getHost().toLowerCase();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public void invert(Path linkDb, final Path[] segments,
            final boolean normalize, final boolean filter)
    throws IOException {
        if (LOG.isInfoEnabled()) {
            LOG.info("NutchwaxLinkDb: starting");
            LOG.info("NutchwaxLinkDb: linkdb: " + linkDb);
            LOG.info("LinkDb: URL normalize: " + normalize);
            LOG.info("LinkDb: URL filter: " + filter);
        }
        JobConf job = createJob(getConf(), linkDb, normalize, filter);
        
        for (int i = 0; i < segments.length; i++) {
            if (LOG.isInfoEnabled()) {
                LOG.info("LinkDb: adding segment: " + segments[i]);
            }
            job.addInputPath(new Path(segments[i], ParseData.DIR_NAME));
        }
        JobClient.runJob(job);
        FileSystem fs = FileSystem.get(getConf());
        if (fs.exists(linkDb)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("LinkDb: merging with existing linkdb: " + linkDb);
            }
            // try to merge
            Path newLinkDb = job.getOutputPath();
            job = LinkDb.createMergeJob(getConf(), linkDb, normalize, filter);
            job.setJobName("NutchwaxLinkDb merge " + linkDb + " " +
                Arrays.asList(segments));
            job.setMapperClass(NutchwaxLinkDbFilter.class);
            job.addInputPath(new Path(linkDb, CURRENT_NAME));
            job.addInputPath(newLinkDb);
            JobClient.runJob(job);
            fs.delete(newLinkDb);
        }
        LinkDb.install(job, linkDb);
        if (LOG.isInfoEnabled()) { LOG.info("LinkDb: done"); }
    }

    /**
     * Copied from parent because method is private there (Its public in
     * crawldb). Additions are on end just before return.
     * @param config
     * @param linkDb
     * @param normalize
     * @param filter
     * @return A jobconf.
     */
    private static JobConf createJob(Configuration config, Path linkDb,
            final boolean normalize, final boolean filter) {
        Path newLinkDb =
            new Path("linkdb-" +
                Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));

        JobConf job = new NutchJob(config);
        job.setJobName("linkdb " + linkDb);

        job.setInputFormat(SequenceFileInputFormat.class);

        job.setMapperClass(LinkDb.class);
        // if we don't run the mergeJob, perform normalization/filtering now
        if (normalize || filter) {
            try {
                FileSystem fs = FileSystem.get(config);
                if (!fs.exists(linkDb)) {
                    job.setBoolean(LinkDbFilter.URL_FILTERING, filter);
                    job.setBoolean(LinkDbFilter.URL_NORMALIZING, normalize);
                }
            } catch (Exception e) {
                LOG.warn("LinkDb createJob: " + e);
            }
        }
        job.setReducerClass(LinkDb.class);

        job.setOutputPath(newLinkDb);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setBoolean("mapred.output.compress", true);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Inlinks.class);

        // Now do the NutchwaxLinkDb config. changing mapper -- we use LinkDb's
        // reducer -- and job name.
        job.setJobName("nutchwaxLinkdb " + linkDb);
        job.setMapperClass(NutchwaxLinkDb.class);
        
        return job;
    }
    
    public static void main(String[] args) throws Exception {
    	int res = new NutchwaxLinkDb().
    		doMain(NutchwaxConfiguration.getConfiguration(), args);
    	System.exit(res);
	}
}
