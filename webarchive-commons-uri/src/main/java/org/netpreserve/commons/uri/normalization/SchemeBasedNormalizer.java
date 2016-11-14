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
package org.netpreserve.commons.uri.normalization;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.netpreserve.commons.uri.Normalizer;
import org.netpreserve.commons.uri.Scheme;
import org.netpreserve.commons.uri.UriBuilder;

/**
 * Normalizers implementing this interface is executed only for the Schemes they claim to support.
 */
public abstract class SchemeBasedNormalizer implements Normalizer {

    /**
     * Get a set of schemes this normalizer supports.
     * <p>
     * @return the supported schemes
     */
    public abstract Set<Scheme> getSupportedSchemes();

    @Override
    public boolean validFor(UriBuilder builder) {
        if (builder.config.isSchemeBasedNormalization()) {
            Set<Scheme> supportedSchemes = getSupportedSchemes();
            if (supportedSchemes.isEmpty()) {
                return true;
            } else {
                return getSupportedSchemes().contains(builder.schemeType());
            }
        }
        return false;
    }

    /**
     * Helper method for creating a static immutable set of Schemes.
     * <p>
     * This can be used by subclasses to create the set of supported schemes.
     * <p>
     * @param first a scheme the set is to contain
     * @param rest remainig schemes the set is to contain
     * @return an immutable set containing the schemes
     */
    protected final static Set<Scheme> immutableSetOf(Scheme first, Scheme... rest) {
        return Collections.unmodifiableSet(EnumSet.of(first, rest));
    }

}
