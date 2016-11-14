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

import org.netpreserve.commons.uri.InParseNormalizer;
import org.netpreserve.commons.uri.parser.Parser;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;

/**
 * Try to make a Uri absolute like the browsers do.
 * <p>
 * Mimics common browser behavior by adding file schema for uri's starting with '/' and http schema for others.
 */
public class InsertCommonSchemesForSchemelessUri implements InParseNormalizer {

    @Override
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

    @Override
    public void describeNormalization(List<NormalizationDescription> descriptions) {
        descriptions.add(NormalizationDescription.builder(InsertCommonSchemesForSchemelessUri.class)
                .name("Insert common schemes for schemeless URI")
                .description("Mimics common browser behavior by adding file schema for uri's starting with '/'"
                        + " and http schema for others.")
                .build());
    }

}
