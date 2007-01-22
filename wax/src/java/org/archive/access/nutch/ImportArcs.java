/*
 * $Id$
 * 
 * Copyright (C) 2003 Internet Archive.
 * 
 * This file is part of the archive-access tools project
 * (http://sourceforge.net/projects/archive-access).
 * 
 * The archive-access tools are free software; you can redistribute them and/or
 * modify them under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or any
 * later version.
 * 
 * The archive-access tools are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * the archive-access tools; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.archive.access.nutch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.ToolBase;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.MapWritable;
import org.apache.nutch.fetcher.Fetcher;
import org.apache.nutch.fetcher.FetcherOutput;
import org.apache.nutch.fetcher.FetcherOutputFormat;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseImpl;
import org.apache.nutch.parse.ParseOutputFormat;
import org.apache.nutch.parse.ParseStatus;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.scoring.ScoringFilterException;
import org.apache.nutch.scoring.ScoringFilters;
import org.apache.nutch.util.StringUtil;
import org.apache.nutch.util.mime.MimeType;
import org.apache.nutch.util.mime.MimeTypeException;
import org.apache.nutch.util.mime.MimeTypes;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.arc.ARCConstants;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;
import org.archive.util.Base32;
import org.archive.util.MimetypeUtils;
import org.archive.util.TextUtils;

/**
 * Ingests ARCs writing ARC Record parse as Nutch FetcherOutputFormat.
 * FOF has four outputs (It used to have 5 but got rid of the
 * empty content):
 * <ul><li>crawl_fetch holds a fat CrawlDatum of all vitals including metadata.
 * Its written below by our {@link WaxFetcherOutputFormat} (innutch by
 * {@link FetcherOutputFormat}).  Here is an example CD: <pre>  Version: 4
 *  Status: 5 (fetch_success)
 *  Fetch time: Wed Mar 15 12:38:49 PST 2006
 *  Modified time: Wed Dec 31 16:00:00 PST 1969
 *  Retries since fetch: 0
 *  Retry interval: 0.0 days
 *  Score: 1.0
 *  Signature: null
 *  Metadata: collection:test arcname:IAH-20060315203614-00000-debord arcoffset:5127 
 * </pre></li>
 * <li>crawl_parse has CrawlDatum of MD5s.  Used making CrawlDB.
 * Its obtained from above fat crawl_fetch CrawlDatum and written
 * out as part of the parse output done by {@link WaxParseOutputFormat}.
 * This latter class writes three files.  This crawl_parse and both
 * of the following parse_text and parse_data.</li>
 * <li>parse_text has text from parse.</li>
 * <li>parse_data has other metadata found by parse (Depends on
 * parser).  This is only input to linkdb.  The html parser
 * adds found out links here and content-type and discovered
 * encoding as well as advertised encoding, etc.</li>
 * </ul>
 */
public class ImportArcs extends ToolBase implements Mapper {
    public final Log LOG = LogFactory.getLog(ImportArcs.class);

    private static final String WAX_SUFFIX = "wax.";
    private static final String WHITESPACE = "\\s+";

    public static final String ARCFILENAME_KEY = "arcname";
    public static final String ARCFILEOFFSET_KEY = "arcoffset";
    public static final String ARCCOLLECTION_KEY = "collection";
    private static final String CONTENT_TYPE_KEY = "content-type";
    private static final String TEXT_TYPE = "text/";
    private static final String APPLICATION_TYPE = "application/";
    public static final String WAX_COLLECTION_KEY =
        ImportArcs.WAX_SUFFIX + ImportArcs.ARCCOLLECTION_KEY;

    private static final String PDF_TYPE = "application/pdf";

    private boolean indexAll;
    private boolean indexRedirects;
    private int contentLimit;
    private int pdfContentLimit;
    private MimeTypes mimeTypes;
    private String segmentName;
    private String collectionName;

    private final NumberFormat numberFormatter = NumberFormat.getInstance();

    private int parseThreshold = -1;

    private static final Pattern ARCHIVEIT_COLLECTION =
        Pattern.compile("(?:ARCHIVEIT-)?([^-\\._]+)[-|_|\\.]([^\\s]+)");
    
    /**
     * Usually the URL in first record looks like this:
     * filedesc://IAH-20060315203614-00000-debord.arc.  But in old
     * ARCs, it can look like this: filedesc://19961022/IA-000001.arc.
     */
    private static final Pattern FILEDESC_ARCNAME =
        Pattern.compile("^(?:filedesc://)(?:[0-9]+\\/)?(.+)(?:\\.arc)$");

    /**
     * Buffer to reuse on each ARCRecord indexing.
     */
    private final byte[] buffer = new byte[1024 * 16];
    
    /**
     * 
     */
    private final ByteArrayOutputStream contentBuffer =
        new ByteArrayOutputStream(1024 * 16);

    /**
     * How long to spend indexing.
     */
    private long maxtime;
    
    private URLNormalizers urlNormalizers;
    private URLFilters filters;

    private boolean sha1 = false;
    
    public ImportArcs() {
    	super();
    }
    
    public ImportArcs(Configuration conf) {
      setConf(conf);
    }
    
    public void configure(final JobConf job) {
        setConf(job);
        this.indexAll = job.getBoolean("wax.index.all", false);
        this.indexRedirects = job.getBoolean("wax.index.redirects", false);
        this.contentLimit = job.getInt("http.content.limit", 1024 * 100);
        final int pdfMultiplicand = job.getInt("wax.pdf.size.multiplicand", 10);
        this.pdfContentLimit = (this.contentLimit == -1) ? this.contentLimit
                : pdfMultiplicand * this.contentLimit;
        this.mimeTypes = MimeTypes.get(job.get("mime.types.file"));
        this.segmentName = job.get(Nutch.SEGMENT_NAME_KEY);
        // Value is in minutes.
        this.maxtime = job.getLong("wax.index.timeout", 60) * 60 * 1000;

        // Get the rsync protocol handler into the mix.
        System.setProperty("java.protocol.handler.pkgs", "org.archive.net");

        // Format numbers output by parse rate logging.
        this.numberFormatter.setMaximumFractionDigits(2);
        this.numberFormatter.setMinimumFractionDigits(2);
        this.parseThreshold = job.getInt("wax.parse.rate.threshold", -1);
        
        this.urlNormalizers =
        	new URLNormalizers(job, URLNormalizers.SCOPE_FETCHER);
        this.filters = new URLFilters(job);
        
        this.sha1 = job.getBoolean("wax.digest.sha1", false);
    }
    
    private class ImportArcsReporter implements Reporter {
        private final Reporter wrappedReporter;
        private long nextUpdate = 0;
        private long time = System.currentTimeMillis();

        private static final long FIVE_MINUTES = 1000 * 60 * 5;
        
        public ImportArcsReporter(final Reporter r) {
            this.wrappedReporter = r;
        }
        
        public void setStatus(final String msg) throws IOException {
            setStatus(msg, false);
        }
        
        public void setStatus(final String msg, final boolean writeThrough)
        throws IOException {
            LOG.info(msg);
            // Only update tasktracker every second -- not for every record.
            long now = System.currentTimeMillis();
            if (writeThrough || now > this.nextUpdate) {
                this.wrappedReporter.setStatus(msg);
                this.nextUpdate = now + 1000;
                this.time = now;
            }
        }
        
        /**
         * Update reporter if its a long time since last log only.
         * @param msg Message to report IF we haven't reported in a long time.
         * @throws IOException
         */
        public void setStatusIfElapse(final String msg)
        throws IOException {
            long now = System.currentTimeMillis();
            if ((now - this.time) > FIVE_MINUTES) {
                setStatus(msg);
            }
        }

        public void progress() throws IOException {
            this.wrappedReporter.progress();
        }
    };

    public void map(final WritableComparable key, final Writable value,
            final OutputCollector output, final Reporter reporter)
            throws IOException {
        final ImportArcsReporter importArcsReporter =
            new ImportArcsReporter(reporter);
        final String arcurl = value.toString();
        if ((arcurl == null) || arcurl.endsWith("work")) {
            importArcsReporter.setStatus("skipping " + arcurl);
            return;
        }

        // Set off indexing in a thread so I can cover it with a timer.
        final Thread t = new IndexingThread(arcurl, output, importArcsReporter);
        t.setDaemon(true);
        t.start();
        final long start = System.currentTimeMillis();
        try {
            for (long period = this.maxtime; t.isAlive() && (period > 0);
                period = this.maxtime - (System.currentTimeMillis() - start)) {
                try {
                    t.join(period);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            cleanup(t, importArcsReporter);
        }
    }

    protected void cleanup(final Thread t, final Reporter reporter)
            throws IOException {
        if (!t.isAlive()) {
            return;
        }
        reporter.setStatus("Killing indexing thread " + t.getName());
        t.interrupt();
        try {
            // Give it some time to die.
            t.join(1000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        if (t.isAlive()) {
            LOG.info(t.getName() + " will not die");
        }
    }

    private class IndexingThread extends Thread {
        private final String arcLocation;
        private final OutputCollector output;
        private final ImportArcsReporter reporter;

        public IndexingThread(final String arcloc, final OutputCollector o,
                final ImportArcsReporter r) {
            // Name this thread same as ARC location.
            super(arcloc);
            this.arcLocation = arcloc;
            this.output = o;
            this.reporter = r;
        }

        public void run() {
            ArchiveReader arc = null;
            // Need a thread that will keep updating TaskTracker during long
            // downloads else tasktracker will kill us.
            Thread reportingDuringDownload = null;
            try {
                this.reporter.setStatus("opening " + this.arcLocation, true);
                reportingDuringDownload = new Thread("reportingDuringDownload") {
                    public void run() {
                        while (!this.isInterrupted()) {
                            try {
                                synchronized (this) {
                                    sleep(1000 * 60); // Sleep a minute.
                                }
                                reporter.setStatus("downloading " +
                                    arcLocation);
                            } catch (final IOException e) {
                                e.printStackTrace();
                                // No point hanging around if we're failing
                                // status.
                                break;
                            } catch (final InterruptedException e) {
                                // Interrupt flag is cleared. Just fall out.
                                break;
                            }
                        }
                    }
                };
                reportingDuringDownload.setDaemon(true);
                reportingDuringDownload.start();
                arc = ArchiveReaderFactory.get(this.arcLocation);
            } catch (final Throwable e) {
                try {
                    final String msg = "Error opening " + this.arcLocation
                            + ": " + e.toString();
                    this.reporter.setStatus(msg, true);
                    LOG.info(msg);
                } catch (final IOException ioe) {
                    LOG.warn(this.arcLocation, ioe);
                }
                return;
            } finally {
                if ((reportingDuringDownload != null)
                        && reportingDuringDownload.isAlive()) {
                    reportingDuringDownload.interrupt();
                }
            }

            arc.setDigest(sha1);
            String arcName = null;
            try {
                // If empty collection name, take arc prefix later below.
                ImportArcs.this.collectionName = getConf().
                    get(ImportArcs.WAX_SUFFIX + ImportArcs.ARCCOLLECTION_KEY);

                final ParseUtil pu = new ParseUtil(getConf());
                // Iterate over each ARCRecord.
                for (final Iterator i = arc.iterator();
                        i.hasNext() && !currentThread().isInterrupted();) {
                    final ARCRecord rec = (ARCRecord) i.next();
                    // First entry has arc name, usually.
                    if (arcName == null) {
                        
                        arcName = trimARCName(rec.getMetaData().getUrl());
                        if ((ImportArcs.this.collectionName == null) ||
                                (ImportArcs.this.collectionName.length() <= 0)) {
                            ImportArcs.this.collectionName =
                                getCollectionFromArcname(arcName);
                        }
                        if ((ImportArcs.this.collectionName == null) ||
                                (ImportArcs.this.collectionName.length() == 0)) {
                            throw new NullPointerException("Collection name can't "
                                    + "be empty");
                        }
                    }
                    if (!isIndex(rec)) {
                        continue;
                    }
                    try {
                        final long recordLength = processRecord(arcName, rec,
                            this.output, this.reporter, pu);
                        if (recordLength >
                                ARCConstants.DEFAULT_MAX_ARC_FILE_SIZE) {
                            // Now, if the content length is larger than a
                            // standard ARC, then it is most likely the last
                            // record in the ARC because ARC is closed after we
                            // exceed 100MB (DEFAULT_MAX_ARC...). Calling
                            // hasNext above will make us read through the
                            // whole record, even if its a 1.7G video. On a
                            // loaded machine, this might cause us timeout with
                            // tasktracker -- so, just skip out here.
                            this.reporter.setStatus("skipping "
                                    + this.arcLocation
                                    + " -- very long record "
                                    + rec.getMetaData());
                            break;
                        }
                    } catch (final Throwable e) {
                        // Failed parse of record. Keep going.
                        LOG.warn("Error processing " + rec.getMetaData(), e);
                    }
                }
                if (currentThread().isInterrupted()) {
                    LOG.info(currentThread().getName() + " interrupted");
                }
                this.reporter.setStatus("closing " + this.arcLocation, true);
            } catch (final Throwable e) {
                // Problem parsing arc file.
                final String msg = "Error parsing " + this.arcLocation;
                try {
                    this.reporter.setStatus(msg, true);
                } catch (final IOException ioe) {
                    ioe.printStackTrace();
                }
                LOG.warn(msg, e);
            } finally {
                try {
                    arc.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         *  Strip scheme prefix and arc or arc.gz suffix if present.
         *  First record looks like this:
         *      filedesc://IAH-20060315203614-00000-debord.arc
         * Strip extraneous prefix and suffix (At least WERA expects an ARC 
         * name without scheme and suffix: i.e.
         * IAH-20060315203614-00000-debord).
         */
        private String trimARCName(String arcname) {
            final Matcher m = ImportArcs.FILEDESC_ARCNAME.matcher(arcname);
            if ((m != null) && m.matches()) {
                arcname = m.group(1);
            }
            return arcname;
        }
    }

    /**
     * @param rec ARC Record to test.
     * @return True if we are to index this record.
     */
    protected boolean isIndex(final ARCRecord rec) {
        return ((rec.getStatusCode() >= 200) && (rec.getStatusCode() < 300))
            || (this.indexRedirects && ((rec.getStatusCode() >= 300) &&
                (rec.getStatusCode() < 400)));
    }

    long processRecord(final String arcName, final ARCRecord rec,
            final OutputCollector output, final ImportArcsReporter reporter,
            final ParseUtil parseUtil) throws IOException {
        final ARCRecordMetaData arcData = rec.getMetaData();
        
        // Get URL.
        String url = arcData.getUrl();
        String oldUrl = url;
        try {
            url = urlNormalizers.normalize(url,
                URLNormalizers.SCOPE_FETCHER);
            url = filters.filter(url); // filter the url
        } catch (Exception e) {
        	LOG.warn("Skipping record. Didn't pass normalization/filter " +
                oldUrl + ": " + e.toString());
            return 0;
        }
        
        final long b = arcData.getContentBegin();
        final long l = arcData.getLength();
        final long recordLength = (l > b)? (l - b): l;

        // Look at ARCRecord meta data line mimetype. It can be empty.  If so,
        // two more chances at figuring it either by looking at HTTP headers or
        // by looking at first couple of bytes of the file.  See below.
        String mimetype =
            getMimetype(arcData.getMimetype(), this.mimeTypes, url);
        if (skip(mimetype)) {
            return recordLength;
        }

        // Copy http headers to nutch metadata.
        final Metadata metaData = new Metadata();
        final Header[] headers = rec.getHttpHeaders();
        for (int j = 0; j < headers.length; j++) {
            final Header header = headers[j];
            if (mimetype == null) {
                // Special handling. If mimetype is still null, try getting it
                // from the http header. I've seen arc record lines with empty
                // content-type and a MIME unparseable file ending; i.e. .MID.
                if ((header.getName() != null) &&
                        header.getName().toLowerCase().equals(
                            ImportArcs.CONTENT_TYPE_KEY)) {
                    mimetype = getMimetype(header.getValue(), null, null);
                    if (skip(mimetype)) {
                        return recordLength;
                    }
                }
            }
            metaData.set(header.getName(), header.getValue());
        }
        
        // This call to reporter setStatus pings the tasktracker telling it our
        // status and telling the task tracker we're still alive (so it doesn't
        // time us out).
        final String noSpacesMimetype =
            TextUtils.replaceAll(ImportArcs.WHITESPACE,
                ((mimetype == null || mimetype.length() <= 0)?
                        "TODO": mimetype),
                    "-");
        final String recordLengthAsStr = Long.toString(recordLength);
        reporter.setStatus(getStatus(url, oldUrl, recordLengthAsStr,
            noSpacesMimetype));
        
        // This is a nutch 'more' field.
        metaData.set("contentLength", recordLengthAsStr);

        rec.skipHttpHeader();
        reporter.setStatusIfElapse("read headers on " + url);

        // TODO: Skip if unindexable type.
        int total = 0;
        // Read in first block. If mimetype still null, look for MAGIC.
        int len = rec.read(this.buffer, 0, this.buffer.length);
        if (mimetype == null) {
            MimeType mt = this.mimeTypes.getMimeType(this.buffer);
            if (mt == null || mt.getName() == null) {
                LOG.warn("Failed to get mimetype for: " + url);
                return recordLength;
            }
            mimetype = mt.getName();
        }
        metaData.set(ImportArcs.CONTENT_TYPE_KEY, mimetype);
        
        // How much do we read total? If pdf, we will read more. If equal to -1,
        // read all.
        int readLimit = (ImportArcs.PDF_TYPE.equals(mimetype))?
            this.pdfContentLimit : this.contentLimit;
        // Reset our contentBuffer so can reuse.  Over the life of an ARC
        // processing will grow to maximum record size.
        this.contentBuffer.reset();
        while ((len != -1) && ((readLimit == -1) || (total < readLimit))) {
            total += len;
            this.contentBuffer.write(this.buffer, 0, len);
            len = rec.read(this.buffer, 0, this.buffer.length);
            reporter.setStatusIfElapse("reading " + url);
        }
        // Close the Record.  We're done with it.  Side-effect is calculation
        // of digest -- if we're digesting.
        rec.close();
        reporter.setStatusIfElapse("closed " + url);
        
        final byte[] contentBytes = this.contentBuffer.toByteArray();
        final CrawlDatum datum = new CrawlDatum();
        datum.setStatus(CrawlDatum.STATUS_FETCH_SUCCESS);
        
        // Calculate digest or use precalculated sha1.
        metaData.set(Nutch.SIGNATURE_KEY, (this.sha1)?
            rec.getDigestStr():
            MD5Hash.digest(contentBytes).toString());
        
        metaData.set(Nutch.SEGMENT_NAME_KEY, this.segmentName);
        // Score at this stage is 1.0f.
        metaData.set(Nutch.SCORE_KEY, Float.toString(datum.getScore()));

        final long startTime = System.currentTimeMillis();
        final Content content = new Content(url, url, contentBytes, mimetype,
                metaData, getConf());

        datum.setFetchTime(Nutchwax.getDate(arcData.getDate()));
        
        MapWritable mw = datum.getMetaData();
        if (mw == null) { 
           mw = new MapWritable();
        }
        mw.put(new Text(ImportArcs.ARCCOLLECTION_KEY),
            new Text(collectionName));
        mw.put(new Text(ImportArcs.ARCFILENAME_KEY), new Text(arcName));
        mw.put(new Text(ImportArcs.ARCFILEOFFSET_KEY),
            new Text(Long.toString(arcData.getOffset())));
        datum.setMetaData(mw);

        Parse parse = null;
        ParseStatus parseStatus;
        try {
            parse = parseUtil.parse(content);
            reporter.setStatusIfElapse("parsed " + url);
            parseStatus = parse.getData().getStatus();
        } catch (final Exception e) {
            parseStatus = new ParseStatus(e);
        }
        if (!parseStatus.isSuccess()) {
            final String status = formatToOneLine(parseStatus.toString());
            LOG.warn("Error parsing: " + mimetype + " " + url + ": " + status);
            parse = null;
        } else {
            // Was it a slow parse?
            final double kbPerSecond = getParseRate(startTime,
                    (contentBytes != null) ? contentBytes.length : 0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(getParseRateLogMessage(url,
                    noSpacesMimetype, kbPerSecond));
            } else if (kbPerSecond < this.parseThreshold) {
                LOG.warn(getParseRateLogMessage(url, noSpacesMimetype,
                        kbPerSecond));
            }
        }
        Writable value = new FetcherOutput(datum, null,
            parse != null ? new ParseImpl(parse) : null);
        output.collect(Nutchwax.generateWaxKey(url, this.collectionName),
            value);
        return recordLength;
    }
    
    protected String getStatus(final String url, String oldUrl,
        final String recordLengthAsStr, final String noSpacesMimetype) {
        // If oldUrl is same as url, don't log.  Otherwise, log original so we
        // can keep url originally imported.
        if (oldUrl.equals(url)) {
            oldUrl = "-";
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("adding ");
        sb.append(url);
        sb.append(" ");
        sb.append(oldUrl);
        sb.append(" ");
        sb.append(recordLengthAsStr);
        sb.append(" ");
        sb.append(noSpacesMimetype);
        return sb.toString();
    }
    
    protected String formatToOneLine(final String s) {
        final StringBuffer sb = new StringBuffer(s.length());
        for (final StringTokenizer st = new StringTokenizer(s, "\t\n\r");
                st.hasMoreTokens(); sb.append(st.nextToken())) {
            ;
        }
        return sb.toString();
    }

    protected static String getCollectionFromArcname(final String arcurl)
            throws URISyntaxException {
        final URI u = new URI(arcurl);
        final String p = u.getPath();
        if (p.length() <= 0) {
            throw new URISyntaxException("Path is empty.", arcurl);
        }
        final int index = p.lastIndexOf('/');
        String arcname = p;
        if (index >= 0) {
            arcname = p.substring(index + 1);
        }
        final Matcher m = ImportArcs.ARCHIVEIT_COLLECTION.matcher(arcname);
        if ((m == null) || !m.matches()) {
            throw new URISyntaxException("Can't find collection in arcname",
                    arcname);
        }
        return m.group(1);
    }

    protected String getParseRateLogMessage(final String url,
            final String mimetype, final double kbPerSecond) {
        return url + " " + mimetype + " parse KB/Sec "
                + this.numberFormatter.format(kbPerSecond);
    }

    protected double getParseRate(final long startTime, final long len) {
        // Get indexing rate:
        long elapsedTime = System.currentTimeMillis() - startTime;
        elapsedTime = (elapsedTime == 0) ? 1 : elapsedTime;
        return (len != 0) ? ((double) len / 1024)
                / ((double) elapsedTime / 1000) : 0;
    }

    protected boolean skip(final String mimetype) {
        boolean decision = false;
        // Are we to index all content?
        if (!this.indexAll) {
            if ((mimetype == null)
                    || (!mimetype.startsWith(ImportArcs.TEXT_TYPE) && !mimetype
                            .startsWith(ImportArcs.APPLICATION_TYPE))) {
                // Skip any but basic types.
                decision = true;
            }
        }
        return decision;
    }
    
    protected String getMimetype(final String mimetype, final MimeTypes mts,
            final String url) {
        if (mimetype != null && mimetype.length() > 0) {
            return checkMimetype(mimetype.toLowerCase());
        }
        if (mts != null && url != null) {
            final MimeType mt = mts.getMimeType(url);
            if (mt != null) {
                return checkMimetype(mt.getName().toLowerCase());
            }
        }
        return null;
    }
    
    protected static String checkMimetype(String mimetype) {
        if ((mimetype == null) || (mimetype.length() <= 0) ||
                mimetype.startsWith(MimetypeUtils.NO_TYPE_MIMETYPE)) {
            return null;
        }
        
        // Test the mimetype makes sense. If not, clear it.
        try {
            new MimeType(mimetype);
        } catch (final MimeTypeException e) {
            mimetype = null;
        }
        return mimetype;
    }

    public void importArcs(final Path arcUrlsDir, final Path segment,
            final String collection) throws IOException {
        LOG.info("ImportArcs segment: " + segment + ", src: " + arcUrlsDir);

        final JobConf job = new JobConf(getConf(), this.getClass());

        job.set(Nutch.SEGMENT_NAME_KEY, segment.getName());

        job.setInputPath(arcUrlsDir);
        job.setMapperClass(ImportArcs.class);

        job.setOutputPath(segment);
        job.setOutputFormat(WaxFetcherOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(FetcherOutput.class);
        // Pass the collection name out to the tasks IF non-null.
        if ((collection != null) && (collection.length() > 0)) {
            job.set(ImportArcs.WAX_SUFFIX + ImportArcs.ARCCOLLECTION_KEY,
                collection);
        }
        job.setJobName("import " + arcUrlsDir + " " + segment);

        JobClient.runJob(job);
        LOG.info("ImportArcs: done");
    }
    
    /**
     * Override of nutch FetcherOutputFormat so I can substitute my own
     * ParseOutputFormat, {@link WaxParseOutputFormat}.  While I'm here,
     * removed content references.  NutchWAX doesn't save content.
     * @author stack
     */
    public static class WaxFetcherOutputFormat extends FetcherOutputFormat {
        public RecordWriter getRecordWriter(final FileSystem fs,
                final JobConf job, final String name, Progressable progress)
        throws IOException {
            Path f = new Path(job.getOutputPath(), CrawlDatum.FETCH_DIR_NAME);
            final Path fetch = new Path(f, name);
            final MapFile.Writer fetchOut = new MapFile.Writer(fs,
                fetch.toString(), Text.class, CrawlDatum.class);
            
            return new RecordWriter() {
                private RecordWriter parseOut;
                {
                    if (Fetcher.isParsing(job)) {
                    	// Here is nutchwax change, using WaxParseOutput
                    	// instead of ParseOutputFormat.
                        this.parseOut = new WaxParseOutputFormat().
                            getRecordWriter(fs, job, name, null);
                    }
                }

                public void write(WritableComparable key, Writable value)
                throws IOException {
                    FetcherOutput fo = (FetcherOutput)value;
                    fetchOut.append(key, fo.getCrawlDatum());
                    if (fo.getParse() != null) {
                        parseOut.write(key, fo.getParse());
                    }
                }

                public void close(Reporter reporter) throws IOException {
                    fetchOut.close();
                    if (parseOut != null) {
                        parseOut.close(reporter);
                    }
                }
            };
        }
    }
    
    /**
     * Copy so I can add collection prefix to produced signature and link
     * CrawlDatums.
     * @author stack
     */
    public static class WaxParseOutputFormat extends ParseOutputFormat {
        public final Log LOG = LogFactory.getLog(WaxParseOutputFormat.class);
        
        private URLNormalizers urlNormalizers;
        private URLFilters filters;
        private ScoringFilters scfilters;


        public RecordWriter getRecordWriter(FileSystem fs, JobConf job,
                String name, Progressable progress)
        throws IOException {
        	// Extract collection prefix from key to use later when adding
        	// signature and link crawldatums.
        	
            this.urlNormalizers =
                new URLNormalizers(job, URLNormalizers.SCOPE_OUTLINK);
            this.filters = new URLFilters(job);
            this.scfilters = new ScoringFilters(job);
            final float interval =
                job.getFloat("db.default.fetch.interval", 30f);
            final boolean ignoreExternalLinks =
                job.getBoolean("db.ignore.external.links", false);
            final boolean sha1 = job.getBoolean("wax.digest.sha1", false);


            Path text = new Path(new Path(job.getOutputPath(),
                    ParseText.DIR_NAME), name);
            Path data = new Path(new Path(job.getOutputPath(),
                    ParseData.DIR_NAME), name);
            Path crawl = new Path(new Path(job.getOutputPath(),
                    CrawlDatum.PARSE_DIR_NAME), name);

            final MapFile.Writer textOut = new MapFile.Writer(job, fs,
                text.toString(), Text.class, ParseText.class,
                CompressionType.RECORD);

            final MapFile.Writer dataOut = new MapFile.Writer(job, fs,
            	data.toString(), Text.class, ParseData.class);

            final SequenceFile.Writer crawlOut = SequenceFile.createWriter(fs,
                job, crawl, Text.class, CrawlDatum.class);

            return new RecordWriter() {

                public void write(WritableComparable key, Writable value)
                        throws IOException {
                    // Test that I can parse the key before I do anything
                    // else. If not, write nothing for this record.
                    String collection = null;
                    String fromUrl = null;
                    String fromHost = null;
                    String toHost = null;
                    try {
                		collection = Nutchwax.getCollectionFromWaxKey(key);
                		fromUrl = Nutchwax.getUrlFromWaxKey(key);
                    } catch (IOException ioe) {
                    	LOG.warn("Skipping record. Can't parse " + key, ioe);
                        return;
                    }
                    if (fromUrl == null || collection == null) {
                    	LOG.warn("Skipping record. Null from or collection " +
                            key);
                        return;
                    }

                    Parse parse = (Parse) value;

                    textOut.append(key, new ParseText(parse.getText()));

                    ParseData parseData = parse.getData();
                    // recover the signature prepared by Fetcher or ParseSegment
                    String sig = parseData.getContentMeta().get(
                            Nutch.SIGNATURE_KEY);
                    if (sig != null) {
                        byte[] signature = (sha1)?
                            Base32.decode(sig): StringUtil.fromHexString(sig);
                        if (signature != null) {
                            // append a CrawlDatum with a signature
                            CrawlDatum d = new CrawlDatum(
                                CrawlDatum.STATUS_SIGNATURE, 0.0f);
                            d.setSignature(signature);
                            crawlOut.append(key, d);
                        }
                    }

                    // collect outlinks for subsequent db update
                    Outlink[] links = parseData.getOutlinks();
                    if (ignoreExternalLinks) {
                        try {
                            fromHost = new URL(fromUrl).getHost().toLowerCase();
                        } catch (MalformedURLException e) {
                            fromHost = null;
                        }
                    } else {
                        fromHost = null;
                    }

                    String[] toUrls = new String[links.length];
                    int validCount = 0;
                    for (int i = 0; i < links.length; i++) {
                        String toUrl = links[i].getToUrl();
                        try {
                            toUrl = urlNormalizers.normalize(toUrl,
                                URLNormalizers.SCOPE_OUTLINK);
                            toUrl = filters.filter(toUrl); // filter the url
                        } catch (Exception e) {
                            toUrl = null;
                        }
                        // ignore links to self (or anchors within the page)
                        if (fromUrl.equals(toUrl)) toUrl = null;
                        if (toUrl != null)
                            validCount++;
                        toUrls[i] = toUrl;
                    }
                	
                    CrawlDatum adjust = null;
                    // compute score contributions and adjustment to the
                    // original score
                    for (int i = 0; i < toUrls.length; i++) {
                        if (toUrls[i] == null)
                            continue;
                        if (ignoreExternalLinks) {
                            try {
                                toHost = new URL(toUrls[i]).getHost().
                                    toLowerCase();
                            } catch (MalformedURLException e) {
                                toHost = null;
                            }
                            if (toHost == null || !toHost.equals(fromHost)) {
                                // external links
                                continue; // skip it
                            }
                        }

                        CrawlDatum target = new CrawlDatum(
                            CrawlDatum.STATUS_LINKED, interval);
                        Text fromURLUTF8 = new Text(fromUrl);
                        Text targetUrl = new Text(toUrls[i]);
                        adjust = null;
                        try {
                            // Scoring now expects first two arguments to be
                            // URLs (More reason to do our own scoring).
                            // St.Ack
                            adjust = scfilters.distributeScoreToOutlink(
                                fromURLUTF8, targetUrl, parseData,
                                target, null, links.length, validCount);
                        } catch (ScoringFilterException e) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("Cannot distribute score from " + key
                                        + " to " + target + " - skipped ("
                                        + e.getMessage());
                            }
                            continue;
                        }
                        Text targetKey =
                            Nutchwax.generateWaxKey(targetUrl, collection);
                        crawlOut.append(targetKey, target);
                        if (adjust != null)
                            crawlOut.append(key, adjust);
                    }
                    dataOut.append(key, parseData);
                }

                public void close(Reporter reporter) throws IOException {
                    textOut.close();
                    dataOut.close();
                    crawlOut.close();
                }
            };
        }
    }

    public void close() {
        // Nothing to close.
    }
    
    public static void doImportUsage(final String message,
    		final int exitCode) {
        if (message != null && message.length() > 0) {
            System.out.println(message);
        }
        System.out.println("Usage: hadoop jar nutchwax.jar import <input>" +
        	" <output> <collection>");
        System.out.println("Arguments:");
        System.out.println(" input       Directory of files" +
        	" listing ARC URLs to import.");
        System.out.println(" output      Directory to import to. Inport is " +
        	"written to a subdir named");
        System.out.println("             for current date under " +
        		"'<output>/segments/'.");
        System.out.println(" collection  Collection name. Added to" +
            " each resource.");
        System.exit(exitCode);
    }
    
    public static void main(String[] args) throws Exception {
        int res = new ImportArcs().
        	doMain(NutchwaxConfiguration.getConfiguration(), args);
        System.exit(res);
    }

    public int run(final String[] args) throws Exception {
    	if (args.length != 3) {
            doImportUsage("ERROR: Wrong number of arguments passed.", 2);
        }
        // Assume list of ARC urls is first arg and output dir the second.
        try {
        	importArcs(new Path(args[0]), new Path(args[1]), args[2]);
        	return 0;
        } catch(Exception e) {
            LOG.fatal("ImportARCs: " + StringUtils.stringifyException(e));
        	return -1;
        }
    }
}
