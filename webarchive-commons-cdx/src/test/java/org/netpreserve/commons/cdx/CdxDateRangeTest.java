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

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class CdxDateRangeTest {

    @Test
    public void testFromSingleDate() {
        CdxDateRange date;
        date = CdxDateRange.fromSingleDate(CdxDate.fromWarcDate("2000"));
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-01-01T00:00:00.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2001-01-01T00:00:00.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000101000000000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20010101000000000000000");

        date = CdxDateRange.fromSingleDate(CdxDate.fromWarcDate("2000-02"));
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-01T00:00:00.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-03-01T00:00:00.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000201000000000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000301000000000000000");

        date = CdxDateRange.fromSingleDate(CdxDate.fromWarcDate("2000-02-02"));
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-02T00:00:00.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-02-03T00:00:00.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000202000000000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000203000000000000000");

        date = CdxDateRange.fromSingleDate(CdxDate.fromWarcDate("2000-02-02T03"));
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-02T03:00:00.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-02-02T04:00:00.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000202030000000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000202040000000000000");

        date = CdxDateRange.fromSingleDate(CdxDate.fromWarcDate("2000-02-02T03:13"));
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-02T03:13:00.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-02-02T03:14:00.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000202031300000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000202031400000000000");

        date = CdxDateRange.fromSingleDate(CdxDate.fromWarcDate("2000-02-02T03:13:20"));
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-02T03:13:20.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-02-02T03:13:21.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000202031320000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000202031321000000000");

        date = CdxDateRange.fromSingleDate(CdxDate.fromWarcDate("2000-02-02T03:13:20Z"));
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-02T03:13:20.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-02-02T03:13:21.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000202031320000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000202031321000000000");

        date = CdxDateRange.fromSingleDate(CdxDate.fromWarcDate("2000-02-02T03:13:20.001Z"));
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-02T03:13:20.001000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-02-02T03:13:20.001000001Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000202031320001000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000202031320001000001");
    }

    @Test
    public void testFromHeritrixDate() {
        CdxDateRange date;
        date = CdxDateRange.fromHeritrixDate("2000");
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-01-01T00:00:00.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2001-01-01T00:00:00.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000101000000000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20010101000000000000000");

        date = CdxDateRange.fromHeritrixDate("200002");
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-01T00:00:00.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-03-01T00:00:00.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000201000000000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000301000000000000000");

        date = CdxDateRange.fromHeritrixDate("20000202");
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-02T00:00:00.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-02-03T00:00:00.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000202000000000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000203000000000000000");

        date = CdxDateRange.fromHeritrixDate("2000020203");
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-02T03:00:00.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-02-02T04:00:00.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000202030000000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000202040000000000000");

        date = CdxDateRange.fromHeritrixDate("200002020313");
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-02T03:13:00.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-02-02T03:14:00.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000202031300000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000202031400000000000");

        date = CdxDateRange.fromHeritrixDate("20000202031320");
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-02T03:13:20.000000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-02-02T03:13:21.000000000Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000202031320000000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000202031321000000000");

        date = CdxDateRange.fromHeritrixDate("20000202031320001");
        assertThat(date.toWarcDateFloor()).isEqualTo("2000-02-02T03:13:20.001000000Z");
        assertThat(date.toWarcDateCeeling()).isEqualTo("2000-02-02T03:13:20.001000001Z");
        assertThat(date.toHeritrixDateFloor()).isEqualTo("20000202031320001000000");
        assertThat(date.toHeritrixDateCeeling()).isEqualTo("20000202031320001000001");
    }

}
