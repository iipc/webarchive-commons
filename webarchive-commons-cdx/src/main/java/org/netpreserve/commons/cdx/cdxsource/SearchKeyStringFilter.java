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

/**
 * A filter encapsulating a String.
 */
final class SearchKeyStringFilter implements SearchKeyFilter<String> {

    private final String filterString;

    private final byte[] filterArray;

    /**
     * Construct a filter from a String.
     *
     * @param filter the filter String
     */
    SearchKeyStringFilter(String filter) {
        this.filterString = filter;
        this.filterArray = filter.getBytes(StandardCharsets.UTF_8);
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
    public String getOriginalValue() {
        return filterString;
    }

}
