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

import org.netpreserve.commons.uri.parser.Parser;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

import org.netpreserve.commons.uri.normalization.SchemeBasedNormalizer;

/**
 * Configuration for a UriBuilder.
 * <p>
 * This class is immutable and thread safe. All methods setting a value returns a fresh copy.
 */
public final class UriBuilderConfig {

    /**
     * Type of case formatting for Hex values.
     */
    public enum HexCase {

        /**
         * Keep the current case for Hex values.
         */
        KEEP(UnaryOperator.identity(), IntUnaryOperator.identity()),
        /**
         * Use upper case for Hex values.
         */
        UPPER(v -> v.toUpperCase(), v -> Character.toUpperCase(v)),
        /**
         * Use lower case for Hex values.
         */
        LOWER(v -> v.toLowerCase(), v -> Character.toLowerCase(v));

        private final UnaryOperator<String> convStringFunc;

        private final IntUnaryOperator convCharFunc;

        /**
         * Constructor for Enum.
         * <p>
         * @param convStringFunc a function for reformatting a string.
         * @param convCharFunc a function for reformatting a code point.
         */
        HexCase(UnaryOperator<String> convStringFunc, IntUnaryOperator convCharFunc) {
            this.convStringFunc = convStringFunc;
            this.convCharFunc = convCharFunc;
        }

        /**
         * Convert a string to the case represented by this HexCase constant.
         * <p>
         * Note that this method does no Hex parsing. It is up to the caller to ensure that the string contains only hex
         * values.
         * <p>
         * @param string the hex values to convert
         * @return the converted string
         */
        public String convert(String string) {
            return convStringFunc.apply(string);
        }

        /**
         * Convert a code point to the case represented by this HexCase constant.
         * <p>
         * @param codepoint the code point to convert
         * @return the converted code point
         */
        public int convert(int codepoint) {
            return convCharFunc.applyAsInt(codepoint);
        }

    }

    private int maxUriLength = Integer.MAX_VALUE;

    private Parser parser = UriConfigs.DEFAULT_PARSER;

    private ReferenceResolver referenceResolver = UriConfigs.DEFAULT_REFERENCE_RESOLVER;

    private List<PreParseNormalizer> preParseNormalizers = Collections.emptyList();

    private List<InParseNormalizer> inParseNormalizers = Collections.emptyList();

    private List<PostParseNormalizer> postParseNormalizers = Collections.emptyList();

    private Charset charset = StandardCharsets.UTF_8;

    private boolean requireAbsoluteUri = false;

    private boolean strictReferenceResolution = true;

    private boolean caseNormalization = true;

    private boolean percentEncodingNormalization = true;

    private HexCase percentEncodingCase = HexCase.KEEP;

    private boolean normalizeIpv4 = true;

    private boolean normalizeIpv6 = true;

    private HexCase ipv6Case = HexCase.KEEP;

    private boolean useIpv6Base85Encoding = false;

    private boolean pathSegmentNormalization = true;

    private boolean schemeBasedNormalization = false;

    private boolean encodeIllegalCharacters = false;

    private boolean punycodeUnknownScheme = false;

    private UriFormat defaultFormat = UriConfigs.DEFAULT_FORMAT;

    /**
     * Get the maximum allowed length for a URI.
     * <p>
     * The default value is {@link Integer#MAX_VALUE}
     * <p>
     * @return the maximum length allowed
     */
    public int getMaxUriLength() {
        return maxUriLength;
    }

    /**
     * Get the {@link Parser} to use.
     * <p>
     * The default is to use {@link UriConfigs#DEFAULT_PARSER}
     * <p>
     * @return the configured parser
     */
    public Parser getParser() {
        return parser;
    }

    /**
     * Get the {@link ReferenceResolver} to be used for resolving URIs against a base URI.
     * <p>
     * Default value is {@link UriConfigs#DEFAULT_REFERENCE_RESOLVER}
     * <p>
     * @return the configured reference resolver
     */
    public ReferenceResolver getReferenceResolver() {
        return referenceResolver;
    }

    /**
     * Get character set to use when parisng URI.
     * <p>
     * @return the configured character set
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Return true if parsing should fail if URI is not absolute.
     * <p>
     * A URI is considered absolute if it has a scheme.
     * <p>
     * @return true for requiring absolute URI
     */
    public boolean isRequireAbsoluteUri() {
        return requireAbsoluteUri;
    }

    /**
     * Returns true if strict reference resolution should be used.
     * <p>
     * The algorithm for reference resolution is described in
     * <a href="https://tools.ietf.org/html/rfc3986#section-5.2.2">RFC 3986 section 5.2.2</a>. For a strict parser, if
     * the reference URI is absolute (i.e. has a scheme) then the target URI should be equal to the reference URI. If
     * the reference URI is not absolute, then a more complex merging algorithm is used. A non strict parser may ignore
     * a scheme in the reference URI if it is identical to the base URI's scheme and as a result use the complex merging
     * even for reference URIs which are absolute.
     * <p>
     * @return true if strict reference resolution should be used
     */
    public boolean isStrictReferenceResolution() {
        return strictReferenceResolution;
    }

    /**
     * Returns true if the URI's case should be normalized.
     * <p>
     * Converts scheme and host to lower case.
     * <p>
     * @return true if the URI's case should be normalized
     */
    public boolean isCaseNormalization() {
        return caseNormalization;
    }

    /**
     * Returns true if percent encoded values should be normalized.
     * <p>
     * For percent encoded characters, unnecessary encoded characters will be decoded. Character case might be
     * normalized dependent on the Percent Encoding Case setting.
     * <p>
     * @return true if percent encoded values should be normalized
     * @see #getPercentEncodingCase()
     */
    public boolean isPercentEncodingNormalization() {
        return percentEncodingNormalization;
    }

    /**
     * Get the policy for character case of percent encoded characters.
     * <p>
     * This value is only relevant if Percent Encoding Normalization is {@code true} or Encode Illegal Characters is
     * {@code true}.
     * <p>
     * Can be one of:
     * <ul>
     * <li>{@link HexCase#UPPER} - upper case hex characters.
     * <li>{@link HexCase#LOWER} - lower case hex characters.
     * <li>{@link HexCase#KEEP} - leave hex characters untouched. When new characters are encoded, upper case will be
     * used.
     * </ul>
     * <p>
     * @return the policy for character case of percent encoded characters
     * @see #isPercentEncodingNormalization()
     * @see #isEncodeIllegalCharacters()
     */
    public HexCase getPercentEncodingCase() {
        return percentEncodingCase;
    }

    /**
     * Returns true if IPv4 addresses should be normalized.
     * <p>
     * If this method returns true, addresses with decimal, hexadecimal and octal numbers are accepted and converted
     * into decimal. If this method returns false, only addresses consisting of decimal numbers will be recognized as
     * IPv4 addresses.
     * <p>
     * @return true if IPv4 addresses should be normalized
     */
    public boolean isNormalizeIpv4() {
        return normalizeIpv4;
    }

    /**
     * Returns true if IPv6 references should be normalized.
     * <p>
     * Normalizing of an IPv6 reference is done in two steps. First the reference is parsed into a number and then it is
     * serialized in normal form.
     * <p>
     * The parser accepts shortened references (i.e. references with consecutive zeroes compacted to '{@code ::}'),
     * embedded IPv4 address and Base85 encoded references (as described in
     * <a href="https://tools.ietf.org/html/rfc1924">RFC 1924</a>).
     * <p>
     * The serialization is dependent on two extra parameters. If {@link #isUseIpv6Base85Encoding()} returns true, the
     * reference is encoded as Base85. Otherwise the reference is encoded using the standard encoding as described in
     * <a href="https://tools.ietf.org/html/rfc1884#section-2.2">RFC 1884 section 2.2, alt. 2</a>. I.e. IPv4 addresses
     * are turned into their two groups of 16 bits representation and the longest sequence of zeroes is compressed. The
     * casing of the hexadecimal characters are dependent on the parameter {@link #getIpv6Case()}.
     * <p>
     * Examples:
     * <ul>
     * <li>{@code 1080:0:0:0:8:800:200C:417a} -> {@code 1080::8:800:200C:417A}
     * <li>{@code 0:0:0:0:0:0:13.1.68.3} -> {@code ::D01:4403}
     * <li>{@code ::8:0:0:0:0:417a} -> {@code 0:0:8::417A}
     * </ul>
     * <p>
     * @return true if IPv6 references should be normalized
     * @see #getIpv6Case()
     * @see #isUseIpv6Base85Encoding()
     */
    public boolean isNormalizeIpv6() {
        return normalizeIpv6;
    }

    /**
     * Get the policy for character case of hex values in IPv6 references.
     * <p>
     * Can be one of:
     * <ul>
     * <li>{@link HexCase#UPPER} - upper case hex characters.
     * <li>{@link HexCase#LOWER} - lower case hex characters.
     * <li>{@link HexCase#KEEP} - leave hex characters untouched. When IPv6 references are normalized, upper case will
     * be used.
     * </ul>
     * <p>
     * @return the policy for character case of hex values in IPv6 references
     */
    public HexCase getIpv6Case() {
        return ipv6Case;
    }

    /**
     * Returns true if Base85 encoding should be used when normalizing IPv6 references.
     * <p>
     * The Base85 encoding described in <a href="https://tools.ietf.org/html/rfc1924">RFC 1924</a> is a more compact
     * representation of IPv6 addresses, which permits encoding in a mere 20 bytes. It doesn't specify an Internet
     * standard, but might be useful for encoding in space constricted environments.
     * <p>
     * This setting is relevant only if {@link #isNormalizeIpv6()} returns true.
     * <p>
     * @return true if Base85 encoding should be used
     * @see #isNormalizeIpv6()
     */
    public boolean isUseIpv6Base85Encoding() {
        return useIpv6Base85Encoding;
    }

    /**
     * Returns true if the URI's path segments should be normalized.
     * <p>
     * @return true if the URI's path segments should be normalized
     */
    public boolean isPathSegmentNormalization() {
        return pathSegmentNormalization;
    }

    /**
     * Returns true if the URI should be normalized with scheme specific rules.
     * <p>
     * When Scheme Based Normalization is enabled, the default parser will do the following:
     * <ul>
     * <li>Puny code international domain names for the schemes known to support it.
     * <li>Remove the port number if it is the default port number for the scheme.
     * <li>If the URI uses the generic syntax for authority and has an empty path, the path will be set to {@code '/'}.
     * </ul>
     * In addition this setting will enable execution of configured Normalizers implementing the
     * {@link SchemeBasedNormalizer} interface.
     * <p>
     * @return true if the URI should be normalized with scheme specific rules
     * @see #getPreParseNormalizers()
     * @see #getInParseNormalizers()
     * @see #getPostParseNormalizers()
     * @see SchemeBasedNormalizer
     */
    public boolean isSchemeBasedNormalization() {
        return schemeBasedNormalization;
    }

    /**
     * Returns true if illegal characters in the URI should be percent encoded.
     * <p>
     * @return true if illegal characters in the URI should be encoded
     */
    public boolean isEncodeIllegalCharacters() {
        return encodeIllegalCharacters;
    }

    /**
     * Returns true if International Domain Names should be puny coded for all schemes.
     * <p>
     * By default schemes known to support puny coding will be encoded if {@link #isSchemeBasedNormalization()} returns
     * true. If this method returns true, all International Domain Names will be encoded regardless of scheme and
     * {@link #isSchemeBasedNormalization()} setting.
     * <p>
     * @return true if IDNs should be encoded for all schemes
     * @see #isSchemeBasedNormalization()
     */
    public boolean isPunycodeUnknownScheme() {
        return punycodeUnknownScheme;
    }

    /**
     * Get the list of configured PreParseNormalizers.
     * <p>
     * @return the list of configured PreParseNormalizers
     */
    public List<PreParseNormalizer> getPreParseNormalizers() {
        return preParseNormalizers;
    }

    /**
     * Get the list of configured InParseNormalizers.
     * <p>
     * @return the list of configured InParseNormalizers
     */
    public List<InParseNormalizer> getInParseNormalizers() {
        return inParseNormalizers;
    }

    /**
     * Get the list of configured PostParseNormalizers.
     * <p>
     * @return the list of configured PostParseNormalizers
     */
    public List<PostParseNormalizer> getPostParseNormalizers() {
        return postParseNormalizers;
    }

    /**
     * Get the default format to serialize Uri as string.
     * <p>
     * @return the default format to serialize Uri as string
     */
    public UriFormat getDefaultFormat() {
        return defaultFormat;
    }

    /**
     * Set the maximum allowed length for a URI.
     * <p>
     * The default value is {@link Integer#MAX_VALUE}
     * <p>
     * @param value the max URI length
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig maxUriLength(final int value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.maxUriLength = value;
        return copy;
    }

    /**
     * Set the {@link Parser} to use.
     * <p>
     * The default is to use {@link UriConfigs#DEFAULT_PARSER}
     * <p>
     * @param value the parser to use
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig parser(final Parser value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.parser = value;
        return copy;
    }

    /**
     * Set the {@link ReferenceResolver} to be used for resolving URIs against a base URI.
     * <p>
     * Default value is {@link UriConfigs#DEFAULT_REFERENCE_RESOLVER}
     * <p>
     * @param value the ReferenceResolver to use
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig referenceResolver(final ReferenceResolver value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.referenceResolver = value;
        return copy;
    }

    /**
     * Set character set to use when parisng URI.
     * <p>
     * @param value the URI's character set
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig charset(final Charset value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.charset = value;
        return copy;
    }

    /**
     * Set the default format to use when serializing Uri to string.
     * <p>
     * @param value the default format to use when serializing Uri to string
     * @return a new immutable UriBuilderConfig
     * @see UriFormat
     */
    public UriBuilderConfig defaultFormat(final UriFormat value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.defaultFormat = value;
        return copy;
    }

    /**
     * Set if parsing should fail if URI is not absolute.
     * <p>
     * A URI is considered absolute if it has a scheme.
     * <p>
     * @param value true if absolute URI is required
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig requireAbsoluteUri(final boolean value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.requireAbsoluteUri = value;
        return copy;
    }

    /**
     * Set if strict reference resolution should be used.
     * <p>
     * The algorithm for reference resolution is described in
     * <a href="https://tools.ietf.org/html/rfc3986#section-5.2.2">RFC 3986 section 5.2.2</a>. For a strict parser, if
     * the reference URI is absolute (i.e. has a scheme) then the target URI should be equal to the reference URI. If
     * the reference URI is not absolute, then a more complex merging algorithm is used. A non strict parser may ignore
     * a scheme in the reference URI if it is identical to the base URI's scheme and as a result use the complex merging
     * even for reference URIs which are absolute. Web browsers are usually not strict.
     * <p>
     * @param value true if strict reference resolution should be used
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig strictReferenceResolution(final boolean value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.strictReferenceResolution = value;
        return copy;
    }

    /**
     * Set if the URI's case should be normalized.
     * <p>
     * Converts scheme and hostname to lower case.
     * <p>
     * @param value set to true for case normalization
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig caseNormalization(final boolean value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.caseNormalization = value;
        return copy;
    }

    /**
     * Set if percent encoded values should be normalized.
     * <p>
     * For percent encoded characters, unnecessary encoded characters will be decoded. Character case might be
     * normalized dependent on the Percent Encoding Case setting.
     * <p>
     * @param value set to true for percent encoding normalization
     * @return a new immutable UriBuilderConfig
     * @see #percentEncodingCase(org.netpreserve.commons.uri.UriBuilderConfig.HexCase)
     */
    public UriBuilderConfig percentEncodingNormalization(final boolean value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.percentEncodingNormalization = value;
        return copy;
    }

    /**
     * Set the policy for character case of percent encoded characters.
     * <p>
     * This value is only relevant if Percent Encoding Normalization is {@code true} or Encode Illegal Characters is
     * {@code true}.
     * <p>
     * Can be one of:
     * <ul>
     * <li>{@link HexCase#UPPER} - upper case hex characters.
     * <li>{@link HexCase#LOWER} - lower case hex characters.
     * <li>{@link HexCase#KEEP} - leave hex characters untouched. When new characters are encoded, upper case will be
     * used.
     * </ul>
     * <p>
     * @param value the character case policy
     * @return a new immutable UriBuilderConfig
     * @see #percentEncodingNormalization(boolean)
     * @see #encodeIllegalCharacters(boolean)
     */
    public UriBuilderConfig percentEncodingCase(final HexCase value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.percentEncodingCase = value;
        return copy;
    }

    /**
     * Set if IPv4 addresses should be normalized.
     * <p>
     * If set to true, addresses with decimal, hexadecimal and octal numbers are accepted and converted into decimal. If
     * set to false, only addresses consisting of decimal numbers will be recognized as IPv4 addresses.
     * <p>
     * @param value true if IPv4 addresses should be normalized
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig normalizeIpv4(final boolean value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.normalizeIpv4 = value;
        return copy;
    }

    /**
     * Set if IPv6 references should be normalized.
     * <p>
     * Normalizing of an IPv6 reference is done in two steps. First the reference is parsed into a number and then it is
     * serialized in normal form.
     * <p>
     * The parser accepts shortened references (i.e. references with consecutive zeroes compacted to '{@code ::}'),
     * embedded IPv4 address and Base85 encoded references (as described in
     * <a href="https://tools.ietf.org/html/rfc1924">RFC 1924</a>).
     * <p>
     * The serialization is dependent on two extra parameters. If {@link #useIpv6Base85Encoding(boolean)} returns true,
     * the reference is encoded as Base85. Otherwise the reference is encoded using the standard encoding as described
     * in
     * <a href="https://tools.ietf.org/html/rfc1884#section-2.2">RFC 1884 section 2.2, alt. 2</a>. I.e. IPv4 addresses
     * are turned into their two groups of 16 bits representation and the longest sequence of zeroes is compressed. The
     * casing of the hexadecimal characters are dependent on the parameter
     * {@link #ipv6Case(org.netpreserve.commons.uri.UriBuilderConfig.HexCase)}.
     * <p>
     * Examples:
     * <ul>
     * <li>{@code 1080:0:0:0:8:800:200C:417a} -> {@code 1080::8:800:200C:417A}
     * <li>{@code 0:0:0:0:0:0:13.1.68.3} -> {@code ::D01:4403}
     * <li>{@code ::8:0:0:0:0:417a} -> {@code 0:0:8::417A}
     * </ul>
     * <p>
     * @param value true if IPv6 references should be normalized
     * @return a new immutable UriBuilderConfig
     * @see #ipv6Case(org.netpreserve.commons.uri.UriBuilderConfig.HexCase)
     * @see #useIpv6Base85Encoding(boolean)
     */
    public UriBuilderConfig normalizeIpv6(final boolean value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.normalizeIpv6 = value;
        return copy;
    }

    /**
     * Set the policy for character casing of hex values in IPv6 references.
     * <p>
     * Can be one of:
     * <ul>
     * <li>{@link HexCase#UPPER} - upper case hex characters.
     * <li>{@link HexCase#LOWER} - lower case hex characters.
     * <li>{@link HexCase#KEEP} - leave hex characters untouched. When IPv6 references are normalized, upper case will
     * be used.
     * </ul>
     * <p>
     * @param value the character case policy
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig ipv6Case(final HexCase value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.ipv6Case = value;
        return copy;
    }

    /**
     * Set if Base85 encoding should be used when normalizing IPv6 references.
     * <p>
     * The Base85 encoding described in <a href="https://tools.ietf.org/html/rfc1924">RFC 1924</a> is a more compact
     * representation of IPv6 addresses, which permits encoding in a mere 20 bytes. It doesn't specify an Internet
     * standard, but might be useful for encoding in space constricted environments.
     * <p>
     * This setting is relevant only if {@link #normalizeIpv6(boolean)} is set to true.
     * <p>
     * @param value true if Base85 encoding of IPv6 references should be used
     * @return a new immutable UriBuilderConfig
     * @see #normalizeIpv6(boolean)
     */
    public UriBuilderConfig useIpv6Base85Encoding(final boolean value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.useIpv6Base85Encoding = value;
        return copy;
    }

    /**
     * Set if the URI's path segments should be normalized.
     * <p>
     * @param value true if the URI's path segments should be normalized
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig pathSegmentNormalization(final boolean value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.pathSegmentNormalization = value;
        return copy;
    }

    /**
     * Set if the URI should be normalized with scheme specific rules.
     * <p>
     * When Scheme Based Normalization is enabled, the default parser will do the following:
     * <ul>
     * <li>Puny code international domain names for the schemes known to support it.
     * <li>Remove the port number if it is the default port number for the scheme.
     * <li>If the URI uses the generic syntax for authority and has an empty path, the path will be set to {@code '/'}.
     * </ul>
     * In addition this setting will enable execution of configured Normalizers implementing the
     * {@link SchemeBasedNormalizer} interface.
     * <p>
     * @param value true if the URI should be normalized with scheme specific rules
     * @return a new immutable UriBuilderConfig
     * @see #addNormalizer(org.netpreserve.commons.uri.Normalizer)
     * @see SchemeBasedNormalizer
     */
    public UriBuilderConfig schemeBasedNormalization(final boolean value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.schemeBasedNormalization = value;
        return copy;
    }

    /**
     * Set if illegal characters in the URI should be percent encoded.
     * <p>
     * @param value true if illegal characters in the URI should be encoded
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig encodeIllegalCharacters(final boolean value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.encodeIllegalCharacters = value;
        return copy;
    }

    /**
     * Set if International Domain Names should be puny coded for all schemes.
     * <p>
     * By default schemes known to support puny coding will be encoded if {@link #schemeBasedNormalization(boolean)} is
     * set to true. If this config parameter is set to true, all International Domain Names will be encoded regardless
     * of scheme and {@link #schemeBasedNormalization(boolean)} setting.
     * <p>
     * @param value true if IDNs should be encoded for all schemes
     * @return a new immutable UriBuilderConfig
     * @see #schemeBasedNormalization(boolean)
     */
    public UriBuilderConfig punycodeUnknownScheme(final boolean value) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        copy.punycodeUnknownScheme = value;
        return copy;
    }

    /**
     * Add a normalizer.
     * <p>
     * @param normalizer the normalizer to add
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig addNormalizer(Normalizer normalizer) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        if (normalizer instanceof PreParseNormalizer) {
            List<PreParseNormalizer> listCopy = new ArrayList<>(copy.preParseNormalizers);
            listCopy.add((PreParseNormalizer) normalizer);
            copy.preParseNormalizers = Collections.unmodifiableList(listCopy);
        }
        if (normalizer instanceof InParseNormalizer) {
            List<InParseNormalizer> listCopy = new ArrayList<>(copy.inParseNormalizers);
            listCopy.add((InParseNormalizer) normalizer);
            copy.inParseNormalizers = Collections.unmodifiableList(listCopy);
        }
        if (normalizer instanceof PostParseNormalizer) {
            List<PostParseNormalizer> listCopy = new ArrayList<>(copy.postParseNormalizers);
            listCopy.add((PostParseNormalizer) normalizer);
            copy.postParseNormalizers = Collections.unmodifiableList(listCopy);
        }
        return copy;
    }

    /**
     * Remove normalizers of a certain type.
     * <p>
     * @param normalizer the type to remove
     * @return a new immutable UriBuilderConfig
     */
    public UriBuilderConfig removeNormalizersOfType(Class<? extends Normalizer> normalizer) {
        UriBuilderConfig copy = new UriBuilderConfig(this);
        if (PreParseNormalizer.class.isAssignableFrom(normalizer)) {
            List<PreParseNormalizer> listCopy = new ArrayList<>(copy.preParseNormalizers);
            listCopy.removeIf((PreParseNormalizer t) -> normalizer.isInstance(t));
            copy.preParseNormalizers = Collections.unmodifiableList(listCopy);
        }
        if (InParseNormalizer.class.isAssignableFrom(normalizer)) {
            List<InParseNormalizer> listCopy = new ArrayList<>(copy.inParseNormalizers);
            listCopy.removeIf((InParseNormalizer t) -> normalizer.isInstance(t));
            copy.inParseNormalizers = Collections.unmodifiableList(listCopy);
        }
        if (PostParseNormalizer.class.isAssignableFrom(normalizer)) {
            List<PostParseNormalizer> listCopy = new ArrayList<>(copy.postParseNormalizers);
            listCopy.removeIf((PostParseNormalizer t) -> normalizer.isInstance(t));
            copy.postParseNormalizers = Collections.unmodifiableList(listCopy);
        }
        return copy;
    }

    /**
     * Construct a new UriBuilderConfig with all parameters set to default values.
     */
    public UriBuilderConfig() {
    }

    /**
     * Constructs a new UriBuilderConfig by copying all fields from another UriBuilderConfig.
     * <p>
     * This constructor is private to ensure immutability.
     * <p>
     * @param src the UriBuilderConfig to copy from
     */
    private UriBuilderConfig(UriBuilderConfig src) {
        this.maxUriLength = src.getMaxUriLength();
        this.parser = src.getParser();
        this.referenceResolver = src.getReferenceResolver();
        this.charset = src.getCharset();
        this.requireAbsoluteUri = src.isRequireAbsoluteUri();
        this.strictReferenceResolution = src.isStrictReferenceResolution();
        this.caseNormalization = src.isCaseNormalization();
        this.percentEncodingNormalization = src.isPercentEncodingNormalization();
        this.percentEncodingCase = src.getPercentEncodingCase();
        this.normalizeIpv4 = src.normalizeIpv4;
        this.normalizeIpv6 = src.normalizeIpv6;
        this.ipv6Case = src.getIpv6Case();
        this.useIpv6Base85Encoding = src.useIpv6Base85Encoding;
        this.pathSegmentNormalization = src.isPathSegmentNormalization();
        this.schemeBasedNormalization = src.isSchemeBasedNormalization();
        this.encodeIllegalCharacters = src.isEncodeIllegalCharacters();
        this.punycodeUnknownScheme = src.isPunycodeUnknownScheme();
        this.preParseNormalizers = src.preParseNormalizers;
        this.inParseNormalizers = src.inParseNormalizers;
        this.postParseNormalizers = src.postParseNormalizers;
        this.defaultFormat = src.getDefaultFormat();
    }

    /**
     * Create a new immutable Uri object using this UriBuilderConfig.
     * <p>
     * This is a shortcut for: {@code new UriBuilder(this).uri(uri).build()}
     * <p>
     * @param uri the uri string
     * @return the parsed and normalized URI
     */
    public Uri buildUri(String uri) {
        return new UriBuilder(this).uri(uri).build();
    }

    /**
     * Create a new UriBuilder using this UriBuilderConfig.
     * <p>
     * This is a shortcut for: {@code new UriBuilder(this)}
     * <p>
     * @return the new UriBuilder
     */
    public UriBuilder builder() {
        return new UriBuilder(this);
    }

    /**
     * Create a new UriBuilder using this UriBuilderConfig, initialized with a URI.
     * <p>
     * This is a shortcut for: {@code new UriBuilder(this).uri(uri)}
     * <p>
     * @param uri the uri string
     * @return a new UriBuilder initilized with the given URI
     */
    public UriBuilder builder(String uri) {
        return new UriBuilder(this).uri(uri);
    }

    /**
     * Create a new UriBuilder using this UriBuilderConfig, initialized with a URI.
     * <p>
     * This is a shortcut for: {@code new UriBuilder(this).uri(uri)}
     * <p>
     * @param uri the uri string
     * @return a new UriBuilder initilized with the given URI
     */
    public UriBuilder builder(Uri uri) {
        return new UriBuilder(this).uri(uri);
    }

    /**
     * Resolve a URI against a base.
     * <p>
     * Both the base and the reference are parsed using this UriBuilderConfig before reference resolution.
     * <p>
     * @param base the base uri
     * @param reference the reference uri
     * @return the uri result from resolving the reference against the base
     * @throws UriException is thrown if reference resolution failed.
     */
    public Uri resolve(String base, String reference) {
        return new UriBuilder(this).uri(base).resolve(reference).build();
    }

    /**
     * Resolve a URI against a base.
     * <p>
     * The base is parsed using this UriBuilderConfig before reference resolution.
     * <p>
     * @param base the base uri
     * @param reference the reference uri
     * @return the uri result from resolving the reference against the base
     * @throws UriException is thrown if reference resolution failed.
     */
    public Uri resolve(String base, Uri reference) {
        return new UriBuilder(this).uri(base).resolve(reference).build();
    }

    /**
     * Resolve a URI against a base.
     * <p>
     * The base is parsed using this UriBuilderConfig before reference resolution.
     * <p>
     * @param base the base uri
     * @param reference the reference uri
     * @return the uri result from resolving the reference against the base
     * @throws UriException is thrown if reference resolution failed.
     */
    public Uri resolve(String base, UriBuilder reference) {
        return new UriBuilder(this).uri(base).resolve(reference).build();
    }

    /**
     * Resolve a URI against a base.
     * <p>
     * The reference is parsed using this UriBuilderConfig before reference resolution.
     * <p>
     * @param base the base uri
     * @param reference the reference uri
     * @return the uri result from resolving the reference against the base
     * @throws UriException is thrown if reference resolution failed.
     */
    public Uri resolve(Uri base, String reference) {
        return new UriBuilder(this).uri(base).resolve(reference).build();
    }

    /**
     * Resolve a URI against a base.
     * <p>
     * @param base the base uri
     * @param reference the reference uri
     * @return the uri result from resolving the reference against the base
     * @throws UriException is thrown if reference resolution failed.
     */
    public Uri resolve(Uri base, Uri reference) {
        return new UriBuilder(this).uri(base).resolve(reference).build();
    }

    /**
     * Resolve a URI against a base.
     * <p>
     * @param base the base uri
     * @param reference the reference uri
     * @return the uri result from resolving the reference against the base
     * @throws UriException is thrown if reference resolution failed.
     */
    public Uri resolve(Uri base, UriBuilder reference) {
        return new UriBuilder(this).uri(base).resolve(reference).build();
    }

}
