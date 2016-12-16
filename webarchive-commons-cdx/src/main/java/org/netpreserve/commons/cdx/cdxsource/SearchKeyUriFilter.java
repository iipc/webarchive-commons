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

import java.nio.charset.StandardCharsets;

import org.netpreserve.commons.uri.PostParseNormalizer;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.uri.normalization.StripSlashAtEndOfPath;

/**
 * A filter encapsulating a URI.
 */
final class SearchKeyUriFilter implements SearchKeyFilter<Uri> {

    private final String filterString;

    private final byte[] filterArray;

    private final Uri uri;

    /**
     * Construct a filter from a URI.
     * <p>
     * @param filter the URI to create the filter from
     * @param uriBuilderConfig the {@link UriBuilderConfig} used for parsing the filter
     * @param uriMatchType the match type which could influence parsing beyond whats in the config
     */
    SearchKeyUriFilter(String filter, UriBuilderConfig uriBuilderConfig, SearchKeyTemplate.UriMatchType uriMatchType) {
        if (uriMatchType == SearchKeyTemplate.UriMatchType.PATH) {
            // If match type is PATH, we need to keep ending slashes because we removed the final '*'.
            uriBuilderConfig = uriBuilderConfig.removeNormalizersOfType(StripSlashAtEndOfPath.class);
        }

        uri = uriBuilderConfig.buildUri(filter);
        filterString = uri.toString();
        filterArray = filterString.getBytes(StandardCharsets.UTF_8);
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
    public Uri getOriginalValue() {
        return uri;
    }

}
