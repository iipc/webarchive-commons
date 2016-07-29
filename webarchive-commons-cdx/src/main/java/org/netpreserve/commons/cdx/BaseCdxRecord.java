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
package org.netpreserve.commons.cdx;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.netpreserve.commons.cdx.json.Value;

/**
 * Base class for implementations of CdxRecord.
 * <p>
 * @param <T>
 */
public abstract class BaseCdxRecord<T extends CdxFormat> implements CdxRecord<T> {

    private final T format;

    private CdxRecordKey key;

    protected boolean modified = false;

    /**
     * Factory method for creating CdxRecords.
     * <p>
     * @param data a character array containing raw record formatted according to the format. Might be null if format
     * allows.
     * @param format a forma for the unparsed CDX data.
     * @return the newly created record.
     * @throws IllegalArgumentException if CdxFormat is not recognized by this method.
     */
    public static final CdxRecord create(final char[] data, final CdxFormat format) {
        if (format instanceof CdxLineFormat) {
            return new CdxLine(data, (CdxLineFormat) format);
        } else if (format instanceof CdxjLineFormat) {
            return new CdxjLine(data, (CdxjLineFormat) format);
        } else if (format instanceof NonCdxLineFormat) {
            return new UnconnectedCdxRecord();
        }

        throw new IllegalArgumentException("Unknow CdxFormat: " + format.getClass());
    }

    public static final CdxRecord create(final String value, final CdxFormat format) {
        return create(value.toCharArray(), format);
    }

    public BaseCdxRecord(final T format) {
        this.format = format;
    }

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
     * Helper method to find the index of a character in a char array.
     * <p>
     * @param src the array to search
     * @param ch the char to look for
     * @param fromIndex where in the src to start. If &lt;= 0, the beggining of the array is assumed. If &gt;=
     * src.length, then -1 is returned.
     * @return the index of the first occurence of ch or -1 if not found.
     */
    public static int indexOf(char[] src, char ch, int fromIndex) {
        final int max = src.length;
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= max) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }

        final char[] value = src;
        for (int i = fromIndex; i < max; i++) {
            if (value[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Helper method to find the last index of a character in a char array.
     * <p>
     * @param src the array to search
     * @param ch the char to look for
     * @param fromIndex where in the src to start, searching backwards. If &lt;= 0, the beggining of the array is
     * assumed. If &gt;= src.length, then -1 is returned.
     * @return the index of the last occurence of ch or -1 if not found.
     */
    public static int lastIndexOf(char[] src, char ch, int fromIndex) {
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= src.length) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }

        final char[] value = src;
        for (int i = fromIndex; i >= 0; i--) {
            if (value[i] == ch) {
                return i;
            }
        }
        return -1;
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
            if (next == null) {
                return false;
            }
            return true;
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
     */
    protected static class ImmutableField implements Field {

        private final FieldName name;

        private final Value value;

        /**
         * Constructs an immutable field.
         * <p>
         * @param name the field name
         * @param value the value
         */
        public ImmutableField(FieldName name, Value value) {
            this.name = Objects.requireNonNull(name);
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public FieldName getFieldName() {
            return name;
        }

        @Override
        public Value getValue() {
            return value;
        }

    }
}
