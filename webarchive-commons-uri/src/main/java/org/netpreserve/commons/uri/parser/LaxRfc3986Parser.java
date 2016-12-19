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
package org.netpreserve.commons.uri.parser;

import java.util.BitSet;

/**
 * A RFC 3986 conformant parser which is a little bit more relaxed on what characters are allowed in qurey and path.
 * <p>
 * The changes done by this parser is to behave like the lax uris used in Heritrix.
 * <p>
 * This parser differ from the strict one in that it in addition to the characters allowed by RFC 3986, allows the
 * characters {@code | < >} in paths and {@code ^ { } [ ] |} in queries.
 * <p>
 * It is more restrictive than the strict parser by not allowing percent encoded characters in registry names (host
 * name).
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

        // Do not allow percent encoded registry names
        allowedInRegistryName = (BitSet) REGISTRY_NAME.clone();
        allowedInRegistryName.clear('%');

        allowedInPath = (BitSet) PATH.clone();
        allowedInPath.set('|');
        allowedInPath.set('<');
        allowedInPath.set('>');
    }

}
