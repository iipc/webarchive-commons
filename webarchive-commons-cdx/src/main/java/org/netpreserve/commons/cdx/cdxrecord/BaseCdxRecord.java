/*
 * Copyright 2015 IIPC.
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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.netpreserve.commons.cdx.CdxFormat;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.CdxRecordKey;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.json.Value;

/**
 * Base class for implementations of CdxRecord.
 * <p>
 * @param <T> The format of the raw data this record is read from. If no such
 * raw data exist, a format of {@link NonCdxLineFormat} should be used.
 */
public abstract class BaseCdxRecord<T extends CdxFormat> implements CdxRecord {

    private final T format;

    private CdxRecordKey key;

    protected boolean modified = false;

    /**
     * Constructs an empty BaseCdxRecord.
     * <p>
     * @param format the format for the raw, possibly unparsed data.
     */
    public BaseCdxRecord(final T format) {
        this.format = format;
    }

    /**
     * Constructs a BaseCdxRecord with only the key set.
     * <p>
     * @param key the key for this record.
     * @param format the format for the raw, possibly unparsed data.
     */
    public BaseCdxRecord(final CdxRecordKey key, final T format) {
        this.format = format;
        this.key = key;
    }

    @Override
    public T getCdxFormat() {
        return format;
    }

    @Override
    public CdxRecordKey getKey() {
        return key;
    }

    @Override
    public void setKey(CdxRecordKey recordKey) {
        this.key = recordKey;
        this.modified = true;
    }

    @Override
    public Value get(String fieldName) {
        return get(FieldName.forName(fieldName));
    }

    @Override
    public int compareTo(CdxRecord other) {
        return key.compareTo(other.getKey());
    }

    /**
     * Helper class for implementing an iterator over a CdxRecord's fields.
     */
    protected abstract class FieldIterator implements Iterator<CdxRecord.Field> {

        private CdxRecord.Field next;

        @Override
        public boolean hasNext() {
            if (next == null) {
                next = getNext();
            }
            return next != null;
        }

        @Override
        public CdxRecord.Field next() {
            if (hasNext()) {
                CdxRecord.Field result = next;
                next = null;
                return result;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Get the next record.
         * <p>
         * The only method needed to be implemented by subclasses.
         * <p>
         * @return the next record.
         */
        protected abstract CdxRecord.Field getNext();

    }

    /**
     * An immutable implementation of the {@link Field} interface.
     * @param <T> the Java type encapsulated by this field
     */
    protected static class ImmutableField<T> implements Field<T> {

        private final FieldName<T> name;

        private final Value<T> value;

        /**
         * Constructs an immutable field.
         * <p>
         * @param name the field name
         * @param value the value
         */
        public ImmutableField(FieldName<T> name, Value<T> value) {
            this.name = Objects.requireNonNull(name);
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public FieldName<T> getFieldName() {
            return name;
        }

        @Override
        public Value<T> getValue() {
            return value;
        }

    }
}
