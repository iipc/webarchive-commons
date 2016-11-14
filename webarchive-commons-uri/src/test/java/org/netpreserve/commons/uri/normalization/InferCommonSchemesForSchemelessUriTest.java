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

import org.junit.Test;
import org.netpreserve.commons.uri.Configurations;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for InferCommonSchemesForSchemelessUriTest.
 */
public class InferCommonSchemesForSchemelessUriTest {

    UriBuilderConfig config = Configurations.USABLE_URI.toBuilder()
            .defaultFormat(Configurations.CANONICALIZED_URI_FORMAT)
            .addNormalizer(new InsertCommonSchemesForSchemelessUri()).build();

    @Test
    public void testMissingHttpScheme() {
        Uri uri = UriBuilder.builder(config).uri("example.com/index.html").build();
        assertThat(uri).hasFieldOrPropertyWithValue("scheme", "http")
                .hasFieldOrPropertyWithValue("host", "example.com")
                .hasFieldOrPropertyWithValue("path", "/index.html")
                .hasFieldOrPropertyWithValue("absolute", true)
                .hasFieldOrPropertyWithValue("absolutePath", true)
                .hasFieldOrPropertyWithValue("registryName", true);
    }

    @Test
    public void testMissingHttpSchemeWithUserAndPort() {
        Uri uri = UriBuilder.builder(config).uri("user@example.com:80/index.html").build();
        assertThat(uri).hasFieldOrPropertyWithValue("scheme", "http")
                .hasFieldOrPropertyWithValue("user", "user")
                .hasFieldOrPropertyWithValue("host", "example.com")
                .hasFieldOrPropertyWithValue("path", "/index.html")
                .hasFieldOrPropertyWithValue("absolute", true)
                .hasFieldOrPropertyWithValue("absolutePath", true)
                .hasFieldOrPropertyWithValue("registryName", true);
    }

    @Test
    public void testMissingFileSchemeOneSlash() {
        Uri uri = UriBuilder.builder(config).uri("/index.html").build();
        assertThat(uri)
                .hasToString("file:///index.html")
                .hasFieldOrPropertyWithValue("scheme", "file")
                .hasFieldOrPropertyWithValue("host", "")
                .hasFieldOrPropertyWithValue("path", "/index.html")
                .hasFieldOrPropertyWithValue("absolute", true)
                .hasFieldOrPropertyWithValue("absolutePath", true)
                .hasFieldOrPropertyWithValue("isRegName", false);
    }

    @Test
    public void testMissingFileSchemeTwoSlash() {
        Uri uri = UriBuilder.builder(config).uri("//example.com/index.html").build();
        assertThat(uri)
                .hasToString("file:///example.com/index.html")
                .hasFieldOrPropertyWithValue("scheme", "file")
                .hasFieldOrPropertyWithValue("host", "")
                .hasFieldOrPropertyWithValue("path", "/example.com/index.html")
                .hasFieldOrPropertyWithValue("absolute", true)
                .hasFieldOrPropertyWithValue("absolutePath", true)
                .hasFieldOrPropertyWithValue("registryName", false);
    }

    @Test
    public void testMissingFileSchemeThreeSlash() {
        Uri uri = UriBuilder.builder(config).uri("//example.com/index.html").build();
        assertThat(uri)
                .hasToString("file:///example.com/index.html")
                .hasFieldOrPropertyWithValue("scheme", "file")
                .hasFieldOrPropertyWithValue("host", "")
                .hasFieldOrPropertyWithValue("path", "/example.com/index.html")
                .hasFieldOrPropertyWithValue("absolute", true)
                .hasFieldOrPropertyWithValue("absolutePath", true)
                .hasFieldOrPropertyWithValue("registryName", false);
    }

}
