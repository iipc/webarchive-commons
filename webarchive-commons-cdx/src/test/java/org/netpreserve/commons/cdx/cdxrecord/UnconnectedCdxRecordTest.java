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

import org.junit.Test;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.json.NullValue;
import org.netpreserve.commons.cdx.json.NumberValue;
import org.netpreserve.commons.cdx.json.StringValue;
import org.netpreserve.commons.cdx.json.TimestampValue;
import org.netpreserve.commons.cdx.json.UriValue;
import org.netpreserve.commons.util.datetime.DateFormat;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class UnconnectedCdxRecordTest {

    /**
     * Test of get method, of class UnconnectedCdxRecord.
     */
    @Test
    public void testGet() {
        String valueString = "{\"uri\":\"http://www.dagbladet.no/premier2000/spiller_2519.html\","
                + "\"mct\":\"text/html\",\"hsc\":404,"
                + "\"sha\":\"4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7\",\"rle\":1506,"
                + "\"ref\":\"warcfile:IAH-20070907235053-00459-heritrix.arc.gz#68224437\","
                + "\"sts\":\"2007-08-23T21:51:21Z\"}";

        CdxRecord record = new UnconnectedCdxRecord(valueString);

        assertThat(record.get(FieldName.URI_KEY)).isEqualTo(NullValue.NULL);
        assertThat(record.get(FieldName.TIMESTAMP).getValue().toFormattedString(DateFormat.WARC))
                .isEqualTo("2007-08-23T21:51:21Z");
        assertThat(record.get(FieldName.ORIGINAL_URI).getValue().toString())
                .isEqualTo("http://www.dagbladet.no/premier2000/spiller_2519.html");
        assertThat(record.get(FieldName.CONTENT_TYPE).getValue()).isEqualTo("text/html");
        assertThat(record.get(FieldName.RESPONSE_CODE).getValue().intValue()).isEqualTo(404);
        assertThat(record.get(FieldName.PAYLOAD_DIGEST).getValue()).isEqualTo("4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7");
        assertThat(record.get(FieldName.RECORD_LENGTH).getValue().intValue()).isEqualTo(1506);
        assertThat(record.get(FieldName.RESOURCE_REF).getValue().toString())
                .isEqualTo("warcfile:IAH-20070907235053-00459-heritrix.arc.gz#68224437");
    }

    /**
     * Test of iterator method, of class UnconnectedCdxRecord.
     */
    @Test
    public void testIterator() {
        String valueString = "{\"uri\":\"http://www.dagbladet.no/premier2000/spiller_2519.html\","
                + "\"mct\":\"text/html\",\"hsc\":404,"
                + "\"sha\":\"4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7\",\"rle\":1506,"
                + "\"ref\":\"warcfile:IAH-20070907235053-00459-heritrix.arc.gz#68224437\"}";

        CdxRecord record = new UnconnectedCdxRecord(valueString);

        assertThat(record.iterator()).containsOnly(
                new BaseCdxRecord.ImmutableField(FieldName.ORIGINAL_URI,
                        UriValue.valueOf("http://www.dagbladet.no/premier2000/spiller_2519.html")),
                new BaseCdxRecord.ImmutableField(FieldName.CONTENT_TYPE, StringValue.valueOf("text/html")),
                new BaseCdxRecord.ImmutableField(FieldName.RESPONSE_CODE, NumberValue.valueOf(404)),
                new BaseCdxRecord.ImmutableField(FieldName.RESOURCE_REF,
                        UriValue.valueOf("warcfile:IAH-20070907235053-00459-heritrix.arc.gz#68224437")),
                new BaseCdxRecord.ImmutableField(FieldName.PAYLOAD_DIGEST,
                        StringValue.valueOf("4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7")),
                new BaseCdxRecord.ImmutableField(FieldName.RECORD_LENGTH, NumberValue.valueOf(1506))
        );
    }

    /**
     * Test of toString method, of class UnconnectedCdxRecord.
     */
    @Test
    public void testToString() {
        String valueString = "{\"uri\":\"http://www.dagbladet.no/premier2000/spiller_2519.html\","
                + "\"mct\":\"text/html\",\"hsc\":404,"
                + "\"sha\":\"4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7\",\"rle\":1506,"
                + "\"ref\":\"warcfile:IAH-20070907235053-00459-heritrix.arc.gz#68224437\"}";

        CdxRecord record = new UnconnectedCdxRecord(valueString);

        assertThatJson(record.toString()).isEqualTo(valueString);
    }

}
