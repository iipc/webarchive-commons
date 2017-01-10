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
 * Base interface for Normalizers that can be added to the Uri parsing process.
 * <p>
 * A normalizer should implement one of the sub-interfaces instead of implementing this one directly.
 * <p>
 * All normalizers must be state less and thread safe.
 */
public interface Normalizer {

    /**
     * Gives the Normalizer an opportunity to decide if this Normalizer should be run for the submitted builder.
     * <p>
     * The default implementation just returns {@code true}
     * <p>
     * @param builder the builder to check
     * @return true if this normalizer should be used for this builder.
     */
    default boolean validFor(UriBuilder builder) {
        return true;
    }

}
