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
import org.netpreserve.commons.util.ArrayUtil;

/**
 * Representation of the key used to lookup records in CDX files.
 * <p>
 * A CDX record key is composed of a canonicalized uri in SURT form, a space and a 14-17 digit time stamp.
 */
public class CdxjLineRecordKey extends CdxRecordKey implements HasUnparsedData {

    private final char[] data;

    /**
     * Construct a new CdxRecordKey from a char array.
     * <p>
     * @param data the complete key as a char array
     */
    public CdxjLineRecordKey(final char[] data) {
        this.data = Objects.requireNonNull(data);
    }

    public CdxjLineRecordKey(final String data) {
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
        if (other instanceof CdxjLineRecordKey) {
            char[] thisData = data;
            char[] otherData = ((CdxjLineRecordKey) other).data;
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
            try {
                int firstDelim = ArrayUtil.indexOf(data, ' ', 0);
                if (firstDelim > 0) {
                    uriKey = StringValue.valueOf(data, 0, firstDelim);
                    firstDelim++;

                    int secondDelim = ArrayUtil.indexOf(data, ' ', firstDelim);
                    if (secondDelim > 0) {
                        timeStamp = TimestampValue.valueOf(data, firstDelim, secondDelim);
                        recordType = StringValue.valueOf(data, secondDelim + 1, data.length);
                        return;
                    } else {
                        throw new IllegalArgumentException("The CDX record key '" + new String(data)
                                + "' is missing the record-type field");
                    }
                } else {
                    throw new IllegalArgumentException("The CDX record key '" + new String(data)
                            + "' is missing the timestamp and record-type field");
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("The CDX record key '" + new String(data)
                        + "' cannot be parsed", e);
            }
        }
    }

}
