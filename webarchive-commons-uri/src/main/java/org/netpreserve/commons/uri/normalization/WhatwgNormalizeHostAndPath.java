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

import org.netpreserve.commons.uri.InParseNormalizer;
import org.netpreserve.commons.uri.parser.Parser;
import org.netpreserve.commons.uri.Scheme;
import org.netpreserve.commons.uri.normalization.report.Description;
import org.netpreserve.commons.uri.normalization.report.Example;

/**
 * WHATWG Normalize start of authority and path.
 * <p>
 * Skip or normalize errorneous slashes at start of authority. Handle windows drive letters at start of path.
 */
public class WhatwgNormalizeHostAndPath implements InParseNormalizer {

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

    @Description(name = "WHATWG Normalize start of authority and path",
                 description = "Skip or normalize errorneous slashes at start of authority. "
                 + "Handle windows drive letters at start of path.")
    @Example(uri = "foo:///example.com/", normalizedUri = "foo:///example.com/")
    @Example(uri = "http:\\\\example.com/", normalizedUri = "http://example.com/")
    @Example(uri = "file:///C|path", normalizedUri = "file:///C:path")
    @Example(uri = "file:/C:path", normalizedUri = "file:///C:path")
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
                if (leadingSlashCount > 0) {
                    parserState.incrementOffset(leadingSlashCount - 1);
                } else {
                    parserState.incrementOffset(0);
                }
                parserState.setHasAuthority(leadingSlashCount >= 2);
            } else if (leadingSlashCount >= 2) {
                parserState.setHasAuthority(true);
                parserState.incrementOffset(2);
            }
        }
    }

    /**
     * Check for and normalize a windows drive letter.
     * <p>
     * A path starts with a windows drive letter if it starts with a single letter in the range a-z,A-z and is followed
     * by a ':' or '|'. If the second character is '|' it is replaced with ':'.
     * <p>
     * @param parserState the current parser state
     * @param offset the offset to check, relative to the parserState.getUri().position()
     * @return true if path starts with a windows drive letter
     */
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

}
