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

        warcDate = VariablePrecisionDateTime.valueOf("2016-12");
        heritrixDate = VariablePrecisionDateTime.valueOf("201612");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T00:00Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.MONTH);

        warcDate = VariablePrecisionDateTime.valueOf("2016-12-01T12:13:54.335Z");
        heritrixDate = VariablePrecisionDateTime.valueOf("20161201121354335");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:13:54.335Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.NANOSECOND);

        warcDate = VariablePrecisionDateTime.valueOf("2016-12-01T12");
        heritrixDate = VariablePrecisionDateTime.valueOf("2016120112");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:00Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.HOUR);

        warcDate = VariablePrecisionDateTime.valueOf("2016-12-01T12:13Z");
        heritrixDate = VariablePrecisionDateTime.valueOf("201612011213");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:13Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.MINUTE);

        warcDate = VariablePrecisionDateTime.valueOf("2016-12-01T12:13:00Z");
        heritrixDate = VariablePrecisionDateTime.valueOf("20161201121300");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:13Z"))
                .hasFieldOrPropertyWithValue("granularity", Granularity.SECOND);

    }

}
