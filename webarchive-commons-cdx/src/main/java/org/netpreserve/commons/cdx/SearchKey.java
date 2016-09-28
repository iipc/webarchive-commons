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

import java.nio.ByteBuffer;
import java.util.Objects;

import org.netpreserve.commons.cdx.cdxsource.ByteBufferUtil;
import org.netpreserve.commons.cdx.cdxsource.SearchKeyFilter;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.util.datetime.DateTimeRange;
import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;

/**
 *
 */
public final class SearchKey {

    private final SearchKeyTemplate skt;

    private final CdxFormat cdxFormat;

    private final SearchKeyFilter<Uri> primaryUriFilter;

    private final SearchKeyFilter<Uri> secondaryUriFilter;

    private final SearchKeyFilter<VariablePrecisionDateTime> fromDateFilter;

    private final SearchKeyFilter<VariablePrecisionDateTime> toDateFilter;

    private SearchKeyFilter<SearchKeyFilter<Uri>> surtHostFilter;

    SearchKey(final SearchKeyTemplate skt, final CdxFormat cdxFormat) {
        this.skt = skt;
        this.cdxFormat = Objects.requireNonNull(cdxFormat);

        if (skt.primaryUriString != null) {
            primaryUriFilter = SearchKeyFilter
                    .newUriFilter(skt.primaryUriString, cdxFormat.getKeyUriFormat(), skt.uriMatchType);
        } else {
            primaryUriFilter = null;
        }
        if (skt.secondaryUriString != null) {
            secondaryUriFilter = SearchKeyFilter.newUriFilter(
                    skt.secondaryUriString, cdxFormat.getKeyUriFormat(), skt.uriMatchType);
        } else {
            secondaryUriFilter = null;
        }

        if (skt.dateRange != null) {
            if (skt.dateRange.hasStartDate()) {
                fromDateFilter = SearchKeyFilter.newDateFilter(skt.dateRange.getStart(), cdxFormat
                        .getKeyDateFormat());
            } else {
                fromDateFilter = null;
            }
            if (skt.dateRange.hasEndDate()) {
                toDateFilter = SearchKeyFilter.newDateFilter(skt.dateRange.getEnd(), cdxFormat.getKeyDateFormat());
            } else {
                toDateFilter = null;
            }
        } else {
            fromDateFilter = null;
            toDateFilter = null;
        }
    }

    public Uri getPrimaryUri() {
        if (primaryUriFilter == null) {
            return null;
        }

        return primaryUriFilter.getOriginalValue();
    }

    public Uri getSecondaryUri() {
        if (secondaryUriFilter == null) {
            return null;
        }

        return secondaryUriFilter.getOriginalValue();
    }

    public DateTimeRange getDateRange() {
        return skt.dateRange;
    }

    public CdxFormat getCdxFormat() {
        return cdxFormat;
    }

    public boolean isBefore(final String keyToTest) {
        switch (skt.uriMatchType) {
            case ALL:
                return false;

            case EXACT:
            case PATH:
                if (keyToTest.compareTo(primaryUriFilter.getFilterString()) < 0) {
                    return true;
                }
                break;

            case HOST:
                if (keyToTest.compareTo(getSurtHostFilter().getFilterString()) < 0) {
                    return true;
                }
                break;

            case RANGE:
                if ((primaryUriFilter != null && keyToTest.compareTo(primaryUriFilter.getFilterString()) < 0)) {
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean included(final String keyToTest) {
        switch (skt.uriMatchType) {
            case ALL:
                return true;

            case EXACT:
                if (primaryUriFilter.getFilterString().equals(keyToTest)) {
                    return true;
                }
                break;

            case PATH:
                if (keyToTest.startsWith(primaryUriFilter.getFilterString())) {
                    return true;
                }
                break;

            case HOST:
                if (keyToTest.startsWith(getSurtHostFilter().getFilterString())) {
                    return true;
                }
                break;

            case RANGE:
                if ((primaryUriFilter == null || primaryUriFilter.getFilterString().compareTo(keyToTest) <= 0)
                        && (secondaryUriFilter == null || secondaryUriFilter.getFilterString().compareTo(keyToTest)
                        > 0)) {
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean included(final ByteBuffer byteBuf) {
        switch (skt.uriMatchType) {
            case ALL:
                return true;

            case EXACT:
                if (ByteBufferUtil.compareToFilter(byteBuf, primaryUriFilter) == 0) {
                    if (skt.dateRange != null && skt.dateRange.hasStartDateOrEndDate()
                            && ByteBufferUtil.nextField(byteBuf) && byteBuf.hasRemaining()) {
                        if (skt.dateRange.hasStartDate() && skt.dateRange.hasEndDate()) {
                            return ByteBufferUtil.between(byteBuf, fromDateFilter, toDateFilter);
                        } else if (skt.dateRange.hasStartDate()) {
                            return ByteBufferUtil.compareToFilter(byteBuf, fromDateFilter) >= 0;
                        } else {
                            return ByteBufferUtil.compareToFilter(byteBuf, toDateFilter) < 0;
                        }
                    }
                    return true;
                }
                break;

            case PATH:
                if (ByteBufferUtil.startsWith(byteBuf, primaryUriFilter)) {
                    return true;
                }
                break;

            case HOST:
                if (ByteBufferUtil.startsWith(byteBuf, getSurtHostFilter())) {
                    return true;
                }
                break;

            case RANGE:
                if (primaryUriFilter == null && secondaryUriFilter == null) {
                    return true;
                }

                if (primaryUriFilter != null && secondaryUriFilter != null) {
                    return ByteBufferUtil.between(byteBuf, primaryUriFilter, secondaryUriFilter);
                } else if (primaryUriFilter != null) {
                    return ByteBufferUtil.compareToFilter(byteBuf, primaryUriFilter) >= 0;
                } else {
                    return ByteBufferUtil.compareToFilter(byteBuf, secondaryUriFilter) < 0;
                }

            default:
                return false;
        }
        return false;
    }

    private SearchKeyFilter<SearchKeyFilter<Uri>> getSurtHostFilter() {
        if (surtHostFilter == null) {
            surtHostFilter = SearchKeyFilter.newSurtHostFilter(primaryUriFilter);
        }
        return surtHostFilter;
    }

}
