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
        UriBuilder builder = UriBuilder.builder(config);

        Rfc3986Parser parser = new Rfc3986Parser();
        String uri;
        int offset = 0;
        int nextOffset;

        uri = "mailto:java-net@java.sun.com";
        nextOffset = parser.parseScheme(builder, uri, offset);
        assertThat(builder.scheme).isEqualTo("mailto");
        assertThat(nextOffset).isEqualTo(7);
        nextOffset = parser.parseAuthority(builder, uri, nextOffset);
        assertThat(builder.authority).isNull();
        assertThat(builder.userinfo).isNull();
        assertThat(builder.host).isNull();
        assertThat(builder.port).isEqualTo(-1);
        assertThat(nextOffset).isEqualTo(7);
        nextOffset = parser.parsePath(builder, uri, nextOffset);
        assertThat(builder.path).isEqualTo("java-net@java.sun.com");
        assertThat(nextOffset).isEqualTo(28);
        nextOffset = parser.parseQuery(builder, uri, nextOffset);
        assertThat(builder.query).isNull();
        assertThat(nextOffset).isEqualTo(28);
        nextOffset = parser.parseFragment(builder, uri, nextOffset);
        assertThat(builder.fragment).isNull();
        assertThat(nextOffset).isEqualTo(28);

        uri = "news:comp.lang.java#fragment";
        nextOffset = parser.parseScheme(builder, uri, offset);
        assertThat(builder.scheme).isEqualTo("news");
        assertThat(nextOffset).isEqualTo(5);
        nextOffset = parser.parseAuthority(builder, uri, nextOffset);
        assertThat(builder.authority).isNull();
        assertThat(builder.userinfo).isNull();
        assertThat(builder.host).isNull();
        assertThat(builder.port).isEqualTo(-1);
        assertThat(nextOffset).isEqualTo(5);
        nextOffset = parser.parsePath(builder, uri, nextOffset);
        assertThat(builder.path).isEqualTo("comp.lang.java");
        assertThat(nextOffset).isEqualTo(19);
        nextOffset = parser.parseQuery(builder, uri, nextOffset);
        assertThat(builder.query).isNull();
        assertThat(nextOffset).isEqualTo(19);
        nextOffset = parser.parseFragment(builder, uri, nextOffset);
        assertThat(builder.fragment).isEqualTo("fragment");
        assertThat(nextOffset).isEqualTo(28);

        uri = "urn:isbn:096139210x";
        nextOffset = parser.parseScheme(builder, uri, offset);
        assertThat(builder.scheme).isEqualTo("urn");
        assertThat(nextOffset).isEqualTo(4);
        nextOffset = parser.parseAuthority(builder, uri, nextOffset);
        assertThat(builder.authority).isNull();
        assertThat(builder.userinfo).isNull();
        assertThat(builder.host).isNull();
        assertThat(builder.port).isEqualTo(-1);
        assertThat(nextOffset).isEqualTo(4);
        nextOffset = parser.parsePath(builder, uri, nextOffset);
        assertThat(builder.path).isEqualTo("isbn:096139210x");
        assertThat(nextOffset).isEqualTo(19);
        nextOffset = parser.parseQuery(builder, uri, nextOffset);
        assertThat(builder.query).isNull();
        assertThat(nextOffset).isEqualTo(19);
        nextOffset = parser.parseFragment(builder, uri, nextOffset);
        assertThat(builder.fragment).isNull();
        assertThat(nextOffset).isEqualTo(19);

        uri = "http://java.sun.com/j2se/1.3/";
        nextOffset = parser.parseScheme(builder, uri, offset);
        assertThat(builder.scheme).isEqualTo("http");
        assertThat(nextOffset).isEqualTo(5);
        nextOffset = parser.parseAuthority(builder, uri, nextOffset);
        assertThat(builder.authority).isEqualTo("java.sun.com");
        assertThat(builder.userinfo).isNull();
        assertThat(builder.host).isEqualTo("java.sun.com");
        assertThat(builder.port).isEqualTo(-1);
        assertThat(nextOffset).isEqualTo(19);
        nextOffset = parser.parsePath(builder, uri, nextOffset);
        assertThat(builder.path).isEqualTo("/j2se/1.3/");
        assertThat(nextOffset).isEqualTo(29);
        nextOffset = parser.parseQuery(builder, uri, nextOffset);
        assertThat(builder.query).isNull();
        assertThat(nextOffset).isEqualTo(29);
        nextOffset = parser.parseFragment(builder, uri, nextOffset);
        assertThat(builder.fragment).isNull();
        assertThat(nextOffset).isEqualTo(29);

        uri = "http://java.sun.com/j2se/1.3/?#";
        nextOffset = parser.parseScheme(builder, uri, offset);
        assertThat(builder.scheme).isEqualTo("http");
        assertThat(nextOffset).isEqualTo(5);
        nextOffset = parser.parseAuthority(builder, uri, nextOffset);
        assertThat(builder.authority).isEqualTo("java.sun.com");
        assertThat(builder.userinfo).isNull();
        assertThat(builder.host).isEqualTo("java.sun.com");
        assertThat(builder.port).isEqualTo(-1);
        assertThat(nextOffset).isEqualTo(19);
        nextOffset = parser.parsePath(builder, uri, nextOffset);
        assertThat(builder.path).isEqualTo("/j2se/1.3/");
        assertThat(nextOffset).isEqualTo(29);
        nextOffset = parser.parseQuery(builder, uri, nextOffset);
        assertThat(builder.query).isEmpty();
        assertThat(nextOffset).isEqualTo(30);
        nextOffset = parser.parseFragment(builder, uri, nextOffset);
        assertThat(builder.fragment).isEmpty();
        assertThat(nextOffset).isEqualTo(31);

        uri = "http://user@java.sun.com:80/j2se/1.3/";
        nextOffset = parser.parseScheme(builder, uri, offset);
        assertThat(builder.scheme).isEqualTo("http");
        assertThat(nextOffset).isEqualTo(5);
        nextOffset = parser.parseAuthority(builder, uri, nextOffset);
        assertThat(builder.authority).isEqualTo("user@java.sun.com:80");
        assertThat(builder.userinfo).isEqualTo("user");
        assertThat(builder.host).isEqualTo("java.sun.com");
        assertThat(builder.port).isEqualTo(80);
        assertThat(nextOffset).isEqualTo(27);
        nextOffset = parser.parsePath(builder, uri, nextOffset);
        assertThat(builder.path).isEqualTo("/j2se/1.3/");
        assertThat(nextOffset).isEqualTo(37);
        nextOffset = parser.parseQuery(builder, uri, nextOffset);
        assertThat(builder.query).isNull();
        assertThat(nextOffset).isEqualTo(37);
        nextOffset = parser.parseFragment(builder, uri, nextOffset);
        assertThat(builder.fragment).isNull();
        assertThat(nextOffset).isEqualTo(37);

        uri = "docs/guide/collections/designfaq.html#28";
        nextOffset = parser.parseScheme(builder, uri, offset);
        assertThat(builder.scheme).isNull();
        assertThat(nextOffset).isEqualTo(0);
        nextOffset = parser.parseAuthority(builder, uri, nextOffset);
        assertThat(builder.authority).isNull();
        assertThat(builder.userinfo).isNull();
        assertThat(builder.host).isNull();
        assertThat(builder.port).isEqualTo(-1);
        assertThat(nextOffset).isEqualTo(0);
        nextOffset = parser.parsePath(builder, uri, nextOffset);
        assertThat(builder.path).isEqualTo("docs/guide/collections/designfaq.html");
        assertThat(nextOffset).isEqualTo(37);
        nextOffset = parser.parseQuery(builder, uri, nextOffset);
        assertThat(builder.query).isNull();
        assertThat(nextOffset).isEqualTo(37);
        nextOffset = parser.parseFragment(builder, uri, nextOffset);
        assertThat(builder.fragment).isEqualTo("28");
        assertThat(nextOffset).isEqualTo(40);

        uri = "../../../demo/jfc/SwingSet2/src/SwingSet2.java";
        nextOffset = parser.parseScheme(builder, uri, offset);
        assertThat(builder.scheme).isNull();
        assertThat(nextOffset).isEqualTo(0);
        nextOffset = parser.parseAuthority(builder, uri, nextOffset);
        assertThat(builder.authority).isNull();
        assertThat(builder.userinfo).isNull();
        assertThat(builder.host).isNull();
        assertThat(builder.port).isEqualTo(-1);
        assertThat(nextOffset).isEqualTo(0);
        nextOffset = parser.parsePath(builder, uri, nextOffset);
        assertThat(builder.path).isEqualTo("../../../demo/jfc/SwingSet2/src/SwingSet2.java");
        assertThat(nextOffset).isEqualTo(46);
        nextOffset = parser.parseQuery(builder, uri, nextOffset);
        assertThat(builder.query).isNull();
        assertThat(nextOffset).isEqualTo(46);
        nextOffset = parser.parseFragment(builder, uri, nextOffset);
        assertThat(builder.fragment).isNull();
        assertThat(nextOffset).isEqualTo(46);

        uri = "file:///~/calendar";
        nextOffset = parser.parseScheme(builder, uri, offset);
        assertThat(builder.scheme).isEqualTo("file");
        assertThat(nextOffset).isEqualTo(5);
        nextOffset = parser.parseAuthority(builder, uri, nextOffset);
        assertThat(builder.authority).isEmpty();
        assertThat(builder.userinfo).isNull();
        assertThat(builder.host).isEmpty();
        assertThat(builder.port).isEqualTo(-1);
        assertThat(nextOffset).isEqualTo(7);
        nextOffset = parser.parsePath(builder, uri, nextOffset);
        assertThat(builder.path).isEqualTo("/~/calendar");
        assertThat(nextOffset).isEqualTo(18);
        nextOffset = parser.parseQuery(builder, uri, nextOffset);
        assertThat(builder.query).isNull();
        assertThat(nextOffset).isEqualTo(18);
        nextOffset = parser.parseFragment(builder, uri, nextOffset);
        assertThat(builder.fragment).isNull();
        assertThat(nextOffset).isEqualTo(18);

        uri = ":foo";
        nextOffset = parser.parseScheme(builder, uri, offset);
        assertThat(builder.scheme).isNull();
        assertThat(nextOffset).isEqualTo(0);
        nextOffset = parser.parseAuthority(builder, uri, nextOffset);
        assertThat(builder.authority).isNull();
        assertThat(builder.userinfo).isNull();
        assertThat(builder.host).isNull();
        assertThat(builder.port).isEqualTo(-1);
        assertThat(nextOffset).isEqualTo(0);
        nextOffset = parser.parsePath(builder, uri, nextOffset);
        assertThat(builder.path).isEqualTo(":foo");
        assertThat(nextOffset).isEqualTo(4);
        nextOffset = parser.parseQuery(builder, uri, nextOffset);
        assertThat(builder.query).isNull();
        assertThat(nextOffset).isEqualTo(4);
        nextOffset = parser.parseFragment(builder, uri, nextOffset);
        assertThat(builder.fragment).isNull();
        assertThat(nextOffset).isEqualTo(4);

        uri = "example.html;jsessionid=deadbeef:deadbeed?parameter=this:value";
        nextOffset = parser.parseScheme(builder, uri, offset);
        assertThat(builder.scheme).isNull();
        assertThat(nextOffset).isEqualTo(0);
        nextOffset = parser.parseAuthority(builder, uri, nextOffset);
        assertThat(builder.authority).isNull();
        assertThat(builder.userinfo).isNull();
        assertThat(builder.host).isNull();
        assertThat(builder.port).isEqualTo(-1);
        assertThat(nextOffset).isEqualTo(0);
        nextOffset = parser.parsePath(builder, uri, nextOffset);
        assertThat(builder.path).isEqualTo("example.html;jsessionid=deadbeef:deadbeed");
        assertThat(nextOffset).isEqualTo(41);
        nextOffset = parser.parseQuery(builder, uri, nextOffset);
        assertThat(builder.query).isEqualTo("parameter=this:value");
        assertThat(nextOffset).isEqualTo(62);
        nextOffset = parser.parseFragment(builder, uri, nextOffset);
        assertThat(builder.fragment).isNull();
        assertThat(nextOffset).isEqualTo(62);
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