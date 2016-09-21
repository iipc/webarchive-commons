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

package org.netpreserve.commons.util.datetime;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class DateFormatTest {

    /**
     * Test of format method, of class DateFormat.
     */
    @Test
    public void testFormat() {
        VariablePrecisionDateTime dateTime;

        dateTime = VariablePrecisionDateTime.valueOf("2016");
        assertThat(DateFormat.WARC.format(dateTime)).isEqualTo("2016");
        assertThat(DateFormat.HERITRIX.format(dateTime)).isEqualTo("2016");
        assertThat(DateFormat.RFC1123.format(dateTime)).isEqualTo("Fri, 1 Jan 2016 00:00:00 GMT");

        dateTime = VariablePrecisionDateTime.valueOf("2016-12");
        assertThat(DateFormat.WARC.format(dateTime)).isEqualTo("2016-12");
        assertThat(DateFormat.HERITRIX.format(dateTime)).isEqualTo("201612");
        assertThat(DateFormat.RFC1123.format(dateTime)).isEqualTo("Thu, 1 Dec 2016 00:00:00 GMT");

        dateTime = VariablePrecisionDateTime.valueOf("2016-12-01");
        assertThat(DateFormat.WARC.format(dateTime)).isEqualTo("2016-12-01");
        assertThat(DateFormat.HERITRIX.format(dateTime)).isEqualTo("20161201");
        assertThat(DateFormat.RFC1123.format(dateTime)).isEqualTo("Thu, 1 Dec 2016 00:00:00 GMT");

        dateTime = VariablePrecisionDateTime.valueOf("2016-12-01T12Z");
        assertThat(DateFormat.WARC.format(dateTime)).isEqualTo("2016-12-01T12Z");
        assertThat(DateFormat.HERITRIX.format(dateTime)).isEqualTo("2016120112");
        assertThat(DateFormat.RFC1123.format(dateTime)).isEqualTo("Thu, 1 Dec 2016 12:00:00 GMT");

        dateTime = VariablePrecisionDateTime.valueOf("2016-12-01T12:13Z");
        assertThat(DateFormat.WARC.format(dateTime)).isEqualTo("2016-12-01T12:13Z");
        assertThat(DateFormat.HERITRIX.format(dateTime)).isEqualTo("201612011213");
        assertThat(DateFormat.RFC1123.format(dateTime)).isEqualTo("Thu, 1 Dec 2016 12:13:00 GMT");

        dateTime = VariablePrecisionDateTime.valueOf("2016-12-01T12:13:54Z");
        assertThat(DateFormat.WARC.format(dateTime)).isEqualTo("2016-12-01T12:13:54Z");
        assertThat(DateFormat.HERITRIX.format(dateTime)).isEqualTo("20161201121354");
        assertThat(DateFormat.RFC1123.format(dateTime)).isEqualTo("Thu, 1 Dec 2016 12:13:54 GMT");

        dateTime = VariablePrecisionDateTime.valueOf("2016-12-01T12:13:54.335Z");
        assertThat(DateFormat.WARC.format(dateTime)).isEqualTo("2016-12-01T12:13:54.335000000Z");
        assertThat(DateFormat.HERITRIX.format(dateTime)).isEqualTo("20161201121354335000000");
        assertThat(DateFormat.RFC1123.format(dateTime)).isEqualTo("Thu, 1 Dec 2016 12:13:54 GMT");
    }

}