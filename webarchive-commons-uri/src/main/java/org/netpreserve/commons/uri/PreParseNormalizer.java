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
package org.netpreserve.commons.uri;

/**
 * A normalizer which is executed before the Uri is parsed into a UriBuilder.
 * <p>
 * URI's processed by a PreParseNormalizer are not parsed into their components. Implementations of PreParseNormalizer
 * then has the opportunity to fix broken URI's. For example remove angle brackets around a URI.
 */
public interface PreParseNormalizer extends Normalizer {

    /**
     * Normalize a URI string before it is parsed.
     * <p>
     * @param uriString the URI string to normalize
     * @return the normalized string
     */
    String normalize(String uriString);

}
