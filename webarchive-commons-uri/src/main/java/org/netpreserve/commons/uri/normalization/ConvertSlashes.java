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
 * Lax trimming.
 * <p>
 * Remove angle brackets and stray TAB/CR/LF. Convert backslashes to forward slashes except in query. Replace nbsp with
 * normal spaces
 */
public class ConvertSlashes implements PreParseNormalizer {

    static final String BACKSLASH = "\\";

    @Override
    @Description(name = "Convert slashes",
                 description = "Convert backslashes to forward slashes "
                 + "except in query.")
    @Example(uri = "http://foo.com\\bar", normalizedUri = "http://foo.com/bar")
    public String normalize(String uriString) {

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

        return uriString;
    }

}
