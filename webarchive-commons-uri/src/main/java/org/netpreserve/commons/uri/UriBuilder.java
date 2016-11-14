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

import java.nio.CharBuffer;

import org.netpreserve.commons.uri.parser.Parser;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.netpreserve.commons.uri.normalization.SchemeBasedNormalizer;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;

import static org.netpreserve.commons.uri.Uri.DEFAULT_PORT_MARKER;

/**
 * Class for build instances of {@link Uri}.
 */
public final class UriBuilder {

    public static final String ESCAPED_SPACE = "%20";

    public static final char NBSP = '\u00A0';

    public final UriBuilderConfig config;

    private String scheme;

    private Scheme schemeType;

    private String user;

    private String password;

    private String host;

    int port = Uri.DEFAULT_PORT_MARKER;

    String path;

    String query;

    ParsedQuery parsedQuery;

    String fragment;

    boolean isRegName;

    boolean isIPv4address;

    boolean isIPv6reference;

    boolean isAbsPath;

    Charset charset = StandardCharsets.UTF_8;

    private UriBuilder(UriBuilderConfig config) {
        this.config = config;
        this.charset = config.getCharset();
    }

    /**
     * Creates a new UriBuilder.
     * <p>
     * The builder is configured with a user submitted {@link UriBuilderConfig}.
     * <p>
     * @param config the configuration used by the returned builder
     * @return the newly created builder.
     */
    public static UriBuilder builder(UriBuilderConfig config) {
        return new UriBuilder(config);
    }

    /**
     * Creates a new UriBuilder.
     * <p>
     * The builder is configured to behave as the UsableUri used in Heritrix.
     * <p>
     * @return the newly created builder.
     */
    public static UriBuilder usableUriBuilder() {
        return builder(Configurations.USABLE_URI);
    }

    /**
     * Creates a new UriBuilder.
     * <p>
     * The builder is configured to only allow URI's which conforms to the RFC 3986. It does little normalizing. The
     * normalizing done is not altering semantics. It normalizes case in places where the URI is case insensitive. Path
     * is normalized for constructs like '{@code foo/../bar/.}'.
     * <p>
     * @return the newly created builder.
     */
    public static UriBuilder strictUriBuilder() {
        return builder(Configurations.STRICT_URI);
    }

    /**
     * Creates a new UriBuilder.
     * <p>
     * The builder is configured to be forgiving. It Normalizes some illegal characters, but otherwise tries to accept
     * the URI as it is as much as possible.
     * <p>
     * @return the newly created builder.
     */
    public static UriBuilder laxUriBuilder() {
        return builder(Configurations.LAX_URI);
    }

    /**
     * Creates a new UriBuilder.
     * <p>
     * The builder is configured with the standard normalizations used in OpenWayback.
     * <p>
     * @return the newly created builder.
     */
    public static UriBuilder canonicalizedUriBuilder() {
        return builder(Configurations.CANONICALIZED_URI);
    }

    /**
     * Create a new builder configured to mimic behavior of major browsers.
     * <p>
     * @return the newly created builder.
     */
    public static UriBuilder mimicBrowserUriBuilder() {
        return builder(Configurations.MIMIC_BROWSER_URI);
    }

    /**
     * Convenience method for creating a Uri.
     * <p>
     * This is a shortcut for {@code UriBuilder.builder(Configurations.MIMIC_BROWSER_URI).uri(uri).build()}
     * <p>
     * @param uri a uri to be parsed
     * @return the parsed and immutable Uri
     */
    public static Uri mimicBrowserUri(String uri) {
        return builder(Configurations.MIMIC_BROWSER_URI).uri(uri).build();
    }

    public UriBuilder charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public Parser parser() {
        return config.getParser();
    }

    public ReferenceResolver resolver() {
        return config.getReferenceResolver();
    }

    public UriBuilder uri(String uriString) {
        Objects.requireNonNull(uriString, "URI cannot be null");
        for (PreParseNormalizer normalizer : config.getPreParseNormalizers()) {
            if (normalizer.validFor(this)) {
                uriString = normalizer.normalize(uriString);
            }
        }

        config.getParser().parseUri(this, uriString, 0);

        return this;
    }

    public UriBuilder uri(Uri uri) {
        this.scheme = uri.scheme;
        this.host = uri.host;
        this.user = uri.user;
        this.password = uri.password;
        this.port = uri.port;
        this.path = uri.path;
        this.query = uri.query;
        this.parsedQuery = uri.parsedQuery;
        this.fragment = uri.fragment;
        this.isRegName = uri.isRegName;
        this.isIPv4address = uri.isIPv4address;
        this.isIPv6reference = uri.isIPv6reference;
        this.isAbsPath = uri.isAbsPath;
        this.charset = uri.charset;

        return this;
    }

    public UriBuilder scheme(String value) {
        schemeType = null;

        if (value == null || value.isEmpty()) {
            scheme = null;
            return this;
        }

        if (config.isCaseNormalization()) {
            scheme = value.toLowerCase();
        } else {
            scheme = value;
        }

        return this;
    }

    public boolean isAuthority() {
        return host != null || user != null || password != null || port != DEFAULT_PORT_MARKER;
    }

    public UriBuilder clearAuthority() {
        host = null;
        user = null;
        password = null;
        port = DEFAULT_PORT_MARKER;
        isIPv4address = false;
        isIPv6reference = false;
        isRegName = false;
        return this;
    }

    public UriBuilder user(final String value) {
        this.user = value;
        return this;
    }

    public UriBuilder password(final String value) {
        this.password = value;
        return this;
    }

    public UriBuilder host(final String value) {
        this.host = value;
        return this;
    }

    public UriBuilder port(final int value) {
        this.port = value;
        return this;
    }

    public UriBuilder path(final String value) {
        config.getParser().parsePath(this, value);
        return this;
    }

    public UriBuilder rawPath(final String value) {
        this.path = value;
        return this;
    }

    public UriBuilder query(final String value) {
        if (value == null) {
            query = null;
        } else {
            query = config.getParser().validateQuery(config, charset, CharBuffer.wrap(value.toCharArray()));
        }
        parsedQuery = null;
        return this;
    }

    public UriBuilder rawQuery(final String value) {
        this.query = value;
        return this;
    }

    public UriBuilder parsedQuery(final ParsedQuery value) {
        query(value.toString());
        parsedQuery = value;
        return this;
    }

    public UriBuilder fragment(final String value) {
        fragment = config.getParser().validateFragment(config, charset, CharBuffer.wrap(value.toCharArray()));
        if (fragment == null) {
            throw new UriException("Illegal fragment: " + value);
        }
        return this;
    }

    public UriBuilder rawFragment(final String value) {
        this.fragment = value;
        return this;
    }

    public UriBuilder setRegNameFlag() {
        this.isIPv6reference = false;
        this.isIPv4address = false;
        this.isRegName = true;
        return this;
    }

    public UriBuilder setIPv4addressFlag() {
        this.isIPv6reference = false;
        this.isIPv4address = true;
        this.isRegName = false;
        return this;
    }

    public UriBuilder setIPv6referenceFlag() {
        this.isIPv6reference = true;
        this.isIPv4address = false;
        this.isRegName = false;
        return this;
    }

    public UriBuilder copyHostFlags(UriBuilder src) {
        this.isIPv6reference = src.isIPv6reference;
        this.isIPv4address = src.isIPv4address;
        this.isRegName = src.isRegName;
        return this;
    }

    public UriBuilder isAbsPath(boolean isAbsPath) {
        this.isAbsPath = isAbsPath;
        return this;
    }

    public Charset charset() {
        return charset;
    }

    public String scheme() {
        return scheme;
    }

    public Scheme schemeType() {
        if (schemeType == null) {
            schemeType = Scheme.forName(scheme);
        }
        return schemeType;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String path() {
        return path;
    }

    public String query() {
        return query;
    }

    public ParsedQuery parsedQuery() {
        if (parsedQuery == null) {
            parsedQuery = new ParsedQuery(query);
        }
        return parsedQuery;
    }

    public String fragment() {
        return fragment;
    }

    public boolean isRegName() {
        return isRegName;
    }

    public boolean isIPv4address() {
        return isIPv4address;
    }

    public boolean isIPv6reference() {
        return isIPv6reference;
    }

    public boolean isAbsPath() {
        return isAbsPath;
    }

    public Uri build() {
        if (scheme == null && config.isRequireAbsoluteUri()) {
            throw new UriException("Uri is not absolute");
        }

        for (PostParseNormalizer normalizer : config.getPostParseNormalizers()) {
            if (normalizer.validFor(this)) {
                normalizer.normalize(this);
            }
        }

        Uri uri = new Uri(this);

        if (uri.toString().length() > config.getMaxUrlLength()) {
            throw new UriException("Created (escaped) uuri > too long");
        }

        return uri;
    }

    public UriBuilder resolve(UriBuilder relative) throws UriException {
        config.getReferenceResolver().resolve(this, relative);
        return this;
    }

    public UriBuilder resolve(Uri relative) throws UriException {
        resolve(UriBuilder.builder(config).uri(relative));
        return this;
    }

    public UriBuilder resolve(String relative) throws UriException {
        UriBuilder uri = UriBuilder.builder(
                config.toBuilder().requireAbsoluteUri(false).build())
                .uri(relative);

        resolve(uri);
        return this;
    }

    /**
     * Get description of all the normalizations configured.
     * <p>
     * @return the list of normalization descriptions
     */
    public List<NormalizationDescription> getNormalizationDescriptions() {
        List<NormalizationDescription> descriptions = new ArrayList<>();

        for (Normalizer normalizer : config.getPreParseNormalizers()) {
            addNormalizationDescription(normalizer, descriptions);
        }

        config.getParser().describeNormalization(descriptions);

        for (Normalizer normalizer : config.getInParseNormalizers()) {
            addNormalizationDescription(normalizer, descriptions);
        }

        for (Normalizer normalizer : config.getPostParseNormalizers()) {
            addNormalizationDescription(normalizer, descriptions);
        }

        return descriptions;
    }

    private void addNormalizationDescription(Normalizer normalizer, List<NormalizationDescription> descriptions) {
        if (normalizer instanceof SchemeBasedNormalizer) {
            if (config.isSchemeBasedNormalization()) {
                normalizer.describeNormalization(descriptions);
            }
        } else {
            normalizer.describeNormalization(descriptions);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("UriBuilder configured with these normalizations:");
        for (NormalizationDescription desc : getNormalizationDescriptions()) {
            sb.append("\n").append(desc.toString("  "));
        }
        return sb.toString();
    }

}
