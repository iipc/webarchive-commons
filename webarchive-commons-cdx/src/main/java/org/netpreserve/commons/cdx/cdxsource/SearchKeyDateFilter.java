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
package org.netpreserve.commons.cdx.cdxsource;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import org.netpreserve.commons.util.datetime.DateFormat;
import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;

/**
 * A filter encapsulating a timestamp.
 */
final class SearchKeyDateFilter implements SearchKeyFilter<VariablePrecisionDateTime> {

    private final String filterString;

    private final byte[] filterArray;

    private final VariablePrecisionDateTime dateTime;

    private int ignorableFrom;

    /**
     * Construct a filter from a timestamp.
     * <p>
     * @param filter the date to create the filter from
     * @param dateFormat the format used for formatting the date
     * @return the newly created filter
     */
    SearchKeyDateFilter(VariablePrecisionDateTime filter, DateFormat format) {
        this.dateTime = filter;
        this.filterString = filter.toFormattedString(format);
        this.filterArray = filterString.getBytes(StandardCharsets.UTF_8);

        OffsetDateTime ts = filter.getDate();
        switch (format) {
            case WARC:
                ignorableFrom = this.filterArray.length - 1 - Math.min(10, Integer.numberOfTrailingZeros(ts.getNano()));
                if (ts.getNano() == 0 && ts.getSecond() == 0) {
                    ignorableFrom -= 3;
                    if (ts.getMinute() == 0) {
                        ignorableFrom -= 3;
                        if (ts.getHour() == 0) {
                            ignorableFrom -= 3;
                        }
                    }
                }
                break;
            case HERITRIX:
                ignorableFrom = this.filterArray.length - Math.min(9, Integer.numberOfTrailingZeros(ts.getNano()));
                if (ts.getNano() == 0 && ts.getSecond() == 0) {
                    ignorableFrom -= 2;
                    if (ts.getMinute() == 0) {
                        ignorableFrom -= 2;
                        if (ts.getHour() == 0) {
                            ignorableFrom -= 2;
                        }
                    }
                }
                break;
            default:
                ignorableFrom = this.filterArray.length;
                break;
        }
    }

    @Override
    public boolean isIgnorableFrom(int fromPos) {
        return fromPos >= ignorableFrom;
    }

    @Override
    public String getFilterString() {
        return filterString;
    }

    @Override
    public byte[] getFilterArray() {
        return this.filterArray;
    }

    @Override
    public VariablePrecisionDateTime getOriginalValue() {
        return dateTime;
    }

    @Override
    public boolean isEndOfField(int c) {
        return c == 'Z' || c == ' ' || c == '\n' || c == '\r';
    }

}
