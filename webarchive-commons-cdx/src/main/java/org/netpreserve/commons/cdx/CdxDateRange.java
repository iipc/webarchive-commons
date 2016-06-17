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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

import static java.time.temporal.ChronoField.*;

/**
 * A representation of a date used for searching.
 * <p>
 * As dates are allowed to be of variable precision, this class computes the earliest and latest dates that will fit
 * into the submitted date.
 * <p>
 * This class is immutable and thread safe.
 */
public final class CdxDateRange {

    static final DateTimeFormatter WARC_DATE_FORMAT_FLOOR;

    static final DateTimeFormatter WARC_DATE_FORMAT_CEELING;

    static final DateTimeFormatter WARC_DATE_OUTPUT_FORMAT;

    static final DateTimeFormatter HERITRIX_DATE_FORMAT_FLOOR;

    static final DateTimeFormatter HERITRIX_DATE_FORMAT_CEELING;

    static final DateTimeFormatter HERITRIX_DATE_OUTPUT_FORMAT;

    static {
        WARC_DATE_FORMAT_FLOOR = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .optionalStart()
                .appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .optionalStart()
                .appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .optionalStart()
                .appendLiteral('T')
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .optionalStart()
                .appendOffsetId()
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                .toFormatter(Locale.ENGLISH);

        WARC_DATE_FORMAT_CEELING = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .optionalStart()
                .appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .optionalStart()
                .appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .optionalStart()
                .appendLiteral('T')
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .optionalStart()
                .appendOffsetId()
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 12)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 31)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 23)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 59)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 59)
                .parseDefaulting(ChronoField.NANO_OF_SECOND, 999999999)
                .toFormatter(Locale.ENGLISH);

        WARC_DATE_OUTPUT_FORMAT = new DateTimeFormatterBuilder()
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

        HERITRIX_DATE_FORMAT_FLOOR = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .optionalStart()
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .optionalStart()
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .optionalStart()
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .optionalStart()
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, false)
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                .toFormatter(Locale.ENGLISH);

        HERITRIX_DATE_FORMAT_CEELING = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .optionalStart()
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .optionalStart()
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .optionalStart()
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .optionalStart()
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, false)
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .optionalEnd()
                .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 12)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 31)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 23)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 59)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 59)
                .parseDefaulting(ChronoField.NANO_OF_SECOND, 999999999)
                .toFormatter(Locale.ENGLISH);

        HERITRIX_DATE_OUTPUT_FORMAT = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .appendFraction(ChronoField.NANO_OF_SECOND, 9, 9, false)
                .toFormatter(Locale.ENGLISH);
    }

    final OffsetDateTime dateFloor;

    final OffsetDateTime dateCeeling;

    /**
     * Private constructor to create a new immutable CdxDate.
     * <p>
     * @param dateFloor the earliest date for this CdxDate.
     * @param dateCeeling the latest date for this CdxDate.
     */
    private CdxDateRange(OffsetDateTime dateFloor, OffsetDateTime dateCeeling) {
        this.dateFloor = dateFloor;
        this.dateCeeling = dateCeeling;
    }

    public static CdxDateRange fromSingleDate(CdxDate date) {
        OffsetDateTime dateCeeling = date.getDate();
        switch (date.getGranularity()) {
            case NANOSECOND:
                dateCeeling = dateCeeling.plusNanos(1);
                break;
            case SECOND:
                dateCeeling = dateCeeling.plusSeconds(1);
                break;
            case MINUTE:
                dateCeeling = dateCeeling.plusMinutes(1);
                break;
            case HOUR:
                dateCeeling = dateCeeling.plusHours(1);
                break;
            case DAY:
                dateCeeling = dateCeeling.plusDays(1);
                break;
            case MONTH:
                dateCeeling = dateCeeling.plusMonths(1);
                break;
            case YEAR:
                dateCeeling = dateCeeling.plusYears(1);
                break;
        }
        return new CdxDateRange(date.getDate(), dateCeeling);
    }

    public static CdxDateRange fromDates(CdxDate fromDateInclusive, CdxDate toDateExclusive) {
        return new CdxDateRange(fromDateInclusive.getDate(), toDateExclusive.getDate());
    }

    public static CdxDateRange from(CdxDate fromDateInclusive) {
        return new CdxDateRange(fromDateInclusive.getDate(), null);
    }

    public static CdxDateRange to(CdxDate toDateExclusive) {
        return new CdxDateRange(null, toDateExclusive.getDate());
    }

    /**
     * Create a new CdxDate from a date formatted according to the WARC standard.
     * @param warcDate the date to parse.
     * @return the validated CdxDate.
     */
//    public static CdxDateRange fromWarcDate(String warcDate) {
//        OffsetDateTime dateFloor = WARC_DATE_FORMAT_FLOOR.parse(warcDate, OffsetDateTime::from);
//        OffsetDateTime dateCeeling = WARC_DATE_FORMAT_CEELING.parse(warcDate, OffsetDateTime::from);
//        return new CdxDateRange(dateFloor, dateCeeling.plusNanos(1L));
//    }

    public static CdxDateRange fromWarcDate(String warcDateFromInclusive, String warcDateToExclusive) {
        OffsetDateTime dateFloor = null;
        OffsetDateTime dateCeeling = null;
        if (warcDateFromInclusive != null && !warcDateFromInclusive.isEmpty()) {
            dateFloor = WARC_DATE_FORMAT_FLOOR.parse(warcDateFromInclusive, OffsetDateTime::from);
        }
        if (warcDateToExclusive != null && !warcDateToExclusive.isEmpty()) {
            dateCeeling = WARC_DATE_FORMAT_CEELING.parse(warcDateToExclusive, OffsetDateTime::from);
        }
        return new CdxDateRange(dateFloor, dateCeeling);
    }

    /**
     * Create a new CdxDate from a Heritrix formatted date.
     * @param heritrixDate the date to parse.
     * @return the validated CdxDate.
     */
    public static CdxDateRange fromHeritrixDate(String heritrixDate) {
        OffsetDateTime dateFloor = HERITRIX_DATE_FORMAT_FLOOR.parse(heritrixDate, OffsetDateTime::from);
        OffsetDateTime dateCeeling = HERITRIX_DATE_FORMAT_CEELING.parse(heritrixDate, OffsetDateTime::from);
        return new CdxDateRange(dateFloor, dateCeeling.plusNanos(1L));
    }

    public static CdxDateRange fromHeritrixDate(String heritrixDateFromInclusive, String heritrixDateToExclusive) {
        OffsetDateTime dateFloor = null;
        OffsetDateTime dateCeeling = null;
        if (heritrixDateFromInclusive != null && !heritrixDateFromInclusive.isEmpty()) {
            dateFloor = HERITRIX_DATE_FORMAT_FLOOR.parse(heritrixDateFromInclusive, OffsetDateTime::from);
        }
        if (heritrixDateToExclusive != null && !heritrixDateToExclusive.isEmpty()) {
            dateCeeling = HERITRIX_DATE_FORMAT_CEELING.parse(heritrixDateToExclusive, OffsetDateTime::from);
        }
        return new CdxDateRange(dateFloor, dateCeeling);
    }

    public boolean hasFromDate() {
        return dateFloor != null;
    }

    public boolean hasToDate() {
        return dateCeeling != null;
    }

    /**
     * Get the earliest date formatted as a WARC date.
     * @return the formatted string.
     */
    public String toWarcDateFloor() {
        return WARC_DATE_OUTPUT_FORMAT.format(dateFloor);
    }

    /**
     * Get the latest date formatted as a WARC date.
     * @return the formatted string.
     */
    public String toWarcDateCeeling() {
        return WARC_DATE_OUTPUT_FORMAT.format(dateCeeling);
    }

    /**
     * Get the earliest date formatted as a Heritrix date.
     * @return the formatted string.
     */
    public String toHeritrixDateFloor() {
        return HERITRIX_DATE_OUTPUT_FORMAT.format(dateFloor);
    }

    /**
     * Get the latest date formatted as a Heritrix date.
     * @return the formatted string.
     */
    public String toHeritrixDateCeeling() {
        return HERITRIX_DATE_OUTPUT_FORMAT.format(dateCeeling);
    }

}
