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

import java.io.ByteArrayOutputStream;
import java.net.IDN;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/**
 *
 */
public class Rfc3986Parser {

    /**
     * Radix used in encoding and decoding.
     */
    static final int RADIX = 16;

    /**
     * BitSet for DIGIT.
     * <p><blockquote><pre>
     * DIGIT    = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
     *            "8" | "9"
     * </pre></blockquote><p>
     */
    protected static final BitSet DIGIT = new BitSet(256);
    // Static initializer for DIGIT
    static {
        for (int i = '0'; i <= '9'; i++) {
            DIGIT.set(i);
        }
    }


    /**
     * BitSet for ALPHA.
     * <p><blockquote><pre>
     * ALPHA         = lowalpha | upalpha
     * </pre></blockquote><p>
     */
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

    /**
     * BitSet for HEX.
     * <p><blockquote><pre>
     * HEX           = DIGIT | "A" | "B" | "C" | "D" | "E" | "F" |
     *                         "a" | "b" | "c" | "d" | "e" | "f"
     * </pre></blockquote><p>
     */
    protected static final BitSet HEX = new BitSet(256);
    // Static initializer for HEX
    static {
        HEX.or(DIGIT);
        for (int i = 'a'; i <= 'f'; i++) {
            HEX.set(i);
        }
        for (int i = 'A'; i <= 'F'; i++) {
            HEX.set(i);
        }
    }

    /**
     * BitSet for gen-delims.
     */
    public static final BitSet GEN_DELIMS = new BitSet(256);
    // Static initializer for GEN_DELIMS
    static {
        GEN_DELIMS.set(':');
        GEN_DELIMS.set('/');
        GEN_DELIMS.set('?');
        GEN_DELIMS.set('#');
        GEN_DELIMS.set('[');
        GEN_DELIMS.set(']');
        GEN_DELIMS.set('@');
    }

    /**
     * BitSet for sub-delims.
     */
    public static final BitSet SUB_DELIMS = new BitSet(256);
    // Static initializer for GEN_DELIMS
    static {
        SUB_DELIMS.set('!');
        SUB_DELIMS.set('$');
        SUB_DELIMS.set('&');
        SUB_DELIMS.set('\'');
        SUB_DELIMS.set('(');
        SUB_DELIMS.set(')');
        SUB_DELIMS.set('*');
        SUB_DELIMS.set('+');
        SUB_DELIMS.set(',');
        SUB_DELIMS.set(';');
        SUB_DELIMS.set('=');
    }

    /**
     * Data characters that are allowed in a URI but do not have a RESERVED
 purpose are called UNRESERVED.
     * <p><blockquote><pre>
 UNRESERVED    = alphanum | mark
 </pre></blockquote><p>
     */
    protected static final BitSet UNRESERVED = new BitSet(256);
    // Static initializer for UNRESERVED
    static {
        UNRESERVED.or(ALPHA);
        UNRESERVED.or(DIGIT);
        UNRESERVED.set('-');
        UNRESERVED.set('.');
        UNRESERVED.set('_');
        UNRESERVED.set('~');
    }

    /**
     * BitSet for RESERVED.
     * <p><blockquote><pre>
 RESERVED      = gen-delims / sub-delims
 </pre></blockquote><p>
     */
    protected static final BitSet RESERVED = new BitSet(256);
    // Static initializer for RESERVED
    static {
        RESERVED.or(GEN_DELIMS);
        RESERVED.or(SUB_DELIMS);
    }

    /**
     * BitSet for PCHAR.
     * <p><blockquote><pre>
 PCHAR         = UNRESERVED | escaped |
                 ":" | "@" | "&amp;" | "=" | "+" | "$" | ","
     * </pre></blockquote><p>
     */
    protected static final BitSet PCHAR = new BitSet(256);
    // Static initializer for PCHAR
    static {
        PCHAR.or(UNRESERVED);
        PCHAR.set('%');
        PCHAR.or(SUB_DELIMS);
        PCHAR.set(':');
        PCHAR.set('@');
    }

    /**
     * BitSet for SCHEME.
     * <p><blockquote><pre>
 SCHEME        = ALPHA *( ALPHA | DIGIT | "+" | "-" | "." )
 </pre></blockquote><p>
     */
    protected static final BitSet SCHEME = new BitSet(256);
    // Static initializer for SCHEME
    static {
        SCHEME.or(ALPHA);
        SCHEME.or(DIGIT);
        SCHEME.set('+');
        SCHEME.set('-');
        SCHEME.set('.');
    }

    /**
     * BitSet for REGISTRY_NAME.
     */
    protected static final BitSet REGISTRY_NAME = new BitSet(256);
    // Static initializer for PATH
    static {
        REGISTRY_NAME.or(UNRESERVED);
        REGISTRY_NAME.set('%');
        REGISTRY_NAME.or(SUB_DELIMS);
    }

    /**
     * BitSet for PATH.
     */
    protected static final BitSet PATH = new BitSet(256);
    // Static initializer for PATH
    static {
        PATH.or(PCHAR);
        PATH.set('/');
    }

    /**
     * BitSet for QUERY.
     */
    protected static final BitSet QUERY = new BitSet(256);
    // Static initializer for QUERY
    static {
        QUERY.or(PCHAR);
        QUERY.set('/');
        QUERY.set('?');
    }

    /**
     * BitSet for FRAGMENT.
     */
    protected static final BitSet FRAGMENT = new BitSet(256);
    // Static initializer for FRAGMENT
    static {
        FRAGMENT.or(PCHAR);
        FRAGMENT.set('/');
        FRAGMENT.set('?');
    }

    protected BitSet allowedInScheme;
    protected BitSet allowedInRegistryName;
    protected BitSet allowedInPath;
    protected BitSet allowedInQuery;
    protected BitSet allowedInFragment;

    public Rfc3986Parser() {
        allowedInScheme = SCHEME;
        allowedInRegistryName = REGISTRY_NAME;
        allowedInPath = PATH;
        allowedInQuery = QUERY;
        allowedInFragment = FRAGMENT;
    }

    void parseUri(UriBuilder builder, String uri, int offset) {
        int nextOffset = offset;
        nextOffset = parseScheme(builder, uri, nextOffset);
        nextOffset = parseAuthority(builder, uri, nextOffset);
        nextOffset = parsePath(builder, uri, nextOffset);
        nextOffset = parseQuery(builder, uri, nextOffset);
        nextOffset = parseFragment(builder, uri, nextOffset);
    }

    int parseScheme(UriBuilder builder, String uri, int offset) {
        int colonIdx = uri.indexOf(':', offset);
        if (colonIdx > 0) {
            builder.scheme = validateScheme(uri, offset, colonIdx);
            if (builder.scheme != null) {
                if (builder.config.isCaseNormalization()) {
                    builder.scheme = builder.scheme.toLowerCase();
                }
                return colonIdx + 1;
            }
        }

        builder.scheme = null;

        if (builder.config.isRequireAbsoluteUri()) {
            throw new UriException("Uri is not absolute");
        }

        return offset;
    }

    String validateScheme(String uri, int soffset, int eoffset) {
        if (ALPHA.get(uri.charAt(soffset))) {
            return validate(uri, soffset, eoffset, allowedInScheme);
        }
        return null;
    }

    int parseAuthority(UriBuilder builder, String uri, int offset) {
        if (uri.length() > offset + 1 && uri.charAt(offset) == '/' && uri.charAt(offset + 1) == '/') {
            int end = indexOf(uri, offset + 2, '/', '?', '#');
            if (end == -1) {
                end = uri.length();
            }

            String authority = uri.substring(offset + 2, end);
            parseAuthority(builder, authority);

            return end;
        } else {
            builder.authority = null;
            builder.userinfo = null;
            builder.host = null;
            builder.port = -1;
            builder.isIPv4address = false;
            builder.isIPv6reference = false;
            return offset;
        }
    }

    void parseAuthority(UriBuilder builder, String authority) {
        int from = 0;

        int next = authority.indexOf('@', from);
        switch (next) {
            case -1:
                builder.userinfo = null;
                break;
            case 0:
                builder.userinfo = "";
                from++;
                break;
            default:
                // TODO: Check and encode
                builder.userinfo = authority.substring(from, next);
                from = next + 1;
                break;
        }

        next = parseIpv6(builder, authority, from);
        if (!builder.isIPv6reference) {
            next = parseIpv4(builder, authority, from);
        }
        if (!builder.isIPv6reference && !builder.isIPv4address) {
            next = parseRegistryName(builder, authority, from);
        }

        parsePort(builder, authority, next);

        constructAuthority(builder);
    }

    void constructAuthority(UriBuilder builder) {
        // set a server-based naming authority
        if (builder.userinfo == null && builder.port == -1 && !builder.isIPv6reference) {
            builder.authority = builder.host;
        } else {
            StringBuilder buf = new StringBuilder();
            if (builder.userinfo != null) {
                buf.append(builder.userinfo);
                buf.append('@');
            }
            if (builder.host != null) {
                if (builder.isIPv6reference) {
                    buf.append('[').append(builder.host).append(']');
                } else {
                    buf.append(builder.host);
                }
                if (builder.port != -1) {
                    buf.append(':');
                    buf.append(builder.port);
                }
            }
            builder.authority = buf.toString();
        }
    }

    int parseIpv6(UriBuilder builder, String authority, int offset) {
        int end = offset;
        if (authority.length() > offset + 3 && authority.charAt(offset) == '[') {
            end = authority.indexOf(']', offset + 2);
            if (end == -1) {
                throw new UriException("Error parsing IPv6reference from " + authority);
            }

            // In IPv6reference, '[', ']' should be excluded
            builder.host = authority.substring(offset + 1, end);
            end++;

            // Set flag
            builder.isIPv6reference = true;
            builder.isIPv4address = false;
            builder.isRegName = false;
        }
        return end;
    }

    int parseIpv4(UriBuilder builder, String authority, int offset) {
        int end = authority.indexOf(':', offset);
        if (end == -1) {
            end = authority.length();
        }

        String ipv4Address = authority.substring(offset, end);
        String[] octets = ipv4Address.split("\\.");
        if (octets.length != 4) {
            return offset;
        }
        for (String octet : octets) {
            try {
                int value = Integer.parseInt(octet);
                if (value < 0 || value > 255) {
                    return offset;
                }
            } catch (NumberFormatException e) {
                return offset;
            }
        }

        builder.host = ipv4Address;

        // Set flag
        builder.isIPv4address = true;
        builder.isIPv6reference = false;
        builder.isRegName = false;

        return end;
    }

    int parseRegistryName(UriBuilder builder, String authority, int offset) {
        int end = authority.indexOf(':', offset);
        if (end == -1) {
            end = authority.length();
        }

        String registryName = authority.substring(offset, end).toLowerCase();

        if (builder.config.isCaseNormalization()) {
            registryName = registryName.toLowerCase();
        }

        registryName = preCheckRegistryName(registryName);

        if (builder.config.isSchemeBasedNormalization()) {
            if (SchemeParams.forName(builder.scheme).punycodedHost) {
                // apply IDN-punycoding, as necessary
                try {
                    builder.host = IDN.toASCII(registryName, IDN.USE_STD3_ASCII_RULES);
                } catch (IllegalArgumentException e) {

                    // check if domain name has ACE prefix, leading/trailing dash, or underscore
                    // we still wish to tolerate those;
                    builder.host = validate(registryName, 0, -1, allowedInRegistryName);
                    if (builder.host == null) {
                        throw new UriException("Invalid hostname: " + builder.host);
                    } else {
                        builder.authority = validateAndNormalize(builder, authority, 0, -1, allowedInRegistryName);
                    }
                }
            } else {
                builder.host = validateRegistryName(builder, registryName, 0, -1);
            }
        } else {
            builder.host = validateRegistryName(builder, registryName, 0, -1);
        }

        // Set flag
        builder.isRegName = true;
        builder.isIPv4address = false;
        builder.isIPv6reference = false;

        return end;
    }

    String preCheckRegistryName(String registryName) {
        return registryName;
    }

    String validateRegistryName(UriBuilder builder, String authority, int soffset, int eoffset) {
        return validateAndNormalize(builder, authority, soffset, eoffset, allowedInRegistryName);
    }

    void parsePort(UriBuilder builder, String authority, int offset) {
        if (authority.length() > offset && authority.charAt(offset) == ':') {
            int from = offset + 1;
            try {
                builder.port = Integer.parseInt(authority.substring(from));
            } catch (NumberFormatException error) {
                throw new UriException("invalid port number in: " + authority);
            }
        }

        // Check valid port number
        if (builder.port < -1 || builder.port == 0 || builder.port > 65535) {
            builder.port = -1;
            throw new UriException("Port out of bounds: " + builder.port);
        }

        // Normalize known port numbers
        if (builder.config.isSchemeBasedNormalization() && builder.port != -1) {
            if (builder.port == SchemeParams.forName(builder.scheme).defaultPort) {
                builder.port = -1;
            }
        }
    }

    int parsePath(UriBuilder builder, String uri, int offset) {
        int end = indexOf(uri, offset, '?', '#');
        if (end == -1) {
            end = uri.length();
        }

        builder.path = validatePath(builder, uri, offset, end);
        if (builder.path == null) {
            throw new UriException("Illegal path: " + uri);
        }

        if (builder.config.isSchemeBasedNormalization() && builder.authority != null && builder.path.isEmpty()) {
            builder.path = "/";
        }

        if (!builder.path.isEmpty() && builder.path.charAt(0) == '/') {
            builder.isAbsPath = true;
            if (builder.config.isPathSegmentNormalization()) {
                builder.path = builder.resolver().removeDotSegments(builder.path);
            }
        } else {
            builder.isAbsPath = false;
        }

        return end;
    }

    String validatePath(UriBuilder builder, String uri, int soffset, int eoffset) {
        return validateAndNormalize(builder, uri, soffset, eoffset, allowedInPath);
    }

    int parseQuery(UriBuilder builder, String uri, int offset) {
        if (uri.length() > offset && uri.charAt(offset) == '?') {
            int end = indexOf(uri, offset, '#');
            if (end == -1) {
                end = uri.length();
            }

            builder.query = validateQuery(builder, uri, offset + 1, end);
            if (builder.query == null) {
                throw new UriException("Illegal query: " + uri);
            }
            return end;
        } else {
            builder.query = null;
            return offset;
        }
    }

    String validateQuery(UriBuilder builder, String uri, int soffset, int eoffset) {
        return validateAndNormalize(builder, uri, soffset, eoffset, allowedInQuery);
    }

    int parseFragment(UriBuilder builder, String uri, int offset) {
        if (uri.length() > offset && uri.charAt(offset) == '#') {
            builder.fragment = validateFragment(builder, uri, offset + 1, -1);
            if (builder.fragment == null) {
                throw new UriException("Illegal fragment: " + uri);
            }
        } else {
            builder.fragment = null;
        }
        return uri.length();
    }

    String validateFragment(UriBuilder builder, String uri, int soffset, int eoffset) {
        return validateAndNormalize(builder, uri, soffset, eoffset, allowedInFragment);
    }

    /**
     * Validate the URI characters within a specific component. The component must be performed
     * after escape encoding. Or it doesn't include escaped characters.
     * <p>
     * It's not that much strict, generous. The strict validation might be performed before being
     * called this method.
     * <p>
     * @param component the characters sequence within the component
     * @param from the starting offset of the given component
     * @param to the ending offset (exclusive) of the given component if -1, it means the length of the
     * component
     * @param generous those characters that are allowed within a component
     * @return if true, it's the correct URI character sequence
     */
    String validateAndNormalize(UriBuilder builder, String component, int from, int to, BitSet generous) {
        // validate each component by generous characters
        if (to == -1) {
            to = component.length();
        }

        StringBuilder result = new StringBuilder(component.substring(from, to));
        boolean illegalChar = false;

        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) == '%' && builder.config.isPercentEncodingNormalization()
                    && result.length() > i + 2
                    && HEX.get(result.charAt(i + 1))
                    && HEX.get(result.charAt(i + 2))) {

                String hexString = result.substring(i + 1, i + 3);
                int val = Integer.parseInt(hexString, RADIX);

//                if (builder.percentEncodingNormalization && UNRESERVED.get(val)) {
                if (generous.get(val) && val != '%') {
                    // decode unnecessarry encoded char
                    result.setCharAt(i, (char) val);
                    result.delete(i + 1, i + 3);
                } else if (builder.config.isCaseNormalization()) {
                    // convert percent encoded to upper case
                    result.replace(i + 1, i + 3, hexString.toUpperCase());
                }
            }

            if (!generous.get(result.charAt(i))) {
                if (builder.config.isEncodeIllegalCharacters()) {
                    illegalChar = true;
                } else {
                    return null;
                }
            }
        }

        if (illegalChar) {
            return encode(result.toString(), generous, builder.charset);
        }

        return result.toString();
    }

    String validate(final String component, final int from, int to, final BitSet generous) {
        // validate each component by generous characters
        if (to == -1) {
            to = component.length();
        }
        for (int i = from; i < to; i++) {
            if (!generous.get(component.charAt(i))) {
                return null;
            }
        }
        return component.substring(from, to);
    }

    final int indexOf(final String src, int fromIndex, final int... ch) {
        final int max = src.length();
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= max) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }

        final char[] value = src.toCharArray();
        for (int i = fromIndex; i < max; i++) {
            for (int j = 0; j < ch.length; j++) {
                if (value[i] == ch[j]) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Encodes an array of bytes into an array of URL safe 7-bit characters. Unsafe characters are escaped.
     *
     * @param urlsafe
     *            bitset of characters deemed URL safe
     * @param bytes
     *            array of bytes to convert to URL safe characters
     * @return array of bytes containing URL safe characters
     */
    final byte[] encode(final BitSet urlsafe, final byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (final byte c : bytes) {
            int b = c;
            if (b < 0) {
                b = 256 + b;
            }
            if (urlsafe.get(b)) {
                if (b == ' ') {
                    b = '+';
                }
                buffer.write(b);
            } else {
                buffer.write('%');
                final char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX));
                final char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
                buffer.write(hex1);
                buffer.write(hex2);
            }
        }
        return buffer.toByteArray();
    }

    /**
     * Encodes a string into its URL safe form using the specified string charset. Unsafe characters are escaped.
     *
     * @param str
     *            string to convert to a URL safe form
     * @param charset
     *            the charset for str
     * @return URL safe string
     */
    final String encode(final String str, final BitSet urlsafe, final Charset charset) {
        if (str == null) {
            return null;
        }
        return new String(encode(urlsafe, str.getBytes(charset)), StandardCharsets.US_ASCII);
    }
}