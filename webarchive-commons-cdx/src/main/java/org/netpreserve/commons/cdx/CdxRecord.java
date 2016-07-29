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

import java.util.Iterator;

import org.netpreserve.commons.cdx.json.Value;

/**
 * A CDX record.
 * <p>
 * @param <T> The format of the raw data this record is read from. If no such raw data exist, a format of
 * {@link NonCdxLineFormat} should be used.
 */
public interface CdxRecord<T extends CdxFormat> extends
        Comparable<CdxRecord>, Iterable<CdxRecord.Field> {

    /**
     * Get the format of the raw data connected to this record.
     * <p>
     * @return The format of the raw data this record is read from or {@link NonCdxLineFormat}, if no such raw data
     * exist.
     */
    T getCdxFormat();

    /**
     * Get the key for this record.
     * <p>
     * @return the key or null if no one exist
     */
    CdxRecordKey getKey();

    /**
     * Set the key for this record.
     * <p>
     * @param recordKey the key to set
     */
    void setKey(CdxRecordKey recordKey);

    /**
     * Convenience method to get a named field by its name as a String.
     * <p>
     * @param fieldName the name of the requested field
     * @return the field value
     * @see #get(org.netpreserve.openwayback.cdxlib.FieldName)
     */
    Value get(String fieldName);

    /**
     * Get a named field.
     * <p>
     * @param fieldName the name of the requested field
     * @return the field value
     */
    Value get(FieldName fieldName);

    /**
     * Returns true if this record contains a value for the specified field name.
     * <p>
     * @param fieldName field name whose presence is to be tested.
     * @return true if this record contains a value for the specified field name.
     */
    boolean hasField(FieldName fieldName);

    @Override
    Iterator<CdxRecord.Field> iterator();

    /**
     * Compares this object with the specified CdxRecord for order.
     * <p>
     * Returns a negative integer, zero, or a positive integer as this CdxRecord is less than, equal to, or greater than
     * the specified CdxRecord.
     * <p>
     * Note: This method uses only the key for determining the natural order. It is expected that the key fields are the
     * url key, timestamp and record type which in general uniquely identifies the line (but it is not guaranteed to do
     * so). In contrast the {@link #equals(java.lang.Object)} method compares the whole record. It is then possible that
     * (x.compareTo(y)==0) == (x.equals(y)) is not always true.
     * <p>
     * @param other the CdxRecord to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     * the specified object
     * @throws NullPointerException if the specified object is null
     */
    @Override
    int compareTo(CdxRecord other);

    /**
     * A combination of field name and value used for the field iterator.
     */
    interface Field {

        /**
         * Get the field name.
         * <p>
         * @return the FieldName object. Never null.
         */
        FieldName getFieldName();

        /**
         * Get the value.
         * <p>
         * The value should never be null. To indicate a null value, an object of type
         * {@link org.netpreserve.commons.cdx.json.NullValue} should be returned.
         * <p>
         * @return the Value object. Never null.
         */
        Value getValue();

    }
}
