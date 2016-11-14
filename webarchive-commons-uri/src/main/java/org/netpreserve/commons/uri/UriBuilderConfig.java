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
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public final class UriBuilderConfig {

    private final int maxUrlLength;

    private final Parser parser;

    private final ReferenceResolver referenceResolver;

    private final PreParseNormalizer[] preParseNormalizers;

    private final InParseNormalizer[] inParseNormalizers;

    private final PostParseNormalizer[] postParseNormalizers;

    private final Charset charset;

    private final boolean requireAbsoluteUri;

    private final boolean strictReferenceResolution;

    private final boolean caseNormalization;

    private final boolean percentEncodingNormalization;

    private final boolean pathSegmentNormalization;

    private final boolean schemeBasedNormalization;

    private final boolean encodeIllegalCharacters;

    private final boolean punycodeUnknownScheme;

    private final UriFormat defaultFormat;

    /**
     * Create a new builder for UriBuilderConfig initialized with sensible defaults for parsing valid Uri's.
     * <p>
     * @return the builder
     */
    public static ConfigBuilder newBuilder() {
        return new ConfigBuilder();
    }

    /**
     * Create a new builder for UriBuilderConfig initialized with this UriBuilderConfig.
     * <p>
     * @return the builder
     */
    public ConfigBuilder toBuilder() {
        return new ConfigBuilder(this);
    }

    private UriBuilderConfig(ConfigBuilder config) {
        this.maxUrlLength = config.getMaxUrlLength();
        this.parser = config.getParser();
        this.referenceResolver = config.getReferenceResolver();
        this.charset = config.getCharset();
        this.requireAbsoluteUri = config.isRequireAbsoluteUri();
        this.strictReferenceResolution = config.isStrictReferenceResolution();
        this.caseNormalization = config.isCaseNormalization();
        this.percentEncodingNormalization = config.isPercentEncodingNormalization();
        this.pathSegmentNormalization = config.isPathSegmentNormalization();
        this.schemeBasedNormalization = config.isSchemeBasedNormalization();
        this.encodeIllegalCharacters = config.isEncodeIllegalCharacters();
        this.punycodeUnknownScheme = config.isPunycodeUnknownScheme();
        this.preParseNormalizers = config.preParseNormalizers.toArray(new PreParseNormalizer[0]);
        this.inParseNormalizers = config.inParseNormalizers.toArray(new InParseNormalizer[0]);
        this.postParseNormalizers = config.postParseNormalizers.toArray(new PostParseNormalizer[0]);
        this.defaultFormat = config.getDefaultFormat();
    }

    public int getMaxUrlLength() {
        return maxUrlLength;
    }

    public Parser getParser() {
        return parser;
    }

    public ReferenceResolver getReferenceResolver() {
        return referenceResolver;
    }

    public Charset getCharset() {
        return charset;
    }

    public boolean isRequireAbsoluteUri() {
        return requireAbsoluteUri;
    }

    public boolean isStrictReferenceResolution() {
        return strictReferenceResolution;
    }

    public boolean isCaseNormalization() {
        return caseNormalization;
    }

    public boolean isPercentEncodingNormalization() {
        return percentEncodingNormalization;
    }

    public boolean isPathSegmentNormalization() {
        return pathSegmentNormalization;
    }

    public boolean isSchemeBasedNormalization() {
        return schemeBasedNormalization;
    }

    public boolean isEncodeIllegalCharacters() {
        return encodeIllegalCharacters;
    }

    public boolean isPunycodeUnknownScheme() {
        return punycodeUnknownScheme;
    }

    public PreParseNormalizer[] getPreParseNormalizers() {
        return preParseNormalizers;
    }

    public InParseNormalizer[] getInParseNormalizers() {
        return inParseNormalizers;
    }

    public PostParseNormalizer[] getPostParseNormalizers() {
        return postParseNormalizers;
    }

    public UriFormat getDefaultFormat() {
        return defaultFormat;
    }

    public static final class ConfigBuilder {

        private int maxUrlLength = Integer.MAX_VALUE;

        private Parser parser = Configurations.STRICT_PARSER;

        private ReferenceResolver referenceResolver = Configurations.REFERENCE_RESOLVER;

        private final List<PreParseNormalizer> preParseNormalizers = new ArrayList<>();

        private final List<InParseNormalizer> inParseNormalizers = new ArrayList<>();

        private final List<PostParseNormalizer> postParseNormalizers = new ArrayList<>();

        private Charset charset = StandardCharsets.UTF_8;

        private boolean requireAbsoluteUri = false;

        private boolean strictReferenceResolution = true;

        private boolean caseNormalization = true;

        private boolean percentEncodingNormalization = true;

        private boolean pathSegmentNormalization = true;

        private boolean schemeBasedNormalization = false;

        private boolean encodeIllegalCharacters = false;

        private boolean punycodeUnknownScheme = false;

        private UriFormat defaultFormat = Configurations.DEFAULT_FORMAT;

        private ConfigBuilder() {
        }

        private ConfigBuilder(UriBuilderConfig config) {
            this.maxUrlLength = config.getMaxUrlLength();
            this.parser = config.getParser();
            this.referenceResolver = config.getReferenceResolver();
            this.charset = config.getCharset();
            this.requireAbsoluteUri = config.isRequireAbsoluteUri();
            this.strictReferenceResolution = config.isStrictReferenceResolution();
            this.caseNormalization = config.isCaseNormalization();
            this.percentEncodingNormalization = config.isPercentEncodingNormalization();
            this.pathSegmentNormalization = config.isPathSegmentNormalization();
            this.schemeBasedNormalization = config.isSchemeBasedNormalization();
            this.encodeIllegalCharacters = config.isEncodeIllegalCharacters();
            this.punycodeUnknownScheme = config.isPunycodeUnknownScheme();
            this.preParseNormalizers.addAll(Arrays.asList(config.preParseNormalizers));
            this.inParseNormalizers.addAll(Arrays.asList(config.inParseNormalizers));
            this.postParseNormalizers.addAll(Arrays.asList(config.postParseNormalizers));
            this.defaultFormat = config.getDefaultFormat();
        }

        public ConfigBuilder maxUrlLength(final int value) {
            this.maxUrlLength = value;
            return this;
        }

        public ConfigBuilder parser(final Parser value) {
            this.parser = value;
            return this;
        }

        public ConfigBuilder referenceResolver(final ReferenceResolver value) {
            this.referenceResolver = value;
            return this;
        }

        public ConfigBuilder charset(final Charset value) {
            this.charset = value;
            return this;
        }

        public ConfigBuilder defaultFormat(final UriFormat value) {
            this.defaultFormat = value;
            return this;
        }

        public ConfigBuilder requireAbsoluteUri(final boolean value) {
            this.requireAbsoluteUri = value;
            return this;
        }

        public ConfigBuilder strictReferenceResolution(final boolean value) {
            this.strictReferenceResolution = value;
            return this;
        }

        public ConfigBuilder caseNormalization(final boolean value) {
            this.caseNormalization = value;
            return this;
        }

        public ConfigBuilder percentEncodingNormalization(final boolean value) {
            this.percentEncodingNormalization = value;
            return this;
        }

        public ConfigBuilder pathSegmentNormalization(final boolean value) {
            this.pathSegmentNormalization = value;
            return this;
        }

        public ConfigBuilder schemeBasedNormalization(final boolean value) {
            this.schemeBasedNormalization = value;
            return this;
        }

        public ConfigBuilder encodeIllegalCharacters(final boolean value) {
            this.encodeIllegalCharacters = value;
            return this;
        }

        public ConfigBuilder punycodeUnknownScheme(final boolean value) {
            this.punycodeUnknownScheme = value;
            return this;
        }

        public ConfigBuilder addNormalizer(Normalizer normalizer) {
            if (normalizer instanceof PreParseNormalizer) {
                this.preParseNormalizers.add((PreParseNormalizer) normalizer);
            }
            if (normalizer instanceof InParseNormalizer) {
                this.inParseNormalizers.add((InParseNormalizer) normalizer);
            }
            if (normalizer instanceof PostParseNormalizer) {
                this.postParseNormalizers.add((PostParseNormalizer) normalizer);
            }
            return this;
        }

        public int getMaxUrlLength() {
            return maxUrlLength;
        }

        public Parser getParser() {
            return parser;
        }

        public ReferenceResolver getReferenceResolver() {
            return referenceResolver;
        }

        public Charset getCharset() {
            return charset;
        }

        public boolean isRequireAbsoluteUri() {
            return requireAbsoluteUri;
        }

        public boolean isStrictReferenceResolution() {
            return strictReferenceResolution;
        }

        public boolean isCaseNormalization() {
            return caseNormalization;
        }

        public boolean isPercentEncodingNormalization() {
            return percentEncodingNormalization;
        }

        public boolean isPathSegmentNormalization() {
            return pathSegmentNormalization;
        }

        public boolean isSchemeBasedNormalization() {
            return schemeBasedNormalization;
        }

        public boolean isEncodeIllegalCharacters() {
            return encodeIllegalCharacters;
        }

        public boolean isPunycodeUnknownScheme() {
            return punycodeUnknownScheme;
        }

        public List<PreParseNormalizer> getPreParseNormalizers() {
            return preParseNormalizers;
        }

        public List<InParseNormalizer> getInParseNormalizers() {
            return inParseNormalizers;
        }

        public List<PostParseNormalizer> getPostParseNormalizers() {
            return postParseNormalizers;
        }

        public UriFormat getDefaultFormat() {
            return defaultFormat;
        }

        public UriBuilderConfig build() {
            return new UriBuilderConfig(this);
        }

    }
}
