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

/**
 *
 */
public class NormalizationExample {
    private final String uri;
    private final String normalizedUri;

    public static class Builder {

        private String uri;

        private String normalizedUri;

        private Builder() {
        }

        public Builder uri(final String value) {
            this.uri = value;
            return this;
        }

        public Builder normalizedUri(final String value) {
            this.normalizedUri = value;
            return this;
        }

        public NormalizationExample build() {
            return new org.netpreserve.commons.uri.normalization.report.NormalizationExample(uri, normalizedUri);
        }

    }
    public static NormalizationExample.Builder builder() {
        return new NormalizationExample.Builder();
    }

    private NormalizationExample(final String uri, final String normalizedUri) {
        this.uri = uri;
        this.normalizedUri = normalizedUri;
    }
    
}
