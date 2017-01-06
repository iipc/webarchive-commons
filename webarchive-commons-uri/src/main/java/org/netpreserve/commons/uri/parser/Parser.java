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
package org.netpreserve.commons.uri.parser;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.uri.normalization.report.NormalizationDescription;

/**
 * URI parser.
 * <p>
 * A Parser is used for parsing a whole URI or some of its components for validation and normalization. The parser
 * itself is stateless and thread safe. All state is kept in a {@link UriBuilder} and a {@link ParserState} object.
 */
public interface Parser {

    void parseUri(UriBuilder builder, String uri, int offset);

    void decomposeAuthority(UriBuilder builder, String authority);

    void parsePath(UriBuilder builder, String value);

    String validateQuery(UriBuilderConfig config, Charset charset, CharBuffer uri);

    String validateFragment(UriBuilderConfig config, Charset charset, CharBuffer uri);

    void describeNormalization(UriBuilder uriBuilder, List<NormalizationDescription> descriptions);

    class ParserState {

        UriBuilder builder;

        CharBuffer uri;

        final Charset charset;

        boolean hasAuthority;

        public ParserState(UriBuilder builder, String uri, int offset) {
            this.builder = builder;
            this.uri = CharBuffer.wrap(uri.toCharArray());
            this.uri.position(offset);
            this.charset = builder.charset();
        }

        public ParserState(UriBuilder builder, String uri) {
            this.builder = builder;
            this.uri = CharBuffer.wrap(uri.toCharArray());
            this.charset = builder.charset();
        }

        public UriBuilder getBuilder() {
            return builder;
        }

        public CharBuffer getUri() {
            return uri;
        }

        public boolean hasAuthority() {
            return hasAuthority;
        }

        public void setHasAuthority(boolean hasAuthority) {
            this.hasAuthority = hasAuthority;
        }

        public boolean moveToNext() {
            if (uri.limit() == uri.capacity()) {
                uri.position(uri.capacity());
            } else {
                uri.position(uri.limit());
                uri.limit(uri.capacity());
            }
            return uri.hasRemaining();
        }

        public void incrementOffset(int value) {
            uri.position(uri.position() + value);
        }

        public boolean uriHasAtLeastMoreChararcters(int minChars) {
            return uri.capacity() >= uri.position() + minChars;
        }

        public String uriToString() {
            return new String(uri.array());
        }

    }

}
