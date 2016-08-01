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
package org.netpreserve.commons.cdx.cdxrecord;

import java.util.Objects;

import org.netpreserve.commons.cdx.CdxRecordKey;
import org.netpreserve.commons.cdx.HasUnparsedData;
import org.netpreserve.commons.cdx.json.StringValue;
import org.netpreserve.commons.cdx.json.TimestampValue;

/**
 * Representation of the key used to lookup records in CDX files.
 * <p>
 * A CDX record key is composed of a canonicalized uri in SURT form, a space and a 14-17 digit time stamp.
 */
public class CdxLineRecordKey extends CdxRecordKey implements HasUnparsedData {

    private static final StringValue DEFAULT_RECORD_TYPE = StringValue.valueOf("response");

    private final char[] data;

    /**
     * Construct a new CdxRecordKey from a char array.
     * <p>
     * @param data the complete key as a char array
     */
    public CdxLineRecordKey(final char[] data) {
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Construct a new CdxRecordKey from a string.
     * <p>
     * @param data the complete key as a string
     */
    public CdxLineRecordKey(final String data) {
        this.data = Objects.requireNonNull(data).toCharArray();
    }

    @Override
    public StringValue getUriKey() {
        if (uriKey == null) {
            parse();
        }
        return uriKey;
    }

    @Override
    public TimestampValue getTimeStamp() {
        if (timeStamp == null) {
            parse();
        }
        return timeStamp;
    }

    @Override
    public StringValue getRecordType() {
        if (recordType == null) {
            parse();
        }
        return recordType;
    }

    @Override
    public int compareTo(CdxRecordKey other) {
        if (other instanceof CdxLineRecordKey) {
            char[] thisData = data;
            char[] otherData = ((CdxLineRecordKey) other).data;
            int lim = Math.min(thisData.length, otherData.length);

            int k = 0;
            while (k < lim) {
                char c1 = thisData[k];
                char c2 = otherData[k];
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
            return thisData.length - otherData.length;
        } else {
            return super.compareTo(other);
        }
    }

    @Override
    public char[] getUnparsed() {
        return data;
    }

    @Override
    public String toString() {
        parse();
        return super.toString();
    }

    /**
     * Parse the char array into its components.
     */
    private void parse() {
        if (uriKey == null) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == ' ') {
                    uriKey = StringValue.valueOf(data, 0, i);
                    timeStamp = TimestampValue.valueOf(data, i + 1, data.length);
                    recordType = DEFAULT_RECORD_TYPE;
                    return;
                }
            }
            throw new IllegalArgumentException("The CDX record key '" + new String(data)
                    + "' cannot be parsed");
        }
    }

}
