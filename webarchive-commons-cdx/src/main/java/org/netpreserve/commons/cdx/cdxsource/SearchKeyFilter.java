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

import org.netpreserve.commons.cdx.SearchKeyTemplate;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.util.datetime.DateFormat;
import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;

/**
 * Filter used internally in the search key.
 * <p>
 * Filters allows for separate handling of different datatypes. It also gives the oportunity to cache computation needed
 * on filters. Since the kind of computation needed might be different for each cdx format, filters are first
 * constructed when a SearchKey is used by a CdxSource which knows the kind of cdx format it reads.
 * <p>
 * @param <T> the type of the value used to create the filter
 */
public interface SearchKeyFilter<T> {

    /**
     * Get the computed filter as a String.
     * <p>
     * @return the computed filter as a String
     */
    String getFilterString();

    /**
     * Get the computed filter as an array.
     * <p>
     * @return the computed filter as an array
     */
    byte[] getFilterArray();

    /**
     * Get the original value used to create the filter.
     * <p>
     * @return the value used to create the filter
     */
    T getOriginalValue();

    /**
     * Check if the remaining of this filter could be ignored when comparing.
     * <p>
     * The default is to compare the whole filed, but in some circumstances, especially when comparing dates with
     * different granularity, it is for example possible to ignore trailing zero's.
     * <p>
     * @param fromPos the position to check from
     * @return true if the remaining part (after fromPos) could be ignored
     */
    default boolean isIgnorableFrom(int fromPos) {
        return false;
    }

    /**
     * Check if a character denotes a end of field marker.
     * <p>
     * The default is to check for space, newline and carriage return, but can be overridden for fields which have
     * special ending character.
     * <p>
     * @param c the character to check
     * @return true if character denotes a end of field marker
     */
    default boolean isEndOfField(int c) {
        return c == ' ' || c == '\n' || c == '\r';
    }

    /**
     * Factory method for creating a new filter encapsulating a String.
     * <p>
     * @param filter the String to create the filter from
     * @return the newly created filter
     */
    static SearchKeyFilter newStringFilter(String filter) {
        return new SearchKeyStringFilter(filter);
    }

    /**
     * Factory method for creating a new filter encapsulating a URI.
     * <p>
     * @param filter the URI to create the filter from
     * @param config the {@link UriBuilderConfig} used for parsing the filter
     * @param matchType the match type which could influence parsing beyond whats in the config
     * @return the newly created filter
     */
    static SearchKeyFilter newUriFilter(String filter, UriBuilderConfig config, SearchKeyTemplate.UriMatchType matchType) {
        return new SearchKeyUriFilter(filter, config, matchType);
    }

    /**
     * Factory method for creating a new filter encapsulating a {@link SearchKey<Uri>} filter and extracts a SURT
     * formatted host from it.
     * <p>
     * @param filter the Uri filter to create the filter from
     * @return the newly created filter
     */
    static SearchKeyFilter newSurtHostFilter(SearchKeyFilter<Uri> filter) {
        return new SearchKeySurtHostFilter(filter);
    }

    /**
     * Factory method for creating a new filter encapsulating a timestamp.
     * <p>
     * @param filter the date to create the filter from
     * @param dateFormat the format used for formatting the date
     * @return the newly created filter
     */
    static SearchKeyFilter newDateFilter(VariablePrecisionDateTime filter, DateFormat dateFormat) {
        return new SearchKeyDateFilter(filter, dateFormat);
    }

}
