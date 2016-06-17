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

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.util.Arrays;
import java.util.Locale;

import org.netpreserve.commons.cdx.time.WarcDateFormatter;

import static org.netpreserve.commons.cdx.CdxDateRange.WARC_DATE_OUTPUT_FORMAT;

/**
 * A representation of a date used for searching.
 * <p>
 * As dates are allowed to be of variable precision, this class computes the earliest and latest dates that will fit
 * into the submitted date.
 * <p>
 * This class is immutable and thread safe.
 */
public final class CdxDate {

    public enum Granularity {

        YEAR,
        MONTH,
        DAY,
        HOUR,
        MINUTE,
        SECOND,
        NANOSECOND;

    }

    private static final DateTimeFormatter WARC_DATE_PARSE_FORMAT = new DateTimeFormatterBuilder()
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
            .optionalEnd()
            .optionalStart()
            .appendOffsetId()
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

    private static final DateTimeFormatter HERITRIX_DATE_PARSE_FORMAT = new DateTimeFormatterBuilder()
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

    final OffsetDateTime date;

    final Granularity granularity;

    /**
     * Private constructor to create a new immutable CdxDate.
     * <p>
     * @param dateFloor the earliest date for this CdxDate.
     * @param dateCeeling the latest date for this CdxDate.
     */
    private CdxDate(OffsetDateTime date, Granularity granularity) {
        this.date = date;
        this.granularity = granularity;
    }

    /**
     * Create a new CdxDate from a date formatted according to the WARC standard.
     * <p>
     * @param warcDate the date to parse.
     * @return the validated CdxDate.
     */
    public static CdxDate fromWarcDate(String warcDate) {
        String[] tokens = warcDate.split("[-T:\\.Z]");
        Granularity granularity = Granularity.values()[tokens.length - 1];
        OffsetDateTime date = WARC_DATE_PARSE_FORMAT.parse(warcDate, OffsetDateTime::from);

        return new CdxDate(date, granularity);
    }

    /**
     * Create a new CdxDate from a Heritrix formatted date.
     * <p>
     * @param heritrixDate the date to parse.
     * @return the validated CdxDate.
     */
    public static CdxDate fromHeritrixDate(String heritrixDate) {
        Granularity granularity;
        switch (heritrixDate.length()) {
            case 4:
                granularity = Granularity.YEAR;
                break;
            case 6:
                granularity = Granularity.MONTH;
                break;
            case 8:
                granularity = Granularity.DAY;
                break;
            case 10:
                granularity = Granularity.HOUR;
                break;
            case 12:
                granularity = Granularity.MINUTE;
                break;
            case 14:
                granularity = Granularity.SECOND;
                break;
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
                granularity = Granularity.NANOSECOND;
                break;
            default:
                throw new IllegalArgumentException("Could not parse date: " + heritrixDate);
        }

        OffsetDateTime date = HERITRIX_DATE_PARSE_FORMAT.parse(heritrixDate, OffsetDateTime::from);

        return new CdxDate(date, granularity);
    }
//        OffsetDateTime dateAdjusted;
//        Temporal date = (Temporal) CdxDateRange.HERITRIX_DATE_OUTPUT_FORMAT.parseBest(heritrixDate,
//                OffsetDateTime::from, LocalDate::from, YearMonth::from, Year::from);
//        if (date instanceof OffsetDateTime) {
//            dateAdjusted = (OffsetDateTime) date;
//        } else {
//            dateAdjusted = CdxDateRange.HERITRIX_DATE_FORMAT_FLOOR.parse(heritrixDate, OffsetDateTime::from);
//        }
//        return new CdxDate(date, dateAdjusted);
//    }

    public Duration distanceTo(CdxDate other) {
        return Duration.between(date, other.date).abs();
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public Granularity getGranularity() {
        return granularity;
    }

    @Override
    public String toString() {
        return "CdxDate{" + "date=" + date + ", granularity=" + granularity + '}';
    }

}
