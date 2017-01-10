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

import org.netpreserve.commons.uri.PreParseNormalizer;
import org.netpreserve.commons.uri.normalization.report.Description;
import org.netpreserve.commons.uri.normalization.report.Example;

/**
 * WHATWG Trimming.
 * <p>
 * Remove leading and trailing control characters and space. Remove stray TAB/CR/LF.
 */
public class WhatwgPreTrimming implements PreParseNormalizer {

    @Description(name = "WHATWG Trimming", description = "Remove leading and trailing control characters and space."
                 + " Remove stray TAB/CR/LF.")
    @Example(uri = " http://www.example.com", normalizedUri = "http://www.example.com/")
    @Example(uri = "http://www.\texample.com", normalizedUri = "http://www.example.com/")
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
        if (st > 0 || len < val.length) {
            return new String(val, st, len - st);
        } else {
            return uriString;
        }
    }

}
