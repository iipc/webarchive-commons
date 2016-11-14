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

import org.netpreserve.commons.uri.parser.Rfc3986ReferenceResolver;
import org.netpreserve.commons.uri.parser.Rfc3986Parser;
import org.junit.Test;
import org.netpreserve.commons.uri.Configurations;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class Rfc3986ReferenceResolverTest {

    /**
     * Test of resolve method, of class Rfc3986ReferenceResolution.
     */
    @Test
    public void testResolve() {
        Uri result;

        result = resolveToRfc3986Base("g:h");
        assertThat(result.toString()).isEqualTo("g:h");

        result = resolveToRfc3986Base("g");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g");

        result = resolveToRfc3986Base("./g");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g");

        result = resolveToRfc3986Base("g/");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g/");

        result = resolveToRfc3986Base("/g");
        assertThat(result.toString()).isEqualTo("http://a/g");

        result = resolveToRfc3986Base("//g");
        assertThat(result.toString()).isEqualTo("http://g");

        result = resolveToRfc3986Base("?y");
        assertThat(result.toString()).isEqualTo("http://a/b/c/d;p?y");

        result = resolveToRfc3986Base("g?y");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g?y");

        result = resolveToRfc3986Base("#s");
        assertThat(result.toString()).isEqualTo("http://a/b/c/d;p?q#s");

        result = resolveToRfc3986Base("g#s");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g#s");

        result = resolveToRfc3986Base("g?y#s");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g?y#s");

        result = resolveToRfc3986Base(";x");
        assertThat(result.toString()).isEqualTo("http://a/b/c/;x");

        result = resolveToRfc3986Base("g;x");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g;x");

        result = resolveToRfc3986Base("g;x?y#s");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g;x?y#s");

        result = resolveToRfc3986Base("");
        assertThat(result.toString()).isEqualTo("http://a/b/c/d;p?q");

        result = resolveToRfc3986Base(".");
        assertThat(result.toString()).isEqualTo("http://a/b/c/");

        result = resolveToRfc3986Base("./");
        assertThat(result.toString()).isEqualTo("http://a/b/c/");

        result = resolveToRfc3986Base("..");
        assertThat(result.toString()).isEqualTo("http://a/b/");

        result = resolveToRfc3986Base("../");
        assertThat(result.toString()).isEqualTo("http://a/b/");

        result = resolveToRfc3986Base("../g");
        assertThat(result.toString()).isEqualTo("http://a/b/g");

        result = resolveToRfc3986Base("../..");
        assertThat(result.toString()).isEqualTo("http://a/");

        result = resolveToRfc3986Base("../../");
        assertThat(result.toString()).isEqualTo("http://a/");

        result = resolveToRfc3986Base("../../g");
        assertThat(result.toString()).isEqualTo("http://a/g");

        result = resolveToRfc3986Base("../../../g");
        assertThat(result.toString()).isEqualTo("http://a/g");

        result = resolveToRfc3986Base("../../../../g");
        assertThat(result.toString()).isEqualTo("http://a/g");

        result = resolveToRfc3986Base("/./g");
        assertThat(result.toString()).isEqualTo("http://a/g");

        result = resolveToRfc3986Base("/../g");
        assertThat(result.toString()).isEqualTo("http://a/g");

        result = resolveToRfc3986Base("g.");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g.");

        result = resolveToRfc3986Base(".g");
        assertThat(result.toString()).isEqualTo("http://a/b/c/.g");

        result = resolveToRfc3986Base("g..");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g..");

        result = resolveToRfc3986Base("..g");
        assertThat(result.toString()).isEqualTo("http://a/b/c/..g");

        result = resolveToRfc3986Base("./../g");
        assertThat(result.toString()).isEqualTo("http://a/b/g");

        result = resolveToRfc3986Base("./g/.");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g/");

        result = resolveToRfc3986Base("g/./h");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g/h");

        result = resolveToRfc3986Base("g/../h");
        assertThat(result.toString()).isEqualTo("http://a/b/c/h");

        result = resolveToRfc3986Base("g;x=1/./y");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g;x=1/y");

        result = resolveToRfc3986Base("g;x=1/../y");
        assertThat(result.toString()).isEqualTo("http://a/b/c/y");

        result = resolveToRfc3986Base("g?y/./x");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g?y/./x");

        result = resolveToRfc3986Base("g?y/../x");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g?y/../x");

        result = resolveToRfc3986Base("g#s/./x");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g#s/./x");

        result = resolveToRfc3986Base("g#s/../x");
        assertThat(result.toString()).isEqualTo("http://a/b/c/g#s/../x");

        result = resolveToRfc3986Base("http:g");
        assertThat(result.toString()).isEqualTo("http:g");
    }

    Uri resolveToRfc3986Base(String relative) {
        String baseUri = "http://a/b/c/d;p?q";
        UriBuilder base = createUriBuilder(baseUri);

        UriBuilder reference = createUriBuilder(relative);
        Rfc3986ReferenceResolver resolver = new Rfc3986ReferenceResolver();
        resolver.resolve(base, reference);

        return base.build();
    }

    UriBuilder createUriBuilder(String uriString) {
        UriBuilderConfig config = Configurations.STRICT_URI.toBuilder()
                .schemeBasedNormalization(false).build();
        UriBuilder uriBuilder = UriBuilder.builder(config);
        Rfc3986Parser parser = new Rfc3986Parser();
        Rfc3986Parser.ParserState parserState = new Rfc3986Parser.ParserState(uriBuilder, uriString, 0);
        parser.parseScheme(parserState);
        parser.parseAuthority(parserState);
        parser.parsePath(parserState);
        parser.parseQuery(parserState);
        parser.parseFragment(parserState);
        return uriBuilder;
    }

}
