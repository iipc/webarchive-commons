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

/**
 * An instance of this class can determine if a CdxRecord is within scope for a search.
 */
public final class SearchKeyTemplate {

    public enum UriMatchType {

        ALL,
        EXACT,
        HOST,
        PATH,
        RANGE;

    }

    final String primaryUriString;

    final DateTimeRange dateRange;

    final String secondaryUriString;

    final UriMatchType uriMatchType;

    final boolean includeFirst;

    public SearchKeyTemplate() {
        this(null, null, UriMatchType.ALL, null, false);
    }

    public SearchKeyTemplate(final String uri, final UriMatchType matchType) {
        this(uri, null, matchType, null, false);
    }

    public SearchKeyTemplate(final String uri, final UriMatchType matchType, final DateTimeRange dateRange) {
        this(uri, null, matchType, dateRange, false);
    }

    public SearchKeyTemplate(final String primaryUri, final String secondaryUri, final UriMatchType matchType,
            final DateTimeRange dateRange, final boolean includeFirst) {
        if (primaryUri != null) {
            this.primaryUriString = primaryUri.trim();
        } else {
            this.primaryUriString = null;
        }
        if (secondaryUri != null) {
            this.secondaryUriString = secondaryUri.trim();
        } else {
            this.secondaryUriString = null;
        }
        this.uriMatchType = matchType;
        this.dateRange = dateRange;
        this.includeFirst = includeFirst;
    }

    public SearchKeyTemplate uri(final String uri) {
        return uri(uri, UriMatchType.EXACT);
    }

    public SearchKeyTemplate uri(final String uri, final UriMatchType matchType) {
        return new SearchKeyTemplate(uri, null, matchType, dateRange, includeFirst);
    }

    public SearchKeyTemplate dateRange(final DateTimeRange dateRange) {
        return new SearchKeyTemplate(primaryUriString, secondaryUriString, uriMatchType, dateRange, includeFirst);
    }

    public SearchKeyTemplate uriRange(final String fromUri, final String toUri) {
        return new SearchKeyTemplate(fromUri, toUri, UriMatchType.RANGE, dateRange, includeFirst);
    }

    public SearchKeyTemplate includeFirst(final boolean includeFirst) {
        return new SearchKeyTemplate(primaryUriString, secondaryUriString, uriMatchType, dateRange, includeFirst);
    }

    public SearchKeyTemplate.UriMatchType getMatchType() {
        return uriMatchType;
    }

    public SearchKey createSearchKey(CdxFormat cdxFormat) {
        return new SearchKey(this, cdxFormat);
    }

}
