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
package org.netpreserve.commons.cdx.time;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

import org.netpreserve.commons.cdx.CdxDate;

/**
 *
 */
public class WarcDateFormatter {

    public static final DateTimeFormatter WARC_DATE_YEAR = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .toFormatter(Locale.ENGLISH);

    public static final DateTimeFormatter WARC_DATE_MONTH = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .toFormatter(Locale.ENGLISH);

    public static final DateTimeFormatter WARC_DATE_DAY = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .toFormatter(Locale.ENGLISH);

    public static final DateTimeFormatter WARC_DATE_HOUR = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .toFormatter(Locale.ENGLISH);

    public static final DateTimeFormatter WARC_DATE_MINUTE = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .toFormatter(Locale.ENGLISH);

    public static final DateTimeFormatter WARC_DATE_SECOND = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendOffsetId()
            .toFormatter(Locale.ENGLISH);

    public static final DateTimeFormatter WARC_DATE_ALL = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendFraction(ChronoField.NANO_OF_SECOND, 9, 9, true)
            .appendOffsetId()
            .toFormatter(Locale.ENGLISH);

    public String format(CdxDate date) {
        return format(date.getDate(), date.getGranularity());
    }

    public String format(CdxDate date, CdxDate.Granularity granularity) {
        return format(date.getDate(), granularity);
    }

    public String format(OffsetDateTime date, CdxDate.Granularity granularity) {
        switch (granularity) {
            case YEAR:
                return date.format(WARC_DATE_YEAR);
            case MONTH:
                return date.format(WARC_DATE_MONTH);
            case DAY:
                return date.format(WARC_DATE_DAY);
            case HOUR:
                return date.format(WARC_DATE_HOUR);
            case MINUTE:
                return date.format(WARC_DATE_MINUTE);
            case SECOND:
                return date.format(WARC_DATE_SECOND);
            default:
                return date.format(WARC_DATE_ALL);
        }
    }
}
