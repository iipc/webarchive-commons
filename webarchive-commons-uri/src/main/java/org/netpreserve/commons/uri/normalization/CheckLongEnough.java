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

import java.util.List;
import java.util.Set;

import org.netpreserve.commons.uri.PostParseNormalizer;
import org.netpreserve.commons.uri.Scheme;
import org.netpreserve.commons.uri.UriException;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;

import static org.netpreserve.commons.uri.Scheme.HTTP;
import static org.netpreserve.commons.uri.Scheme.HTTPS;
import static org.netpreserve.commons.uri.normalization.SchemeBasedNormalizer.immutableSetOf;

/**
 * Check if the URI is long enough to be valid.
 * <p>
 * The check is only done for URI's with http and https schemes.
 * <p>
 * This isn't really a normalizer since it will throw an exception instead of fixing the URI.
 */
public class CheckLongEnough extends SchemeBasedNormalizer implements PostParseNormalizer {

    private static final Set<Scheme> SUPPORTED_SCHEMES = immutableSetOf(HTTP, HTTPS);

    @Override
    public void normalize(UriBuilder builder) {
        if ((!builder.isAuthority() || builder.host().isEmpty()) && builder.path().length() <= 2) {
            throw new UriException("http(s) scheme specific part is too short: " + toString());
        }
    }

    @Override
    public Set<Scheme> getSupportedSchemes() {
        return SUPPORTED_SCHEMES;
    }

    @Override
    public void describeNormalization(List<NormalizationDescription> descriptions) {
        // No description is added because this normalizer doesn't normalize, but throws an exception.
    }

}
