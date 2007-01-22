$Id$

See associated docs directory for requirements, installation and build
instruction or visit http://archive-access.sourceforge.net/projects/nutch/.

The rest of the README is taken up with versions of hadoop and nutch that 
nutchwax depends on including patches made to hadoop and nutch to releases.


HADOOP VERSION AND PATCHES

Hadoop release version is 0.9.2.  0.9.1 fails when you try to use local
filesystem.  Turning of speculative reduce seems to fix things but the
hadoop 0.9.1. has it set to true in bundled hadoop-default.xml.  See
HADOOP-827.   

Here is single patch we make against it (TODO: TEST still works and
still needed):

http://issues.apache.org/jira/browse/HADOOP-145

Index: src/java/org/apache/hadoop/fs/LocalFileSystem.java
===================================================================
--- src/java/org/apache/hadoop/fs/LocalFileSystem.java	(revision 393675)
+++ src/java/org/apache/hadoop/fs/LocalFileSystem.java	(working copy)
@@ -362,6 +362,11 @@
     public void reportChecksumFailure(File f, FSInputStream in,
                                       long start, long length, int crc) {
       try {
+        if (getConf().getBoolean("io.skip.checksum.errors", false)) {
+            // If this flag is set, do not move aside the file.
+            LOG.warn("DEBUG: Not moving file " + p.toString());
+            return;
+        }
         // canonicalize f   
         f = makeAbsolute(f).getCanonicalFile();
       

If you are seeing jobs fail because of complaints about DFS lease expiration,
try the below patch with an ipc.client.timeout setting of 20 or 30 seconds:

Index: src/java/org/apache/hadoop/dfs/DFSClient.java
===================================================================
--- src/java/org/apache/hadoop/dfs/DFSClient.java	(revision 409788)
+++ src/java/org/apache/hadoop/dfs/DFSClient.java	(working copy)
@@ -403,18 +434,23 @@
         public void run() {
             long lastRenewed = 0;
             while (running) {
-                if (System.currentTimeMillis() - lastRenewed > (LEASE_PERIOD / 2)) {
+                // Divide by 3 instead of by 2 so we start renewing earlier
+                // and set down "ipc.client.timeout" from its 60 to 20 or 30.
+                // See this note for why:
+                // http://mail-archives.apache.org/mod_mbox/lucene-hadoop-dev/200607.mbox/%3C331ED54F-9FA7-48FE-A604-017CC54DA524@yahoo-inc.com%3E
+                if (System.currentTimeMillis() - lastRenewed > (LEASE_PERIOD / 3)) {
                     try {
                         namenode.renewLease(clientName);
                         lastRenewed = System.currentTimeMillis();
                     } catch (IOException ie) {
                       String err = StringUtils.stringifyException(ie);
-                      LOG.warning("Problem renewing lease for " + clientName +
+                      LOG.warn("Problem renewing lease for " + clientName +
                                   ": " + err);
                     }
                 }
                 try {
-                    Thread.sleep(1000);
+                    // Renew every 3 seconds, not every 1 second.
+                    Thread.sleep(1000 * 3);
                 } catch (InterruptedException ie) {
                 }
             }


NUTCH VERSION AND PATCHES

Version of nutch on builds.archive.org NutchWAX is built against.

stack@bregeon:~/workspace/nutch$ svn info
Path: .
URL: http://svn.apache.org/repos/asf/lucene/nutch/trunk
Repository Root: http://svn.apache.org/repos/asf
Repository UUID: 13f79535-47bb-0310-9956-ffa450edef68
Revision: 492357
Node Kind: directory
Schedule: normal
Last Changed Author: ab
Last Changed Rev: 491291
Last Changed Date: 2006-12-30 11:13:06 -0800 (Sat, 30 Dec 2006)
Properties Last Updated: 2007-01-03 11:34:45 -0800 (Wed, 03 Jan 2007)

Below are patches made against the nutch thats built into nutchwax.
You may be able to do without them.  Apply if you you are OOME'ing
because too many links found building crawldb or merging segments.

# This patch fixes SegmentMerger OOME'ing.  It puts upper bound on links
# we add to a page (Saw OOME in 1.8 Gig heap trying to add 500k links
# to single key.  Also includes part of NUTCH-333.
Index: src/java/org/apache/nutch/segment/SegmentMerger.java
===================================================================
--- src/java/org/apache/nutch/segment/SegmentMerger.java	(revision 486923)
+++ src/java/org/apache/nutch/segment/SegmentMerger.java	(working copy)
@@ -41,6 +41,7 @@
 import org.apache.nutch.parse.ParseText;
 import org.apache.nutch.protocol.Content;
 import org.apache.nutch.util.NutchConfiguration;
+import org.apache.nutch.util.NutchJob;
 
 /**
  * This tool takes several segments and merges their data together. Only the
@@ -98,6 +99,7 @@
   private URLFilters filters = null;
   private long sliceSize = -1;
   private long curCount = 0;
+  private int maxLinked;
   
   /**
    * Wraps inputs in an {@link MetaWrapper}, to permit merging different
@@ -257,6 +259,7 @@
     if (sliceSize > 0) {
       sliceSize = sliceSize / conf.getNumReduceTasks();
     }
+    this.maxLinked = conf.getInt("db.linked.max", 1000);
   }
   
   private Text newKey = new Text();
@@ -301,7 +304,7 @@
     String lastPDname = null;
     String lastPTname = null;
     TreeMap linked = new TreeMap();
-    while (values.hasNext()) {
+    VALUES_LOOP: while (values.hasNext()) {
       MetaWrapper wrapper = (MetaWrapper)values.next();
       Object o = wrapper.get();
       String spString = wrapper.getMeta(SEGMENT_PART_KEY);
@@ -355,6 +358,17 @@
             linked.put(sp.segmentName, segLinked);
           }
           segLinked.add(val);
+          if (segLinked.size() <= this.maxLinked) {
+        	  segLinked.add(val);
+          } else {
+        	  LOG.info("SKIPPING SEGLINKED LARGE " +
+                  segLinked.size() + ", * linked size " + linked.size() +
+                  ", name " + sp.segmentName + ", key " + key);
+              break VALUES_LOOP;
+           }
+           if ((segLinked.size() % 1000) == 0) {
+               LOG.info("SEGLINKED SIZE " + segLinked.size() + ", key " + key);
+           }
         } else {
           throw new IOException("Cannot determine segment part: " + sp.partName);
         }
@@ -460,7 +474,7 @@
     if (LOG.isInfoEnabled()) {
       LOG.info("Merging " + segs.length + " segments to " + out + "/" + segmentName);
     }
-    JobConf job = new JobConf(getConf());
+    JobConf job = new NutchJob(getConf());
     job.setJobName("mergesegs " + out + "/" + segmentName);
     job.setBoolean("segment.merger.filter", filter);
     job.setLong("segment.merger.slice", slice);
Index: src/java/org/apache/nutch/segment/SegmentReader.java
===================================================================
--- src/java/org/apache/nutch/segment/SegmentReader.java	(revision 486923)
+++ src/java/org/apache/nutch/segment/SegmentReader.java	(working copy)
@@ -36,6 +36,7 @@
 import org.apache.nutch.protocol.Content;
 import org.apache.nutch.util.LogUtil;
 import org.apache.nutch.util.NutchConfiguration;
+import org.apache.nutch.util.NutchJob; 
 
 /** Dump the content of a segment. */
 public class SegmentReader extends Configured implements Reducer {
@@ -147,7 +148,7 @@
   }
 
   private JobConf createJobConf() {
-    JobConf job = new JobConf(getConf());
+    JobConf job = new NutchJob(getConf());
     job.setBoolean("segment.reader.co", this.co);
     job.setBoolean("segment.reader.fe", this.fe);
     job.setBoolean("segment.reader.ge", this.ge);

# NUTCH-311
#
Index: src/java/org/apache/nutch/crawl/CrawlDbReducer.java
===================================================================
--- src/java/org/apache/nutch/crawl/CrawlDbReducer.java	(revision 486923)
+++ src/java/org/apache/nutch/crawl/CrawlDbReducer.java	(working copy)
@@ -38,11 +38,13 @@
   private ArrayList linked = new ArrayList();
   private ScoringFilters scfilters = null;
   private boolean additionsAllowed;
+  private int maxLinked;
 
   public void configure(JobConf job) {
     retryMax = job.getInt("db.fetch.retry.max", 3);
     scfilters = new ScoringFilters(job);
     additionsAllowed = job.getBoolean(CrawlDb.CRAWLDB_ADDITIONS_ALLOWED, true);
+    this.maxLinked = job.getInt("db.linked.max", 10000);
   }
 
   public void close() {}
@@ -56,7 +58,7 @@
     byte[] signature = null;
     linked.clear();
 
-    while (values.hasNext()) {
+    VALUES_LOOP: while (values.hasNext()) {
       CrawlDatum datum = (CrawlDatum)values.next();
 
       if (highest == null || datum.getStatus() > highest.getStatus()) {
@@ -71,6 +73,10 @@
         break;
       case CrawlDatum.STATUS_LINKED:
         linked.add(datum);
+        if (linked.size() > this.maxLinked) {
+            LOG.info("Breaking. " + key + " has > than " + this.maxLinked);
+            break VALUES_LOOP;
+        }
         break;
       case CrawlDatum.STATUS_SIGNATURE:
         signature = datum.getSignature();
