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

import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriFormat;

/**
 * A SURT encoder which follows the original implementation as used in Heritrix.
 */
public class StrictSurtEncoder implements SurtEncoder {

    @Override
    public void encode(StringBuilder sb, Uri uri, UriFormat uriFormat) {
        sb.append("(");

        String host = uriFormat.getHost(uri);
        if (host != null) {
            if (uri.isRegistryName()) {

                // other hostname match: do reverse
                int hostSegEnd = host.length();
                for (int i = hostSegEnd; i >= 0; i--) {
                    if (i > 0 && host.charAt(i - 1) != '.') {
                        continue;
                    }
                    sb.append(host, i, hostSegEnd); // rev getHost segment
                    sb.append(',');     // ','
                    hostSegEnd = i - 1;
                }
            } else {
                sb.append(host);
            }
        }

        if (uriFormat.getPort(uri) != null) {
            sb.append(':').append(uriFormat.getPort(uri));
        }

        if (uriFormat.getUser(uri) != null || uriFormat.getPassword(uri) != null) {
            sb.append('@');
        }
        if (uriFormat.getUser(uri) != null) {
            sb.append(uriFormat.getUser(uri));
        }
        if (uriFormat.getPassword(uri) != null) {
            sb.append(':');
            sb.append(uriFormat.getPassword(uri));
        }

        sb.append(')');
    }

}
