/*
 * $Id: ImportArcs.java 1494 2007-02-15 17:47:58Z stack-sf $
 * 
 * Copyright (C) 2007 Internet Archive.
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
package org.archive.access.nutch.mapred;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapRunnable;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.ReflectionUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.arc.ARCConstants;
import org.archive.io.arc.ARCRecord;

/**
 * MapRunner that passes an ARCRecord to configured mapper.
 * Configured mapper must be implementation of {@link ARCMapRunner}.
 * @author stack
 */
public class ARCMapRunner implements MapRunnable {
    public final Log LOG = LogFactory.getLog(this.getClass().getName());
    private ARCRecordMapper mapper;

    /**
     * How long to spend indexing.
     */
    private long maxtime;

    
    public void configure(JobConf job) {
      this.mapper = (ARCRecordMapper)ReflectionUtils.
          newInstance(job.getMapperClass(), job);
      // Value is in minutes.
      this.maxtime = job.getLong("wax.index.timeout", 60) * 60 * 1000;
    }
    
    public void run(RecordReader input, OutputCollector output,
            Reporter reporter)
    throws IOException {
        try {
            WritableComparable key = input.createKey(); // Unused.
            Writable value = input.createValue();
            while (input.next(key, value)) {
                doArc(value.toString(), output, new ARCReporter(reporter));
            }
        } finally {
            this.mapper.close();
        }
    }
    
    protected void doArc(final String arcurl, final  OutputCollector output,
            final ARCReporter reporter)
    throws IOException {
        if ((arcurl == null) || arcurl.endsWith("work")) {
            reporter.setStatus("skipping " + arcurl, true);
            return;
        }

        // Set off indexing in a thread so I can cover it with a timer.
        final Thread t = new IndexingThread(arcurl, output, reporter);
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
            cleanup(t, reporter);
        }
    }
    
    protected void cleanup(final Thread t, final ARCReporter reporter)
            throws IOException {
        if (!t.isAlive()) {
            return;
        }
        reporter.setStatus("Killing indexing thread " + t.getName(), true);
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
        private final ARCReporter reporter;

        public IndexingThread(final String arcloc, final OutputCollector o,
                final ARCReporter r) {
            // Name this thread same as ARC location.
            super(arcloc);
            this.arcLocation = arcloc;
            this.output = o;
            this.reporter = r;
        }
        
        /**
         * @return Null if fails download.
         */
        protected ArchiveReader getArchiveReader() {
            ArchiveReader arc = null;
            // Need a thread that will keep updating TaskTracker during long
            // downloads else tasktracker will kill us.
            Thread reportingDuringDownload = null;
            try {
                this.reporter.setStatus("opening " + this.arcLocation, true);
                reportingDuringDownload = new Thread("reportDuringDownload") {
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
            } finally {
                if ((reportingDuringDownload != null)
                        && reportingDuringDownload.isAlive()) {
                    reportingDuringDownload.interrupt();
                }
            }
            return arc;
        }

        public void run() {
            if (this.arcLocation == null || this.arcLocation.length() <= 0) {
                return;
            }
            ArchiveReader arc = getArchiveReader();
            if (arc == null) {
                return;
            }

            try {
                ARCMapRunner.this.mapper.onARCOpen();
                
                // Iterate over each ARCRecord.
                for (final Iterator i = arc.iterator();
                        i.hasNext() && !currentThread().isInterrupted();) {
                    final ARCRecord rec = (ARCRecord)i.next();
                    
                    
                    try {
                        ARCMapRunner.this.mapper.map(
                            new Text(rec.getMetaData().getUrl()),
                            new ObjectWritable(rec), this.output,
                            this.reporter);
                        
                        final long b = rec.getMetaData().getContentBegin();
                        final long l = rec.getMetaData().getLength();
                        final long recordLength = (l > b)? (l - b): l;
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
                            this.reporter.setStatus("skipping " +
                                this.arcLocation + " -- very long record " +
                                rec.getMetaData());
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
                    ARCMapRunner.this.mapper.onARCClose();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}