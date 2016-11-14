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

import java.io.ByteArrayOutputStream;
import java.net.IDN;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.List;

import org.netpreserve.commons.uri.InParseNormalizer;
import org.netpreserve.commons.uri.Scheme;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.uri.UriException;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;

/**
 *
 */
public class Rfc3986Parser implements Parser {

    /**
     * Radix used in encoding and decoding.
     */
    static final int RADIX = 16;

    /**
     * BitSet for DIGIT.
     * <p>
     * <blockquote><pre>
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
     * <p>
     * <blockquote><pre>
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
     * <p>
     * <blockquote><pre>
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
     * Data characters that are allowed in a URI but do not have a RESERVED purpose are called UNRESERVED.
     * <p>
     * <blockquote><pre>
     * UNRESERVED    = alphanum | mark
     * </pre></blockquote><p>
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
     * <p>
     * <blockquote><pre>
     * RESERVED      = gen-delims / sub-delims
     * </pre></blockquote><p>
     */
    protected static final BitSet RESERVED = new BitSet(256);

    // Static initializer for RESERVED
    static {
        RESERVED.or(GEN_DELIMS);
        RESERVED.or(SUB_DELIMS);
    }

    /**
     * BitSet for PCHAR.
     * <p>
     * <blockquote><pre>
     * PCHAR         = UNRESERVED | escaped |
     * ":" | "@" | "&amp;" | "=" | "+" | "$" | ","
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
     * <p>
     * <blockquote><pre>
     * SCHEME        = ALPHA *( ALPHA | DIGIT | "+" | "-" | "." )
     * </pre></blockquote><p>
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

    // Static initializer for REGISTRY_NAME
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

    /**
     * BitSet for END_OF_QUERY.
     */
    protected static final BitSet END_OF_QUERY = new BitSet(256);

    // Static initializer for END_OF_QUERY
    static {
        END_OF_QUERY.set('#');
    }

    /**
     * BitSet for END_OF_PATH.
     */
    protected static final BitSet END_OF_PATH = new BitSet(256);

    // Static initializer for END_OF_PATH
    static {
        END_OF_PATH.or(END_OF_QUERY);
        END_OF_PATH.set('?');
    }

    /**
     * BitSet for END_OF_AUTHORITY.
     */
    protected static final BitSet END_OF_AUTHORITY = new BitSet(256);

    // Static initializer for END_OF_AUTHORITY
    static {
        END_OF_AUTHORITY.or(END_OF_PATH);
        END_OF_AUTHORITY.set('/');
    }

    protected BitSet allowedInScheme;

    protected BitSet allowedInUserInfo;

    protected BitSet allowedInRegistryName;

    protected BitSet allowedInPath;

    protected BitSet allowedInQuery;

    protected BitSet allowedInFragment;

    protected BitSet endOfAuthority;

    protected BitSet endOfPath;

    protected BitSet endOfQuery;

    public Rfc3986Parser() {
        allowedInScheme = SCHEME;
        allowedInUserInfo = PCHAR;
        allowedInRegistryName = REGISTRY_NAME;
        allowedInPath = PATH;
        allowedInQuery = QUERY;
        allowedInFragment = FRAGMENT;
        endOfAuthority = END_OF_AUTHORITY;
        endOfPath = END_OF_PATH;
        endOfQuery = END_OF_QUERY;
    }

    public void parseUri(UriBuilder builder, String uri, int offset) {
        ParserState parserState = new ParserState(builder, uri, offset);
        parseScheme(parserState);
        parseAuthority(parserState);
        parsePath(parserState);
        parseQuery(parserState);
        parseFragment(parserState);
    }

    void parseScheme(ParserState parserState) {
        int colonIdx = indexOf(parserState.uri, ':');
        if (colonIdx > 0) {
            parserState.uri.limit(colonIdx);
            parserState.builder.scheme(validateScheme(parserState.uri));
            if (parserState.builder.scheme() != null) {
                if (parserState.moveToNext()) {
                    parserState.incrementOffset(1);
                }
                return;
            } else {
                parserState.uri.limit(parserState.uri.capacity());
            }
        }

        parserState.builder.scheme(null);
    }

    String validateScheme(CharBuffer uri) {
        if (ALPHA.get(uri.get(uri.position()))) {
            return validate(uri, allowedInScheme);
        }
        return null;
    }

    void checkHasAuthority(ParserState parserState) {
        for (InParseNormalizer normalizer : parserState.config.getInParseNormalizers()) {
            if (normalizer.validFor(parserState.builder)) {
                normalizer.preParseAuthority(parserState);
            }
        }

        if (!parserState.hasAuthority && parserState.uriHasAtLeastMoreChararcters(2)
                && parserState.uri.charAt(0) == '/'
                && parserState.uri.charAt(1) == '/') {
            parserState.hasAuthority = true;
            parserState.incrementOffset(2);
        }
    }

    void parseAuthority(ParserState parserState) {
        checkHasAuthority(parserState);

        if (parserState.hasAuthority) {
            int end = indexOf(parserState.uri, endOfAuthority);
            if (end == -1) {
                end = parserState.uri.capacity();
            }
            parserState.uri.limit(end);
            parserState.builder.clearAuthority();
            decomposeAuthority(parserState);

            parserState.moveToNext();
        }
    }

    @Override
    public void decomposeAuthority(UriBuilder builder, String authority) {
        decomposeAuthority(new ParserState(builder, authority));
    }

    public void decomposeAuthority(ParserState parserState) {
        UriBuilder builder = parserState.builder;
        CharBuffer uri = parserState.uri;
        int endOfAuthority = uri.limit();

        int next = lastIndexOf(uri, '@');
        if (next == uri.position()) {
            next = -1;
            parserState.incrementOffset(1);
        }

        if (next != -1) {
            int passIdx = indexOf(uri, ':');
            if (passIdx == -1 || passIdx >= next) {
                uri.limit(next);
                builder.user(validateAndNormalize(builder.config, builder.charset(), uri, allowedInUserInfo));
            } else {
                uri.limit(passIdx);
                builder.user(validateAndNormalize(builder.config, builder.charset(), uri, allowedInUserInfo));
                if (next > passIdx) {
                    uri.limit(next);
                    uri.position(passIdx + 1);
                    builder.password(
                            validateAndNormalize(builder.config, builder.charset(), uri, allowedInUserInfo));
                }
            }
            uri.limit(endOfAuthority);
            uri.position(next + 1);
        }

        // Check for IPv6 reference
        if (uri.remaining() > 3 && uri.charAt(0) == '[') {
            parserState.incrementOffset(1);
            int endOfIpv6 = indexOf(uri, ']');
            if (endOfIpv6 == -1) {
                throw new UriException("Error parsing IPv6reference from " + parserState.uriToString());
            }
            uri.limit(endOfIpv6);
            parseIpv6(builder, uri.toString());
            uri.limit(endOfAuthority);
            uri.position(endOfIpv6 + 1);
        }

        // Eventual IPv6 reference is now consumed. We can safely search for port separator ':'
        int endOfHost = indexOf(uri, ':');
        if (endOfHost == -1) {
            endOfHost = endOfAuthority;
        }
        uri.limit(endOfHost);

        if (!builder.isIPv6reference()) {
            String host = uri.toString();

            // Normalize unicode characters
            host = java.text.Normalizer.normalize(host, java.text.Normalizer.Form.NFKC);
            parseIpv4(builder, host);

            if (!builder.isIPv4address()) {
                parseRegistryName(parserState, host);
            }

            if (builder.schemeType() != Scheme.FILE && builder.schemeType().isSpecial()
                    && (builder.host() == null || builder.host().isEmpty())) {
                throw new UriException("Host cannot be empty for " + builder.scheme() + " scheme");
            }
        }

        if (endOfAuthority > endOfHost) {
            uri.limit(endOfAuthority);
            uri.position(endOfHost + 1);
            parsePort(builder, uri);
        }
    }

    void parseIpv6(UriBuilder builder, String host) {
        builder.host(IpUtil.checkAndNormalizeIpv6(host));

        if (builder.host() != null) {
            builder.setIPv6referenceFlag();
        }
    }

    void parseIpv4(UriBuilder builder, String ipv4Address) {
        builder.host(IpUtil.checkIpv4(ipv4Address));
        if (builder.host() != null) {
            builder.setIPv4addressFlag();
        }
    }

    void parseRegistryName(ParserState parserState, String registryName) {
        UriBuilder builder = parserState.builder;

        if (builder.config.isCaseNormalization()) {
            registryName = registryName.toLowerCase();
        }

        for (InParseNormalizer normalizer : parserState.config.getInParseNormalizers()) {
            if (normalizer.validFor(parserState.builder)) {
                registryName = normalizer.preParseHost(parserState, registryName);
            }
        }

        if (builder.config.isSchemeBasedNormalization()) {
            if (builder.config.isPunycodeUnknownScheme() || builder.schemeType().isPunycodedHost()) {
                // apply IDN-punycoding, as necessary
                try {
                    builder.host(IDN.toASCII(registryName, IDN.USE_STD3_ASCII_RULES));
                } catch (IllegalArgumentException e) {

                    // check if domain name has ACE prefix, leading/trailing dash, or underscore
                    // we still wish to tolerate those;
                    builder.host(validate(registryName, allowedInRegistryName));
                    if (builder.host() == null) {
                        throw new UriException("Invalid hostname: " + builder.host());
                    } else {
                        builder.host(validateAndNormalize(
                                builder.config, builder.charset(), registryName, allowedInRegistryName));
                    }
                }
            } else {
                builder.host(validateAndNormalize(builder.config, builder.charset(), registryName, allowedInRegistryName));
            }
        } else {
            builder.host(validateAndNormalize(builder.config, builder.charset(), registryName, allowedInRegistryName));
        }

        // Set flag
        if (!builder.host().isEmpty()) {
            builder.setRegNameFlag();
        }
    }

    void parsePort(UriBuilder builder, CharBuffer authority) {
        if (authority.hasRemaining()) {
            try {
                builder.port(Integer.parseInt(authority.toString()));

                // Check valid port number
                if (builder.port() <= Uri.DEFAULT_PORT_MARKER || builder.port() == 0 || builder.port() > 65535) {
                    throw new UriException("Port out of bounds: " + builder.port());
                }
            } catch (NumberFormatException error) {
                throw new UriException("invalid port number in: " + authority);
            }
        }

        // Normalize known port numbers
        if (builder.config.isSchemeBasedNormalization() && builder.port() != Uri.DEFAULT_PORT_MARKER) {
            if (builder.port() == builder.schemeType().defaultPort()) {
                builder.port(Uri.DEFAULT_PORT_MARKER);
            }
        }
    }

    public void parsePath(final UriBuilder builder, final String value) {
        parsePath(new ParserState(builder, value));
    }

    void parsePath(ParserState parserState) {
        int end = indexOf(parserState.uri, endOfPath);
        if (end == -1) {
            end = parserState.uri.capacity();
        }
        parserState.uri.limit(end);

        String path = validatePath(parserState);
        if (path == null) {
            throw new UriException("Illegal path: " + parserState.uri);
        }
        if (parserState.config.isSchemeBasedNormalization() && parserState.builder.isAuthority()
                && path.isEmpty()) {
            path = "/";
        }

        if (!path.isEmpty() && path.charAt(0) == '/') {
            parserState.builder.isAbsPath(true);
            if (parserState.config.isPathSegmentNormalization()) {
                path = parserState.builder.resolver().removeDotSegments(path);
            }
        } else {
            parserState.builder.isAbsPath(false);
        }

        parserState.builder.rawPath(path);
        parserState.moveToNext();
    }

    String validatePath(ParserState parserState) {
        return validateAndNormalize(parserState.config, parserState.charset, parserState.uri, allowedInPath);
    }

    void parseQuery(ParserState parserState) {
        if (parserState.uri.hasRemaining() && parserState.uri.charAt(0) == '?') {
            parserState.incrementOffset(1);
            int end = indexOf(parserState.uri, endOfQuery);
            if (end == -1) {
                end = parserState.uri.capacity();
            }
            parserState.uri.limit(end);

            parserState.builder.rawQuery(validateQuery(parserState.config, parserState.charset, parserState.uri));
            if (parserState.builder.query() == null) {
                throw new UriException("Illegal query: " + parserState.uriToString());
            }
            parserState.moveToNext();
        } else {
            parserState.builder.rawQuery(null);
        }
    }

    public String validateQuery(UriBuilderConfig config, Charset charset, CharBuffer uri) {
        return validateAndNormalize(config, charset, uri, allowedInQuery);
    }

    void parseFragment(ParserState parserState) {
        if (parserState.uri.hasRemaining() && parserState.uri.charAt(0) == '#') {
            parserState.incrementOffset(1);
            parserState.builder.rawFragment(
                    validateFragment(parserState.config, parserState.charset, parserState.uri));
            if (parserState.builder.fragment() == null) {
                throw new UriException("Illegal fragment: " + parserState.uri);
            }
        } else {
            parserState.builder.rawFragment(null);
        }

        parserState.moveToNext();
    }

    public String validateFragment(UriBuilderConfig config, Charset charset, CharBuffer uri) {
        return validateAndNormalize(config, charset, uri, allowedInFragment);
    }

    /**
     * Validate the URI characters within a specific component. The component must be performed after escape encoding.
     * Or it doesn't include escaped characters.
     * <p>
     * It's not that much strict, generous. The strict validation might be performed before being called this method.
     * <p>
     * @param config the UriBuilderConfig to use for determining what normalization to do
     * @param charset the character set of the URI
     * @param component the characters sequence within the component
     * @param from the starting offset of the given component
     * @param to the ending offset (exclusive) of the given component if -1, it means the length of the component
     * @param generous those characters that are allowed within a component
     * @return if true, it's the correct URI character sequence
     */
    String validateAndNormalize(UriBuilderConfig config, Charset charset, CharBuffer component, BitSet generous) {
        if (component == null) {
            return null;
        }

        StringBuilder result = new StringBuilder(component);
        boolean illegalChar = false;

        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) == '%' && config.isPercentEncodingNormalization()
                    && result.length() > i + 2
                    && HEX.get(result.charAt(i + 1))
                    && HEX.get(result.charAt(i + 2))) {

                String hexString = result.substring(i + 1, i + 3);
                int val = Integer.parseInt(hexString, RADIX);

                if (generous.get(val) && val != '%') {
                    // decode unnecessarry encoded char
                    result.setCharAt(i, (char) val);
                    result.delete(i + 1, i + 3);
                } else if (config.isCaseNormalization()) {
                    // convert percent encoded to upper case
                    result.replace(i + 1, i + 3, hexString.toUpperCase());
                }
            }

            if (!generous.get(result.charAt(i))) {
                if (config.isEncodeIllegalCharacters()) {
                    illegalChar = true;
                } else {
                    return null;
                }
            }
        }

        if (illegalChar) {
            return new String(encode(generous, result.toString().getBytes(charset)), StandardCharsets.US_ASCII);
        }

        return result.toString();
    }

    String validateAndNormalize(UriBuilderConfig config, Charset charset, String component, BitSet generous) {
        return validateAndNormalize(config, charset, CharBuffer.wrap(component.toCharArray()), generous);
    }

    String validate(final String component, final BitSet generous) {
        return validate(CharBuffer.wrap(component.toCharArray()), generous);
    }

    String validate(final CharBuffer component, final BitSet generous) {
        final int from = component.position();
        final int to = component.limit();
        final char[] value = component.array();

        for (int i = from; i < to; i++) {
            if (!generous.get(value[i])) {
                return null;
            }
        }
        return component.toString();
    }

    final int indexOf(final CharBuffer src, final int... ch) {
        final int from = src.position();
        final int to = src.limit();
        final char[] value = src.array();

        for (int i = from; i < to; i++) {
            for (int j = 0; j < ch.length; j++) {
                if (value[i] == ch[j]) {
                    return i;
                }
            }
        }
        return -1;
    }

    final int indexOf(final CharBuffer src, final BitSet ch) {
        final int from = src.position();
        final int to = src.limit();
        final char[] value = src.array();

        for (int i = from; i < to; i++) {
            if (ch.get(value[i])) {
                return i;
            }
        }
        return -1;
    }

    final int lastIndexOf(final CharBuffer src, final int... ch) {
        final int from = src.position();
        final int to = src.limit();
        final char[] value = src.array();

        for (int i = to - 1; i >= from; i--) {
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
     * <p>
     * @param urlsafe bitset of characters deemed URL safe
     * @param bytes array of bytes to convert to URL safe characters
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
     * Add a human readable description of the normalization done by this class.
     * <p>
     * @param descriptions A list of descriptions which this class can add its own descriptions to.
     */
    @Override
    public void describeNormalization(List<NormalizationDescription> descriptions) {
        descriptions.add(NormalizationDescription.builder(Rfc3986Parser.class)
                .name("Missing")
                .description("The description of normalization done by the parser is missing")
                .build());
    }

}
