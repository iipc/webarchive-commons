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
 * Tests for DateTimeRange.
 */
public class DateTimeRangeTest {

    /**
     * Test of ofSingleDate method, of class DateTimeRange.
     */
    @Test
    public void testOfSingleDate() {
        DateTimeRange date;
        date = DateTimeRange.ofSingleDate(VariablePrecisionDateTime.valueOf("2000"));
        assertThat(date.getStart().toFormattedString(DateFormat.WARC)).isEqualTo("2000-01-01T00:00:00.000000000Z");
        assertThat(date.getEnd().toFormattedString(DateFormat.WARC)).isEqualTo("2001-01-01T00:00:00.000000000Z");
        assertThat(date.getStart().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000101000000000000000");
        assertThat(date.getEnd().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20010101000000000000000");

        date = DateTimeRange.ofSingleDate(VariablePrecisionDateTime.valueOf("2000-02"));
        assertThat(date.getStart().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-01T00:00:00.000000000Z");
        assertThat(date.getEnd().toFormattedString(DateFormat.WARC)).isEqualTo("2000-03-01T00:00:00.000000000Z");
        assertThat(date.getStart().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000201000000000000000");
        assertThat(date.getEnd().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000301000000000000000");

        date = DateTimeRange.ofSingleDate(VariablePrecisionDateTime.valueOf("2000-02-02"));
        assertThat(date.getStart().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-02T00:00:00.000000000Z");
        assertThat(date.getEnd().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-03T00:00:00.000000000Z");
        assertThat(date.getStart().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000202000000000000000");
        assertThat(date.getEnd().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000203000000000000000");

        date = DateTimeRange.ofSingleDate(VariablePrecisionDateTime.valueOf("2000-02-02T03"));
        assertThat(date.getStart().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-02T03:00:00.000000000Z");
        assertThat(date.getEnd().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-02T04:00:00.000000000Z");
        assertThat(date.getStart().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000202030000000000000");
        assertThat(date.getEnd().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000202040000000000000");

        date = DateTimeRange.ofSingleDate(VariablePrecisionDateTime.valueOf("2000-02-02T03:13"));
        assertThat(date.getStart().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-02T03:13:00.000000000Z");
        assertThat(date.getEnd().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-02T03:14:00.000000000Z");
        assertThat(date.getStart().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000202031300000000000");
        assertThat(date.getEnd().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000202031400000000000");

        date = DateTimeRange.ofSingleDate(VariablePrecisionDateTime.valueOf("2000-02-02T03:13:20"));
        assertThat(date.getStart().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-02T03:13:20.000000000Z");
        assertThat(date.getEnd().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-02T03:13:21.000000000Z");
        assertThat(date.getStart().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000202031320000000000");
        assertThat(date.getEnd().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000202031321000000000");

        date = DateTimeRange.ofSingleDate(VariablePrecisionDateTime.valueOf("2000-02-02T03:13:20Z"));
        assertThat(date.getStart().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-02T03:13:20.000000000Z");
        assertThat(date.getEnd().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-02T03:13:21.000000000Z");
        assertThat(date.getStart().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000202031320000000000");
        assertThat(date.getEnd().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000202031321000000000");

        date = DateTimeRange.ofSingleDate(VariablePrecisionDateTime.valueOf("2000-02-02T03:13:20.001Z"));
        assertThat(date.getStart().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-02T03:13:20.001000000Z");
        assertThat(date.getEnd().toFormattedString(DateFormat.WARC)).isEqualTo("2000-02-02T03:13:20.001000001Z");
        assertThat(date.getStart().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000202031320001000000");
        assertThat(date.getEnd().toFormattedString(DateFormat.HERITRIX)).isEqualTo("20000202031320001000001");
    }

    /**
     * Test of ofRangeExpression method, of class DateTimeRange.
     */
    @Test
    public void testOfRangeExpression() {
        DateTimeRange date;
        date = DateTimeRange.ofRangeExpression("2007");
        assertThat(date.getStart().toFormattedString(DateFormat.WARC)).isEqualTo("2007-01-01T00:00:00.000000000Z");
        assertThat(date.getEnd().toFormattedString(DateFormat.WARC)).isEqualTo("2008-01-01T00:00:00.000000000Z");

        date = DateTimeRange.ofRangeExpression("2007;2009");
        assertThat(date.getStart().toFormattedString(DateFormat.WARC)).isEqualTo("2007-01-01T00:00:00.000000000Z");
        assertThat(date.getEnd().toFormattedString(DateFormat.WARC)).isEqualTo("2009-01-01T00:00:00.000000000Z");

        date = DateTimeRange.ofRangeExpression("2007;");
        assertThat(date.getStart().toFormattedString(DateFormat.WARC)).isEqualTo("2007-01-01T00:00:00.000000000Z");
        assertThat(date.getEnd()).isNull();

        date = DateTimeRange.ofRangeExpression(";2009");
        assertThat(date.getStart()).isNull();
        assertThat(date.getEnd().toFormattedString(DateFormat.WARC)).isEqualTo("2009-01-01T00:00:00.000000000Z");

        date = DateTimeRange.ofRangeExpression(null);
        assertThat(date.getStart()).isNull();
        assertThat(date.getEnd()).isNull();

        date = DateTimeRange.ofRangeExpression("");
        assertThat(date.getStart()).isNull();
        assertThat(date.getEnd()).isNull();
    }

    /**
     * Test of contains method, of class DateTimeRange.
     */
    @Test
    public void testContains() {
        DateTimeRange date;
        date = DateTimeRange.ofRangeExpression("2007-01-01");
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-01"))).isTrue();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-01T13:14"))).isTrue();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-02"))).isFalse();

        date = DateTimeRange.ofRangeExpression("2007-01-01;2007-01-03");
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-01"))).isTrue();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-01T13:14"))).isTrue();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-02"))).isTrue();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-03"))).isFalse();

        date = DateTimeRange.ofRangeExpression("2007-01-02;");
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-01"))).isFalse();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-01T13:14"))).isFalse();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-02"))).isTrue();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-03"))).isTrue();

        date = DateTimeRange.ofRangeExpression(";2007-01-02");
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-01"))).isTrue();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-01T13:14"))).isTrue();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-02"))).isFalse();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-03"))).isFalse();

        date = DateTimeRange.ofRangeExpression(null);
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-01"))).isTrue();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-01T13:14"))).isTrue();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-02"))).isTrue();
        assertThat(date.contains(VariablePrecisionDateTime.valueOf("2007-01-03"))).isTrue();
    }
}
