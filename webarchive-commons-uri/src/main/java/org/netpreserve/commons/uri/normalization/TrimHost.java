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
import org.netpreserve.commons.uri.UriException;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;
import org.netpreserve.commons.uri.normalization.report.NormalizationExample;
import org.netpreserve.commons.uri.parser.Parser;

/**
 *
 */
public class TrimHost implements InParseNormalizer {
    @Override
    public String preParseHost(Parser.ParserState parserState, String host) {
        if (host.contains("..")) {
            throw new UriException("Illegal hostname: " + host);
        }
        host = trim(host, '.');

        return host;
    }

    private String trim(String string, char trim) {
        int len = string.length();
        int st = 0;
        char[] val = string.toCharArray();

        while ((st < len) && (val[st] == trim)) {
            st++;
        }
        while ((st < len) && (val[len - 1] == trim)) {
            len--;
        }
        return ((st > 0) || (len < string.length())) ? string.substring(st, len) : string;
    }

    @Override
    public void describeNormalization(List<NormalizationDescription> descriptions) {
        descriptions.add(NormalizationDescription.builder(LaxTrimming.class)
                .name("Trim host")
                .description("Remove leading and trailing dots from host name.")
                .example(NormalizationExample.builder()
                        .uri("<http://foo.com./bar>").normalizedUri("http://foo.com/bar").build())
                .build());
    }

}
