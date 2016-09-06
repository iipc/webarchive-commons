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

/**
 * A representation of a date range used for searching.
 * <p>
 * As dates are allowed to be of variable precision, this class can compute the earliest and latest dates that will fit
 * into the submitted date. A date range can also be constructed from a start date and an end date, or if only a start
 * or an end date is used for construction, the date range will be open ended.
 * <p>
 * This class is immutable and thread safe.
 */
public final class DateTimeRange {

    final VariablePrecisionDateTime dateStart;

    final VariablePrecisionDateTime dateEnd;

    /**
     * Private constructor to create a new immutable CdxDateRange.
     * <p>
     * @param startDate the earliest date for this VariablePrecisionDateTime.
     * @param endDate the latest date for this VariablePrecisionDateTime.
     */
    private DateTimeRange(VariablePrecisionDateTime startDate, VariablePrecisionDateTime endDate) {
        if (startDate != null) {
            this.dateStart = new VariablePrecisionDateTime(startDate.getDate(), Granularity.NANOSECOND);
        } else {
            this.dateStart = null;
        }

        if (endDate != null) {
            this.dateEnd = new VariablePrecisionDateTime(endDate.getDate(), Granularity.NANOSECOND);
        } else {
            this.dateEnd = null;
        }
    }

    /**
     * Obtain a DateTimeRange from a single date.
     * <p>
     * This method will create a date range based on the granularity of the date. For example a date range based on the
     * date {@code 2016-02-15} (granularity is day), will have a start date of {@code 2000-02-15T00:00:00.000000000Z}
     * and an end date of {@code 2000-02-16T00:00:00.000000000Z}.
     * <p>
     * @param date the date to obtain a date range from.
     * @return the immutable date range, not null.
     */
    public static DateTimeRange ofSingleDate(VariablePrecisionDateTime date) {
        OffsetDateTime endDate = date.getDate();
        switch (date.getGranularity()) {
            case NANOSECOND:
                endDate = endDate.plusNanos(1);
                break;
            case SECOND:
                endDate = endDate.plusSeconds(1);
                break;
            case MINUTE:
                endDate = endDate.plusMinutes(1);
                break;
            case HOUR:
                endDate = endDate.plusHours(1);
                break;
            case DAY:
                endDate = endDate.plusDays(1);
                break;
            case MONTH:
                endDate = endDate.plusMonths(1);
                break;
            case YEAR:
                endDate = endDate.plusYears(1);
                break;
            default:
                // All granularities should be supported. If not, this is a bug.
                throw new RuntimeException("Granularity " + date.getGranularity() + " is not supported.");
        }
        return new DateTimeRange(date, new VariablePrecisionDateTime(endDate, date.getGranularity()));
    }

    /**
     * Obtain a date range from two dates.
     * <p>
     * @param startDateInclusive the start date (inclusive)
     * @param endDateExclusive the end date (exclusive)
     * @return the immutable date range, not null.
     */
    public static DateTimeRange between(VariablePrecisionDateTime startDateInclusive,
            VariablePrecisionDateTime endDateExclusive) {
        return new DateTimeRange(startDateInclusive, endDateExclusive);
    }

    /**
     * Obtain an open ended date range beginning at the submitted date and with infinite end.
     * <p>
     * @param startDateInclusive the start date (inclusive)
     * @return the immutable date range, not null.
     */
    public static DateTimeRange start(VariablePrecisionDateTime startDateInclusive) {
        return new DateTimeRange(startDateInclusive, null);
    }

    /**
     * Obtain an open ended date range ending at the submitted date and with infinite start.
     * <p>
     * @param endDateExclusive the end date (exclusive)
     * @return the immutable date range, not null.
     */
    public static DateTimeRange end(VariablePrecisionDateTime endDateExclusive) {
        return new DateTimeRange(null, endDateExclusive);
    }

    /**
     * Obtain a DateTimeRange from a single date.
     * <p>
     * Convenience method. Does the same as {@code DateTimeRange.ofSingleDate(VariablePrecisionDateTime.of(date))}
     * <p>
     * @param date the date to obtain a date range from.
     * @return the immutable date range, not null.
     * @see #ofSingleDate(org.netpreserve.commons.cdx.CdxDate)
     */
    public static DateTimeRange ofSingleDate(String date) {
        return DateTimeRange.ofSingleDate(VariablePrecisionDateTime.valueOf(date));
    }

    /**
     * Obtain a date range from two dates.
     * <p>
     * Convenience method. Does the same as
     * {@code DateTimeRange.between(VariablePrecisionDateTime.of(startDate), VariablePrecisionDateTime.of(endDate))}
     * <p>
     * @param startDateInclusive the start date (inclusive)
     * @param endDateExclusive the end date (exclusive)
     * @return the immutable date range, not null.
     * @see #between(org.netpreserve.commons.cdx.CdxDate, org.netpreserve.commons.cdx.CdxDate)
     */
    public static DateTimeRange between(String startDateInclusive, String endDateExclusive) {
        VariablePrecisionDateTime start = null;
        VariablePrecisionDateTime end = null;

        if (startDateInclusive != null && !startDateInclusive.isEmpty()) {
            start = VariablePrecisionDateTime.valueOf(startDateInclusive);
        }

        if (endDateExclusive != null && !endDateExclusive.isEmpty()) {
            end = VariablePrecisionDateTime.valueOf(endDateExclusive);
        }

        return DateTimeRange.between(start, end);
    }

    /**
     * Obtain an open ended date range beginning at the submitted date and with infinite end.
     * <p>
     * Convenience method. Does the same as {@code DateTimeRange.start(VariablePrecisionDateTime.of(startDate))}
     * <p>
     * @param startDateInclusive the start date (inclusive)
     * @return the immutable date range, not null.
     */
    public static DateTimeRange start(String startDateInclusive) {
        return DateTimeRange.start(VariablePrecisionDateTime.valueOf(startDateInclusive));
    }

    /**
     * Obtain an open ended date range ending at the submitted date and with infinite start.
     * <p>
     * Convenience method. Does the same as {@code DateTimeRange.end(VariablePrecisionDateTime.of(startDate))}
     * <p>
     * @param endDateExclusive the end date (exclusive)
     * @return the immutable date range, not null.
     */
    public static DateTimeRange end(String endDateExclusive) {
        return DateTimeRange.end(VariablePrecisionDateTime.valueOf(endDateExclusive));
    }

    /**
     * Obtain a date range from date range expression.
     * <p>
     * The date range expression consists of one or two dates separated by '{@code ,}' or '{@code ;}'. The dates can be
     * in any format supported by {@link VariablePrecisionDateTime}. An expression starting or ending with a '{@code ,}'
     * or '{@code ;}' is an open ended date. If there is only one date and no separators, it is treated the same way as
     * {@link #ofSingleDate(java.lang.String)}.
     * <p>
     * Examples:
     * <ul>
     * <li>'{@code 2007-04}' - a range starting at 2007-04-01 (inclusive) and ending on 2007-05-01 (exclusive).
     * <li>'{@code 2007-04;2007-05}' - a range starting at 2007-04-01 (inclusive) and ending on 2007-05-01 (exclusive).
     * <li>'{@code 2007-04;}' - a range starting at 2007-04-01 (inclusive) with no end.
     * <li>'{@code ;2007-04}' - a range consisting of every date before 2007-04-01 (exclusive).
     * <li>'' (empty or null) - a range consisting of every possible date.
     * </ul>
     * <p>
     * @param exp the date range expression
     * @return the immutable date range, not null.
     */
    public static DateTimeRange ofRangeExpression(String exp) {
        if (exp == null || exp.isEmpty()) {
            return new DateTimeRange(null, null);
        }

        String[] dates = exp.split("[,;]", 2);

        if (dates.length == 1) {
            return ofSingleDate(dates[0]);
        } else {
            return between(dates[0], dates[1]);
        }
    }

    /**
     * Check if this date range has a start date.
     * <p>
     * A date range without a start date is considered a date range with infinite start date.
     * <p>
     * @return true if start date is set.
     */
    public boolean hasStartDate() {
        return dateStart != null;
    }

    /**
     * Check if this date range has an end date.
     * <p>
     * A date range without an end date is considered a date range with infinite end date.
     * <p>
     * @return true if end date is set.
     */
    public boolean hasEndDate() {
        return dateEnd != null;
    }

    /**
     * Check if this date range has a start or end date.
     * <p>
     * A date range without a start or end date is considered a date range including all possible dates.
     * <p>
     * @return true if start date or end date is set.
     */
    public boolean hasStartDateOrEndDate() {
        return dateStart != null || dateEnd != null;
    }

    /**
     * Get the start date of this range.
     * <p>
     * @return the start date, possibly null if infinite start.
     * @see #hasStartDate()
     */
    public VariablePrecisionDateTime getStart() {
        return dateStart;
    }

    /**
     * Get the end date of this range.
     * <p>
     * @return the end date, possibly null if infinite end.
     * @see #hasEndDate()
     */
    public VariablePrecisionDateTime getEnd() {
        return dateEnd;
    }

    /**
     * Check if a {@link VariablePrecisionDateTime} is within the limits of this range.
     * <p>
     * @param timestamp the timestamp to check
     * @return true if within limits
     */
    public boolean contains(VariablePrecisionDateTime timestamp) {
        if (hasStartDateOrEndDate()) {
            if (hasStartDate() && hasEndDate()) {
                return (timestamp.compareTo(getStart()) >= 0) && (timestamp.compareTo(getEnd()) < 0);
            } else if (hasStartDate()) {
                return timestamp.compareTo(getStart()) >= 0;
            } else {
                return timestamp.compareTo(getEnd()) < 0;
            }
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        return "DateTimeRange{" + "dateStart=" + dateStart + ", dateEnd=" + dateEnd + '}';
    }

}
