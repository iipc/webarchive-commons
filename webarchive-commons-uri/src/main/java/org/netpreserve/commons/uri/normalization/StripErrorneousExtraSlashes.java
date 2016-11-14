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

import org.netpreserve.commons.uri.InParseNormalizer;
import org.netpreserve.commons.uri.parser.Parser;
import org.netpreserve.commons.uri.Scheme;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;

import static org.netpreserve.commons.uri.Scheme.HTTP;
import static org.netpreserve.commons.uri.Scheme.HTTPS;
import static org.netpreserve.commons.uri.Scheme.FTP;
import static org.netpreserve.commons.uri.Scheme.FTPS;

/**
 * Normalizer for skipping errorneous extra slashes.
 *
 * Skips extra slashes at start of authority
 */
public class StripErrorneousExtraSlashes extends SchemeBasedNormalizer implements InParseNormalizer {

    private static final Set<Scheme> SUPPORTED_SCHEMES = immutableSetOf(HTTP, HTTPS, FTP, FTPS);

    @Override
    public void preParseAuthority(Parser.ParserState parserState) {
        // Skip errorneous extra slashes at start of authority
        if (!parserState.hasAuthority() && parserState.uriHasAtLeastMoreChararcters(1)
                && parserState.getUri().charAt(0) == '/') {

            int leadingSlashCount = 1;
            while (parserState.uriHasAtLeastMoreChararcters(1 + leadingSlashCount)
                    && parserState.getUri().charAt(leadingSlashCount) == '/') {
                leadingSlashCount++;
            }
            if (leadingSlashCount >= 2) {
                parserState.setHasAuthority(true);
                parserState.incrementOffset(leadingSlashCount);
            }
        }
    }

    @Override
    public Set<Scheme> getSupportedSchemes() {
        return SUPPORTED_SCHEMES;
    }

    @Override
    public void describeNormalization(List<NormalizationDescription> descriptions) {
        descriptions.add(NormalizationDescription.builder(StripErrorneousExtraSlashes.class)
                .name("Strip errorneous extra slashes")
                .description("Skips extra slashes at start of authority.")
                .build());
    }

}
