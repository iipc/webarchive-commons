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
 * A normalizer which is executed after the Uri is parsed into a UriBuilder.
 * <p>
 * URI's processed by a PostParseNormalizer are already parsed into their components. That means they are valid URI's
 * according to the rules specified in {@link UriBuilderConfig}. The type of normalization done by implementations of a
 * PostParseNormalizer is to reduce the number of invariants, not to fix broken URI's. Examples could be sorting the
 * query parameters and removing session id's.
 */
public interface PostParseNormalizer extends Normalizer {

    /**
     * Normalize a parsed URI.
     * <p>
     * The URI is already parsed into its components. That means it is a valid URI according to the rules specified in
     * {@link UriBuilderConfig}.
     * <p>
     * @param builder the parsed URI to normalize. The UriBuilder is normalized in place
     */
    void normalize(UriBuilder builder);

}
