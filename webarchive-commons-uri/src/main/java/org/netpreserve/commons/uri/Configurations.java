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

import org.netpreserve.commons.uri.normalization.AllLowerCase;
import org.netpreserve.commons.uri.normalization.CheckLongEnough;
import org.netpreserve.commons.uri.normalization.LaxTrimming;
import org.netpreserve.commons.uri.normalization.OptimisticDnsScheme;
import org.netpreserve.commons.uri.normalization.QueryNormalizers;
import org.netpreserve.commons.uri.normalization.StripTrailingEscapedSpaceOnAuthority;
import org.netpreserve.commons.uri.normalization.StripExtraSlashes;
import org.netpreserve.commons.uri.normalization.StripWwwN;

/**
 * Common configurations to use with UriBuilder.
 */
public final class Configurations {

    public static final Rfc3986Parser STRICT_PARSER = new Rfc3986Parser();

    public static final Rfc3986Parser LAX_PARSER = new LaxRfc3986Parser();

    public static final Rfc3986ReferenceResolver REFERENCE_RESOLVER = new Rfc3986ReferenceResolver();

    public static final UriFormat DEFAULT_FORMAT = UriFormat.builder().build();

    public static final UriFormat USABLE_URI_FORMAT = UriFormat.builder()
            .ignoreFragment(true).build();

    public static final UriFormat CANONICALIZED_URI_FORMAT = UriFormat.builder()
            .ignoreUserInfo(true)
            .ignoreFragment(true).build();

    public static final UriFormat SURT_KEY_FORMAT = UriFormat.builder()
            .surtEncoding(true)
            .ignoreScheme(true)
            .ignoreUserInfo(true)
            .ignoreFragment(true).build();

    public static final UriBuilderConfig STRICT_URI = UriBuilderConfig.newBuilder()
            .parser(Configurations.STRICT_PARSER)
            .referenceResolver(Configurations.REFERENCE_RESOLVER)
            .requireAbsoluteUri(false)
            .strictReferenceResolution(true)
            .caseNormalization(true)
            .percentEncodingNormalization(true)
            .pathSegmentNormalization(true)
            .schemeBasedNormalization(true)
            .encodeIllegalCharacters(false)
            .defaultFormat(Configurations.DEFAULT_FORMAT)
            .build();

    public static final UriBuilderConfig USABLE_URI = UriBuilderConfig.newBuilder()
            .parser(Configurations.LAX_PARSER)
            .referenceResolver(Configurations.REFERENCE_RESOLVER)
            .requireAbsoluteUri(true)
            .strictReferenceResolution(false)
            .caseNormalization(true)
            .percentEncodingNormalization(true)
            .pathSegmentNormalization(true)
            .schemeBasedNormalization(true)
            .encodeIllegalCharacters(true)
             // Consider URIs too long for IE as illegal.
            .maxUrlLength(2083)
            .defaultFormat(Configurations.USABLE_URI_FORMAT)
            .addNormalizer(new LaxTrimming())
            .addNormalizer(new StripExtraSlashes())
            .addNormalizer(new StripTrailingEscapedSpaceOnAuthority())
            .addNormalizer(new OptimisticDnsScheme())
            .addNormalizer(new CheckLongEnough())
            .build();

    public static final UriBuilderConfig CANONICALIZED_URI =
            UriBuilderConfig.newBuilder()
            .parser(Configurations.LAX_PARSER)
            .referenceResolver(Configurations.REFERENCE_RESOLVER)
            .requireAbsoluteUri(true)
            .strictReferenceResolution(false)
            .caseNormalization(true)
            .percentEncodingNormalization(true)
            .pathSegmentNormalization(true)
            .schemeBasedNormalization(true)
            .encodeIllegalCharacters(true)
             // Consider URIs too long for IE as illegal.
            .maxUrlLength(2083)
            .defaultFormat(Configurations.CANONICALIZED_URI_FORMAT)
            .addNormalizer(new LaxTrimming())
            .addNormalizer(new AllLowerCase())
            .addNormalizer(new StripWwwN())
            .addNormalizer(new QueryNormalizers())
            .addNormalizer(new StripExtraSlashes())
            .addNormalizer(new StripTrailingEscapedSpaceOnAuthority())
            .addNormalizer(new OptimisticDnsScheme())
            .addNormalizer(new CheckLongEnough())
//            .addNormalizer(new StripSessionIDs());
//            .addNormalizer(new StripSessionCFIDs());
//            .addNormalizer(new FixupQueryString());
            .build();

    public static final UriBuilderConfig SURT_KEY =
            UriBuilderConfig.newBuilder()
            .parser(Configurations.LAX_PARSER)
            .referenceResolver(Configurations.REFERENCE_RESOLVER)
            .requireAbsoluteUri(true)
            .strictReferenceResolution(false)
            .caseNormalization(true)
            .percentEncodingNormalization(true)
            .pathSegmentNormalization(true)
            .schemeBasedNormalization(true)
            .encodeIllegalCharacters(true)
             // Consider URIs too long for IE as illegal.
            .maxUrlLength(2083)
            .defaultFormat(Configurations.SURT_KEY_FORMAT)
            .addNormalizer(new LaxTrimming())
            .addNormalizer(new AllLowerCase())
            .addNormalizer(new StripWwwN())
            .addNormalizer(new QueryNormalizers())
            .addNormalizer(new StripExtraSlashes())
            .addNormalizer(new StripTrailingEscapedSpaceOnAuthority())
            .addNormalizer(new OptimisticDnsScheme())
            .addNormalizer(new CheckLongEnough())
//            .addNormalizer(new StripSessionIDs());
//            .addNormalizer(new StripSessionCFIDs());
//            .addNormalizer(new FixupQueryString());
            .build();

    private Configurations() {
    }

}
