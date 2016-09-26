/*
 * Copyright 2016 The International Internet Preservation Consortium.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.json.NullValue;
import org.netpreserve.commons.cdx.json.SimpleJsonParser;
import org.netpreserve.commons.cdx.json.Value;

/**
 * CdxRecord implementation with no relation to a specific format.
 * <p>
 * This is typically used when creating a new CDX file from scratch e.g. from a WARC file.
 */
public class UnconnectedCdxRecord extends BaseCdxRecord<NonCdxLineFormat> {

    Map<FieldName, Field> fields = new HashMap<>();

    /**
     * Default constructor.
     */
    public UnconnectedCdxRecord() {
        super(NonCdxLineFormat.FORMAT);
    }

    /**
     * Create instance by copying content from another CdxRecord.
     * <p>
     * @param cdxRecord the record to make a copy of
     */
    public UnconnectedCdxRecord(CdxRecord cdxRecord) {
        super(NonCdxLineFormat.FORMAT);
        for (Field f : cdxRecord) {
            fields.put(f.getFieldName(), f);
        }
    }

    /**
     * Create instance by parsing a json block formatted according to the json block part of the CDXJ format.
     * <p>
     * @param json the data to parse
     */
    public UnconnectedCdxRecord(char[] json) {
        super(NonCdxLineFormat.FORMAT);
        Map<FieldName, Value> parsedFieldMap = new SimpleJsonParser(json, 0).parseObject();
        fields = new HashMap<>();
        for (Map.Entry<FieldName, Value> entry : parsedFieldMap.entrySet()) {
            fields.put(entry.getKey(), new ImmutableField(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Create instance by parsing a json block formatted according to the json block part of the CDXJ format.
     * <p>
     * @param json the string to parse
     */
    public UnconnectedCdxRecord(String json) {
        this(json.toCharArray());
    }

    @Override
    public <T> Value<T> get(FieldName<T> fieldName) {
        Field f = fields.get(fieldName);
        if (f == null) {
            if (getKey() != null) {
                if (fieldName == FieldName.URI_KEY) {
                    return (Value<T>) getKey().getUriKey();
                }
                if (fieldName == FieldName.TIMESTAMP) {
                    return (Value<T>) getKey().getTimeStamp();
                }
                if (fieldName == FieldName.RECORD_TYPE) {
                    return (Value<T>) getKey().getRecordType();
                }
            }
            return NullValue.NULL;
        } else {
            return f.getValue();
        }
    }

    /**
     * Set a fields value.
     * <p>
     * @param fieldName the name of the field.
     * @param value the value for the field.
     * @param <T> The Java type encapsulated by the field value
     * @return returns this object for the convenience of chaining method calls.
     */
    public <T> UnconnectedCdxRecord set(FieldName<T> fieldName, Value<T> value) {
        if (value.getValue() == null) {
            return this;
        }
        fields.put(fieldName, new ImmutableField(fieldName, value));
        return this;
    }

    @Override
    public boolean hasField(FieldName fieldName) {
        return fields.containsKey(fieldName);
    }

    @Override
    public Iterator<Field> iterator() {
        return fields.values().iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean notFirst = false;
        for (Field f : fields.values()) {
            if (notFirst) {
                sb.append(", ");
            } else {
                notFirst = true;
            }
            sb.append(f);
        }
        return sb.append("}").toString();
    }

}
