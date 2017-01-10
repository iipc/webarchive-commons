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
 * Normalizer which turn the URI string into lower case before parsing.
 */
public class AllLowerCase implements PreParseNormalizer {

    @Override
    @Description(name = "All lower case",
                 description = "Turns all the characters of the URI into lower case.")
    @Example(uri = "HTTP://example.COM/path/Index.HTM", normalizedUri = "http://example.com/path/index.htm")
    public String normalize(String uriString) {
        return uriString.toLowerCase();
    }

}
