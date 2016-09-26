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
package org.netpreserve.commons.cdx.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

import org.netpreserve.commons.util.datetime.DateFormat;
import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;

/**
 *
 */
public final class TimestampValue implements Value<VariablePrecisionDateTime>, Comparable<TimestampValue> {

    private VariablePrecisionDateTime value;

    private String unparsed;

    private TimestampValue(final String value) {
        this.unparsed = value;
    }

    private TimestampValue(final VariablePrecisionDateTime value) {
        this.value = value;
    }

    public static TimestampValue valueOf(char[] src, int start, int end) {
        return new TimestampValue(String.copyValueOf(src, start, end - start));
    }

    public static TimestampValue valueOf(VariablePrecisionDateTime value) {
        return new TimestampValue(value);
    }

    public static TimestampValue valueOf(String value) {
        return new TimestampValue(value);
    }

    @Override
    public VariablePrecisionDateTime getValue() {
        if (value == null) {
            value = VariablePrecisionDateTime.valueOf(unparsed);
        }
        return value;
    }

    @Override
    public String toString() {
        if (unparsed == null) {
            unparsed = value.toString();
        }
        return unparsed;
    }

    @Override
    public void toJson(Writer out) throws IOException {
        out.write('\"');
        out.write(getValue().toFormattedString(DateFormat.WARC));
        out.write('\"');
    }

    @Override
    public int compareTo(TimestampValue other) {
        return getValue().compareTo(other.getValue());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(getValue());
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
        final TimestampValue other = (TimestampValue) obj;
        if (!Objects.equals(this.getValue(), other.getValue())) {
            return false;
        }
        return true;
    }

}
