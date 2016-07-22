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

import java.time.OffsetDateTime;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for Class CdxDate.
 */
public class CdxDateTest {

    /**
     * Test of of method, of class CdxDate.
     */
    @Test
    public void testOf() {
        CdxDate warcDate;
        CdxDate heritrixDate;

        warcDate = CdxDate.of("2016-12");
        heritrixDate = CdxDate.of("201612");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T00:00Z"))
                .hasFieldOrPropertyWithValue("granularity", CdxDate.Granularity.MONTH);

        warcDate = CdxDate.of("2016-12-01T12:13:54.335Z");
        heritrixDate = CdxDate.of("20161201121354335");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:13:54.335Z"))
                .hasFieldOrPropertyWithValue("granularity", CdxDate.Granularity.NANOSECOND);

        warcDate = CdxDate.of("2016-12-01T12");
        heritrixDate = CdxDate.of("2016120112");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:00Z"))
                .hasFieldOrPropertyWithValue("granularity", CdxDate.Granularity.HOUR);

        warcDate = CdxDate.of("2016-12-01T12:13Z");
        heritrixDate = CdxDate.of("201612011213");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:13Z"))
                .hasFieldOrPropertyWithValue("granularity", CdxDate.Granularity.MINUTE);

        warcDate = CdxDate.of("2016-12-01T12:13:00Z");
        heritrixDate = CdxDate.of("20161201121300");
        assertThat(warcDate)
                .isEqualTo(heritrixDate)
                .hasFieldOrPropertyWithValue("date", OffsetDateTime.parse("2016-12-01T12:13Z"))
                .hasFieldOrPropertyWithValue("granularity", CdxDate.Granularity.SECOND);

    }

}
