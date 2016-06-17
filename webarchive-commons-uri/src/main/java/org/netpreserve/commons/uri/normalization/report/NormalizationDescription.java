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

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NormalizationDescription {
    private final String name;
    private final String description;
    private final List<NormalizationExample> example;

    public static class Builder {

        private String name;

        private String description;

        private List<NormalizationExample> example = new ArrayList<>();

        private Builder() {
        }

        public Builder name(final String value) {
            this.name = value;
            return this;
        }

        public Builder description(final String value) {
            this.description = value;
            return this;
        }

        public Builder example(final NormalizationExample value) {
            this.example.add(value);
            return this;
        }

        public NormalizationDescription build() {
            return new org.netpreserve.commons.uri.normalization.report.NormalizationDescription(name, description, example);
        }

    }
    public static NormalizationDescription.Builder builder() {
        return new NormalizationDescription.Builder();
    }

    private NormalizationDescription(final String name, final String description, final List<NormalizationExample> example) {
        this.name = name;
        this.description = description;
        this.example = example;
    }

}
