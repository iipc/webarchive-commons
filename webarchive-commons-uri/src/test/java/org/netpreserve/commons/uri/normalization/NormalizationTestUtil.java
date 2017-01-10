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
package org.netpreserve.commons.uri.normalization;

import java.util.ArrayList;
import java.util.List;

import org.netpreserve.commons.uri.UriConfigs;
import org.netpreserve.commons.uri.Normalizer;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.uri.normalization.report.NormalizationConfigReport;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;
import org.netpreserve.commons.uri.normalization.report.NormalizationExample;
import org.netpreserve.commons.uri.parser.Parser;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class NormalizationTestUtil {

    public static void testNormalizerExamples(Normalizer normalizer) {
        UriBuilderConfig config = new UriBuilderConfig()
                .parser(UriConfigs.DEFAULT_PARSER)
                .referenceResolver(UriConfigs.DEFAULT_REFERENCE_RESOLVER)
                .requireAbsoluteUri(false)
                .strictReferenceResolution(true)
                .caseNormalization(false)
                .percentEncodingNormalization(false)
                .pathSegmentNormalization(false)
                .schemeBasedNormalization(true)
                .encodeIllegalCharacters(false)
                .defaultFormat(UriConfigs.DEFAULT_FORMAT)
                .addNormalizer(normalizer);

        UriBuilder builder = config.builder();

        NormalizationConfigReport configReport = NormalizationConfigReport.parse(builder);
        for (NormalizationDescription desc : configReport.getNormalizationDescriptions()) {
            if (normalizer.getClass().getName().equals(desc.getImplementingClass())) {
                assertThat(desc.getExamples())
                        .as("Warning: No examples found for: %s", normalizer.getClass().getName())
                        .isNotEmpty();

                for (NormalizationExample example : desc.getExamples()) {
                    try {
                        assertThat(builder.uri(example.getUri()).build())
                                .as("Example '%s' for normalization '%s' defined in '%s' failed", example, desc
                                        .getName(),
                                        desc.getImplementingClass()).hasToString(example.getNormalizedUri());
                    } catch (Exception e) {
                        fail("Caught exception while evaluating example '" + example + "' defined in '"
                                + desc.getImplementingClass() + "'", e);
                    }
                }
            }
        }
    }

    public static void testParserNormalizationExamples(Parser parser) {
        UriBuilderConfig config = new UriBuilderConfig()
                .parser(parser)
                .referenceResolver(UriConfigs.DEFAULT_REFERENCE_RESOLVER)
                .strictReferenceResolution(true)
                .defaultFormat(UriConfigs.DEFAULT_FORMAT)
                .caseNormalization(false)
                .encodeIllegalCharacters(false)
                .normalizeIpv4(false)
                .normalizeIpv6(false)
                .ipv6Case(UriBuilderConfig.HexCase.KEEP)
                .pathSegmentNormalization(false)
                .percentEncodingNormalization(false)
                .percentEncodingCase(UriBuilderConfig.HexCase.KEEP)
                .punycodeUnknownScheme(false)
                .requireAbsoluteUri(false)
                .schemeBasedNormalization(false)
                .useIpv6Base85Encoding(false);

        UriBuilder builder;

        builder = config.caseNormalization(true).builder();
        testOneParserNormalizationConfig(builder);

        builder = config.encodeIllegalCharacters(true).percentEncodingCase(UriBuilderConfig.HexCase.KEEP).builder();
        testOneParserNormalizationConfig(builder);

        builder = config.encodeIllegalCharacters(true).percentEncodingCase(UriBuilderConfig.HexCase.UPPER).builder();
        testOneParserNormalizationConfig(builder);

        builder = config.encodeIllegalCharacters(true).percentEncodingCase(UriBuilderConfig.HexCase.LOWER).builder();
        testOneParserNormalizationConfig(builder);

        builder = config.normalizeIpv4(true).builder();
        testOneParserNormalizationConfig(builder);

        builder = config.normalizeIpv6(true).builder();
        testOneParserNormalizationConfig(builder);

        builder = config.normalizeIpv6(true).useIpv6Base85Encoding(true).builder();
        testOneParserNormalizationConfig(builder);

        builder = config.pathSegmentNormalization(true).builder();
        testOneParserNormalizationConfig(builder);

        builder = config.percentEncodingNormalization(true).percentEncodingCase(UriBuilderConfig.HexCase.KEEP)
                .builder();
        testOneParserNormalizationConfig(builder);

        builder = config.percentEncodingNormalization(true).percentEncodingCase(UriBuilderConfig.HexCase.UPPER)
                .builder();
        testOneParserNormalizationConfig(builder);

        builder = config.percentEncodingNormalization(true).percentEncodingCase(UriBuilderConfig.HexCase.LOWER)
                .builder();
        testOneParserNormalizationConfig(builder);

        builder = config.punycodeUnknownScheme(true).builder();
        testOneParserNormalizationConfig(builder);

        builder = config.schemeBasedNormalization(true).builder();
        testOneParserNormalizationConfig(builder);
    }

    private static void testOneParserNormalizationConfig(UriBuilder builder) {
        List<NormalizationDescription> descriptions = new ArrayList<>();

        builder.parser().describeNormalization(builder, descriptions);
        for (NormalizationDescription desc : descriptions) {
            try {
                assertThat(Class.forName(desc.getImplementingClass()))
                        .as("Wrong implementing class in normalization description")
                        .isAssignableFrom(builder.parser().getClass());
            } catch (ClassNotFoundException ex) {
                fail("Non existing implementing class in normalization description", ex);
            }
            assertThat(desc.getExamples())
                    .as("Warning: No examples found for normalization '%s' in parser '%s'",
                            desc.getName(), builder.parser().getClass().getName())
                    .isNotEmpty();

            for (NormalizationExample example : desc.getExamples()) {
                try {
                    assertThat(builder.uri(example.getUri()).build())
                            .as("Example '%s' for normalization '%s' defined in '%s' failed", example, desc.getName(),
                                    desc.getImplementingClass()).hasToString(example.getNormalizedUri());
                } catch (Exception e) {
                    fail("Caught exception while evaluating example '" + example + "' for normalization '"
                            + desc.getName() + "' defined in '" + desc.getImplementingClass() + "'", e);
                }
            }
        }
    }

}
