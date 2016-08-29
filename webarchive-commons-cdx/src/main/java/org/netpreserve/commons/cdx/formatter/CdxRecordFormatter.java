/*
 * Copyright 2016 IIPC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netpreserve.commons.cdx.formatter;

import java.io.IOException;
import java.io.Writer;

import org.netpreserve.commons.cdx.CdxFormat;
import org.netpreserve.commons.cdx.cdxrecord.CdxLineFormat;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.CdxRecordKey;
import org.netpreserve.commons.cdx.cdxrecord.CdxjLineFormat;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.HasUnparsedData;
import org.netpreserve.commons.cdx.SearchResult;
import org.netpreserve.commons.cdx.cdxrecord.NonCdxLineFormat;
import org.netpreserve.commons.cdx.json.NullValue;
import org.netpreserve.commons.cdx.json.StringValue;
import org.netpreserve.commons.cdx.json.TimestampValue;
import org.netpreserve.commons.cdx.json.Value;
import org.netpreserve.commons.uri.Configurations;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriBuilder;

/**
 *
 */
public class CdxRecordFormatter {

    private static final CdxLineFormatter CDX_LINE_FORMATTER = new CdxLineFormatter();

    private static final CdxjLineFormatter CDXJ_LINE_FORMATTER = new CdxjLineFormatter();

    private final CdxFormat format;

    private final CdxFormatter formatter;

    public CdxRecordFormatter(CdxFormat format) {
        this.format = format;
        if (format instanceof CdxLineFormat) {
            formatter = CDX_LINE_FORMATTER;
        } else if (format instanceof CdxjLineFormat) {
            formatter = CDXJ_LINE_FORMATTER;
        } else {
            throw new IllegalArgumentException("No formatter for format: " + format.getClass());
        }
    }

    public CdxRecordFormatter(CdxFormatter formatter) {
        this.format = NonCdxLineFormat.FORMAT;
        this.formatter = formatter;
    }

    public String format(final CdxRecord record) {
        return format(record, false);
    }

    public String format(final CdxRecord record, final boolean createKey) {
        if (createKey) {
            CdxRecordKey key = createKey(record);
            record.setKey(key);
        } else if (record instanceof HasUnparsedData && record.getCdxFormat().equals(format)) {
            return new String(((HasUnparsedData) record).getUnparsed());
        }

        StringBuilderWriter sbw = new StringBuilderWriter();
        try {
            formatter.format(sbw, record, format);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return sbw.toString();
    }

    public void format(final Writer out, final CdxRecord record, final boolean createKey) throws IOException {
        if (createKey) {
            CdxRecordKey key = createKey(record);
            record.setKey(key);
        } else if (record instanceof HasUnparsedData && record.getCdxFormat().equals(format)) {
            out.write(((HasUnparsedData) record).getUnparsed());
            return;
        }

        try {
            formatter.format(out, record, format);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void format(final Writer out, final SearchResult searchResult, final long limit)
            throws IOException {

        long count = 0;
        for (CdxRecord cdxLine : searchResult) {
            if (count >= limit) {
                break;
            }
            count++;

            format(out, cdxLine, false);
            out.append('\n');
        }

    }

    /**
     * Create a key for a record from the uri and timestamp of the record.
     * <p>
     * @param record the record to create a key for.
     * @return the created key.
     */
    CdxRecordKey createKey(CdxRecord record) {
        Uri surt = UriBuilder.builder(Configurations.SURT_KEY)
                .uri(record.get(FieldName.ORIGINAL_URI).toString()).build();

        Value timeStamp = record.get(FieldName.TIMESTAMP);
        if (timeStamp instanceof NullValue) {
            timeStamp = record.getKey().getTimeStamp();
        }

        Value recordType = record.get(FieldName.RECORD_TYPE);
        if (recordType instanceof NullValue) {
            recordType = record.getKey().getRecordType();
        }

        CdxRecordKey key = new CdxRecordKey(
                StringValue.valueOf(surt.toString()), (TimestampValue) timeStamp, (StringValue) recordType);

        return key;
    }

}
