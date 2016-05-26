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
package org.netpreserve.commons.uri;

import org.junit.Test;
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
        assertThat(parserState.builder.scheme).isEqualTo("mailto");
        assertThat(parserState.offset).isEqualTo(7);
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.authority).isNull();
        assertThat(parserState.builder.userinfo).isNull();
        assertThat(parserState.builder.host).isNull();
        assertThat(parserState.builder.port).isEqualTo(-1);
        assertThat(parserState.offset).isEqualTo(7);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path).isEqualTo("java-net@java.sun.com");
        assertThat(parserState.offset).isEqualTo(28);
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query).isNull();
        assertThat(parserState.offset).isEqualTo(28);
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment).isNull();
        assertThat(parserState.offset).isEqualTo(28);

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "news:comp.lang.java#fragment", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme).isEqualTo("news");
        assertThat(parserState.offset).isEqualTo(5);
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.authority).isNull();
        assertThat(parserState.builder.userinfo).isNull();
        assertThat(parserState.builder.host).isNull();
        assertThat(parserState.builder.port).isEqualTo(-1);
        assertThat(parserState.offset).isEqualTo(5);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path).isEqualTo("comp.lang.java");
        assertThat(parserState.offset).isEqualTo(19);
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query).isNull();
        assertThat(parserState.offset).isEqualTo(19);
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment).isEqualTo("fragment");
        assertThat(parserState.offset).isEqualTo(28);

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "urn:isbn:096139210x", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme).isEqualTo("urn");
        assertThat(parserState.offset).isEqualTo(4);
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.authority).isNull();
        assertThat(parserState.builder.userinfo).isNull();
        assertThat(parserState.builder.host).isNull();
        assertThat(parserState.builder.port).isEqualTo(-1);
        assertThat(parserState.offset).isEqualTo(4);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path).isEqualTo("isbn:096139210x");
        assertThat(parserState.offset).isEqualTo(19);
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query).isNull();
        assertThat(parserState.offset).isEqualTo(19);
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment).isNull();
        assertThat(parserState.offset).isEqualTo(19);

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "http://java.sun.com/j2se/1.3/", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme).isEqualTo("http");
        assertThat(parserState.offset).isEqualTo(5);
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.authority).isEqualTo("java.sun.com");
        parser.decomposeAuthority(parserState.builder, parserState.builder.authority);
        assertThat(parserState.builder.userinfo).isNull();
        assertThat(parserState.builder.host).isEqualTo("java.sun.com");
        assertThat(parserState.builder.port).isEqualTo(-1);
        assertThat(parserState.offset).isEqualTo(19);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path).isEqualTo("/j2se/1.3/");
        assertThat(parserState.offset).isEqualTo(29);
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query).isNull();
        assertThat(parserState.offset).isEqualTo(29);
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment).isNull();
        assertThat(parserState.offset).isEqualTo(29);

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "http://java.sun.com/j2se/1.3/?#", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme).isEqualTo("http");
        assertThat(parserState.offset).isEqualTo(5);
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.authority).isEqualTo("java.sun.com");
        parser.decomposeAuthority(parserState.builder, parserState.builder.authority);
        assertThat(parserState.builder.userinfo).isNull();
        assertThat(parserState.builder.host).isEqualTo("java.sun.com");
        assertThat(parserState.builder.port).isEqualTo(-1);
        assertThat(parserState.offset).isEqualTo(19);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path).isEqualTo("/j2se/1.3/");
        assertThat(parserState.offset).isEqualTo(29);
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query).isEmpty();
        assertThat(parserState.offset).isEqualTo(30);
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment).isEmpty();
        assertThat(parserState.offset).isEqualTo(31);

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "http://user@java.sun.com:80/j2se/1.3/", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme).isEqualTo("http");
        assertThat(parserState.offset).isEqualTo(5);
        parser.parseAuthority(parserState);
        parser.decomposeAuthority(parserState.builder, parserState.builder.authority);
        assertThat(parserState.builder.authority).isEqualTo("user@java.sun.com:80");
        assertThat(parserState.builder.userinfo).isEqualTo("user");
        assertThat(parserState.builder.host).isEqualTo("java.sun.com");
        assertThat(parserState.builder.port).isEqualTo(80);
        assertThat(parserState.offset).isEqualTo(27);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path).isEqualTo("/j2se/1.3/");
        assertThat(parserState.offset).isEqualTo(37);
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query).isNull();
        assertThat(parserState.offset).isEqualTo(37);
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment).isNull();
        assertThat(parserState.offset).isEqualTo(37);

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "docs/guide/collections/designfaq.html#28", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme).isNull();
        assertThat(parserState.offset).isEqualTo(0);
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.authority).isNull();
        assertThat(parserState.builder.userinfo).isNull();
        assertThat(parserState.builder.host).isNull();
        assertThat(parserState.builder.port).isEqualTo(-1);
        assertThat(parserState.offset).isEqualTo(0);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path).isEqualTo("docs/guide/collections/designfaq.html");
        assertThat(parserState.offset).isEqualTo(37);
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query).isNull();
        assertThat(parserState.offset).isEqualTo(37);
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment).isEqualTo("28");
        assertThat(parserState.offset).isEqualTo(40);

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "../../../demo/jfc/SwingSet2/src/SwingSet2.java", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme).isNull();
        assertThat(parserState.offset).isEqualTo(0);
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.authority).isNull();
        assertThat(parserState.builder.userinfo).isNull();
        assertThat(parserState.builder.host).isNull();
        assertThat(parserState.builder.port).isEqualTo(-1);
        assertThat(parserState.offset).isEqualTo(0);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path).isEqualTo("../../../demo/jfc/SwingSet2/src/SwingSet2.java");
        assertThat(parserState.offset).isEqualTo(46);
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query).isNull();
        assertThat(parserState.offset).isEqualTo(46);
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment).isNull();
        assertThat(parserState.offset).isEqualTo(46);

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "file:///~/calendar", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme).isEqualTo("file");
        assertThat(parserState.offset).isEqualTo(5);
        parser.parseAuthority(parserState);
        parser.decomposeAuthority(parserState.builder, parserState.builder.authority);
        assertThat(parserState.builder.authority).isEmpty();
        assertThat(parserState.builder.userinfo).isNull();
        assertThat(parserState.builder.host).isEmpty();
        assertThat(parserState.builder.port).isEqualTo(-1);
        assertThat(parserState.offset).isEqualTo(7);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path).isEqualTo("/~/calendar");
        assertThat(parserState.offset).isEqualTo(18);
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query).isNull();
        assertThat(parserState.offset).isEqualTo(18);
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment).isNull();
        assertThat(parserState.offset).isEqualTo(18);

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), ":foo", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme).isNull();
        assertThat(parserState.offset).isEqualTo(0);
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.authority).isNull();
        assertThat(parserState.builder.userinfo).isNull();
        assertThat(parserState.builder.host).isNull();
        assertThat(parserState.builder.port).isEqualTo(-1);
        assertThat(parserState.offset).isEqualTo(0);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path).isEqualTo(":foo");
        assertThat(parserState.offset).isEqualTo(4);
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query).isNull();
        assertThat(parserState.offset).isEqualTo(4);
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment).isNull();
        assertThat(parserState.offset).isEqualTo(4);

        parserState = new Rfc3986Parser.ParserState(UriBuilder.builder(config), "example.html;jsessionid=deadbeef:deadbeed?parameter=this:value", 0);
        parser.parseScheme(parserState);
        assertThat(parserState.builder.scheme).isNull();
        assertThat(parserState.offset).isEqualTo(0);
        parser.parseAuthority(parserState);
        assertThat(parserState.builder.authority).isNull();
        assertThat(parserState.builder.userinfo).isNull();
        assertThat(parserState.builder.host).isNull();
        assertThat(parserState.builder.port).isEqualTo(-1);
        assertThat(parserState.offset).isEqualTo(0);
        parser.parsePath(parserState);
        assertThat(parserState.builder.path).isEqualTo("example.html;jsessionid=deadbeef:deadbeed");
        assertThat(parserState.offset).isEqualTo(41);
        parser.parseQuery(parserState);
        assertThat(parserState.builder.query).isEqualTo("parameter=this:value");
        assertThat(parserState.offset).isEqualTo(62);
        parser.parseFragment(parserState);
        assertThat(parserState.builder.fragment).isNull();
        assertThat(parserState.offset).isEqualTo(62);
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