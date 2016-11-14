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

import org.junit.Test;
import org.netpreserve.commons.uri.Configurations;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriBuilder;

import static org.assertj.core.api.Assertions.*;
import static org.netpreserve.commons.uri.parser.UriAssert.assertThat;

/**
 *
 */
public class StrictUriTest {

    /**
     * Test of scheme method, of class Uri.
     */
    @Test
    public void testScheme() {
        Uri instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@example.com:123/foo#bar").build();
        assertThat(instance)
                .hasScheme("http");

        instance = UriBuilder.strictUriBuilder()
                .uri("//example.com/foo#bar").build();
        assertThat(instance)
                .hasScheme(null);

        instance = UriBuilder.strictUriBuilder()
                .uri("//example.com:123/foo#bar").build();
        assertThat(instance)
                .hasScheme(null)
                .hasAuthority("example.com:123")
                .hasPath("/foo")
                .hasFragment("bar");

        instance = UriBuilder.strictUriBuilder()
                .uri("/foo#bar").build();
        assertThat(instance)
                .hasScheme(null)
                .hasAuthority(null)
                .hasPath("/foo")
                .hasFragment("bar");

        instance = UriBuilder.strictUriBuilder()
                .uri("foo#bar").build();
        assertThat(instance)
                .hasScheme(null)
                .hasAuthority(null)
                .hasPath("foo")
                .hasFragment("bar");
    }

    /**
     * Test of authority methods, of class Uri.
     */
    @Test
    public void testAuthority() {
        Uri instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@example.com:123/foo#bar").build();
        assertThat(instance)
                .hasAuthority("john:doe@example.com:123")
                .hasUserinfo("john:doe")
                .hasHost("example.com")
                .hasDecodedHost("example.com")
                .hasPort(123)
                .hasDecodedPort(123);

        instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@127.0.0.1:123/foo#bar").build();
        assertThat(instance)
                .hasAuthority("john:doe@127.0.0.1:123")
                .hasUserinfo("john:doe")
                .hasHost("127.0.0.1")
                .hasDecodedHost("127.0.0.1")
                .hasPort(123)
                .hasDecodedPort(123);

        instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@127.0.0.1:80/foo#bar").build();
        assertThat(instance)
                .hasAuthority("john:doe@127.0.0.1")
                .hasUserinfo("john:doe")
                .hasHost("127.0.0.1")
                .hasDecodedHost("127.0.0.1")
                .hasPort(Uri.DEFAULT_PORT_MARKER)
                .hasDecodedPort(80);

        instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@127.0.0.1/foo#bar").build();
        assertThat(instance)
                .hasAuthority("john:doe@127.0.0.1")
                .hasUserinfo("john:doe")
                .hasHost("127.0.0.1")
                .hasDecodedHost("127.0.0.1")
                .hasPort(Uri.DEFAULT_PORT_MARKER)
                .hasDecodedPort(80);
    }

    /**
     * Test of path method, of class Uri.
     */
    @Test
    public void testPath() {
        Uri instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@example.com:123/foo#bar").build();
        assertThat(instance)
                .hasPath("/foo")
                .hasDecodedPath("/foo");
    }

    /**
     * Test of query method, of class Uri.
     */
    @Test
    public void testQuery() {
        Uri instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@example.com:123/foo?q#bar").build();
        assertThat(instance)
                .hasQuery("q");
    }

    /**
     * Test of fragment method, of class Uri.
     */
    @Test
    public void testFragment() {
        Uri instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@example.com:123/foo#bar").build();
        assertThat(instance)
                .hasFragment("bar");
    }

    /**
     * Test of isRegistryName method, of class Uri.
     */
    @Test
    public void testAddress() {
        Uri instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@example.com:123/foo?q#bar").build();
        assertThat(instance)
                .isRegistryName(true)
                .isIPv4address(false)
                .isIPv6reference(false);

        instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@127.0.0.1:123/foo#bar").build();
        assertThat(instance)
                .isRegistryName(false)
                .isIPv4address(true)
                .isIPv6reference(false);

        instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@[::1]:123/foo#bar").build();
        assertThat(instance)
                .isRegistryName(false)
                .isIPv4address(false)
                .isIPv6reference(true);

        instance = UriBuilder.strictUriBuilder()
                .uri("mailto:john.doe@example.com").build();
        assertThat(instance)
                .isRegistryName(false)
                .isIPv4address(false)
                .isIPv6reference(false);
    }

    /**
     * Test of isAbsolute method, of class Uri.
     */
    @Test
    public void testIsAbsolute() {
        Uri instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@example.com:123/foo#bar").build();
        assertThat(instance)
                .isAbsolute(true);

        instance = UriBuilder.strictUriBuilder()
                .uri("//example.com/foo#bar").build();
        assertThat(instance)
                .isAbsolute(false);

        instance = UriBuilder.strictUriBuilder()
                .uri("foo#bar").build();
        assertThat(instance)
                .isAbsolute(false);

        instance = UriBuilder.strictUriBuilder()
                .uri("mailto:john.doe@example.com").build();
        assertThat(instance)
                .isAbsolute(true);
    }

    /**
     * Test of isAbsolutePath method, of class Uri.
     */
    @Test
    public void testIsAbsolutePath() {
        Uri instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@example.com:123/foo#bar").build();
        assertThat(instance)
                .isAbsolutePath(true);

        instance = UriBuilder.strictUriBuilder()
                .uri("//example.com/foo#bar").build();
        assertThat(instance)
                .isAbsolutePath(true);

        instance = UriBuilder.strictUriBuilder()
                .uri("foo#bar").build();
        assertThat(instance)
                .isAbsolutePath(false);

        instance = UriBuilder.strictUriBuilder()
                .uri("mailto:john.doe@example.com").build();
        assertThat(instance)
                .isAbsolutePath(false);
    }

    /**
     * Test of toString method, of class Uri.
     */
    @Test
    public void testToString() {
        Uri instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@example.com:123/foo#bar").build();
        assertThat(instance)
                .hasToString("http://john:doe@example.com:123/foo#bar");

        instance = UriBuilder.strictUriBuilder().uri("http://john:doe@127.0.0.1:123/foo#bar")
                .build();
        assertThat(instance)
                .hasToString("http://john:doe@127.0.0.1:123/foo#bar");
    }

    /**
     * Test of toCustomString method, of class Uri.
     */
    @Test
    public void testToCustomString() {
        Uri instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@example.com:123/foo#bar").build();
        assertThat(instance)
                .usingCustomUriFormat(Configurations.DEFAULT_FORMAT)
                .hasToCustomString("http://john:doe@example.com:123/foo#bar")
                .usingCustomUriFormat(Configurations.USABLE_URI_FORMAT)
                .hasToCustomString("http://john:doe@example.com:123/foo")
                .usingCustomUriFormat(Configurations.CANONICALIZED_URI_FORMAT)
                .hasToCustomString("http://example.com:123/foo")
                .usingCustomUriFormat(Configurations.SURT_KEY_FORMAT)
                .hasToCustomString("(com,example,:123)/foo");

        instance = UriBuilder.strictUriBuilder().uri("http://john:doe@127.0.0.1:123/foo#bar")
                .build();
        assertThat(instance)
                .usingCustomUriFormat(Configurations.DEFAULT_FORMAT)
                .hasToCustomString("http://john:doe@127.0.0.1:123/foo#bar")
                .usingCustomUriFormat(Configurations.USABLE_URI_FORMAT)
                .hasToCustomString("http://john:doe@127.0.0.1:123/foo")
                .usingCustomUriFormat(Configurations.CANONICALIZED_URI_FORMAT)
                .hasToCustomString("http://127.0.0.1:123/foo")
                .usingCustomUriFormat(Configurations.SURT_KEY_FORMAT)
                .hasToCustomString("(127.0.0.1:123)/foo");
    }

    /**
     * Test of toDecodedString method, of class Uri.
     */
    @Test
    public void testToDecodedString() {
        Uri instance = UriBuilder.strictUriBuilder()
                .uri("http://john:doe@example.com:123/fo%20o#bar").build();
        assertThat(instance)
                .hasToString("http://john:doe@example.com:123/fo%20o#bar")
                .hasToDecodedString("http://john:doe@example.com:123/fo o#bar");

        instance = UriBuilder.strictUriBuilder()
                .uri("http://xn--rksmrgs-5wao1o.josefßon.org/foo#bar").build();
        assertThat(instance)
                .hasToString("http://xn--rksmrgs-5wao1o.josefsson.org/foo#bar")
                .hasToDecodedString("http://räksmörgås.josefsson.org/foo#bar");
    }

}
