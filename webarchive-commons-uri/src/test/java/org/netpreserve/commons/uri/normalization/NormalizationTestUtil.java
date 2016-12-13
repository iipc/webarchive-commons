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

import org.assertj.core.api.Fail;
import org.netpreserve.commons.uri.Configurations;
import org.netpreserve.commons.uri.Normalizer;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.uri.normalization.report.NormalizationConfigReport;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;
import org.netpreserve.commons.uri.normalization.report.NormalizationExample;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class NormalizationTestUtil {

    public static void testNormalizerExamples(Normalizer normalizer) {
        UriBuilderConfig.ConfigBuilder config = UriBuilderConfig.newBuilder()
                .parser(Configurations.STRICT_PARSER)
                .referenceResolver(Configurations.REFERENCE_RESOLVER)
                .requireAbsoluteUri(false)
                .strictReferenceResolution(true)
                .caseNormalization(false)
                .percentEncodingNormalization(false)
                .pathSegmentNormalization(false)
                .schemeBasedNormalization(true)
                .encodeIllegalCharacters(false)
                .defaultFormat(Configurations.DEFAULT_FORMAT);

        config.addNormalizer(normalizer);

        UriBuilder builder = UriBuilder.builder(config.build());

        boolean testsFound = false;

        NormalizationConfigReport configReport = NormalizationConfigReport.parse(builder);
        for (NormalizationDescription desc : configReport.getNormalizationDescriptions()) {
            if (normalizer.getClass().getName().equals(desc.getImplementingClass())) {
                for (NormalizationExample example : desc.getExamples()) {
                    try {
                        assertThat(builder.uri(example.getUri()).build())
                                .as("Example '%s' for normalization '%s' defined in '%s' failed", example, desc
                                        .getName(),
                                        desc.getImplementingClass()).hasToString(example.getNormalizedUri());
                        testsFound = true;
                    } catch (Exception e) {
                        Fail.fail("Caught exception while evaluating example '" + example + "' defined in '" + desc
                                .getImplementingClass() + "'", e);
                    }
                }
            }
        }

        if (!testsFound) {
            System.err.println("Warning: No examples found for Normalizer: " + normalizer.getClass().getName());
        }
    }

}
