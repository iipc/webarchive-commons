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
import org.netpreserve.commons.uri.parser.MimicBrowserReferenceResolver;
import org.netpreserve.commons.uri.parser.LegacyWaybackSurtEncoder;
import org.netpreserve.commons.uri.parser.MimicBrowserParser;
import org.netpreserve.commons.uri.parser.Rfc3986Parser;
import org.netpreserve.commons.uri.parser.Rfc3986ReferenceResolver;
import org.netpreserve.commons.uri.parser.LaxRfc3986Parser;
import org.netpreserve.commons.uri.normalization.AllLowerCase;
import org.netpreserve.commons.uri.normalization.CheckLongEnough;
import org.netpreserve.commons.uri.normalization.InferCommonSchemesForSchemelessUri;
import org.netpreserve.commons.uri.normalization.LaxTrimming;
import org.netpreserve.commons.uri.normalization.OptimisticDnsScheme;
import org.netpreserve.commons.uri.normalization.StripErrorneousExtraSlashes;
import org.netpreserve.commons.uri.normalization.StripSessionId;
import org.netpreserve.commons.uri.normalization.StripTrailingEscapedSpaceOnAuthority;
import org.netpreserve.commons.uri.normalization.StripSlashAtEndOfPath;
import org.netpreserve.commons.uri.normalization.StripWwwN;
import org.netpreserve.commons.uri.normalization.MimicBrowserNormalizer;
import org.netpreserve.commons.uri.normalization.TrimHost;

/**
 * Common configurations to use with UriBuilder.
 */
public final class UriConfigs {

    public static final Parser STRICT_PARSER = new Rfc3986Parser();

    public static final Parser LAX_PARSER = new LaxRfc3986Parser();

    public static final Parser MIMIC_BROWSER_PARSER = new MimicBrowserParser();

    public static final ReferenceResolver REFERENCE_RESOLVER = new Rfc3986ReferenceResolver();

    public static final ReferenceResolver MIMIC_BROWSER_REFERENCE_RESOLVER = new MimicBrowserReferenceResolver();

    public static final UriFormat DEFAULT_FORMAT = new UriFormat();

    public static final UriFormat USABLE_URI_FORMAT = new UriFormat()
            .ignoreFragment(true);

    public static final UriFormat CANONICALIZED_URI_FORMAT = new UriFormat()
            .ignoreUser(true)
            .ignorePassword(true)
            .ignoreFragment(true);

    public static final UriFormat SURT_KEY_FORMAT = new UriFormat()
            .surtEncoding(true)
            .ignoreScheme(true)
            .ignoreUser(true)
            .ignorePassword(true)
            .ignoreFragment(true)
            .decodeHost(true);

    public static final UriFormat LEGACY_SURT_KEY_FORMAT = new UriFormat()
            .surtEncoding(true)
            .ignoreScheme(true)
            .ignoreUser(true)
            .ignorePassword(true)
            .ignoreFragment(true)
            .decodeHost(false)
            .surtEncoder(new LegacyWaybackSurtEncoder());

    public static final UriBuilderConfig STRICT = new UriBuilderConfig()
            .parser(UriConfigs.STRICT_PARSER)
            .referenceResolver(UriConfigs.REFERENCE_RESOLVER)
            .requireAbsoluteUri(false)
            .strictReferenceResolution(true)
            .caseNormalization(true)
            .percentEncodingNormalization(true)
            .pathSegmentNormalization(true)
            .schemeBasedNormalization(true)
            .encodeIllegalCharacters(false)
            .defaultFormat(UriConfigs.DEFAULT_FORMAT);

    /**
     * A Uri config trying to mimic the behavior of major browsers as described by
     * <a herf="https://url.spec.whatwg.org/">whatwg.org</a>.
     */
    public static final UriBuilderConfig WHATWG = new UriBuilderConfig()
            .parser(UriConfigs.MIMIC_BROWSER_PARSER)
            .referenceResolver(UriConfigs.MIMIC_BROWSER_REFERENCE_RESOLVER)
            .requireAbsoluteUri(false)
            .strictReferenceResolution(false)
            .caseNormalization(true)
            .percentEncodingNormalization(false)
            .upperCaseIpv6HexValues(false)
            .pathSegmentNormalization(true)
            .schemeBasedNormalization(true)
            .encodeIllegalCharacters(true)
            .punycodeUnknownScheme(true)
            .defaultFormat(UriConfigs.DEFAULT_FORMAT)
            .addNormalizer(new MimicBrowserNormalizer());

    /**
     * A forgiving URI config.
     * <p>
     * Normalizes some illegal characters, but otherwise tries to accept the URI as it is as much as possible.
     */
    public static final UriBuilderConfig LAX_URI = new UriBuilderConfig()
            .parser(UriConfigs.LAX_PARSER)
            .referenceResolver(UriConfigs.REFERENCE_RESOLVER)
            .requireAbsoluteUri(false)
            .strictReferenceResolution(false)
            .caseNormalization(true)
            .percentEncodingNormalization(true)
            .pathSegmentNormalization(true)
            .schemeBasedNormalization(true)
            .encodeIllegalCharacters(true)
            .defaultFormat(UriConfigs.DEFAULT_FORMAT)
            .addNormalizer(new LaxTrimming())
            .addNormalizer(new StripErrorneousExtraSlashes())
            .addNormalizer(new StripSlashAtEndOfPath())
            .addNormalizer(new StripTrailingEscapedSpaceOnAuthority())
            .addNormalizer(new OptimisticDnsScheme())
            .addNormalizer(new CheckLongEnough());

    /**
     * A config that behave as the UsableUri used in Heritrix.
     */
    public static final UriBuilderConfig HERITRIX = new UriBuilderConfig()
            .parser(UriConfigs.LAX_PARSER)
            .referenceResolver(UriConfigs.REFERENCE_RESOLVER)
            .requireAbsoluteUri(true)
            .strictReferenceResolution(false)
            .caseNormalization(true)
            .upperCaseIpv6HexValues(true)
            .percentEncodingNormalization(true)
            .pathSegmentNormalization(true)
            .schemeBasedNormalization(true)
            .encodeIllegalCharacters(true)
            // Consider URIs too long for IE as illegal.
            .maxUrlLength(2083)
            .defaultFormat(UriConfigs.USABLE_URI_FORMAT)
            .addNormalizer(new LaxTrimming())
            .addNormalizer(new StripErrorneousExtraSlashes())
            .addNormalizer(new StripSlashAtEndOfPath())
            .addNormalizer(new StripTrailingEscapedSpaceOnAuthority())
            .addNormalizer(new OptimisticDnsScheme())
            .addNormalizer(new CheckLongEnough())
            .addNormalizer(new TrimHost());

    /**
     * A config with the standard normalizations used in OpenWayback.
     */
    public static final UriBuilderConfig WAYBACK = new UriBuilderConfig()
            .parser(UriConfigs.LAX_PARSER)
            .referenceResolver(UriConfigs.MIMIC_BROWSER_REFERENCE_RESOLVER)
            .requireAbsoluteUri(true)
            .strictReferenceResolution(false)
            .caseNormalization(true)
            .percentEncodingNormalization(true)
            .pathSegmentNormalization(true)
            .schemeBasedNormalization(true)
            .encodeIllegalCharacters(true)
            // Consider URIs too long for IE as illegal.
            .maxUrlLength(2083)
            .defaultFormat(UriConfigs.CANONICALIZED_URI_FORMAT)
            .addNormalizer(new LaxTrimming())
            .addNormalizer(new StripWwwN())
            .addNormalizer(new StripSessionId())
            .addNormalizer(new StripErrorneousExtraSlashes())
            .addNormalizer(new StripSlashAtEndOfPath())
            .addNormalizer(new StripTrailingEscapedSpaceOnAuthority())
            .addNormalizer(new OptimisticDnsScheme())
            .addNormalizer(new CheckLongEnough());

    public static final UriBuilderConfig SURT_KEY = new UriBuilderConfig()
            .parser(UriConfigs.LAX_PARSER)
            .referenceResolver(UriConfigs.REFERENCE_RESOLVER)
            .requireAbsoluteUri(true)
            .strictReferenceResolution(false)
            .caseNormalization(true)
            .percentEncodingNormalization(true)
            .pathSegmentNormalization(true)
            .schemeBasedNormalization(true)
            .encodeIllegalCharacters(true)
            // Consider URIs too long for IE as illegal.
            .maxUrlLength(2083)
            .defaultFormat(UriConfigs.SURT_KEY_FORMAT)
            .addNormalizer(new LaxTrimming())
            .addNormalizer(new StripWwwN())
            .addNormalizer(new StripSessionId())
            .addNormalizer(new StripErrorneousExtraSlashes())
            .addNormalizer(new StripSlashAtEndOfPath())
            .addNormalizer(new StripTrailingEscapedSpaceOnAuthority())
            .addNormalizer(new InferCommonSchemesForSchemelessUri())
            .addNormalizer(new OptimisticDnsScheme())
            .addNormalizer(new CheckLongEnough());

    public static final UriBuilderConfig LEGACY_SURT_KEY = new UriBuilderConfig()
            .parser(UriConfigs.LAX_PARSER)
            .referenceResolver(UriConfigs.REFERENCE_RESOLVER)
            .requireAbsoluteUri(true)
            .strictReferenceResolution(false)
            .caseNormalization(true)
            .percentEncodingNormalization(true)
            .pathSegmentNormalization(true)
            .schemeBasedNormalization(true)
            .encodeIllegalCharacters(true)
            // Consider URIs too long for IE as illegal.
            .maxUrlLength(2083)
            .defaultFormat(UriConfigs.LEGACY_SURT_KEY_FORMAT)
            .addNormalizer(new LaxTrimming())
            .addNormalizer(new AllLowerCase())
            .addNormalizer(new StripWwwN())
            .addNormalizer(new StripSessionId())
            .addNormalizer(new StripErrorneousExtraSlashes())
            .addNormalizer(new StripSlashAtEndOfPath())
            .addNormalizer(new StripTrailingEscapedSpaceOnAuthority())
            .addNormalizer(new InferCommonSchemesForSchemelessUri())
            .addNormalizer(new OptimisticDnsScheme())
            .addNormalizer(new CheckLongEnough());

    private UriConfigs() {
    }

}
