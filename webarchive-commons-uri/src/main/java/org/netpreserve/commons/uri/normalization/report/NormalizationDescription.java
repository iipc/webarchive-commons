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
 * Documentation of a step in URI normalization.
 */
public class NormalizationDescription {

    private final String name;

    private final String description;

    private final String implementingClass;

    private final NormalizationExample[] example;

    public static class Builder {

        private String name = "Missing";

        private String description = "No description";

        private String implementingClass;

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

        public Builder implementingClass(final Class value) {
            this.implementingClass = value.getName();
            return this;
        }

        public Builder example(final NormalizationExample value) {
            this.example.add(value);
            return this;
        }

        public NormalizationDescription build() {
            return new NormalizationDescription(name, description, implementingClass,
                    example.toArray(new NormalizationExample[0]));
        }

    }

    public static NormalizationDescription.Builder builder(final Class implementingClass) {
        return new NormalizationDescription.Builder().implementingClass(implementingClass);
    }

    private NormalizationDescription(
            final String name,
            final String description,
            final String implementingClass,
            final NormalizationExample[] example) {

        this.name = name;
        this.description = description;
        this.implementingClass = implementingClass;
        this.example = example;
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String indent) {
        StringBuilder sb = new StringBuilder(indent + name)
                .append("\n  " + indent + "Description: " + description)
                .append("\n  " + indent + "Implemented by: " + implementingClass);

        if (example.length > 0) {
            sb.append("\n  " + indent + "Example");
            if (example.length > 1) {
                sb.append("s:");
            } else {
                sb.append(":");
            }
            for (NormalizationExample ex : example) {
                sb.append("\n    " + indent + ex.toString());
            }
        }
        return sb.toString();
    }

}
