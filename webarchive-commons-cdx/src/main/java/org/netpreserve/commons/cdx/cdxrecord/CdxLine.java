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

import java.util.Arrays;
import java.util.Iterator;
import org.netpreserve.commons.cdx.CdxRecordKey;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.HasUnparsedData;
import org.netpreserve.commons.cdx.json.BooleanValue;
import org.netpreserve.commons.cdx.json.NullValue;
import org.netpreserve.commons.cdx.json.NumberValue;
import org.netpreserve.commons.cdx.json.StringValue;
import org.netpreserve.commons.cdx.json.TimestampValue;
import org.netpreserve.commons.cdx.json.UriValue;
import org.netpreserve.commons.cdx.json.Value;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.util.ArrayUtil;
import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;

/**
 * A CdxRecord constructed from a CDX formatted input line in the legacy CDX format.
 */
public class CdxLine extends BaseCdxRecord<CdxLineFormat> implements HasUnparsedData {

    private static final char EMPTY_FIELD_VALUE = '-';

    final char[] data;

    private int[] fieldOffsets;

    private int[] fieldLengths;

    private transient Value[] fieldCache;

    /**
     * Construct a new CdxLine.
     *
     * @param line a string containing the raw data.
     * @param format the format of the input line.
     */
    public CdxLine(final String line, final CdxLineFormat format) {
        this(line.toCharArray(), format);
    }

    /**
     * Construct a new CdxLine.
     *
     * @param line a character array containing the raw data.
     * @param format the format of the input line.
     */
    public CdxLine(final char[] line, final CdxLineFormat format) {
        super(getKeyFromLine(line), format);
        this.data = line;

        parseFields();
    }

    @Override
    public <V> Value<V> get(FieldName<V> fieldName) {
        return getValue(fieldName, getCdxFormat().indexOf(fieldName));
    }

    /**
     * Get the field value at a certain position in the input line.
     * <p>
     * @param fieldIndex the index of the field requested
     * @return the field's value
     */
    public Value get(int fieldIndex) {
        if (fieldIndex < 0 || fieldIndex >= fieldOffsets.length) {
            throw new IllegalArgumentException("No such field");
        }
        return getValue(getCdxFormat().getField(fieldIndex), fieldIndex);
    }

    /**
     * Get a field value.
     *
     * @param name the field name
     * @param fieldIndex the field index in the input line
     * @return the field's value
     */
    private Value getValue(FieldName name, int fieldIndex) {
        if (fieldIndex < 0 || fieldIndex > getCdxFormat().getLength()) {
            return NullValue.NULL;
        }

        if (fieldCache == null) {
            fieldCache = new Value[getCdxFormat().getLength()];
        }
        if (fieldCache[fieldIndex] != null) {
            return fieldCache[fieldIndex];
        }

        Value result;
        if (data[fieldOffsets[fieldIndex]] == EMPTY_FIELD_VALUE) {
            result = NullValue.NULL;
        } else {
            if (name.getType() == String.class || name.getType() == Object.class) {
                    result = StringValue.valueOf(data, fieldOffsets[fieldIndex],
                            fieldOffsets[fieldIndex] + fieldLengths[fieldIndex]);
            } else if (name.getType() == Number.class) {
                    result = NumberValue.valueOf(data, fieldOffsets[fieldIndex],
                            fieldOffsets[fieldIndex] + fieldLengths[fieldIndex]);
            } else if (name.getType() == Uri.class) {
                    result = UriValue.valueOf(data, fieldOffsets[fieldIndex],
                            fieldOffsets[fieldIndex] + fieldLengths[fieldIndex]);
            } else if (name.getType() == Boolean.class) {
                    result = BooleanValue.valueOf(data, fieldOffsets[fieldIndex],
                            fieldOffsets[fieldIndex] + fieldLengths[fieldIndex]);
            } else if (name.getType() == VariablePrecisionDateTime.class) {
                    result = TimestampValue.valueOf(data, fieldOffsets[fieldIndex],
                            fieldOffsets[fieldIndex] + fieldLengths[fieldIndex]);
            } else {
                result = NullValue.NULL;
            }
        }
        fieldCache[fieldIndex] = result;
        return result;
    }

    @Override
    public boolean hasField(FieldName fieldName) {
        return getCdxFormat().indexOf(fieldName) != CdxLineFormat.MISSING_FIELD;
    }

    /**
     * Gets the data represented by this object.
     * <p>
     * @return the output data
     */
    @Override
    public String toString() {
        if (modified) {
            StringBuilder sb = new StringBuilder(data.length);
            sb.append(getKey().toString());
            for (int i = 2; i < getCdxFormat().getLength(); i++) {
                sb.append(' ');
                Value val = get(i);
                if (val instanceof NullValue) {
                    sb.append('-');
                } else {
                    sb.append(get(i));
                }
            }
            return sb.toString();
        } else {
            return String.valueOf(data);
        }
    }

    @Override
    public char[] getUnparsed() {
        if (modified) {
            StringBuilder sb = new StringBuilder(data.length);
            sb.append(getKey().toString());
            for (int i = 2; i < getCdxFormat().getLength(); i++) {
                sb.append(' ');
                sb.append(get(i));
            }
            char[] result = new char[sb.length()];
            sb.getChars(0, sb.length(), result, 0);
            return result;
        } else {
            return data;
        }
    }

    @Override
    public Iterator<Field> iterator() {
        return new FieldIterator() {
            int fieldIdx = 0;

            CdxLineFormat format = getCdxFormat();

            int length = format.getLength();

            @Override
            protected Field getNext() {
                while (fieldIdx < length) {
                    Value value = get(fieldIdx);
                    if (value != NullValue.NULL) {
                        FieldName name = format.getField(fieldIdx);
                        fieldIdx++;
                        return new ImmutableField(name, value);
                    } else {
                        fieldIdx++;
                    }
                }
                return null;
            }

        };
    }

    /**
     * Find the positions of the fields in the underlying character array.
     */
    private void parseFields() {
        int fieldCount = 0;
        int lastIndex = 0;
        int currIndex;
        char delimiter = getCdxFormat().getDelimiter();
        fieldOffsets = new int[getCdxFormat().getLength()];
        fieldLengths = new int[getCdxFormat().getLength()];

        do {
            currIndex = ArrayUtil.indexOf(data, delimiter, lastIndex);
            if (currIndex > 0) {
                fieldOffsets[fieldCount] = lastIndex;
                fieldLengths[fieldCount] = currIndex - lastIndex;
            } else {
                fieldOffsets[fieldCount] = lastIndex;
                fieldLengths[fieldCount] = data.length - lastIndex;
                break;
            }
            lastIndex = currIndex + 1;
            fieldCount++;
        } while (lastIndex > 0);
    }

    /**
     * Extract the first two fields from a CDX line.
     * <p>
     * @param line the line containing the key
     * @return a CdxRecordKey with the parsed values
     */
    static final CdxRecordKey getKeyFromLine(final char[] line) {
        int indexOfSecondField = ArrayUtil.indexOf(line, ' ', 0);
        if (indexOfSecondField > 0) {
            int indexOfThirdField = ArrayUtil.indexOf(line, ' ', indexOfSecondField + 1);
            if (indexOfThirdField > 0) {
                return new CdxLineRecordKey(Arrays.copyOf(line, indexOfThirdField));
            } else if (line.length > indexOfSecondField + 1) {
                return new CdxLineRecordKey(line);
            }
        }

        throw new IllegalArgumentException("The CDX record '" + new String(line)
                + "' cannot be parsed");
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Arrays.hashCode(this.data);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final CdxLine other = (CdxLine) obj;
        return Arrays.equals(this.data, other.data);
    }

}
