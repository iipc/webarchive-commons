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

import org.netpreserve.commons.uri.parser.SurtEncoder;
import org.netpreserve.commons.uri.parser.StrictSurtEncoder;

/**
 * A format describing how to convert a Uri into a String.
 */
public final class UriFormat {

    private static final SurtEncoder DEFAULT_ENCODER = new StrictSurtEncoder();

    final boolean surtEncoding;

    final boolean ignoreScheme;

    final boolean ignoreAuthority;

    final boolean ignoreUser;

    final boolean ignorePassword;

    final boolean ignoreHost;

    final boolean ignorePort;

    final boolean ignorePath;

    final boolean ignoreQuery;

    final boolean ignoreFragment;

    final boolean decodeHost;

    final boolean decodePath;

    final SurtEncoder surtEncoder;

    public String getUser(Uri uri) {
        if (!ignoreUser) {
            return uri.getUser();
        }
        return null;
    }

    public String getPassword(Uri uri) {
        if (!ignorePassword) {
            return uri.getPassword();
        }
        return null;
    }

    public String getHost(Uri uri) {
        if (!ignoreHost) {
            if (decodeHost) {
                return uri.getDecodedHost();
            } else {
                return uri.getHost();
            }
        }
        return null;
    }

    public Integer getPort(Uri uri) {
        if (!ignorePort && uri.getPort() != Uri.DEFAULT_PORT_MARKER) {
            return uri.getPort();
        }
        return null;
    }

    /**
     * Builder for UriFormat.
     */
    public static final class Builder {

        private boolean surtEncoding;

        private boolean ignoreScheme;

        private boolean ignoreAuthority;

        private boolean ignoreUser;

        private boolean ignorePassword;

        private boolean ignoreHost;

        private boolean ignorePort;

        private boolean ignorePath;

        private boolean ignoreQuery;

        private boolean ignoreFragment;

        private boolean decodeHost;

        private boolean decodePath;

        private SurtEncoder surtEncoder;

        /**
         * This class should not be constructed directly. Use UriFormat.builder() instead.
         */
        private Builder() {
        }

        /**
         * Use SURT encoding.
         * <p>
         * @param value true if SURT encoding should be used.
         * @return this builder for method call chaining
         */
        public Builder surtEncoding(final boolean value) {
            this.surtEncoding = value;
            return this;
        }

        /**
         * Set the SurtEncoder implementation used when surtEncoding is set.
         * <p>
         * If no encoder is set, {@link StrictSurtEncoder} is used.
         * <p>
         * @param value the SurtEncoder to use.
         * @return this builder for method call chaining
         */
        public Builder surtEncoder(final SurtEncoder value) {
            this.surtEncoder = value;
            return this;
        }

        /**
         * Ignore Scheme in output.
         * <p>
         * @param value true if Scheme should be ignored.
         * @return this builder for method call chaining
         */
        public Builder ignoreScheme(final boolean value) {
            this.ignoreScheme = value;
            return this;
        }

        /**
         * Ignore Authority in output.
         * <p>
         * @param value true if Authority should be ignored.
         * @return this builder for method call chaining
         */
        public Builder ignoreAuthority(final boolean value) {
            this.ignoreAuthority = value;
            return this;
        }

        /**
         * Ignore User in output.
         * <p>
         * @param value true if User should be ignored.
         * @return this builder for method call chaining
         */
        public Builder ignoreUser(final boolean value) {
            this.ignoreUser = value;
            return this;
        }

        /**
         * Ignore Password in output.
         * <p>
         * @param value true if Password should be ignored.
         * @return this builder for method call chaining
         */
        public Builder ignorePassword(final boolean value) {
            this.ignorePassword = value;
            return this;
        }

        /**
         * Ignore Host in output.
         * <p>
         * @param value true if Host should be ignored.
         * @return this builder for method call chaining
         */
        public Builder ignoreHost(final boolean value) {
            this.ignoreHost = value;
            return this;
        }

        /**
         * Ignore Port in output.
         * <p>
         * @param value true if Port should be ignored.
         * @return this builder for method call chaining
         */
        public Builder ignorePort(final boolean value) {
            this.ignorePort = value;
            return this;
        }

        /**
         * Ignore Path in output.
         * <p>
         * @param value true if Path should be ignored.
         * @return this builder for method call chaining
         */
        public Builder ignorePath(final boolean value) {
            this.ignorePath = value;
            return this;
        }

        /**
         * Ignore Query in output.
         * <p>
         * @param value true if Query should be ignored.
         * @return this builder for method call chaining
         */
        public Builder ignoreQuery(final boolean value) {
            this.ignoreQuery = value;
            return this;
        }

        /**
         * Ignore Fragment in output.
         * <p>
         * @param value true if Fragment should be ignored.
         * @return this builder for method call chaining
         */
        public Builder ignoreFragment(final boolean value) {
            this.ignoreFragment = value;
            return this;
        }

        /**
         * Decode the Host in output.
         * <p>
         * If the host is an IDN, it could be puny encoded or contain international characters in UTF-8. The default is
         * to puny encode. Setting this value to true decodes the host into UTF-8.
         * <p>
         * @param value true if Host should be decoded to UTF-8.
         * @return this builder for method call chaining
         */
        public Builder decodeHost(final boolean value) {
            this.decodeHost = value;
            return this;
        }

        /**
         * Decode any percent encoded characters.
         * <p>
         * @param value true if the path should be decoded.
         * @return this builder for method call chaining
         */
        public Builder decodePath(final boolean value) {
            this.decodePath = value;
            return this;
        }

        /**
         * Create a UriFormat from this builder.
         * <p>
         * @return the newly created UriFormat.
         */
        public UriFormat build() {
            return new UriFormat(this);
        }

    }

    /**
     * Create a new builder for UriFormat.
     * <p>
     * @return the new builder
     */
    public static UriFormat.Builder builder() {
        return new UriFormat.Builder();
    }

    /**
     * Create a new builder for UriFormat initialized with this UriFormat.
     * <p>
     * @return the new builder
     */
    public UriFormat.Builder toBuilder() {
        Builder formatBuilder = new UriFormat.Builder();
        formatBuilder.surtEncoding = surtEncoding;
        formatBuilder.ignoreScheme = ignoreScheme;
        formatBuilder.ignoreAuthority = ignoreAuthority;
        formatBuilder.ignoreUser = ignoreUser;
        formatBuilder.ignorePassword = ignorePassword;
        formatBuilder.ignoreHost = ignoreHost;
        formatBuilder.ignorePort = ignorePort;
        formatBuilder.ignorePath = ignorePath;
        formatBuilder.ignoreQuery = ignoreQuery;
        formatBuilder.ignoreFragment = ignoreFragment;
        formatBuilder.decodeHost = decodeHost;
        formatBuilder.decodePath = decodePath;
        formatBuilder.surtEncoder = surtEncoder;

        return formatBuilder;
    }

    /**
     * Create a UriFormat from a builder.
     * <p>
     * This constructor is private since formats should be created by a builder.
     * <p>
     * @param formatBuilder the builder
     */
    private UriFormat(final Builder formatBuilder) {
        this.surtEncoding = formatBuilder.surtEncoding;
        this.ignoreScheme = formatBuilder.ignoreScheme;
        this.ignoreAuthority = formatBuilder.ignoreAuthority;
        this.ignoreUser = formatBuilder.ignoreUser;
        this.ignorePassword = formatBuilder.ignorePassword;
        this.ignoreHost = formatBuilder.ignoreHost;
        this.ignorePort = formatBuilder.ignorePort;
        this.ignorePath = formatBuilder.ignorePath;
        this.ignoreQuery = formatBuilder.ignoreQuery;
        this.ignoreFragment = formatBuilder.ignoreFragment;
        this.decodeHost = formatBuilder.decodeHost;
        this.decodePath = formatBuilder.decodePath;
        if (formatBuilder.surtEncoder != null) {
            this.surtEncoder = formatBuilder.surtEncoder;
        } else {
            this.surtEncoder = DEFAULT_ENCODER;
        }
    }

}
