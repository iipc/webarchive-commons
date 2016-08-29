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

import org.netpreserve.commons.cdx.SearchResult;

/**
 *
 */
public class SizeLimitingSearchResult implements SearchResult {

    private final SearchResult src;

    private final long limit;

    public SizeLimitingSearchResult(SearchResult src, long limit) {
        this.src = src;
        this.limit = limit;
    }

    @Override
    public CdxIterator iterator() {
        return src.iterator().limit(limit);
    }

    @Override
    public void close() {
        src.close();
    }

    @Override
    public SearchResult limit(long maxSize) {
        return new SizeLimitingSearchResult(this, limit);
    }

}
