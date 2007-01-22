/* WaxIndexingFilter
 * 
 * $Id$
 * 
 * Copyright (C) 2005 Internet Archive.
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
package org.archive.access.nutch.indexer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.crawl.MapWritable;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.util.mime.MimeType;
import org.apache.nutch.util.mime.MimeTypeException;
import org.apache.nutch.util.mime.MimeTypes;
import org.archive.access.nutch.NutchwaxConfiguration;
import org.archive.util.ArchiveUtils;
import org.archive.util.Base32;


/**
 * Add to the index fields needed by Internet Archive searching.
 * 
 * @author Stack
 */
public class WaxIndexingFilter implements IndexingFilter {
    public static final Log LOGGER =
        LogFactory.getLog(WaxIndexingFilter.class.getName());
    
    private Configuration conf;
    
    // Below names need to sync with whats in
    // {@link org.archive.access.nutch.Arc2Segment}
    public static final String ARCFILENAME_KEY = "arcname";
    public static final String ARCFILEOFFSET_KEY = "arcoffset";
    public static final String ARCCOLLECTION_KEY = "collection";
    public static final String DATE_KEY = "date";
    private static final String CONTENT_TYPE_KEY = "content-type";

    public static final String EXACTURL_KEY = "exacturl";

    /**
     * Set into metadata by the nutch html parser.
     */
    private static final String ENCODING_KEY = "CharEncodingForConversion";

    private MessageDigest md = null;

    /** Get the MimeTypes resolver instance. */
    private final static MimeTypes MIME =
        MimeTypes.get(NutchwaxConfiguration.getConfiguration().
            get("mime.types.file"));
    
    public WaxIndexingFilter() throws NoSuchAlgorithmException {
        super();
        this.md = MessageDigest.getInstance("MD5");
    }

    public Document filter(Document doc, Parse parse, Text url,
            CrawlDatum datum, Inlinks inlinks) {
        if (url == null || url.getLength() <= 0) {
            LOGGER.error(doc.toString() + " has no url");
            return doc;
        }
        String urlStr = url.toString();
        // Stored, indexed and un-tokenized. Date is already GMT so don't
        // mess w/ timezones. Date is stored as seconds since epoch to
        // facilitate sorting (The Lucene Sort interprets the IA 14-char
        // date string as a float; rounding of float values equates floats
        // that shouldn't equate: e.g:
        // float f = Float.parseFloat("20050524133833");
        // float g = Float.parseFloat("20050524133834");
        // float h = Float.parseFloat("20050524133835");
        // System.out.println(f == g);
        // System.out.println(f == h);
        // ...prints true twice.
        // So, have seconds since epoch for the date we index.
        long seconds = datum.getFetchTime() / 1000;
        if (seconds > Integer.MAX_VALUE) {
            LOGGER.warn("Fetch time " + Long.toString(seconds) +
                " is > Integer.MAX_VALUE. Setting to zero");
            seconds = 0;
        }
        doc.add(new Field(DATE_KEY, ArchiveUtils.zeroPadInteger((int) seconds),
                Field.Store.YES, Field.Index.UN_TOKENIZED));

        // Add as stored, unindexed, and untokenized. Don't warn if absent.
        // Its not a tradegy.
        add(urlStr, doc, "encoding", parse.getData().getMeta(ENCODING_KEY),
                false, true, true, false, false);
        
        
        // Get metadatas.
        MapWritable mw = datum.getMetaData();
        ParseData pd = parse.getData();
        
        // Add as stored, indexed, and untokenized but not lowercased.
        add(urlStr, doc, ARCCOLLECTION_KEY,
        	getMetadataValue(ARCCOLLECTION_KEY, pd, mw),
                false, true, true, false);
        
        // Add as stored, indexed, and untokenized. Preserve case for
        // arcname since eventually it will be used to find an arc on
        // filesystem.
        add(urlStr, doc, ARCFILENAME_KEY,
        	getMetadataValue(ARCFILENAME_KEY, pd, mw),
                false, true, true, false);
        add(urlStr, doc, ARCFILEOFFSET_KEY,
        	getMetadataValue(ARCFILEOFFSET_KEY, pd, mw),
                false, true, false, false);
        
        // This is a nutch 'more' field.
        add(urlStr, doc, "contentLength",
            parse.getData().getMeta("contentLength"),
                false, true, false, false);
        // Mimetype. The ARC2Segment tool stores the content-type into
        // metadata with a key of 'content-type'.
        String mimetype = parse.getData().getMeta(CONTENT_TYPE_KEY);
        if (mimetype == null || mimetype.length() == 0) {
            MimeType mt = (MIME.getMimeType(urlStr));
            if (mt != null) {
                mimetype = mt.getName();
            }
        }
        try {
            // Test the mimetype makes some sense. If not, don't add.
            mimetype = (new MimeType(mimetype)).getName();
        } catch (MimeTypeException e) {
            LOGGER.error(urlStr + ", mimetype " + mimetype + ": "
                    + e.toString());
            // Clear mimetype because caused exception.
            mimetype = null;
        }
        if (mimetype != null) {
            // wera wants the sub and primary types in index. So they are
            // stored but not searchable. nutch adds primary and subtypes
            // as well as complete type all to one 'type' field.
            final String type = "type";
            add(urlStr, doc, type, mimetype, true, false, true, false);
            int index = mimetype.indexOf('/');
            if (index > 0) {
                String tmp = mimetype.substring(0, index);
                add(urlStr, doc, "primaryType", tmp, true, true, false, false);
                add(urlStr, doc, type, tmp, true, false, true, false);
                if (index + 1 < mimetype.length()) {
                    tmp = mimetype.substring(index + 1);
                    add(urlStr, doc, "subType", tmp, true, true, false, false);
                    add(urlStr, doc, type, tmp, true, false, true, false);
                }
            }
        }
        // Add as not lowercased, not stored, indexed, and not tokenized.
        add(urlStr, doc, EXACTURL_KEY, escapeUrl(url.toString()), false, false,
                true, false);
        return doc;
    }
    
    private String getMetadataValue(final String key, final ParseData pd,
    		final MapWritable mw) {
        String v = pd.getMeta(key);
        if (v == null || v.length() == 0 && mw != null) {
        	Writable w = mw.get(new Text(key));
        	if (w != null) {
        		v = w.toString();
        	}
        }
        return v;
    }

    private String escapeUrl(String url) {
        this.md.reset();
        return Base32.encode(this.md.digest(url.getBytes()));
    }
    
    private void add(final String url, final Document doc,
            final String fieldName, final String fieldValue,
            boolean lowerCase, boolean store, boolean index,
            boolean tokenize) {
        add(url, doc, fieldName, fieldValue, lowerCase, store, index, tokenize,
            true);
    }

    private void add(final String url, final Document doc,
            final String fieldName, final String fieldValue,
            boolean lowerCase, boolean store, boolean index,
            boolean tokenize, final boolean warn) {
        if (fieldValue == null || fieldValue.length() <= 0) {
            if (warn) {
                LOGGER.error("No " + fieldName + " for url " + url);
            }
            return;
        }
        doc.add(new Field(fieldName,
            (lowerCase? fieldValue.toLowerCase(): fieldValue),
            store? Field.Store.YES: Field.Store.NO,
            index?
                (tokenize? Field.Index.TOKENIZED: Field.Index.UN_TOKENIZED):
                Field.Index.NO));
    }

    public Configuration getConf() {
        return this.conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }
}
