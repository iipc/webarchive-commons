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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netpreserve.commons.cdx.json.NullValue;
import org.netpreserve.commons.cdx.json.SimpleJsonParser;
import org.netpreserve.commons.cdx.json.Value;

/**
 * A representation of a line in a CDX file.
 */
public class CdxjLine extends BaseCdxRecord<CdxjLineFormat> implements HasUnparsedData {

    final char[] data;

    final int jsonBlockOffset;

    Map<FieldName, Field> fields;

    public CdxjLine(final String line, final CdxjLineFormat format) {
        this(line.toCharArray(), format);
    }

    public CdxjLine(final char[] line, final CdxjLineFormat format) {
        super(getKeyFromLine(line), format);
        this.data = line;
        this.jsonBlockOffset = ((CdxjLineRecordKey) getKey()).getUnparsed().length;

//        parseFields();
    }

//    public CdxjLine(final char[] key, final char[] value, CdxjLineFormat format) {
//        super(new CdxRecordKey(key), format);
//        this.data = value;
//    }
//
//    public CdxjLine(final char[] line, final CdxjLineFormat format) {
//        super(getKeyFromLine(line), format);
//        this.data = Arrays.copyOfRange(line, getKey().length() + 1, line.length);
//    }
//
//    public CdxjLine(final String line, final CdxjLineFormat format) {
//        this(line.toCharArray(), format);
//    }

    @Override
    public Value get(FieldName fieldName) {
        parseFields();
        Field f = fields.get(fieldName);
        if (f == null) {
            return NullValue.NULL;
        } else {
            return f.getValue();
        }
    }

    @Override
    public boolean hasField(FieldName fieldName) {
        parseFields();
        return fields.containsKey(fieldName);
    }

    @Override
    public Iterator<Field> iterator() {
        parseFields();
        return fields.values().iterator();
    }

    final void parseFields() {
        if (fields == null) {
            Map<FieldName,Value> parsedFieldMap = new SimpleJsonParser(data, jsonBlockOffset).parseObject();
            fields = new HashMap<>();
            for (Map.Entry<FieldName, Value> entry : parsedFieldMap.entrySet()) {
                fields.put(entry.getKey(), new ImmutableField(entry.getKey(), entry.getValue()));
            }
        }
    }

//    @Override
//    public int hashCode() {
//        int hash = 1;
//        char[] array = data.array();
//        int start = data.arrayOffset();
//        int limit = start + fieldLengths[0] + fieldLengths[1];
//        for (int i = limit; i >= start; i--) {
//            hash = 31 * hash + (int) array[i];
//        }
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final CdxjLine other = (CdxjLine) obj;
//        if (!Objects.equals(this.data, other.data)) {
//            return false;
//        }
//        return true;
//    }
    /**
     * Extract the fields before the json block from a CDXJ line.
     * <p>
     * @param line the line containing the key
     * @return a CdxRecordKey with the parsed values
     */
    static final CdxRecordKey getKeyFromLine(final char[] line) {
        int indexOfJsonBlock = indexOf(line, '{', 0);
        if (indexOfJsonBlock == -1) {
            throw new IllegalArgumentException("The CDX record '" + new String(line)
                    + "' cannot be parsed");
        }
        return new CdxjLineRecordKey(Arrays.copyOf(line, indexOfJsonBlock - 1));
    }

    @Override
    public char[] getUnparsed() {
        return data;
    }

    @Override
    public String toString() {
        return getKey().toString() + ' ' + String.copyValueOf(data);
    }

}
