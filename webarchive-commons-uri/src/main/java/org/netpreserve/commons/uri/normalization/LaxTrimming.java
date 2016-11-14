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

import org.netpreserve.commons.uri.PreParseNormalizer;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;
import org.netpreserve.commons.uri.normalization.report.NormalizationExample;

/**
 *
 */
public class LaxTrimming implements PreParseNormalizer {
    static final char SPACE = ' ';

    static final char NBSP = '\u00A0';

    static final String EMPTY_STRING = "";

    static final String BACKSLASH = "\\";

    @Override
    public String normalize(String uriString) {

        /*
         * Remove angle brackets around an URI.
         */
        uriString = trimPair(uriString, "<", ">");

        // Replace nbsp with normal spaces (so that they get stripped if at
        // ends, or encoded if in middle)
        if (uriString.indexOf(NBSP) >= 0) {
            uriString = uriString.replace(NBSP, SPACE);
        }

        // IE converts backslashes preceding the query string to slashes, rather
        // than to %5C. Since URIs that have backslashes usually work only with
        // IE, we will convert backslashes to slashes as well.
        int nextBackslash = uriString.indexOf(BACKSLASH);
        if (nextBackslash >= 0) {
            int queryStart = uriString.indexOf('?');
            StringBuilder tmp = new StringBuilder(uriString);
            while (nextBackslash >= 0
                    && (queryStart < 0 || nextBackslash < queryStart)) {
                tmp.setCharAt(nextBackslash, '/');
                nextBackslash = uriString.indexOf(BACKSLASH, nextBackslash + 1);
            }
            uriString = tmp.toString();
        }

        // Remove stray TAB/CR/LF
        uriString = remove(uriString, '\n', '\r', '\t');

        return uriString.trim();
    }

    String remove(String src, char... ch) {
        final int len = src.length();
        int i = -1;
        int removed = 0;
        char[] value = src.toCharArray();

        while (++i < len) {
            if (removed > 0) {
                value[i - removed] = value[i];
            }

            for (int j = 0; j < ch.length; j++) {
                if (value[i] == ch[j]) {
                    removed++;
                    break;
                }
            }
        }

        if (removed > 0) {
            return new String(value, 0, len - removed);
        } else {
            return src;
        }
    }

    private String trimPair(String string, String ch1, String ch2) {
        if (string.startsWith(ch1) && string.endsWith(ch2)) {
            return string.substring(1, string.length() - 1);
        } else {
            return string;
        }
    }

    @Override
    public void describeNormalization(List<NormalizationDescription> descriptions) {
        descriptions.add(NormalizationDescription.builder(LaxTrimming.class)
                .name("Remove angle brackets")
                .description("Remove angle brackets around an URI.")
                .example(NormalizationExample.builder()
                        .uri("<http://foo.com/bar>").normalizedUri("http://foo.com/bar").build())
                .build());
        descriptions.add(NormalizationDescription.builder(LaxTrimming.class)
                .name("Replace nbsp")
                .description("Replace nbsp with normal spaces so that they get stripped if at ends,"
                        + " or encoded if in middle.")
                .build());
        descriptions.add(NormalizationDescription.builder(LaxTrimming.class)
                .name("Convert backslashes")
                .description("IE converts backslashes preceding the query string to slashes, rather than to %5C. "
                        + "Since URIs that have backslashes usually work only with IE, we will convert "
                        + "backslashes to slashes as well.")
                .build());
        descriptions.add(NormalizationDescription.builder(LaxTrimming.class)
                .name("Remove stray whitespace")
                .description("Remove stray TAB/CR/LF.")
                .build());
    }
}
