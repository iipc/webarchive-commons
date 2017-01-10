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
import java.util.Objects;

import org.netpreserve.commons.uri.normalization.report.NormalizationConfigReport;

import static org.netpreserve.commons.uri.Uri.DEFAULT_PORT_MARKER;

/**
 * Class for build instances of {@link Uri}.
 *
 * Instances of this class are not thread safe.
 */
public final class UriBuilder {

    private final UriBuilderConfig config;

    private String scheme;

    private Scheme schemeType;

    private String user;

    private String password;

    private String host;

    private int port = Uri.DEFAULT_PORT_MARKER;

    private String path;

    private String query;

    transient ParsedQuery parsedQuery;

    private String fragment;

    private boolean isRegName;

    private boolean isIPv4address;

    private boolean isIPv6reference;

    private boolean isAbsPath;

    private Charset charset = StandardCharsets.UTF_8;

    /**
     * Construct a new UriBuilder.
     * <p>
     * @param config the configuration used by this builder.
     */
    public UriBuilder(UriBuilderConfig config) {
        this.config = config;
        this.charset = config.getCharset();
    }

    /**
     * Set the character set used when parsing this URI.
     * <p>
     * Default value is UTF-8.
     * <p>
     * @param charset the character set to be used.
     * @return this builder for command chaining.
     */
    public UriBuilder charset(Charset charset) {
        this.charset = Objects.requireNonNull(charset, "Character set cannot be null");
        return this;
    }

    /**
     * Get the configuration used by this builder.
     * <p>
     * @return the configuration
     */
    public UriBuilderConfig config() {
        return config;
    }

    /**
     * Get the parser used by this builder.
     * <p>
     * @return the parser
     */
    public Parser parser() {
        return config.getParser();
    }

    /**
     * Get the reference resolver used by this builder.
     * <p>
     * @return the reference resolver
     */
    public ReferenceResolver resolver() {
        return config.getReferenceResolver();
    }

    /**
     * Set a complete URI.
     * <p>
     * The URI will be parsed and all the fields in this UriBuilder set. If any of the fields should be set explicitly,
     * their relevant setters must be called after this method.
     * <p>
     * @param uriString the URI as a string
     * @return this builder for command chaining
     */
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

    /**
     * Set a complete URI.
     * <p>
     * The UriBuilder will get all its fields from the submitted URI. No parsing will be done, so if the submitted URI
     * was parsed with another configuration than the one configured for this UriBuilder, it might be that some of the
     * field are not conformant to the configuration for this UriBuilder. If any of the fields should be set explicitly,
     * their relevant setters must be called after this method.
     * <p>
     * @param uri the URI as a parsed object
     * @return this builder for command chaining
     */
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

    /**
     * Set the scheme for this builder.
     * <p>
     * @param value the scheme or null if no scheme (URI is not absolute)
     * @return this builder for command chaining
     */
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

    /**
     * Returns true if this builder has an authority.
     * <p>
     * This builder has an authority if one or more of the fields {@link #user()}, {@link #password()}, {@link #host()}
     * or {@link port()} is set.
     * <p>
     * @return true if this builder has an authority
     */
    public boolean isAuthority() {
        return host != null || user != null || password != null || port != DEFAULT_PORT_MARKER;
    }

    /**
     * Clear all fields related to authority.
     * <p>
     * The fields cleared are: {@link #user()}, {@link #password()}, {@link #host()}, {@link #port()}. In addition the
     * following flags are set to false: {@link #isIPv4address()}, {@link #isIPv6reference()}, {@link #isRegName()}
     * <p>
     * @return this builder for command chaining
     */
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

    /**
     * Set the user for this builder.
     * <p>
     * @param value the user
     * @return this builder for command chaining
     */
    public UriBuilder user(final String value) {
        this.user = value;
        return this;
    }

    /**
     * Set the password for this builder.
     * <p>
     * @param value the password
     * @return this builder for command chaining
     */
    public UriBuilder password(final String value) {
        this.password = value;
        return this;
    }

    /**
     * Set the host for this builder.
     * <p>
     * This method should be used with care since it might cause the flags indicating the type of host value to be out
     * of sync with the host field.
     * <p>
     * @param value the host
     * @return this builder for command chaining
     * @see #isRegName()
     * @see #isIPv4address()
     * @see #isIPv6reference()
     */
    public UriBuilder host(final String value) {
        this.host = value;
        return this;
    }

    /**
     * Set the port for this builder.
     * <p>
     * @param value the port
     * @return this builder for command chaining
     */
    public UriBuilder port(final int value) {
        this.port = value;
        return this;
    }

    /**
     * Set the path for this builder.
     * <p>
     * The path is validated and normalized according to this builders configuration.
     * <p>
     * @param value the path
     * @return this builder for command chaining
     */
    public UriBuilder path(final String value) {
        config.getParser().parsePath(this, value);
        return this;
    }

    /**
     * Set the raw path for this builder.
     * <p>
     * The path is set as is with no validation or normalizing.
     * <p>
     * @param value the path
     * @return this builder for command chaining
     */
    public UriBuilder rawPath(final String value) {
        this.path = value;
        return this;
    }

    /**
     * Set the query string for this builder.
     * <p>
     * The query is validated and normalized according to this builders configuration.
     * <p>
     * @param value the query
     * @return this builder for command chaining
     */
    public UriBuilder query(final String value) {
        if (value == null) {
            query = null;
        } else {
            query = config.getParser().validateQuery(config, charset, CharBuffer.wrap(value.toCharArray()));
        }
        parsedQuery = null;
        return this;
    }

    /**
     * Set the raw query string for this builder.
     * <p>
     * The query is set as is with no validation or normalizing.
     * <p>
     * @param value the query
     * @return this builder for command chaining
     */
    public UriBuilder rawQuery(final String value) {
        this.query = value;
        return this;
    }

    /**
     * Set the parsed query for this builder.
     * <p>
     * This method is only relevant for schemas supporting queries of the form: {@code ?var1=val1&var2=val2} which is
     * commonly used, for example by the http scheme.
     * <p>
     * This method is typically used for modifying queries for http(s).
     * <p>
     * <b>Example:</b>
     * <pre>
     * builder.parsedQuery(builder.parsedQuery().remove("jsessionid"));
     * </pre>
     * <p>
     * @param value the parsed query
     * @return this builder for command chaining
     */
    public UriBuilder parsedQuery(final ParsedQuery value) {
        query(value.toString());
        parsedQuery = value;
        return this;
    }

    /**
     * Set the fragment for this builder.
     * <p>
     * The fragment is validated and normalized according to this builders configuration.
     * <p>
     * @param value the fragment
     * @return this builder for command chaining
     */
    public UriBuilder fragment(final String value) {
        fragment = config.getParser().validateFragment(config, charset, CharBuffer.wrap(value.toCharArray()));
        if (fragment == null) {
            throw new UriException("Illegal fragment: " + value);
        }
        return this;
    }

    /**
     * Set the raw fragment for this builder.
     * <p>
     * The fragment is set as is with no validation or normalizing.
     * <p>
     * @param value the fragment
     * @return this builder for command chaining
     */
    public UriBuilder rawFragment(final String value) {
        this.fragment = value;
        return this;
    }

    /**
     * Set the registry name flag.
     * <p>
     * This flag indicates that the host is a registry based name and not an IP address.
     * <p>
     * This method is mainly for use by parsers and should be used with care since it might cause the flag setting to be
     * out of sync with the host field.
     * <p>
     * @return this builder for command chaining
     * @see #host(java.lang.String)
     */
    public UriBuilder setRegNameFlag() {
        this.isIPv6reference = false;
        this.isIPv4address = false;
        this.isRegName = true;
        return this;
    }

    /**
     * Set the IPv4 flag.
     * <p>
     * This flag indicates that the host is a IPv4 address.
     * <p>
     * This method is mainly for use by parsers and should be used with care since it might cause the flag setting to be
     * out of sync with the host field.
     * <p>
     * @return this builder for command chaining
     * @see #host(java.lang.String)
     */
    public UriBuilder setIPv4addressFlag() {
        this.isIPv6reference = false;
        this.isIPv4address = true;
        this.isRegName = false;
        return this;
    }

    /**
     * Set the IPv6 flag.
     * <p>
     * This flag indicates that the host is a IPv6 address.
     * <p>
     * This method is mainly for use by parsers and should be used with care since it might cause the flag setting to be
     * out of sync with the host field.
     * <p>
     * @return this builder for command chaining
     * @see #host(java.lang.String)
     */
    public UriBuilder setIPv6referenceFlag() {
        this.isIPv6reference = true;
        this.isIPv4address = false;
        this.isRegName = false;
        return this;
    }

    /**
     * Helper method for copying host flag settings from another builder to this.
     * <p>
     * This method is mainly for use by reference resolvers and should be used with care since it might cause the flag
     * setting to be out of sync with the host field.
     * <p>
     * @param src the builder to copy the flags from
     * @return this builder for command chaining
     * @see #host(java.lang.String)
     */
    public UriBuilder copyHostFlags(UriBuilder src) {
        this.isIPv6reference = src.isIPv6reference;
        this.isIPv4address = src.isIPv4address;
        this.isRegName = src.isRegName;
        return this;
    }

    /**
     * Set absolute path flag.
     * <p>
     * A URI's path is absolute if it starts with the character {@code '/'}. This method is only relevant for URIs with
     * a scheme which defines a hierarchical path (eg. http, ftp).
     * <p>
     * This method is mainly for use by parsers and should be used with care since it might cause the flag setting to be
     * out of sync with the path field.
     * <p>
     * @param isAbsPath true if this builders path is absolute
     * @return this builder for command chaining
     * @see #path(java.lang.String)
     */
    public UriBuilder isAbsPath(boolean isAbsPath) {
        this.isAbsPath = isAbsPath;
        return this;
    }

    /**
     * Get the character set of this builder.
     * <p>
     * @return the character set
     */
    public Charset charset() {
        return charset;
    }

    /**
     * Get the scheme of this builder.
     * <p>
     * @return the scheme
     */
    public String scheme() {
        return scheme;
    }

    /**
     * Get the scheme type of this builder.
     * <p>
     * The scheme type is defined for some common schemes. If the scheme is not known, {@link Scheme#UNKNOWN} is
     * returned. If the URI is not absolute (i.e. no scheme), {@link Scheme#UNDEFINED} is returned.
     * <p>
     * @return the scheme type
     */
    public Scheme schemeType() {
        if (schemeType == null) {
            schemeType = Scheme.forName(scheme);
        }
        return schemeType;
    }

    /**
     * Get the user of this builder.
     * <p>
     * @return the user
     */
    public String user() {
        return user;
    }

    /**
     * Get the password of this builder.
     * <p>
     * @return the password
     */
    public String password() {
        return password;
    }

    /**
     * Get the host of this builder.
     * <p>
     * @return the host
     */
    public String host() {
        return host;
    }

    /**
     * Get the port of this builder.
     * <p>
     * @return the port
     */
    public int port() {
        return port;
    }

    /**
     * Get the path of this builder.
     * <p>
     * @return the path
     */
    public String path() {
        return path;
    }

    /**
     * Get the query of this builder.
     * <p>
     * @return the query
     */
    public String query() {
        return query;
    }

    /**
     * Get a parsed version of the URI's query component.
     * <p>
     * The query is the part after a question mark. Not all schemes support queries. The RFC 3986 doesn't specify a
     * format for queries for those schemas that supports it. This method only supports queries of the form:
     * {@code ?var1=val1&var2=val2} which is commonly used for example by the http scheme. For schemas not using this
     * form, {@link #getQuery()} can be used to get the query as a string.
     * <p>
     * @return the URI's query. Can be null
     * @see #getQuery()
     */
    public ParsedQuery parsedQuery() {
        if (parsedQuery == null) {
            parsedQuery = new ParsedQuery(query);
        }
        return parsedQuery;
    }

    /**
     * Get the fragment of this builder.
     * <p>
     * @return the fragment
     */
    public String fragment() {
        return fragment;
    }

    /**
     * Returns true if this builder's host is a registry name.
     * <p>
     * This flag indicates that the host is a registry based name and not an IP address.
     * @return true if this builder's host is a registry name
     */
    public boolean isRegName() {
        return isRegName;
    }

    /**
     * Returns true if this builder's host is a IPv4 address.
     * <p>
     * @return true if this builder's host is a IPv4 address
     */
    public boolean isIPv4address() {
        return isIPv4address;
    }

    /**
     * Returns true if this builder's host is a IPv6 reference.
     * <p>
     * @return true if this builder's host is a IPv6 reference
     */
    public boolean isIPv6reference() {
        return isIPv6reference;
    }

    /**
     * Returns true if this builders path is absolute.
     * <p>
     * A URI's path is absolute if it starts with the character {@code '/'}. This method is only relevant for URIs with
     * a scheme which defines a hierarchical path (eg. http, ftp).
     * <p>
     * @return true if this builders path is absolute
     */
    public boolean isAbsPath() {
        return isAbsPath;
    }

    /**
     * Build an immutable Uri object represented by this builder.
     * <p>
     * @return the immutable Uri object.
     * @throws UriException is thrown if any post parse normalizing fails.
     */
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

        if (uri.toString().length() > config.getMaxUriLength()) {
            throw new UriException("Created (escaped) uuri > too long");
        }

        return uri;
    }

    /**
     * Resolve a URI against this builder.
     * <p>
     * @param reference the reference
     * @return this builder changed to the result of resolving the submitted reference against this builder
     * @throws UriException is thrown if reference resolution failed.
     */
    public UriBuilder resolve(UriBuilder reference) throws UriException {
        config.getReferenceResolver().resolve(this, reference);
        return this;
    }

    /**
     * Resolve a URI against this builder.
     * <p>
     * @param reference the reference
     * @return this builder changed to the result of resolving the submitted reference against this builder
     * @throws UriException is thrown if reference resolution failed.
     */
    public UriBuilder resolve(Uri reference) throws UriException {
        resolve(new UriBuilder(config).uri(reference));
        return this;
    }

    /**
     * Resolve a URI against this builder.
     * <p>
     * @param reference the reference
     * @return this builder changed to the result of resolving the submitted reference against this builder
     * @throws UriException is thrown if reference resolution failed.
     */
    public UriBuilder resolve(String reference) throws UriException {
        UriBuilder uri = new UriBuilder(config.requireAbsoluteUri(false)).uri(reference);
        resolve(uri);
        return this;
    }

    /**
     * Get description of all the normalizations configured.
     * <p>
     * @return the list of normalization descriptions
     */
    public NormalizationConfigReport getNormalizationDescriptions() {
        return NormalizationConfigReport.parse(this);
    }

    @Override
    public String toString() {
        return getNormalizationDescriptions().toString();
    }

}
