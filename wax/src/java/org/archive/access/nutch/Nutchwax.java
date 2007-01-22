/* Nutchwax
 * 
 * $Id$
 * 
 * Created on Feb 14, 2006
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.crawl.Generator;
import org.apache.nutch.indexer.DeleteDuplicates;
import org.apache.nutch.indexer.IndexMerger;
import org.archive.util.ArchiveUtils;

/**
 * Script to run all indexing jobs from index through merge of final index.
 */
public class Nutchwax {
    public static final Log LOG =
        LogFactory.getLog(Nutchwax.class.getName());
    
    private static final String KEY_COLLECTION_PREFIX = "c=";
    private static final String KEY_COLLECTION_SUFFIX = ",u=";
    private static final Pattern COLLECTION =
        Pattern.compile("^\\s*c=([^,]+),u=(.*)\\s*", Pattern.DOTALL);

    private final static List JOBS = Arrays.asList(new String[] {
        "import", "update", "invert", "index", "dedup", "merge", "all",
        "class"});

    // Lazy initialize these two variables to delay complaint about hadoop not
    // being present -- if its not.  Meantime I get command-line processing
    // done.
    private FileSystem fs = null;
    private JobConf conf = null;
    
    /**
     * Default constructor.
     * @throws IOException 
     */
    public Nutchwax() throws IOException {
        super();
    }
    
    public synchronized JobConf getJobConf() {
        if (this.conf == null) {
            this.conf = new JobConf(NutchwaxConfiguration.getConfiguration());
        }
        return this.conf;
    }
    
    public synchronized FileSystem getFS() throws IOException {
        if (this.fs == null) {
            this.fs = FileSystem.get(getJobConf());
        }
        return this.fs;
    }
    
    protected class OutputDirectories {
        private final Path output;
        private final Path crawlDb;
        private final Path linkDb;
        private final Path segments;
        private final Path indexes;
        private final Path index;
        private final Path tmpDir;

        public OutputDirectories(final Path output) throws IOException {
            this.output = output;
            this.crawlDb = new Path(output + "/crawldb");
            this.linkDb = new Path(output + "/linkdb");
            this.segments = new Path(output + "/segments");
            this.indexes = new Path(output + "/indexes");
            this.index = new Path(output + "/index");
            this.tmpDir = getJobConf().getLocalPath("mapred.temp.dir",
                Generator.generateSegmentName());
        }

        public Path getCrawlDb() {
            return crawlDb;
        }

        public Path getIndexes() {
            return indexes;
        }

        public Path getLinkDb() {
            return linkDb;
        }

        public Path getSegments() {
            return segments;
        }

        public Path getTmpDir() {
            return tmpDir;
        }

        public Path getIndex() {
            return index;
        }

        public Path getOutput() {
            return output;
        }
    }

    /**
     * Run passed list of mapreduce indexing jobs. Jobs are always run in
     * order: import, update, etc.
     * 
     * @throws Exception
     */
    protected void doAll(final Path input, final String collectionName,
            final OutputDirectories od)
    throws Exception {
        doImport(input, collectionName, od);
        doUpdate(od);
        doInvert(od);
        doIndexing(od);
        doDedup(od);
        doMerge(od);
        LOG.info("Nutchwax finished.");
    }
    
    protected void doImport(final Path input, final String collectionName,
            final OutputDirectories od)
    throws IOException {
        Path segment = new Path(od.getSegments(),
            Generator.generateSegmentName() +
                ((collectionName == null || collectionName.length() <= 0)?
                        "": "-" + collectionName));
        new ImportArcs(getJobConf()).importArcs(input, segment,
            collectionName);
    }
    
    protected void doUpdate(final OutputDirectories od)
    throws IOException {
        doUpdate(od, null);
    }
    
    protected void doUpdate(final OutputDirectories od,
            final String[] segments)
    throws IOException {
        LOG.info("updating crawldb " + od.getCrawlDb());
        // Need to make sure the db dir exists before progressing.
        Path dbPath = new Path(od.getCrawlDb(), CrawlDb.CURRENT_NAME);
        if (!getFS().exists(dbPath)) {
            getFS().mkdirs(dbPath);
        }
        CrawlDb cdb = new NutchwaxCrawlDb(getJobConf());
        if (segments != null) {
            List<Path> paths = new ArrayList<Path>(segments.length);
            for (int i = 0; i < segments.length; i++) {
                Path p = new Path(segments[i]);
                if (!getFS().exists(p)) {
                    throw new FileNotFoundException(p.toString());
                }
                paths.add(p);
            }
            cdb.update(od.getCrawlDb(), paths.toArray(new Path[paths.size()]),
                true, true);
        } else {
            Path[] allSegments = getSegments(od);
            // This just does the last segment created.
            cdb.update(od.getCrawlDb(),
                new Path[] {allSegments[allSegments.length - 1]}, true, true);
        }
    }

    protected Path [] getSegments(final OutputDirectories od)
    throws IOException {
        Path[] allSegments = getFS().listPaths(od.getSegments());
        if (allSegments == null || allSegments.length <= 0) {
            throw new FileNotFoundException(od.getSegments().toString());
        }
        return allSegments;
    }
    
    protected void doInvert(final OutputDirectories od, final Path [] segments)
    throws IOException {
        createLinkdb(od);
        new NutchwaxLinkDb(getJobConf()).
        	invert(od.getLinkDb(), segments, true, true);
    }
    
    protected void doInvert(final OutputDirectories od)
    throws IOException {
        LOG.info("inverting links in " + od.getSegments());
        new NutchwaxLinkDb(getJobConf()).
        	invert(od.getLinkDb(), getSegments(od), true, true);
    }
    
    protected boolean createLinkdb(final OutputDirectories od)
    throws IOException {
        boolean result = false;
        // Make sure the linkdb exists.  Otherwise the install where
        // the temporary location gets moved to the permanent fails.
        if (getFS().mkdirs(new Path(od.getLinkDb(),
                NutchwaxLinkDb.CURRENT_NAME))) {
            LOG.info("Created " + od.getLinkDb());
            result = true;
        }
        return result;
    }
    
    protected void doIndexing(final OutputDirectories od)
    throws IOException {
        doIndexing(od, getFS().listPaths(od.getSegments()));
    }
    
    protected void doIndexing(final OutputDirectories od,
        final Path [] segments)
    throws IOException {
        LOG.info(" indexing " + segments);
        new NutchwaxIndexer(getJobConf()).index(od.getIndexes(),
            od.getCrawlDb(), od.getLinkDb(), segments);
    }
    
    protected void doDedup(final OutputDirectories od) throws IOException {
        LOG.info("dedup " + od.getIndex());
        new DeleteDuplicates(getJobConf()).dedup(new Path[] {od.getIndexes()});
    }
    
    protected void doMerge(final OutputDirectories od) throws IOException {
        LOG.info("index merge " + od.getOutput() + " using tmpDir=" +
            od.getTmpDir());
        new IndexMerger(getJobConf()).merge(getFS().listPaths(od.getIndexes()),
            od.getIndex(), od.getTmpDir());
    }

    protected void doClass(final String [] args) {
        // Redo args so absent our nutchwax 'class' command.
        final int cmdOffset = 2;
        final String [] newArgs = new String[args.length - cmdOffset];
        final String className = args[1];
        for (int i = 0; i < args.length; i++) {
            if (i < cmdOffset) {
                continue;
            }
            newArgs[i - cmdOffset] = args[i];
        }
        // From http://www.javaworld.com/javaworld/javaqa/1999-06/01-outside.html
        Class [] argTypes = new Class[1];
        argTypes[0] = String[].class;
        try {
            Method mainMethod =
                Class.forName(className).getDeclaredMethod("main", argTypes);
            mainMethod.invoke(newArgs, new Object [] {newArgs});
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void doJob(final String jobName, final String [] args)
    throws Exception {
        if (jobName.equals("import")) {
            // Usage: hadoop jar nutchwax.jar import input output name
            if (args.length != 4) {
                ImportArcs.doImportUsage(
                    "ERROR: Wrong number of arguments passed.", 2);
            }
            final Path input = new Path(args[1]);
            final Path output = new Path(args[2]);
            final String collectionName = args[3];
            checkArcsDir(input);
            OutputDirectories od = new OutputDirectories(output);
            doImport(input, collectionName, od);
        } else if (jobName.equals("update")) {
            // Usage: hadoop jar nutchwax.jar update output
            if (args.length < 2) {
                doUpdateUsage("ERROR: Wrong number of arguments passed.", 2);
            }
            OutputDirectories od = new OutputDirectories(new Path(args[1]));
            if (args.length == 2) {
                doUpdate(od);
            } else {
                for (int i = 2; i < args.length; i++) {
                    doUpdate(od, new String [] {args[i]});
                }
            }
        } else if (jobName.equals("invert")) {
            // Usage: hadoop jar nutchwax.jar invert output
            if (args.length < 2) {
                doInvertUsage("ERROR: Wrong number of arguments passed.", 2);
            }
            OutputDirectories od = new OutputDirectories(new Path(args[1]));
            if (args.length == 2) {
                doInvert(od);
            } else {
                final int offset = 2;
                Path [] segments = new Path[args.length - offset];
                for (int i = offset; i < args.length; i++) {
                    Path f = new Path(args[i]);
                    if (!getFS().exists(f)) {
                        throw new FileNotFoundException(f.toString());
                    }
                    segments[i - offset] = f;
                }
                doInvert(od, segments);
            }
        } else if (jobName.equals("index")) {
            // Usage: hadoop jar nutchwax.jar index output
            if (args.length < 2) {
                doIndexUsage("ERROR: Wrong number of arguments passed.", 2);
            }
            OutputDirectories od = new OutputDirectories(new Path(args[1]));
            if (args.length == 2) {
                doIndexing(od);
            } else {
                final int offset = 2;
                Path [] segments = new Path[args.length - offset];
                for (int i = offset; i < args.length; i++) {
                    Path f = new Path(args[i]);
                    if (!getFS().exists(f)) {
                        throw new FileNotFoundException(f.toString());
                    }
                    segments[i - offset] = f;
                }
                doIndexing(od, segments);
            }
        } else if (jobName.equals("dedup")) {
            // Usage: hadoop jar nutchwax.jar dedup output
            if (args.length != 2) {
                doDedupUsage("Wrong number of arguments passed.", 2);
            }
            doDedup(new OutputDirectories(new Path(args[1])));
        } else if (jobName.equals("merge")) {
            // Usage: hadoop jar nutchwax.jar merge output");
            if (args.length != 2) {
                doMergeUsage("ERROR: Wrong number of arguments passed.", 2);
            }
            doMerge(new OutputDirectories(new Path(args[1])));
        } else if (jobName.equals("all")) {
            // Usage: hadoop jar nutchwax.jar import input output name
            if (args.length != 4) {
                doAllUsage("ERROR: Wrong number of arguments passed.", 2);
            }
            final Path input = new Path(args[1]);
            final Path output = new Path(args[2]);
            final String collectionName = args[3];
            checkArcsDir(input);
            OutputDirectories od = new OutputDirectories(output);
            doAll(input, collectionName, od);
        } else if (jobName.equals("class")) {
            if (args.length < 2) {
                doClassUsage("ERROR: Wrong number of arguments passed.", 2);
            }
            doClass(args);
        } else {
            usage("ERROR: No handler for job name " + jobName, 4);
            System.exit(0);
        }
    }

    /**
     * Check the arcs dir exists and looks like it has files that list ARCs
     * (rather than ARCs themselves).
     * 
     * @param arcsDir Directory to examine.
     * @throws IOException
     */
    protected void checkArcsDir(final Path arcsDir)
            throws IOException {
        if (!getFS().exists(arcsDir)) {
            throw new IOException(arcsDir + " does not exist.");
        }
        if (!fs.isDirectory(arcsDir)) {
            throw new IOException(arcsDir + " is not a directory.");
        }

        final Path [] files = getFS().listPaths(arcsDir);
        for (int i = 0; i < files.length; i++) {
            if (!getFS().isFile(files[i])) {
                throw new IOException(files[i] + " is not a file.");
            }
            if (files[i].getName().toLowerCase().endsWith(".arc.gz")) {
                throw new IOException(files[i] + " is an ARC file (ARCSDIR " +
                    "should contain text file listing ARCs rather than " +
                    "actual ARCs).");
            }
        }
    }
    
    public static Text generateWaxKey(WritableComparable key,
            final String collection) {
        return generateWaxKey(key.toString(), collection);
    }
    
    public static Text generateWaxKey(final String keyStr,
            final String collection) {
        if (collection == null) {
            throw new NullPointerException("Collection is null for " + keyStr);
        }
        if (keyStr == null) {
            throw new NullPointerException("keyStr is null");
        }
        if (keyStr.startsWith(KEY_COLLECTION_PREFIX)) {
            LOG.warn("Key already has collection prefix: " + keyStr
                    + ". Skipping.");
            return new Text(keyStr);
        }
        
        return new Text(KEY_COLLECTION_PREFIX + collection.trim() +
            KEY_COLLECTION_SUFFIX + keyStr.trim());
    }
    
    public static String getCollectionFromWaxKey(final WritableComparable key)
    throws IOException {
        Matcher m = COLLECTION.matcher(key.toString());
        if (m == null || !m.matches()) {
            throw new IOException("Key doesn't have collection " +
                    "prefix <" + key.toString() + ">");
        }
        return m.group(1);
    }
    
    public static String getUrlFromWaxKey(final WritableComparable key)
    throws IOException {
        Matcher m = COLLECTION.matcher(key.toString());
        if (m == null || !m.matches()) {
            throw new IOException("Key doesn't have collection " +
                    " prefix: " + key);
        }
        return m.group(2);
    }
    
    public static long getDate(String d)
    throws IOException {
        long date = 0;
        try {
            date = ArchiveUtils.getDate(d).getTime();
        } catch (final java.text.ParseException e) {
            throw new IOException("Failed parse of date: " + d + ": " +
                e.getMessage());
        }
        // Date can be < 0 if pre-1970 (Seen in some old ARCs).
        return date >= 0? date: 0;
    }

    public static void usage(final String message, final int exitCode) {
        if (message != null && message.length() > 0) {
            System.out.println(message);
        }

        System.out.println("Usage: hadoop jar nutchwax.jar <job> [args]");
        System.out.println("Launch NutchWAX job(s) on a hadoop platform.");
        System.out.println("Type 'hadoop jar nutchwax.jar help <job>' for" +
            " help on a specific job.");
        System.out.println("Jobs (usually) must be run in the order " +
            "listed below.");
        System.out.println("Available jobs:");
        System.out.println(" import  Import ARCs.");
        System.out.println(" update  Update dbs with recent imports.");
        System.out.println(" invert  Invert links.");
        System.out.println(" index   Index segments.");
        System.out.println(" dedup   Deduplicate by URL or content MD5.");
        System.out.println(" merge   Merge segment indices into one.");
        System.out.println(" all     Runs all above jobs in order.");
        System.out.println(" class   Run the passed class's main.");
        
        System.exit(exitCode);
    }
    
    public static void doUpdateUsage(final String message,
            final int exitCode) {
        if (message != null && message.length() > 0) {
            System.out.println(message);
        }
        System.out.println("Usage: hadoop jar nutchwax.jar update <output> " +
                "[<segments>...]");
        System.out.println("Arguments:");
        System.out.println(" output    Directory to write crawldb under.");
        System.out.println("Options:");
        System.out.println(" segments  List of segments to update crawldb " +
                "with. If none supplied, updates");
        System.out.println("            using latest segment found.");
        System.exit(exitCode);
    }
    
    public static void doInvertUsage(final String message,
            final int exitCode) {
        if (message != null && message.length() > 0) {
            System.out.println(message);
        }
        System.out.println("Usage: hadoop jar nutchwax.jar invert <output> " +
            "[<segments>...]");
        System.out.println("Arguments:");
        System.out.println(" output    Directory to write linkdb under.");
        System.out.println("Options:");
        System.out.println(" segments  List of segments to update linkdb " +
            "with. If none supplied, all under");
        System.out.println("           '<output>/segments/' " +
            "are passed.");
        System.exit(exitCode);
    }
    
    public static void doIndexUsage(final String message,
            final int exitCode) {
        if (message != null && message.length() > 0) {
            System.out.println(message);
        }
        System.out.println("Usage: hadoop jar nutchwax.jar index <output> " +
            "[<segments>...]");
        System.out.println("Arguments:");
        System.out.println(" output    Directory to write indexes under.");
        System.out.println("Options:");
        System.out.println(" segments  List of segments to index. " +
            "If none supplied, all under");
        System.out.println("           '<output>/segments/' " +
            "are indexed.");
        System.exit(exitCode);
    }
    
    public static void doDedupUsage(final String message,
            final int exitCode) {
        if (message != null && message.length() > 0) {
            System.out.println(message);
        }
        System.out.println("Usage: hadoop jar nutchwax.jar dedup <output>");
        System.out.println("Arguments:");
        System.out.println(" output  Directory in which indices" +
            " to dedup reside.");
        System.exit(exitCode);
    }
    
    public static void doMergeUsage(final String message,
            final int exitCode) {
        if (message != null && message.length() > 0) {
            System.out.println(message);
        }
        System.out.println("Usage: hadoop jar nutchwax.jar merge <output>");
        System.out.println("Arguments:");
        System.out.println(" output  Directory in which indices" +
            " to merge reside.");
        System.exit(exitCode);
    }
    
    public static void doAllUsage(final String message,
            final int exitCode) {
        if (message != null && message.length() > 0) {
            System.out.println(message);
        }
        System.out.println("Usage: hadoop jar nutchwax.jar all <input> " +
            "<output> <collection>");
        System.out.println("Arguments:");
        System.out.println(" input       Directory of files" +
            " listing ARC URLs to import.");
        System.out.println(" output      Directory write indexing product to.");
        System.out.println(" collection  Collection name. Added to" +
            " each resource.");
        System.exit(exitCode);
    }
    
    public static void doClassUsage(final String message,
            final int exitCode) {
        if (message != null && message.length() > 0) {
            System.out.println(message);
        }
        System.out.println("Usage: hadoop jar nutchwax.jar class CLASS ...");
        System.out.println("Arguments:");
        System.out.println(" CLASS    Name of class to run. Invokes main " +
            "passing command-line arguments.");
        System.out.println("          For example, use to run nutch " +
            "commands. Below is list of command");
        System.out.println("          name and implementing class. " +
                "Pass name of class only and emits usage.");
        System.out.println();
        System.out.println("          readdb      " +
            "org.apache.nutch.crawl.CrawlDbReader");
        System.out.println("          mergedb     " +
            "org.apache.nutch.crawl.CrawlDbMerger");
        System.out.println("          readlinkdb  " +
            "org.apache.nutch.crawl.LinkDbReader");
        System.out.println("          segread     " +
            "org.apache.nutch.segment.SegmentReader");
        System.out.println("          mergesegs   " +
            "org.apache.nutch.segment.SegmentMerger");
        System.out.println("          mergelinkdb " +
            "org.apache.nutch.crawl.LinkDbMerger");
        System.exit(exitCode);
    }

    static void doJobHelp(final String jobName) {
        if (!JOBS.contains(jobName)) {
            usage("ERROR: Unknown job " + jobName, 1);
        }
        if (jobName.equals("import")) {
            ImportArcs.doImportUsage(null, 1);
        } else if (jobName.equals("update")) {
            doUpdateUsage(null, 1);
        } else if (jobName.equals("invert")) {
            doInvertUsage(null, 1);
        } else if (jobName.equals("index")) {
            doIndexUsage(null, 1);
        } else if (jobName.equals("dedup")) {
            doDedupUsage(null, 1);
        } else if (jobName.equals("merge")) {
            doMergeUsage(null, 1);
        } else if (jobName.equals("all")) {
            doAllUsage(null, 1);
        } else if (jobName.equals("class")) {
            doClassUsage(null, 1);
        } else {
            usage("ERROR: No help for job name " + jobName, 4);
        }
    }

    public static void main(String args[]) throws Exception {
        if (args.length < 1) {
            usage(null, 0);
            return;
        }

        if (args[0].toLowerCase().equals("help")) {
            if (args.length == 1) {
                usage("ERROR: Add command you need help on.", 0);
                return;
            }
            doJobHelp(args[1].toLowerCase());
        }
        
        final String jobName = args[0].toLowerCase();
        if (!JOBS.contains(jobName)) {
            usage("ERROR: Unknown <job> " + jobName, 1);
        }
        
        Nutchwax ia = new Nutchwax();
        ia.doJob(jobName, args);
    }
}
