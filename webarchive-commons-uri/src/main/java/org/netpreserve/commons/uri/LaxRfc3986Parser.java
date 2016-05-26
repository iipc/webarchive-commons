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
package org.netpreserve.commons.uri;

import java.util.BitSet;

/**
 *
 */
public class LaxRfc3986Parser extends Rfc3986Parser {

    public LaxRfc3986Parser() {
        // Laxed to conform to browser behavior.
        allowedInQuery = (BitSet) QUERY.clone();
        allowedInQuery.set('^');
        allowedInQuery.set('{');
        allowedInQuery.set('}');
        allowedInQuery.set('[');
        allowedInQuery.set(']');
        allowedInQuery.set('|');
//        allowedInQuery.set('<');
//        allowedInQuery.set('>');
//        allowedInQuery.set('"');

        // Do not allow percent encoded registry names
        allowedInRegistryName = (BitSet) REGISTRY_NAME.clone();
        allowedInRegistryName.clear('%');

        allowedInPath = (BitSet) PATH.clone();
        allowedInPath.set('|');
        allowedInPath.set('<');
        allowedInPath.set('>');
//        allowedInPath.set('[');
//        allowedInPath.set(']');
//        allowedInPath.set('"');
    }

    @Override
    String preCheckRegistryName(String registryName) {
        if (registryName.contains("..")) {
            throw new UriException("Illegal hostname: " + registryName);
        }
        registryName = trim(registryName, '.');

        return registryName;
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

}
