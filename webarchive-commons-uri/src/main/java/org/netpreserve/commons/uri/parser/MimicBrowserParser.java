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

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;

import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.uri.UriException;

/**
 *
 */
public class MimicBrowserParser extends Rfc3986Parser {

    BitSet simpleEncodeSet = new BitSet(256);

    public MimicBrowserParser() {
        // Laxed to conform to browser behavior.
        allowedInQuery = (BitSet) QUERY.clone();
        allowedInQuery.set('^');
        allowedInQuery.set('{');
        allowedInQuery.set('}');
        allowedInQuery.set('[');
        allowedInQuery.set(']');
        allowedInQuery.set('|');
        allowedInQuery.set('`');

        // Do not allow percent encoded registry names
        allowedInRegistryName = (BitSet) REGISTRY_NAME.clone();
        allowedInRegistryName.clear('%');

        allowedInPath = (BitSet) PATH.clone();
        allowedInPath.set('[');
        allowedInPath.set(']');

        allowedInUserInfo.clear(':');
        allowedInUserInfo.clear('@');

        for (int i = 0x20; i <= 0x7e; i++) {
            simpleEncodeSet.set(i);
        }

        endOfAuthority.set('\\');
    }

    /**
     * Parse port number.
     * <p>
     * This implementation differs from the strict by allowing empty port number and port number 0.
     * <p>
     * @param builder
     * @param authority
     * @param offset
     */
    @Override
    void parsePort(UriBuilder builder, CharBuffer authority) {
        if (authority.hasRemaining()) {
            try {
                builder.port(Integer.parseInt(authority.toString()));
            } catch (NumberFormatException error) {
                throw new UriException("invalid port number in: " + authority);
            }
        } else {
            builder.port(Uri.DEFAULT_PORT_MARKER);
        }

        // Check valid port number
        if (builder.port() < Uri.DEFAULT_PORT_MARKER || builder.port() > 65535) {
            throw new UriException("Port out of bounds: " + builder.port());
        }

        // Normalize known port numbers
        if (builder.config.isSchemeBasedNormalization() && builder.port() != Uri.DEFAULT_PORT_MARKER) {
            if (builder.port() == builder.schemeType().defaultPort()) {
                builder.port(Uri.DEFAULT_PORT_MARKER);
            }
        }
    }

    @Override
    void parseIpv4(UriBuilder builder, String ipv4Address) {
        builder.host(IpUtil.checkAndNormalizeIpv4(ipv4Address));
        if (builder.host() != null) {
            builder.setIPv4addressFlag();
        }
    }

    @Override
    String validatePath(ParserState parserState) {
        UriBuilder builder = parserState.getBuilder();
        CharBuffer uri = parserState.getUri();
        if ((builder.scheme() == null || !builder.schemeType().isSpecial())
                && (!builder.isAuthority() && (!uri.hasRemaining() || uri.charAt(0) != '/'))) {
            return validateAndNormalize(parserState.config, parserState.charset, parserState.uri, simpleEncodeSet);
        } else {
            replace(uri, '\\', '/');
            String path = validateAndNormalize(parserState.config, parserState.charset, parserState.uri, allowedInPath);
            path = path.replaceAll("%2[eE]", ".");
            return path;
        }
    }

    @Override
    public String validateFragment(UriBuilderConfig config, Charset charset, CharBuffer uri) {
        return uri.toString();
    }

    CharBuffer replace(CharBuffer src, char oldChar, char newChar) {
        final int from = src.position();
        final int to = src.limit();
        final char[] value = src.array();

        for (int i = from; i < to; i++) {
            if (value[i] == oldChar) {
                value[i] = newChar;
            }
        }
        return src;
    }

}
