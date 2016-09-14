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

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

/**
 * A date format for formatting {@link VariablePrecisionDateTime} instances to String.
 */
public enum DateFormat {

    /**
     * A format as defined in the WARC standard.
     */
    WARC(buildWarcFormatters()),
    /**
     * A format as used in Heritrix.
     */
    HERITRIX(buildHeritrixFormatters()),
    /**
     * A format as defined in RFC 1123 and used by the Memento protocol.
     */
    RFC1123(buildRfc1123Formatters());

    private final DateTimeFormatter[] formatters;

    /**
     * Constructs a new DateFormat.
     * <p>
     * @param formatters the list of formatters
     */
    DateFormat(DateTimeFormatter[] formatters) {
        this.formatters = formatters;
    }

    /**
     * Format a {@link VariablePrecisionDateTime} as a String.
     * <p>
     * @param dateTime the DateTime to format
     * @return the formatted string
     */
    public String format(VariablePrecisionDateTime dateTime) {
        return formatters[dateTime.granularity.ordinal()].format(dateTime.date);
    }

    /**
     * Create array of formatters for formatting dates of different granularities allowed in the WARC date format.
     * <p>
     * @return an array with a formatter for each legal granularity
     */
    private static DateTimeFormatter[] buildWarcFormatters() {
        DateTimeFormatter[] warcFormats = new DateTimeFormatter[Granularity.values().length];

        DateTimeFormatterBuilder warcDateBuilder = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4);
        warcFormats[Granularity.YEAR.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        warcDateBuilder.appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR, 2);
        warcFormats[Granularity.MONTH.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        warcDateBuilder.appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH, 2);
        warcFormats[Granularity.DAY.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        warcDateBuilder.appendLiteral('T')
                .appendValue(ChronoField.HOUR_OF_DAY, 2);
        warcFormats[Granularity.HOUR.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        warcDateBuilder.appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2);
        warcFormats[Granularity.MINUTE.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        warcDateBuilder.appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2);
        warcFormats[Granularity.SECOND.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        warcDateBuilder.appendFraction(ChronoField.NANO_OF_SECOND, 9, 9, true)
                .appendOffsetId();
        warcFormats[Granularity.NANOSECOND.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        return warcFormats;
    }

    /**
     * Create array of formatters for formatting dates of different granularities allowed in the Heritrix date format.
     * <p>
     * @return an array with a formatter for each legal granularity
     */
    private static DateTimeFormatter[] buildHeritrixFormatters() {
        DateTimeFormatter[] heritrixFormats;

        heritrixFormats = new DateTimeFormatter[Granularity.values().length];

        DateTimeFormatterBuilder warcDateBuilder = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4);
        heritrixFormats[Granularity.YEAR.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        warcDateBuilder.appendValue(ChronoField.MONTH_OF_YEAR, 2);
        heritrixFormats[Granularity.MONTH.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        warcDateBuilder.appendValue(ChronoField.DAY_OF_MONTH, 2);
        heritrixFormats[Granularity.DAY.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        warcDateBuilder.appendValue(ChronoField.HOUR_OF_DAY, 2);
        heritrixFormats[Granularity.HOUR.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        warcDateBuilder.appendValue(ChronoField.MINUTE_OF_HOUR, 2);
        heritrixFormats[Granularity.MINUTE.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        warcDateBuilder.appendValue(ChronoField.SECOND_OF_MINUTE, 2);
        heritrixFormats[Granularity.SECOND.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        warcDateBuilder.appendFraction(ChronoField.NANO_OF_SECOND, 9, 9, false);
        heritrixFormats[Granularity.NANOSECOND.ordinal()] = warcDateBuilder.toFormatter(Locale.ENGLISH);

        return heritrixFormats;
    }

    /**
     * Create array of formatters for formatting dates of different granularities allowed in RFC 1123 date format.
     * <p>
     * Since RFC 1123 doesn't allow different granularities, the dates are formatted as the rest of the date/time values
     * are null for granularities corser than SECOND.
     * <p>
     * @return an array with a formatter for each legal granularity
     */
    private static DateTimeFormatter[] buildRfc1123Formatters() {
        DateTimeFormatter[] rfc1123Formats;

        rfc1123Formats = new DateTimeFormatter[Granularity.values().length];

        for (int i = 0; i < rfc1123Formats.length; i++) {
            rfc1123Formats[i] = DateTimeFormatter.RFC_1123_DATE_TIME;
        }

        return rfc1123Formats;
    }

}
