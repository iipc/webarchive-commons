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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.netpreserve.commons.uri.normalization.SchemeBasedNormalizer;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;

/**
 * Class for build instances of {@link Uri}.
 */
public final class UriBuilder {

    public static final String ESCAPED_SPACE = "%20";

    public static final char NBSP = '\u00A0';

    public final UriBuilderConfig config;

    String scheme;

    String authority;

    String userinfo;

    String host;

    int port = -1;

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

    public static UriBuilder builder(UriBuilderConfig config) {
        return new UriBuilder(config);
    }

    public static UriBuilder usableUriBuilder() {
        return builder(Configurations.USABLE_URI);
    }

    public static UriBuilder strictUriBuilder() {
        return builder(Configurations.STRICT_URI);
    }

    public static UriBuilder canonicalizedUriBuilder() {
        return builder(Configurations.CANONICALIZED_URI);
    }

    public UriBuilder charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public Rfc3986Parser parser() {
        return config.getParser();
    }

    public Rfc3986ReferenceResolver resolver() {
        return config.getReferenceResolver();
    }

    public UriBuilder uri(String uriString) {
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
        this.authority = uri.authority;
        this.host = uri.host;
        this.userinfo = uri.userinfo;
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
        if (value == null || value.isEmpty()) {
            scheme = null;
            return this;
        }

        scheme = value.toLowerCase();
        return this;
    }

    public UriBuilder userinfo(final String value) {
        this.userinfo = value;
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
        config.getParser().parsePath(new Rfc3986Parser.ParserState(this, value, 0));
        return this;
    }

    public UriBuilder query(final String value) {
        query = config.getParser().validateQuery(this, value, 0, -1);
        if (query == null) {
            throw new UriException("Illegal query: " + value);
        }
        parsedQuery = null;
        return this;
    }

    public UriBuilder parsedQuery(final ParsedQuery value) {
        query(parsedQuery.toString());
        parsedQuery = value;
        return this;
    }

    public UriBuilder fragment(final String value) {
        fragment = config.getParser().validateFragment(this, value, 0, -1);
        if (fragment == null) {
            throw new UriException("Illegal fragment: " + value);
        }
        return this;
    }

    public String scheme() {
        return scheme;
    }

    public String authority() {
        return authority;
    }

    public String userinfo() {
        return userinfo;
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

    public Uri build() {
        if (scheme == null && config.isRequireAbsoluteUri()) {
            throw new UriException("Uri is not absolute");
        }

        if (authority != null) {
            parser().decomposeAuthority(this, authority);
        } else {
            parser().constructAuthority(this);
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
        if (authority != null) {
            parser().decomposeAuthority(this, authority);
        }
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
