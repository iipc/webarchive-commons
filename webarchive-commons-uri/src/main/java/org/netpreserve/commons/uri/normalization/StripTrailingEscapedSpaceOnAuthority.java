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

import org.netpreserve.commons.uri.InParseNormalizer;
import org.netpreserve.commons.uri.Rfc3986Parser;

import static org.netpreserve.commons.uri.UriBuilder.ESCAPED_SPACE;

/**
 *
 */
public class StripTrailingEscapedSpaceOnAuthority implements InParseNormalizer {

    @Override
    public void postParseAuthority(Rfc3986Parser.ParserState parserState) {
        // Remove trailing escaped space
        String authority = parserState.getAuthority();
        while (authority.endsWith(ESCAPED_SPACE)) {
            authority = authority.substring(0, authority.length() - 3);
        }
        parserState.setAuthority(authority);
    }

}
