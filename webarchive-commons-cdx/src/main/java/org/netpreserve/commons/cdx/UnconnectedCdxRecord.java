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
package org.netpreserve.commons.cdx;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netpreserve.commons.cdx.json.NullValue;
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

    @Override
    public Value get(FieldName fieldName) {
        Field f = fields.get(fieldName);
        if (f == null) {
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
     * @return returns this object for the convenience of chaining method calls.
     */
    public UnconnectedCdxRecord set(FieldName fieldName, Value value) {
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

}
