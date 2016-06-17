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

import java.util.Set;

import org.netpreserve.commons.uri.InParseNormalizer;
import org.netpreserve.commons.uri.PostParseNormalizer;
import org.netpreserve.commons.uri.Rfc3986Parser;
import org.netpreserve.commons.uri.UriBuilder;

import static org.netpreserve.commons.uri.UriBuilder.SCHEME_HTTP;
import static org.netpreserve.commons.uri.UriBuilder.SCHEME_HTTPS;

/**
 * Normalizer for skipping errorneous extra slashes.
 *
 * Skips:
 * <ul>
 *   <li>extra slashes at start of authority</li>
 *   <li>double slashes in path</li>
 *   <li>slashs at end of path</li>
 * </ul>
 */
public class StripErrorneousExtraSlashes extends SchemeBasedNormalizer implements InParseNormalizer {

    private static final Set<String> SUPPORTED_SCHEMES = immutableSetOf(SCHEME_HTTP, SCHEME_HTTPS);

    @Override
    public void preParseAuthority(Rfc3986Parser.ParserState parserState) {
        // Skip errorneous extra slashes at start of authority
        if (!parserState.hasAuthority() && parserState.uriHasAtLeastMoreChararcters(1)
                && parserState.getUri().charAt(parserState.getOffset()) == '/') {

            int leadingSlashCount = 1;
            while (parserState.uriHasAtLeastMoreChararcters(1 + leadingSlashCount)
                    && parserState.getUri().charAt(parserState.getOffset() + leadingSlashCount) == '/') {
                leadingSlashCount++;
            }
            if (leadingSlashCount >= 2) {
                parserState.setHasAuthority(true);
                parserState.incrementOffset(leadingSlashCount);
            }
        }
    }

    @Override
    public Set<String> getSupportedSchemes() {
        return SUPPORTED_SCHEMES;
    }

}
