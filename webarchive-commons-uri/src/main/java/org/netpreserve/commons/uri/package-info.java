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
/**
 * Classes for parsing, resolving and formatting URI's.
 * <p>
 * Parsing a URI consist of four steps:
 * <ul>
 * <li>Create a {@link UriBuilderConfig} object and modify it to your needs. The config object is immutable and safe to
 * reuse by multiple threads.
 * <li>Create a {@link UriBuilder} from the UriBuilderConfig. UriBuilders are <em>not</em> thread safe and you should
 * create a new one for every URI you want to parse.
 * <li>Set the URI on the UriBuilder or set any individual fields.
 * <li>Build an immutable {@link Uri} object by calling {@code build()} on the UriBuilder.
 * </ul>
 * <pre>
 * UriBuilderConfig conf = new UriBuilderConfig(); // Creates a new configuration with default values
 * <p>
 * Uri uri = conf.builder()                        // Creates a new UriBuilder initialized with the configuration
 *            .uri("http://example.com/path")      // Let the builder parse a URI
 *            .build();                            // Build an immutable Uri object from the builder
 * </pre>
 * <p>
 * A shortcut for doing the above:
 * <pre>
 * Uri uri = new UriBuilderConfig().buildUri("http://example.com/path");
 * </pre>
 * <p>
 * To tweak the configuration there are a lot of options. The modifying methods always creates a new config object so it
 * is important to create a new assignment for every method call like this:
 * <pre>
 * // OK
 * UriBuilderConfig conf = new UriBuilderConfig();
 * conf = conf.pathSegmentNormalization(true);
 * conf = conf.requireAbsoluteUri(false);
 *
 * // WRONG
 * UriBuilderConfig conf = new UriBuilderConfig();
 * conf.pathSegmentNormalization(true);
 * conf.requireAbsoluteUri(false);
 * </pre>
 * <p>
 * Since all the modifying methods returns a new config object, it is preferred to chain the calls.
 * <pre>
 * UriBuilderConfig conf = new UriBuilderConfig()
 *     .pathSegmentNormalization(true)
 *     .requireAbsoluteUri(false)
 *     .caseNormalization(true)
 *     .schemeBasedNormalization(true)
 *     .encodeIllegalCharacters(false)
 *     .addNormalizer(new StripSlashAtEndOfPath());
 * </pre>
 * <p>
 * As a convenience, several configurations already exist in the UriConfigs class. A common usage:
 * <pre>
 * Uri uri = UriConfigs.WHATWG.buildUri("http://example.com/path");
 * </pre>
 * <p>
 * The {@link UriConfigs#WHATWG}-configuration normalizes URIs in compliance with
 * <a href="https://url.spec.whatwg.org/">WHATWG's URL spec</a>.
 */
package org.netpreserve.commons.uri;
