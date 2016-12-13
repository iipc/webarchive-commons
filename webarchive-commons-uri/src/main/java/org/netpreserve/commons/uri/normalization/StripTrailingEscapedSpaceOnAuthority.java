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
import org.netpreserve.commons.uri.normalization.report.Description;
import org.netpreserve.commons.uri.normalization.report.Example;
import org.netpreserve.commons.uri.parser.Parser;

import static org.netpreserve.commons.uri.UriBuilder.ESCAPED_SPACE;

/**
 *
 */
public class StripTrailingEscapedSpaceOnAuthority implements InParseNormalizer {

    @Override
    @Description(name = "Strip trailing escaped space on authority",
                 description = "Removes escaped space i.e. %20, from end of authority.")
    @Example(uri = "http://www.example.com%20/path", normalizedUri = "http://www.example.com/path")
    public String preParseHost(Parser.ParserState parserState, String host) {
        // Remove trailing escaped space
        while (host.endsWith(ESCAPED_SPACE)) {
            host = host.substring(0, host.length() - 3);
        }
        return host;
    }

}
