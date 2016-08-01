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
package org.netpreserve.commons.cdx;

import java.util.Objects;

import org.netpreserve.commons.cdx.json.StringValue;
import org.netpreserve.commons.cdx.json.TimestampValue;

/**
 * The key used to lookup records in CDX files.
 * <p>
 * A CDX record key is composed of a canonicalized uri in SURT form, a space and a 14-17 digit time stamp.
 */
public class CdxRecordKey implements Comparable<CdxRecordKey> {

    protected StringValue uriKey;

    protected TimestampValue timeStamp;

    protected StringValue recordType;

    /**
     * Constructor with no argument.
     * <p>
     * Should only be used in specialized subclasses.
     */
    protected CdxRecordKey() {

    }

    /**
     * Construct a new CdxRecordKey.
     *
     * @param uriKey the canonicalized Uri
     * @param timeStamp the timestamp string formatted as a WARC-, or Heritrix timestamp
     * @param recordType the record type.
     */
    public CdxRecordKey(final String uriKey, final String timeStamp, final String recordType) {
        this.uriKey = StringValue.valueOf(Objects.requireNonNull(uriKey));
        this.timeStamp = TimestampValue.valueOf(Objects.requireNonNull(timeStamp));
        this.recordType = StringValue.valueOf(Objects.requireNonNull(recordType));
    }

    /**
     * Construct a new CdxRecordKey.
     *
     * @param uriKey the canonicalized Uri
     * @param timeStamp the timestamp
     * @param recordType the record type.
     */
    public CdxRecordKey(final StringValue uriKey, final TimestampValue timeStamp, final StringValue recordType) {
        this.uriKey = Objects.requireNonNull(uriKey);
        this.timeStamp = Objects.requireNonNull(timeStamp);
        this.recordType = Objects.requireNonNull(recordType);
    }

    /**
     * Get the canonicalized Uri.
     * <p>
     * @return the canonicalized Uri.
     */
    public StringValue getUriKey() {
        return uriKey;
    }

    /**
     * Get the timestamp.
     *
     * @return the timestamp
     */
    public TimestampValue getTimeStamp() {
        return timeStamp;
    }

    /**
     * Get the record type.
     *
     * @return the record type
     */
    public StringValue getRecordType() {
        return recordType;
    }

    @Override
    public int compareTo(CdxRecordKey other) {
        int result = getUriKey().getValue().compareTo(other.getUriKey().getValue());
        if (result == 0) {
            result = getTimeStamp().getValue().compareTo(other.getTimeStamp().getValue());
            if (result == 0) {
                result = getRecordType().getValue().compareTo(other.getRecordType().getValue());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return uriKey.toString() + ' ' + timeStamp.toString() + ' ' + recordType.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(getUriKey());
        hash = 73 * hash + Objects.hashCode(getTimeStamp());
        hash = 73 * hash + Objects.hashCode(getRecordType());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CdxRecord)) {
            return false;
        }
        final CdxRecordKey other = (CdxRecordKey) obj;
        if (!Objects.equals(this.getUriKey(), other.getUriKey())) {
            return false;
        }
        if (!Objects.equals(this.getTimeStamp(), other.getTimeStamp())) {
            return false;
        }
        if (!Objects.equals(this.getRecordType(), other.getRecordType())) {
            return false;
        }
        return true;
    }

}
