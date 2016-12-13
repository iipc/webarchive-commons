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

/**
 * Try to make a Uri absolute like the browsers do.
 * <p>
 * Mimics common browser behavior by adding file schema for uri's starting with '/' and http schema for others.
 */
public class InferCommonSchemesForSchemelessUri implements InParseNormalizer {

    @Override
    @Description(name = "Infer common schemes for schemeless URI",
                 description = "Mimics common browser behavior by adding file schema for uri's starting with '/'"
                 + " and http schema for others. This effectively turns every relative URI into absolute.")
    @Example(uri = "/path", normalizedUri = "file:///path")
    @Example(uri = "host", normalizedUri = "http://host/")
    public void preParseAuthority(Parser.ParserState parserState) {
        if (parserState.getBuilder().scheme() == null) {
            if (parserState.getUri().charAt(0) == '/') {
                parserState.setHasAuthority(true);
                parserState.getBuilder().scheme("file");
                if (parserState.getUri().charAt(1) == '/') {
                    parserState.incrementOffset(1);
                }
            } else {
                parserState.getBuilder().scheme("http");
                parserState.setHasAuthority(true);
            }
        }
    }

}
