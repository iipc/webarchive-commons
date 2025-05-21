/*
 *  This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.archive.io.warc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.archive.format.ArchiveFileConstants;
import org.archive.io.UTF8Bytes;
import org.archive.io.WriterPoolMember;
import org.archive.util.ArchiveUtils;
import org.archive.util.anvl.Element;


/**
 * WARC implementation.
 *
 * <p>Assumption is that the caller is managing access to this
 * WARCWriter ensuring only one thread accessing this WARC instance
 * at any one time.
 * 
 * <p>While being written, WARCs have a '.open' suffix appended.
 *
 * @author stack
 * @version $Revision: 4604 $ $Date: 2006-09-05 22:38:18 -0700 (Tue, 05 Sep 2006) $
 */
public class WARCWriter extends WriterPoolMember
implements WARCConstants {
    public static final String TOTALS = "totals";
    public static final String SIZE_ON_DISK = "sizeOnDisk";
    public static final String TOTAL_BYTES = "totalBytes";
    public static final String CONTENT_BYTES = "contentBytes";
    public static final String NUM_RECORDS = "numRecords";

    private static final Logger logger = 
        Logger.getLogger(WARCWriter.class.getName());
    
    /**
     * NEWLINE as bytes.
     */
    public static byte [] CRLF_BYTES;
    static {
        try {
            CRLF_BYTES = CRLF.getBytes(DEFAULT_ENCODING);
        } catch(Exception e) {
            e.printStackTrace();
        }
    };

    /**
     * Temporarily accumulates stats managed externally by
     * WARCWriterProcessor. WARCWriterProcessor will call
     * {@link #resetTmpStats()}, write some records, then add
     * {@link #getTmpStats()} into its long-term running totals.
     */
    private Map<String, Map<String, Long>> tmpStats;
    
    /** Temporarily accumulates info on written warc records for use externally. */
    private LinkedList<WARCRecordInfo> tmpRecordLog = new LinkedList<WARCRecordInfo>();
    
    /**
     * Constructor.
     * Takes a stream. Use with caution. There is no upperbound check on size.
     * Will just keep writing.  Only pass Streams that are bounded. 
     * @param serialNo  used to generate unique file name sequences
     * @param out Where to write.
     * @param f File the <code>out</code> is connected to.
     */
    public WARCWriter(final AtomicInteger serialNo,
    		final OutputStream out, final File f,
    		final WARCWriterPoolSettings settings)
    throws IOException {
        super(serialNo, out, f, settings);
    }
            
    /**
     * Constructor.
     */
    public WARCWriter(final AtomicInteger serialNo,
            final WARCWriterPoolSettings settings) {
        super(serialNo, settings, WARC_FILE_EXTENSION);
    }
    
    @Override
    protected String createFile(File file) throws IOException {
    	String filename = super.createFile(file);
    	writeWarcinfoRecord(filename);
        return filename;
    }
    
    protected void baseCharacterCheck(final char c, final String parameter)
    throws IllegalArgumentException {
        // TODO: Too strict?  UNICODE control characters?
        if (Character.isISOControl(c) || !Character.isValidCodePoint(c)) {
            throw new IllegalArgumentException("Contains illegal character 0x" +
                Integer.toHexString(c) + ": " + parameter);
        }
    }
    
    protected String checkHeaderValue(final String value)
    throws IllegalArgumentException {
        for (int i = 0; i < value.length(); i++) {
        	final char c = value.charAt(i);
        	baseCharacterCheck(c, value);
        	if (Character.isWhitespace(c)) {
                throw new IllegalArgumentException("Contains disallowed white space 0x" +
                    Integer.toHexString(c) + ": " + value);
        	}
        }
        return value;
    }
    
    protected String checkHeaderLineMimetypeParameter(final String parameter)
    throws IllegalArgumentException {
    	StringBuilder sb = new StringBuilder(parameter.length());
    	boolean wasWhitespace = false;
        for (int i = 0; i < parameter.length(); i++) {
        	char c = parameter.charAt(i);
        	if (Character.isWhitespace(c)) {
        		// Map all to ' ' and collapse multiples into one.
        		// TODO: Make sure white space occurs in legal location --
        		// before parameter or inside quoted-string.
        		if (wasWhitespace) {
        			continue;
        		}
        		wasWhitespace = true;
        		c = ' ';
        	} else {
        		wasWhitespace = false;
        		baseCharacterCheck(c, parameter);
        	}
        	sb.append(c);
        }
        
        return sb.toString();
    }

//    protected String createRecordHeader(final String type,
//    		final String url, final String create14DigitDate,
//    		final String mimetype, final URI recordId,
//    		final ANVLRecord xtraHeaders, final long contentLength)
    protected String createRecordHeader(WARCRecordInfo metaRecord)
    throws IllegalArgumentException {
    	final StringBuilder sb =
    		new StringBuilder(2048/*A SWAG: TODO: Do analysis.*/);
    	sb.append(WARC_ID).append(CRLF);
        sb.append(HEADER_KEY_TYPE).append(COLON_SPACE).append(metaRecord.getType()).
            append(CRLF);
        // Do not write a subject-uri if not one present.
        if (!StringUtils.isEmpty(metaRecord.getUrl())) {
            sb.append(HEADER_KEY_URI).append(COLON_SPACE).
                append(checkHeaderValue(metaRecord.getUrl())).append(CRLF);
        }
        sb.append(HEADER_KEY_DATE).append(COLON_SPACE).
            append(metaRecord.getCreate14DigitDate()).append(CRLF);
        if (metaRecord.getExtraHeaders() != null) {
            for (final Iterator<Element> i = metaRecord.getExtraHeaders().iterator(); i.hasNext();) {
                sb.append(i.next()).append(CRLF);
            }
        }

        sb.append(HEADER_KEY_ID).append(COLON_SPACE).append('<').
            append(metaRecord.getRecordId().toString()).append('>').append(CRLF);
        if (metaRecord.getContentLength() > 0) {
            sb.append(CONTENT_TYPE).append(COLON_SPACE).append(
                checkHeaderLineMimetypeParameter(metaRecord.getMimetype())).append(CRLF);
        }
        sb.append(CONTENT_LENGTH).append(COLON_SPACE).
            append(Long.toString(metaRecord.getContentLength())).append(CRLF);
    	
    	return sb.toString();
    }

    public void writeRecord(WARCRecordInfo recordInfo)
    throws IOException {

        if (recordInfo.getContentLength() == 0 &&
                (recordInfo.getExtraHeaders() == null || recordInfo.getExtraHeaders().size() <= 0)) {
            throw new IllegalArgumentException("Cannot write record " +
            "of content-length zero and base headers only.");
        }

        String header;
        try {
            header = createRecordHeader(recordInfo);

        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE,"could not write record type: " + recordInfo.getType() 
                    + "for URL: " + recordInfo.getUrl(), e);
            return;
        }

        long contentBytes = 0;
        long totalBytes = 0;
        long startPosition;

        startPosition = getPosition();
        try {
            preWriteRecordTasks();

            // TODO: Revisit encoding of header.
            byte[] bytes = header.getBytes(WARC_HEADER_ENCODING);
            write(bytes);
            totalBytes += bytes.length;

            // Write out the header/body separator.
            write(CRLF_BYTES);
            totalBytes += CRLF_BYTES.length;

            if (recordInfo.getContentStream() != null && recordInfo.getContentLength() > 0) {
                contentBytes += copyFrom(recordInfo.getContentStream(),
                        recordInfo.getContentLength(),
                        recordInfo.getEnforceLength());
                totalBytes += contentBytes;
            }

            // Write out the two blank lines at end of all records.
            write(CRLF_BYTES);
            write(CRLF_BYTES);
            totalBytes += 2 * CRLF_BYTES.length;
            
            recordInfo.setWARCFilename(getFilenameWithoutOccupiedSuffix());
            recordInfo.setWARCFileOffset(startPosition);
            tmpRecordLog.add(recordInfo);
        } finally {
            postWriteRecordTasks();
            tally(recordInfo.getType(), contentBytes, totalBytes, getPosition() - startPosition);
        }
    }

    public String getFilenameWithoutOccupiedSuffix() {
        String name = getFile().getName();
        if (name.endsWith(ArchiveFileConstants.OCCUPIED_SUFFIX)) {
            name = name.substring(0, name.length() - ArchiveFileConstants.OCCUPIED_SUFFIX.length());
        }
        return name;
    }
    
    // if compression is enabled, sizeOnDisk means compressed bytes; if not, it
    // should be the same as totalBytes (right?)
    protected void tally(WARCRecordType warcRecordType, long contentBytes, long totalBytes, long sizeOnDisk) {
        if (tmpStats == null) {
            tmpStats = new HashMap<String, Map<String,Long>>();
        }

        // add to stats for this record type
        Map<String, Long> substats = tmpStats.get(warcRecordType.toString());
        if (substats == null) {
            substats = new HashMap<String, Long>();
            tmpStats.put(warcRecordType.toString(), substats);
        }
        subtally(substats, contentBytes, totalBytes, sizeOnDisk);
        
        // add to totals
        substats = tmpStats.get(TOTALS);
        if (substats == null) {
            substats = new HashMap<String, Long>();
            tmpStats.put(TOTALS, substats);
        }
        subtally(substats, contentBytes, totalBytes, sizeOnDisk);
    }

    protected void subtally(Map<String, Long> substats, long contentBytes,
            long totalBytes, long sizeOnDisk) {
        
        if (substats.get(NUM_RECORDS) == null) {
            substats.put(NUM_RECORDS, 1l);
        } else {
            substats.put(NUM_RECORDS, substats.get(NUM_RECORDS) + 1);
        }
        
        if (substats.get(CONTENT_BYTES) == null) {
            substats.put(CONTENT_BYTES, contentBytes);
        } else {
            substats.put(CONTENT_BYTES, substats.get(CONTENT_BYTES) + contentBytes);
        }
        
        if (substats.get(TOTAL_BYTES) == null) {
            substats.put(TOTAL_BYTES, totalBytes);
        } else {
            substats.put(TOTAL_BYTES, substats.get(TOTAL_BYTES) + totalBytes);
        }
        
        if (substats.get(SIZE_ON_DISK) == null) {
            substats.put(SIZE_ON_DISK, sizeOnDisk);
        } else {
            substats.put(SIZE_ON_DISK, substats.get(SIZE_ON_DISK) + sizeOnDisk);
        }
    }

    protected URI generateRecordId(final Map<String, String> qualifiers)
    throws IOException {
        return ((WARCWriterPoolSettings)settings).getRecordIDGenerator().getQualifiedRecordID(qualifiers);
    }
    
    protected URI generateRecordId(final String key, final String value)
    throws IOException {
    	return ((WARCWriterPoolSettings)settings).getRecordIDGenerator().getQualifiedRecordID(key, value);
    }
    
    public URI writeWarcinfoRecord(String filename)
	throws IOException {
    	return writeWarcinfoRecord(filename, null);
    }
    
    public URI writeWarcinfoRecord(String filename, final String description)
        	throws IOException {
        WARCRecordInfo recordInfo = new WARCRecordInfo();
        recordInfo.setType(WARCRecordType.warcinfo);
        recordInfo.setCreate14DigitDate(ArchiveUtils.getLog14Date());
        recordInfo.setMimetype("application/warc-fields");

        // Strip .open suffix if present.
        if (filename.endsWith(ArchiveFileConstants.OCCUPIED_SUFFIX)) {
        	filename = filename.substring(0,
        		filename.length() - ArchiveFileConstants.OCCUPIED_SUFFIX.length());
        }
        recordInfo.addExtraHeader(HEADER_KEY_FILENAME, filename);
        if (description != null && description.length() > 0) {
            recordInfo.addExtraHeader(CONTENT_DESCRIPTION, description);
        }
        
        // Add warcinfo body.
        byte [] warcinfoBody = null;
        if (settings.getMetadata() == null) {
        	// TODO: What to write into a warcinfo?  What to associate?
        	warcinfoBody = "TODO: Unimplemented".getBytes();
        } else {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	for (final Iterator<String> i = settings.getMetadata().iterator();
        			i.hasNext();) {
        		baos.write(i.next().toString().getBytes(UTF8Bytes.UTF8));
        	}
        	warcinfoBody = baos.toByteArray();
        }
        recordInfo.setContentStream(new ByteArrayInputStream(warcinfoBody));
        recordInfo.setContentLength((long) warcinfoBody.length);
        recordInfo.setEnforceLength(true);
        
        recordInfo.setRecordId(generateRecordId(TYPE, WARCRecordType.warcinfo.toString()));
        
        writeRecord(recordInfo);
        
        // TODO: If at start of file, and we're writing compressed,
        // write out our distinctive GZIP extensions.
        return recordInfo.getRecordId();
    }
    
    /**
     * @see WARCWriter#tmpStats for usage model
     */
    public void resetTmpStats() {
        if (tmpStats != null) {
            for (Map<String, Long> substats : tmpStats.values()) {
                for (Entry<String, Long> entry : substats.entrySet()) {
                    entry.setValue(0l);
                }
            }
        }
    }

    public Map<String, Map<String, Long>> getTmpStats() {
        return tmpStats;
    }

    public static long getStat(Map<String, Map<String, Long>> map, String key,
            String subkey) {
        if (map != null && map.get(key) != null
                && map.get(key).get(subkey) != null) {
            return map.get(key).get(subkey);
        } else {
            return 0l;
        }
    }

    public static long getStat(
            ConcurrentMap<String, ConcurrentMap<String, AtomicLong>> map,
            String key, String subkey) {
        if (map != null && map.get(key) != null
                && map.get(key).get(subkey) != null) {
            return map.get(key).get(subkey).get();
        } else {
            return 0l;
        }
    }

    public void resetTmpRecordLog() {
        tmpRecordLog.clear();
    }

    public Iterable<WARCRecordInfo> getTmpRecordLog() {
        return tmpRecordLog;
    }
}
