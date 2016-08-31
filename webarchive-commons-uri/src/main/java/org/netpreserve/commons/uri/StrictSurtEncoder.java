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

/**
 * A SURT encoder which follows the original implementation as used in Heritrix.
 */
public class StrictSurtEncoder implements SurtEncoder {

    @Override
    public void encode(StringBuilder sb, Uri uri, UriFormat uriFormat) {
        sb.append("(");

        if (!uriFormat.ignoreHost && uri.host != null) {
            if (uri.isRegName) {
                String host;
                if (uriFormat.decodeHost) {
                    host = uri.getDecodedHost();
                } else {
                    host = uri.host;
                }

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
                sb.append(uri.host);
            }
        }

        if (!uriFormat.ignorePort && uri.port != -1) {
            sb.append(':').append(uri.port);
        }

        if (!uriFormat.ignoreUserInfo && uri.userinfo != null) {
            sb.append('@');
            sb.append(uri.userinfo);
        }

        sb.append(')');
    }

}
