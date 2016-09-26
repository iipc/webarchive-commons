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

import java.util.BitSet;
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
 * @param <T> The format of the raw data this record is read from. If no such raw data exist, a format of
 * {@link NonCdxLineFormat} should be used.
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

        private final BitSet handledKeyFields = new BitSet(3);

        private CdxRecord.Field next;

        @Override
        public final boolean hasNext() {
            if (next == null) {
                next = getNext();

                // Make sure that also the key fields gets included in the iterator
                if (next != null) {
                    if (next.getFieldName() == FieldName.URI_KEY) {
                        handledKeyFields.set(0);
                    } else if (next.getFieldName() == FieldName.TIMESTAMP) {
                        handledKeyFields.set(1);
                    } else if (next.getFieldName() == FieldName.RECORD_TYPE) {
                        handledKeyFields.set(2);
                    }
                } else {
                    if (!handledKeyFields.get(0)) {
                        handledKeyFields.set(0);
                        next = new ImmutableField(FieldName.URI_KEY, get(FieldName.URI_KEY));
                    } else if (!handledKeyFields.get(1)) {
                        handledKeyFields.set(1);
                        next = new ImmutableField(FieldName.TIMESTAMP, get(FieldName.TIMESTAMP));
                    } else if (!handledKeyFields.get(2)) {
                        handledKeyFields.set(2);
                        next = new ImmutableField(FieldName.RECORD_TYPE, get(FieldName.RECORD_TYPE));
                    }
                }
            }

            return next != null;
        }

        @Override
        public final CdxRecord.Field next() {
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
     * <p>
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

        @Override
        public String toString() {
            return "\"" + name + "\"=" + value.toJson();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.name);
            hash = 61 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!Field.class.isInstance(obj)) {
                return false;
            }
            final Field<?> other = (Field<?>) obj;
            if (!Objects.equals(this.name, other.getFieldName())) {
                return false;
            }
            if (!Objects.equals(this.value, other.getValue())) {
                return false;
            }
            return true;
        }

    }
}
