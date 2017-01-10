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
package org.netpreserve.commons.uri.normalization.report;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;
import org.netpreserve.commons.uri.InParseNormalizer;
import org.netpreserve.commons.uri.Normalizer;
import org.netpreserve.commons.uri.PostParseNormalizer;
import org.netpreserve.commons.uri.PreParseNormalizer;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.uri.normalization.SchemeBasedNormalizer;

/**
 *
 */
public final class NormalizationConfigReport {

    private final List<NormalizationDescription> normalizations;

    public static NormalizationConfigReport parse(UriBuilder uriBuilder) {
        return new NormalizationConfigReport(uriBuilder);
    }

    private NormalizationConfigReport(UriBuilder uriBuilder) {
        UriBuilderConfig config = uriBuilder.config();
        this.normalizations = new ArrayList<>();

        for (Normalizer normalizer : config.getPreParseNormalizers()) {
            if (!(normalizer instanceof SchemeBasedNormalizer) || config.isSchemeBasedNormalization()) {
                evalNormalizerConfigurations(PreParseNormalizer.class, (PreParseNormalizer) normalizer);
            }
        }

        config.getParser().describeNormalization(uriBuilder, normalizations);

        for (Normalizer normalizer : config.getInParseNormalizers()) {
            if (!(normalizer instanceof SchemeBasedNormalizer) || config.isSchemeBasedNormalization()) {
                evalNormalizerConfigurations(InParseNormalizer.class, (InParseNormalizer) normalizer);
            }
        }

        for (Normalizer normalizer : config.getPostParseNormalizers()) {
            if (!(normalizer instanceof SchemeBasedNormalizer) || config.isSchemeBasedNormalization()) {
                evalNormalizerConfigurations(PostParseNormalizer.class, (PostParseNormalizer) normalizer);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Configured normalizations:");
        for (NormalizationDescription desc : normalizations) {
            sb.append("\n").append(desc.toString("  "));
        }
        return sb.toString();
    }

    public String toJson(boolean prettyPrint) {
        try {
            Class.forName("com.google.gson.Gson");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Gson is needed in not classpath to write normalization config report as JSON.");
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        if (prettyPrint) {
            gsonBuilder.setPrettyPrinting();
        }
        return gsonBuilder.create().toJson(this);
    }

    public NormalizationDescription[] getNormalizationDescriptions() {
        return normalizations.toArray(new NormalizationDescription[0]);
    }

    private <T extends Normalizer> void evalNormalizerConfigurations(Class<T> normalizerType, T normalizer) {
        Class<T> cl = (Class<T>) normalizer.getClass();

        for (Method interfaceMethod : normalizerType.getDeclaredMethods()) {
            try {
                Method classMethod = cl.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
                Description descr = classMethod.getAnnotation(Description.class);
                if (descr != null) {
                    Example[] examples = classMethod.getAnnotationsByType(Example.class);
                    normalizations.add(new NormalizationDescription(cl, descr, examples));
                }
            } catch (NoSuchMethodException | SecurityException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
