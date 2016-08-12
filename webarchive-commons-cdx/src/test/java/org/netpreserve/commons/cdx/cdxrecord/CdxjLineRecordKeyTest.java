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
import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Test methods for CdxjLineRecordKey.
 */
public class CdxjLineRecordKeyTest {

    /**
     * Test of getUriKey method, of class CdxjLineRecordKey.
     */
    @Test
    public void testGetUriKey() {
        String keyString1 = "(no,vg,)/";
        CdxjLineRecordKey key1 = new CdxjLineRecordKey(keyString1);
        assertThatThrownBy(() -> {
            key1.getUriKey();
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing the timestamp and record-type field")
                .hasNoCause();

        String keyString2 = "(no,vg,)/ 20070904150410";
        CdxjLineRecordKey key2 = new CdxjLineRecordKey(keyString2);
        assertThatThrownBy(() -> {
            key2.getUriKey();
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing the record-type field")
                .hasNoCause();

        String keyString3 = "(no,vg,)/ 20070904150410 request";
        CdxjLineRecordKey key3 = new CdxjLineRecordKey(keyString3);
        assertThat(key3.getUriKey().getValue()).isEqualTo("(no,vg,)/");
    }

    /**
     * Test of getTimeStamp method, of class CdxjLineRecordKey.
     */
    @Test
    public void testGetTimeStamp() {
        String keyString = "(no,vg,)/ 20070904150410 request";
        CdxjLineRecordKey key = new CdxjLineRecordKey(keyString);
        assertThat(key.getTimeStamp().getValue()).isEqualTo(VariablePrecisionDateTime.of("2007-09-04T15:04:10"));
    }

    /**
     * Test of getRecordType method, of class CdxjLineRecordKey.
     */
    @Test
    public void testGetRecordType() {
        String keyString = "(no,vg,)/ 20070904150410 request";
        CdxjLineRecordKey key = new CdxjLineRecordKey(keyString);
        assertThat(key.getRecordType().getValue()).isEqualTo("request");
    }

    /**
     * Test of compareTo method, of class CdxjLineRecordKey.
     */
    @Test
    public void testCompareTo() {
        String keyString1 = "(no,vg,)/ 20070904150410 request";
        CdxjLineRecordKey key1 = new CdxjLineRecordKey(keyString1);

        String keyString2 = "(no,vg,)/ 20070904150410 response";
        CdxjLineRecordKey key2 = new CdxjLineRecordKey(keyString2);

        String keyString3 = "(no,vg,)/ 20070904150411 request";
        CdxjLineRecordKey key3 = new CdxjLineRecordKey(keyString3);

        assertThat(key2).isGreaterThan(key1);
        assertThat(key3).isGreaterThan(key1);
        assertThat(key3).isGreaterThan(key2);
    }

    /**
     * Test of getUnparsed method, of class CdxjLineRecordKey.
     */
    @Test
    public void testGetUnparsed() {
        String keyString = "(no,vg,)/ 20070904150410 request";
        CdxjLineRecordKey key = new CdxjLineRecordKey(keyString);
        assertThat(key.getUnparsed()).isEqualTo(keyString.toCharArray());
    }

    /**
     * Test of toString method, of class CdxjLineRecordKey.
     */
    @Test
    public void testToString() {
        String keyString = "(no,vg,)/ 20070904150410 request";
        CdxjLineRecordKey key = new CdxjLineRecordKey(keyString);
        assertThat(key).hasToString(keyString);
    }

}
