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

import org.netpreserve.commons.uri.parser.Parser;

/**
 * A Normalizer which hooks into the parsing process.
 * <p>
 * An InParseNormalizer might modify the state of the parsing process while parsing. For example it can infer a standard
 * scheme (e.g. http) for schemeless URI's which in turn might influence how the rest of the URI is parsed.
 */
public interface InParseNormalizer extends Normalizer {

    /**
     * Hook into the parsing process before the Authority is parsed.
     * <p>
     * @param parserState the parserState to manipulate
     */
    default void preParseAuthority(Parser.ParserState parserState) {

    }

    /**
     * Hook into the parsing process after the Host is detected, but before any validation.
     * <p>
     * @param parserState the parserState
     * @param host the detected host
     * @return the host string after manipulation
     */
    default String preParseHost(Parser.ParserState parserState, String host) {
        return host;
    }

}
