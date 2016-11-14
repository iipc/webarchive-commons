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

import static org.netpreserve.commons.uri.Scheme.DNS;
import static org.netpreserve.commons.uri.normalization.SchemeBasedNormalizer.immutableSetOf;

public class OptimisticDnsScheme extends SchemeBasedNormalizer implements PostParseNormalizer {

    private static final Set<Scheme> SUPPORTED_SCHEMES = immutableSetOf(DNS);

    @Override
    public void normalize(UriBuilder builder) {
        if (builder.host() != null) {
            builder.path("");
        } else if (!builder.path().isEmpty()) {
            builder.config.getParser().decomposeAuthority(builder, builder.path());
            if (builder.host() != null) {
                builder.path("");
            }
        }
    }

    @Override
    public Set<Scheme> getSupportedSchemes() {
        return SUPPORTED_SCHEMES;
    }

    @Override
    public void describeNormalization(List<NormalizationDescription> descriptions) {
        descriptions.add(NormalizationDescription.builder(OptimisticDnsScheme.class)
                .name("Optimistic DNS scheme")
                .description("If dns host is found in the authority, the path is removed. "
                        + "If dns host is found in the path, it is moved to the authority.")
                .build());
    }

}
