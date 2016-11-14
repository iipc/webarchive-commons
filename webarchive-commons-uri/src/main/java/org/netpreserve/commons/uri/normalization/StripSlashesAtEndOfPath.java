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
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;

import static org.netpreserve.commons.uri.Scheme.HTTP;
import static org.netpreserve.commons.uri.Scheme.HTTPS;

/**
 * Normalizer for skipping extra slashes in path.
 *
 * Skips:
 * <ul>
 *   <li>double slashes in path</li>
 *   <li>slashes at end of path</li>
 * </ul>
 */
public class StripSlashesAtEndOfPath extends SchemeBasedNormalizer implements PostParseNormalizer {

    private static final Set<Scheme> SUPPORTED_SCHEMES = immutableSetOf(HTTP, HTTPS);

    @Override
    public void normalize(UriBuilder builder) {
        if (!builder.path().isEmpty()) {
            builder.path(builder.path().replace("/+", "/"));
            if (builder.path().endsWith("/")) {
                builder.path(builder.path().substring(0, builder.path().length() - 1));
            }
        }
    }

    @Override
    public Set<Scheme> getSupportedSchemes() {
        return SUPPORTED_SCHEMES;
    }

    @Override
    public void describeNormalization(List<NormalizationDescription> descriptions) {
        descriptions.add(NormalizationDescription.builder(StripSlashesAtEndOfPath.class)
                .name("Strip slashes in path")
                .description("Strips double slashes in path and slash at end of path.")
                .build());
    }

}
