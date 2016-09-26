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
package org.netpreserve.commons.cdx.cdxrecord;

import org.netpreserve.commons.cdx.cdxrecord.CdxLine;
import org.netpreserve.commons.cdx.cdxrecord.CdxLineFormat;
import org.junit.Test;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.json.NullValue;
import org.netpreserve.commons.cdx.json.NumberValue;
import org.netpreserve.commons.cdx.json.StringValue;
import org.netpreserve.commons.cdx.json.TimestampValue;
import org.netpreserve.commons.cdx.json.UriValue;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class CdxLineTest {

    /**
     * Test of get method, of class CdxLine.
     */
    @Test
    public void testGet() {
        String line = "as,terra)/gfx/whitepixel.gif 20070821183528"
                + " https://www.terra.as/gfx/whitepixel.gif image/gif 200"
                + " FQ2WDWANAVUTLNKHGLRCOG4HKQWKHLVQ - - 396 36942074"
                + " IAH-20070821182921-00150.arc.gz";
        CdxLine instance = new CdxLine(line, CdxLineFormat.CDX11LINE);

        assertThat(instance.get(FieldName.URI_KEY)).isEqualTo(StringValue.valueOf("as,terra)/gfx/whitepixel.gif"));
        assertThat(instance.get(FieldName.TIMESTAMP)).isEqualTo(TimestampValue.valueOf("2007-08-21T18:35:28Z"));
        assertThat(instance.get(FieldName.ORIGINAL_URI)).isEqualTo(
                UriValue.valueOf("https://www.terra.as/gfx/whitepixel.gif"));
        assertThat(instance.get(FieldName.CONTENT_TYPE)).isEqualTo(StringValue.valueOf("image/gif"));
        assertThat(instance.get(FieldName.RESPONSE_CODE)).isEqualTo(NumberValue.valueOf(200));
        assertThat(instance.get(FieldName.ROBOT_FLAGS)).isEqualTo(NullValue.NULL);
        assertThat(instance.get(FieldName.REDIRECT)).isEqualTo(NullValue.NULL);
        assertThat(instance.get(FieldName.RECORD_LENGTH)).isEqualTo(NumberValue.valueOf(396));
        assertThat(instance.get(FieldName.DIGEST)).isEqualTo(StringValue.valueOf("FQ2WDWANAVUTLNKHGLRCOG4HKQWKHLVQ"));
        assertThat(instance.get(FieldName.OFFSET)).isEqualTo(NumberValue.valueOf(36942074));
        assertThat(instance.get(FieldName.FILENAME)).isEqualTo(StringValue.valueOf("IAH-20070821182921-00150.arc.gz"));

        assertThat(instance.getKey().getUriKey()).isEqualTo(StringValue.valueOf("as,terra)/gfx/whitepixel.gif"));
        assertThat(instance.getKey().getTimeStamp()).isEqualTo(TimestampValue.valueOf("2007-08-21T18:35:28Z"));
        assertThat(instance.getKey().getRecordType()).isEqualTo(StringValue.valueOf("response"));
    }

    /**
     * Test of toString method, of class CdxLine.
     */
    @Test
    public void testToString() {
    }

    /**
     * Test of hashCode method, of class CdxLine.
     */
    @Test
    public void testHashCode() {
    }

    /**
     * Test of equals method, of class CdxLine.
     */
    @Test
    public void testEquals() {
    }

    @Test
    public void testIterator() {
        String line = "as,terra)/gfx/whitepixel.gif 20070821183528"
                + " https://www.terra.as/gfx/whitepixel.gif image/gif 200"
                + " FQ2WDWANAVUTLNKHGLRCOG4HKQWKHLVQ - - 396 36942074"
                + " IAH-20070821182921-00150.arc.gz";
        CdxLine record = new CdxLine(line, CdxLineFormat.CDX11LINE);

        assertThat(record.iterator()).containsOnly(
                new BaseCdxRecord.ImmutableField(FieldName.ORIGINAL_URI,
                        UriValue.valueOf("https://www.terra.as/gfx/whitepixel.gif")),
                new BaseCdxRecord.ImmutableField(FieldName.CONTENT_TYPE, StringValue.valueOf("image/gif")),
                new BaseCdxRecord.ImmutableField(FieldName.RESPONSE_CODE, NumberValue.valueOf(200)),
                new BaseCdxRecord.ImmutableField(FieldName.TIMESTAMP, TimestampValue.valueOf("2007-08-21T18:35:28Z")),
                new BaseCdxRecord.ImmutableField(FieldName.OFFSET, NumberValue.valueOf(36942074)),
                new BaseCdxRecord.ImmutableField(FieldName.FILENAME,
                        StringValue.valueOf("IAH-20070821182921-00150.arc.gz")),
                new BaseCdxRecord.ImmutableField(FieldName.RECORD_TYPE, StringValue.valueOf("response")),
                new BaseCdxRecord.ImmutableField(FieldName.DIGEST,
                        StringValue.valueOf("FQ2WDWANAVUTLNKHGLRCOG4HKQWKHLVQ")),
                new BaseCdxRecord.ImmutableField(FieldName.URI_KEY,
                        StringValue.valueOf("as,terra)/gfx/whitepixel.gif")),
                new BaseCdxRecord.ImmutableField(FieldName.RECORD_LENGTH, NumberValue.valueOf(396))
        );
    }

}
