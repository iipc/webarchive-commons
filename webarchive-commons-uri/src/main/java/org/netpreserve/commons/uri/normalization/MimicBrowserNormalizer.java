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

import java.nio.CharBuffer;
import java.util.BitSet;
import java.util.List;

import org.netpreserve.commons.uri.InParseNormalizer;
import org.netpreserve.commons.uri.parser.Parser;
import org.netpreserve.commons.uri.PreParseNormalizer;
import org.netpreserve.commons.uri.Scheme;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;

/**
 *
 */
public class MimicBrowserNormalizer implements PreParseNormalizer, InParseNormalizer {

    protected static final BitSet ALPHA = new BitSet(256);

    // Static initializer for ALPHA
    static {
        for (int i = 'a'; i <= 'z'; i++) {
            ALPHA.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            ALPHA.set(i);
        }
    }

    @Override
    public String normalize(String uriString) {
        char[] val = uriString.toCharArray();
        int len = val.length;
        int st = 0;

        // Remove leading controls and space
        while ((st < len) && (val[st] <= ' ')) {
            st++;
        }

        // Remove trailing controls and space
        while ((st < len) && (val[len - 1] <= ' ')) {
            len--;
        }

        // Remove tab and newline
        int removed = 0;
        for (int i = st; i < len; i++) {
            if (removed > 0) {
                val[i - removed] = val[i];
            }

            if (val[i] == '\n' || val[i] == '\r' || val[i] == '\t') {
                removed++;
            }
        }

        len -= removed;
        return ((st > 0) || (len < val.length)) ? new String(val, st, len - st) : uriString;
    }

    @Override
    public void preParseAuthority(Parser.ParserState parserState) {
        // Skip errorneous extra slashes at start of authority
        if (!parserState.hasAuthority() && parserState.uriHasAtLeastMoreChararcters(1)) {
            int leadingSlashCount = 0;
            while (parserState.uriHasAtLeastMoreChararcters(1 + leadingSlashCount)
                    && (parserState.getUri().charAt(leadingSlashCount) == '/'
                    || parserState.getUri().charAt(leadingSlashCount) == '\\')) {
                leadingSlashCount++;
            }
            if (parserState.getBuilder().schemeType() == Scheme.FILE) {
                if (leadingSlashCount < 2) {
                    parserState.setHasAuthority(false);
                    isWindowsDriveLetter(parserState, 0);
                } else if (leadingSlashCount == 2) {
                    parserState.setHasAuthority(true);
                    parserState.incrementOffset(2);
                    isWindowsDriveLetter(parserState, 0);
                } else {
                    parserState.setHasAuthority(true);
                    parserState.incrementOffset(leadingSlashCount - 1);
                    isWindowsDriveLetter(parserState, 1);
                }
            } else if (parserState.getBuilder().scheme() == null
                    && isWindowsDriveLetter(parserState, leadingSlashCount)) {
                parserState.incrementOffset(leadingSlashCount > 0 ? leadingSlashCount - 1 : 0);
                parserState.setHasAuthority(leadingSlashCount >= 2);
            } else if (leadingSlashCount >= 2) {
                parserState.setHasAuthority(true);
                parserState.incrementOffset(2);
            }
        }
    }

    @Override
    public String preParseHost(Parser.ParserState parserState, String host) {
        if (parserState.getBuilder().schemeType() == Scheme.FILE && "localhost".equals(host)) {
            return "";
        }
        return host;
    }

    boolean isWindowsDriveLetter(Parser.ParserState parserState, int offset) {
        CharBuffer uri = parserState.getUri();

        if (uri.remaining() > offset + 1 && ALPHA.get(uri.charAt(offset))) {
            if (uri.charAt(offset + 1) == ':') {
                return true;
            }
            if (uri.charAt(offset + 1) == '|') {
                uri.put(uri.position() + offset + 1, ':');
                return true;
            }
        }
        return false;
    }

    @Override
    public void describeNormalization(List<NormalizationDescription> descriptions) {
        descriptions.add(NormalizationDescription.builder(MimicBrowserNormalizer.class)
                .name("Trim")
                .description("Remove leading and trailing control characters and space.")
                .build());
        descriptions.add(NormalizationDescription.builder(MimicBrowserNormalizer.class)
                .name("Remove stray whitespace")
                .description("Remove stray TAB/CR/LF.")
                .build());
    }

}
