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
 *
 */
public final class UriFormat {

    final boolean surtEncoding;

    final boolean ignoreScheme;

    final boolean ignoreAuthority;

    final boolean ignoreUserInfo;

    final boolean ignoreHost;

    final boolean ignorePort;

    final boolean ignorePath;

    final boolean ignoreQuery;

    final boolean ignoreFragment;

    final boolean decodeHost;

    final boolean decodePath;

    public static final class Builder {

        private boolean surtEncoding;

        private boolean ignoreScheme;

        private boolean ignoreAuthority;

        private boolean ignoreUserInfo;

        private boolean ignoreHost;

        private boolean ignorePort;

        private boolean ignorePath;

        private boolean ignoreQuery;

        private boolean ignoreFragment;

        private boolean decodeHost;

        private boolean decodePath;

        private Builder() {
        }

        public Builder surtEncoding(final boolean value) {
            this.surtEncoding = value;
            return this;
        }

        public Builder ignoreScheme(final boolean value) {
            this.ignoreScheme = value;
            return this;
        }

        public Builder ignoreAuthority(final boolean value) {
            this.ignoreAuthority = value;
            return this;
        }

        public Builder ignoreUserInfo(final boolean value) {
            this.ignoreUserInfo = value;
            return this;
        }

        public Builder ignoreHost(final boolean value) {
            this.ignoreHost = value;
            return this;
        }

        public Builder ignorePort(final boolean value) {
            this.ignorePort = value;
            return this;
        }

        public Builder ignorePath(final boolean value) {
            this.ignorePath = value;
            return this;
        }

        public Builder ignoreQuery(final boolean value) {
            this.ignoreQuery = value;
            return this;
        }

        public Builder ignoreFragment(final boolean value) {
            this.ignoreFragment = value;
            return this;
        }

        public UriFormat build() {
            return new UriFormat(this);
        }

        public Builder decodeHost(final boolean value) {
            this.decodeHost = value;
            return this;
        }

        public Builder decodePath(final boolean value) {
            this.decodePath = value;
            return this;
        }
    }

    public static UriFormat.Builder builder() {
        return new UriFormat.Builder();
    }

    public static UriFormat.Builder builder(UriFormat format) {
        Builder formatBuilder = new UriFormat.Builder();
        formatBuilder.surtEncoding = format.surtEncoding;
        formatBuilder.ignoreScheme = format.ignoreScheme;
        formatBuilder.ignoreAuthority = format.ignoreAuthority;
        formatBuilder.ignoreUserInfo = format.ignoreUserInfo;
        formatBuilder.ignoreHost = format.ignoreHost;
        formatBuilder.ignorePort = format.ignorePort;
        formatBuilder.ignorePath = format.ignorePath;
        formatBuilder.ignoreQuery = format.ignoreQuery;
        formatBuilder.ignoreFragment = format.ignoreFragment;
        formatBuilder.decodeHost = format.decodeHost;
        formatBuilder.decodePath = format.decodePath;

        return formatBuilder;
    }

    private UriFormat(final Builder formatBuilder) {
        this.surtEncoding = formatBuilder.surtEncoding;
        this.ignoreScheme = formatBuilder.ignoreScheme;
        this.ignoreAuthority = formatBuilder.ignoreAuthority;
        this.ignoreUserInfo = formatBuilder.ignoreUserInfo;
        this.ignoreHost = formatBuilder.ignoreHost;
        this.ignorePort = formatBuilder.ignorePort;
        this.ignorePath = formatBuilder.ignorePath;
        this.ignoreQuery = formatBuilder.ignoreQuery;
        this.ignoreFragment = formatBuilder.ignoreFragment;
        this.decodeHost = formatBuilder.decodeHost;
        this.decodePath = formatBuilder.decodePath;
    }

}
