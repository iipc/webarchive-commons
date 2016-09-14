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

import java.time.OffsetDateTime;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for Class VariablePrecisionDateTimeTest.
 */
public class VariablePrecisionDateTimeTest {

    /**
     * Test of of method, of class VariablePrecisionDateTimeTest.
     */
    @Test
    public void testValueOf() {
        VariablePrecisionDateTime warcDate;
        VariablePrecisionDateTime heritrixDate;
        VariablePrecisionDateTime rfc1123Date;

        warcDate = VariablePrecisionDateTime.valueOf("2016-12");
        heritrixDate = VariablePrecisionDateTime.valueOf("201612");
        rfc1123Date = VariablePrecisionDateTime.valueOf("Thu, 1 Dec 2016 00:00:00 GMT");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T00:00Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.MONTH);
        assertThat(rfc1123Date)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T00:00Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.SECOND);

        warcDate = VariablePrecisionDateTime.valueOf("2016-12-01T12:13:54.335Z");
        heritrixDate = VariablePrecisionDateTime.valueOf("20161201121354335");
        rfc1123Date = VariablePrecisionDateTime.valueOf("Thu, 1 Dec 2016 12:13:54 GMT");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:13:54.335Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.NANOSECOND);
        assertThat(rfc1123Date)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:13:54Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.SECOND);

        warcDate = VariablePrecisionDateTime.valueOf("2016-12-01T12");
        heritrixDate = VariablePrecisionDateTime.valueOf("2016120112");
        rfc1123Date = VariablePrecisionDateTime.valueOf("Thu, 1 Dec 2016 12:00:00 GMT");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:00Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.HOUR);
        assertThat(rfc1123Date)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:00Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.SECOND);

        warcDate = VariablePrecisionDateTime.valueOf("2016-12-01T12:13Z");
        heritrixDate = VariablePrecisionDateTime.valueOf("201612011213");
        rfc1123Date = VariablePrecisionDateTime.valueOf("Thu, 1 Dec 2016 12:13:00 GMT");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:13Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.MINUTE);
        assertThat(rfc1123Date)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:13Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.SECOND);

        warcDate = VariablePrecisionDateTime.valueOf("2016-12-01T12:13:00Z");
        heritrixDate = VariablePrecisionDateTime.valueOf("20161201121300");
        rfc1123Date = VariablePrecisionDateTime.valueOf("Thu, 1 Dec 2016 12:13:00 GMT");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:13Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.SECOND);
        assertThat(rfc1123Date)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:13Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.SECOND);

    }

}
