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

/**
 *
 */
public class UriBuilder {

    public static final String SCHEME_HTTP = "http";

    public static final String SCHEME_HTTPS = "https";

    public static final String SCHEME_FTP = "ftp";

    public static final String SCHEME_FTPS = "ftps";

    public static final String SCHEME_DNS = "dns";

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
        for (PreParseNormalizer preNormalizer : config.getPreNormalizers()) {
            uriString = preNormalizer.normalize(uriString);
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

    public UriBuilder authority(final String value) {
        config.getParser().parseAuthority(this, value);
        return this;
    }

    public UriBuilder userinfo(final String value) {
        this.userinfo = value;
        config.getParser().constructAuthority(this);
        return this;
    }

    public UriBuilder host(final String value) {
        this.host = value;
        config.getParser().constructAuthority(this);
        return this;
    }

    public UriBuilder port(final int value) {
        this.port = value;
        config.getParser().constructAuthority(this);
        return this;
    }

    public UriBuilder path(final String value) {
        config.getParser().parsePath(this, value, 0);
        return this;
    }

    public UriBuilder query(final String value) {
        query = config.getParser().validateQuery(this, value, 0, -1);
        if (query == null) {
            throw new UriException("Illegal query: " + value);
        }
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

    public String fragment() {
        return fragment;
    }

    public Uri build() {
        for (PostParseNormalizer normalizer : config.getPostNormalizers()) {
            normalizer.normalize(this);
        }

        Uri uri = new Uri(this);

        if (uri.toString().length() > config.getMaxUrlLength()) {
            throw new UriException("Created (escaped) uuri > too long");
        }

        return uri;
    }

    public String trimHostnameDots(String src) {
        int len = src.length();
        int st = 0;
        int i = 0;
        int removed = 0;
        char[] value = src.toCharArray();

        while ((st < len) && (value[st] == '.')) {
            st++;
        }

        i = st;
        while (++i < len) {
            if (removed > 0) {
                value[i - removed] = value[i];
            }
            if (value[i] == '.' && value[i - 1 - removed] == '.') {
                removed++;
            }
        }

        while ((st < len) && (value[len - 1] == '.')) {
            len--;
        }

        if (st > 0 || removed > 0 || len < src.length()) {
            return new String(value, st, len - removed);
        } else {
            return src;
        }
    }

    public UriBuilder resolve(UriBuilder relative) throws UriException {
        config.getReferenceResolver().resolve(this, relative);
        return this;
    }

    public UriBuilder resolve(Uri relative) throws UriException {
        config.getReferenceResolver().resolve(this, UriBuilder.builder(config).uri(relative));
        return this;
    }

    public UriBuilder resolve(String relative) throws UriException {
        UriBuilder uri = UriBuilder.builder(
                config.toBuilder().requireAbsoluteUri(false).build())
                .uri(relative);

        config.getReferenceResolver().resolve(this, uri);
        return this;
    }

}
