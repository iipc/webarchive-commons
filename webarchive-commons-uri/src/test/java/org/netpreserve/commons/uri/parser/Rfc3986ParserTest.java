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
public class Rfc3986ParserTest {

    /**
     * Test of parseScheme method, of class Rfc3986Parser.
     */
    @Test
    public void testParseParts() {
        UriBuilderConfig config = Configurations.STRICT_URI.toBuilder()
                .schemeBasedNormalization(false).build();

        Rfc3986Parser parser = new Rfc3986Parser();
        Rfc3986Parser.ParserState parserState;

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "mailto:java-net@java.sun.com", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme()).isEqualTo("mailto");
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.user()).isNull();
        assertThat(parserState.builder.password()).isNull();
        assertThat(parserState.builder.host()).isNull();
        assertThat(parserState.builder.port()).isEqualTo(Uri.DEFAULT_PORT_MARKER);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path()).isEqualTo("java-net@java.sun.com");
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query()).isNull();
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment()).isNull();

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "news:comp.lang.java#fragment", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme()).isEqualTo("news");
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.user()).isNull();
        assertThat(parserState.builder.password()).isNull();
        assertThat(parserState.builder.host()).isNull();
        assertThat(parserState.builder.port()).isEqualTo(Uri.DEFAULT_PORT_MARKER);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path()).isEqualTo("comp.lang.java");
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query()).isNull();
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment()).isEqualTo("fragment");

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "urn:isbn:096139210x", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme()).isEqualTo("urn");
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.user()).isNull();
        assertThat(parserState.builder.password()).isNull();
        assertThat(parserState.builder.host()).isNull();
        assertThat(parserState.builder.port()).isEqualTo(Uri.DEFAULT_PORT_MARKER);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path()).isEqualTo("isbn:096139210x");
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query()).isNull();
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment()).isNull();

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "http://java.sun.com/j2se/1.3/", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme()).isEqualTo("http");
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.user()).isNull();
        assertThat(parserState.builder.password()).isNull();
        assertThat(parserState.builder.host()).isEqualTo("java.sun.com");
        assertThat(parserState.builder.port()).isEqualTo(Uri.DEFAULT_PORT_MARKER);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path()).isEqualTo("/j2se/1.3/");
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query()).isNull();
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment()).isNull();

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "http://java.sun.com/j2se/1.3/?#", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme()).isEqualTo("http");
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.user()).isNull();
        assertThat(parserState.builder.password()).isNull();
        assertThat(parserState.builder.host()).isEqualTo("java.sun.com");
        assertThat(parserState.builder.port()).isEqualTo(Uri.DEFAULT_PORT_MARKER);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path()).isEqualTo("/j2se/1.3/");
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query()).isEmpty();
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment()).isEmpty();

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "http://user@java.sun.com:80/j2se/1.3/", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme()).isEqualTo("http");
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.user()).isEqualTo("user");
        assertThat(parserState.builder.password()).isNull();
        assertThat(parserState.builder.host()).isEqualTo("java.sun.com");
        assertThat(parserState.builder.port()).isEqualTo(80);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path()).isEqualTo("/j2se/1.3/");
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query()).isNull();
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment()).isNull();

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "docs/guide/collections/designfaq.html#28", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme()).isNull();
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.user()).isNull();
        assertThat(parserState.builder.password()).isNull();
        assertThat(parserState.builder.host()).isNull();
        assertThat(parserState.builder.port()).isEqualTo(Uri.DEFAULT_PORT_MARKER);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path()).isEqualTo("docs/guide/collections/designfaq.html");
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query()).isNull();
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment()).isEqualTo("28");

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "../../../demo/jfc/SwingSet2/src/SwingSet2.java", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme()).isNull();
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.user()).isNull();
        assertThat(parserState.builder.password()).isNull();
        assertThat(parserState.builder.host()).isNull();
        assertThat(parserState.builder.port()).isEqualTo(Uri.DEFAULT_PORT_MARKER);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path()).isEqualTo("../../../demo/jfc/SwingSet2/src/SwingSet2.java");
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query()).isNull();
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment()).isNull();

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "file:///~/calendar", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme()).isEqualTo("file");
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.user()).isNull();
        assertThat(parserState.builder.password()).isNull();
        assertThat(parserState.builder.host()).isEmpty();
        assertThat(parserState.builder.port()).isEqualTo(Uri.DEFAULT_PORT_MARKER);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path()).isEqualTo("/~/calendar");
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query()).isNull();
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment()).isNull();

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), ":foo", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme()).isNull();
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.user()).isNull();
        assertThat(parserState.builder.password()).isNull();
        assertThat(parserState.builder.host()).isNull();
        assertThat(parserState.builder.port()).isEqualTo(Uri.DEFAULT_PORT_MARKER);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path()).isEqualTo(":foo");
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query()).isNull();
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment()).isNull();

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "example.html;jsessionid=deadbeef:deadbeed?parameter=this:value", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme()).isNull();
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.user()).isNull();
        assertThat(parserState.builder.password()).isNull();
        assertThat(parserState.builder.host()).isNull();
        assertThat(parserState.builder.port()).isEqualTo(Uri.DEFAULT_PORT_MARKER);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path()).isEqualTo("example.html;jsessionid=deadbeef:deadbeed");
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query()).isEqualTo("parameter=this:value");
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment()).isNull();
    }

    @Test
    public void testNormalization() {
        UriBuilder builder = UriBuilder.strictUriBuilder();
        Rfc3986Parser parser = new Rfc3986Parser();
        String uri;
        int nextOffset;

        uri = "example://a/b/c/%7Bfoo%7D";
        parser.parseUri(builder, uri, 0);

        Uri uri1 = builder.build();

        uri = "eXAMPLE://a/./b/../b/%63/%7bfoo%7d";
        parser.parseUri(builder, uri, 0);

        Uri uri2 = builder.build();

        assertThat(uri1).isEqualTo(uri2);
    }
}