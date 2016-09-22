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

import org.netpreserve.commons.util.datetime.DateTimeRange;

import java.nio.ByteBuffer;

import org.netpreserve.commons.cdx.cdxsource.ByteBufferUtil;
import org.netpreserve.commons.cdx.cdxsource.SearchKeyFilter;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;

/**
 * An instance of this class can determine if a CdxRecord is within scope for a search.
 */
public final class SearchKey {

    public enum UriMatchType {

        ALL,
        EXACT,
        HOST,
        PATH,
        RANGE;

    }

    private final String primaryUriString;

    private final DateTimeRange dateRange;

    private final String secondaryUriString;

    private final UriMatchType uriMatchType;

    private final CdxFormat cdxFormat;

    private final SearchKeyFilter<Uri> primaryUriFilter;

    private final SearchKeyFilter<Uri> secondaryUriFilter;

    private final SearchKeyFilter<VariablePrecisionDateTime> fromDateFilter;

    private final SearchKeyFilter<VariablePrecisionDateTime> toDateFilter;

    private SearchKeyFilter<SearchKeyFilter<Uri>> surtHostFilter;

    public SearchKey() {
        this(null, null, null, null, null, null, null, UriMatchType.ALL, null, false, false);
    }

    public SearchKey(final String uri, final UriMatchType matchType) {
        this(uri.trim(), null, null, null, null, null, null, matchType, null, true, false);
    }

    public SearchKey(final String uri, final UriMatchType matchType, final DateTimeRange dateRange) {
        this(uri.trim(), null, null, null, dateRange, null, null, matchType, null, false, false);
    }

    private SearchKey(String primaryUri, SearchKeyFilter primaryUriFilter,
            String secondaryUri, SearchKeyFilter secondaryUriFilter,
            DateTimeRange dateRange, SearchKeyFilter fromDateFilter, SearchKeyFilter toDateFilter,
            UriMatchType uriMatchType, CdxFormat cdxFormat, boolean parseUri, boolean parseDate) {

        if (parseUri && cdxFormat != null) {

            if (primaryUri != null) {
                primaryUriFilter = SearchKeyFilter.newUriFilter(primaryUri, cdxFormat.getKeyUriFormat(), uriMatchType);
            }
            if (secondaryUriFilter == null && secondaryUri != null) {
                secondaryUriFilter = SearchKeyFilter.newUriFilter(
                        secondaryUri, cdxFormat.getKeyUriFormat(), uriMatchType);
            }
        }

        if (parseDate && cdxFormat != null && dateRange != null) {
            if (dateRange.hasStartDate()) {
                fromDateFilter = SearchKeyFilter.newDateFilter(dateRange.getStart(), cdxFormat.getKeyDateFormat());
            }
            if (dateRange.hasEndDate()) {
                toDateFilter = SearchKeyFilter.newDateFilter(dateRange.getEnd(), cdxFormat.getKeyDateFormat());
            }
        }

        this.primaryUriString = primaryUri;
        this.primaryUriFilter = primaryUriFilter;
        this.secondaryUriString = secondaryUri;
        this.secondaryUriFilter = secondaryUriFilter;
        this.uriMatchType = uriMatchType;
        this.cdxFormat = cdxFormat;
        this.dateRange = dateRange;
        this.fromDateFilter = fromDateFilter;
        this.toDateFilter = toDateFilter;
    }

    public SearchKey uri(final String uri) {
        return uri(uri, UriMatchType.EXACT);
    }

    public SearchKey uri(final String uri, final UriMatchType matchType) {
        return new SearchKey(uri.trim(), null, null, null, dateRange, fromDateFilter, toDateFilter,
                matchType, cdxFormat, true, false);
    }

    public SearchKey dateRange(final DateTimeRange dateRange) {
        return new SearchKey(primaryUriString, primaryUriFilter, secondaryUriString, secondaryUriFilter, dateRange,
                null, null, uriMatchType, cdxFormat, false, true);
    }

    public SearchKey uriRange(final String fromUri, final String toUri) {
        return new SearchKey(fromUri.trim(), null, toUri.trim(), null, dateRange, null, null,
                UriMatchType.RANGE, cdxFormat, true, false);
    }

    public SearchKey cdxFormat(final CdxFormat format) {
        return new SearchKey(primaryUriString, null, secondaryUriString, null, dateRange, null, null,
                uriMatchType, format, true, true);
    }

    public Uri getPrimaryUri() {
        if (cdxFormat == null) {
            throw new IllegalStateException("Cannot get parsed URI when CdxFormat is not set");
        }

        if (primaryUriFilter == null) {
            return null;
        }

        return primaryUriFilter.getOriginalValue();
    }

    public Uri getSecondaryUri() {
        if (cdxFormat == null) {
            throw new IllegalStateException("Cannot get parsed URI when CdxFormat is not set");
        }

        if (secondaryUriFilter == null) {
            return null;
        }

        return secondaryUriFilter.getOriginalValue();
    }

    public UriMatchType getMatchType() {
        return uriMatchType;
    }

    public DateTimeRange getDateRange() {
        return dateRange;
    }

    public CdxFormat getCdxFormat() {
        return cdxFormat;
    }

    public boolean isBefore(final String keyToTest) {
        if (cdxFormat == null) {
            throw new IllegalStateException("CdxFormat must be set");
        }

        switch (uriMatchType) {
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
        if (cdxFormat == null) {
            throw new IllegalStateException("CdxFormat must be set");
        }

        switch (uriMatchType) {
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
        if (cdxFormat == null) {
            throw new IllegalStateException("CdxFormat must be set");
        }

        switch (uriMatchType) {
            case ALL:
                return true;

            case EXACT:
                if (ByteBufferUtil.compareToFilter(byteBuf, primaryUriFilter) == 0) {
                    if (dateRange != null && dateRange.hasStartDateOrEndDate()
                            && ByteBufferUtil.nextField(byteBuf) && byteBuf.hasRemaining()) {
                        if (dateRange.hasStartDate() && dateRange.hasEndDate()) {
                            return ByteBufferUtil.between(byteBuf, fromDateFilter, toDateFilter);
                        } else if (dateRange.hasStartDate()) {
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
