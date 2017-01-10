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
import org.netpreserve.commons.uri.UriConfigs;
import org.netpreserve.commons.uri.Uri;

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
        Uri instance = UriConfigs.STRICT.buildUri("http://john:doe@example.com:123/foo#bar");
        assertThat(instance)
                .hasScheme("http");

        instance = UriConfigs.STRICT.buildUri("//example.com/foo#bar");
        assertThat(instance)
                .hasScheme(null);

        instance = UriConfigs.STRICT.buildUri("//example.com:123/foo#bar");
        assertThat(instance)
                .hasScheme(null)
                .hasAuthority("example.com:123")
                .hasPath("/foo")
                .hasFragment("bar");

        instance = UriConfigs.STRICT.buildUri("/foo#bar");
        assertThat(instance)
                .hasScheme(null)
                .hasAuthority(null)
                .hasPath("/foo")
                .hasFragment("bar");

        instance = UriConfigs.STRICT.buildUri("foo#bar");
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
        Uri instance = UriConfigs.STRICT.buildUri("http://john:doe@example.com:123/foo#bar");
        assertThat(instance)
                .hasAuthority("john:doe@example.com:123")
                .hasUserinfo("john:doe")
                .hasHost("example.com")
                .hasDecodedHost("example.com")
                .hasPort(123)
                .hasDecodedPort(123);

        instance = UriConfigs.STRICT.buildUri("http://john:doe@127.0.0.1:123/foo#bar");
        assertThat(instance)
                .hasAuthority("john:doe@127.0.0.1:123")
                .hasUserinfo("john:doe")
                .hasHost("127.0.0.1")
                .hasDecodedHost("127.0.0.1")
                .hasPort(123)
                .hasDecodedPort(123);

        instance = UriConfigs.STRICT.buildUri("http://john:doe@127.0.0.1:80/foo#bar");
        assertThat(instance)
                .hasAuthority("john:doe@127.0.0.1")
                .hasUserinfo("john:doe")
                .hasHost("127.0.0.1")
                .hasDecodedHost("127.0.0.1")
                .hasPort(Uri.DEFAULT_PORT_MARKER)
                .hasDecodedPort(80);

        instance = UriConfigs.STRICT.buildUri("http://john:doe@127.0.0.1/foo#bar");
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
        Uri instance = UriConfigs.STRICT.buildUri("http://john:doe@example.com:123/foo#bar");
        assertThat(instance)
                .hasPath("/foo")
                .hasDecodedPath("/foo");
    }

    /**
     * Test of query method, of class Uri.
     */
    @Test
    public void testQuery() {
        Uri instance = UriConfigs.STRICT.buildUri("http://john:doe@example.com:123/foo?q#bar");
        assertThat(instance)
                .hasQuery("q");
    }

    /**
     * Test of fragment method, of class Uri.
     */
    @Test
    public void testFragment() {
        Uri instance = UriConfigs.STRICT.buildUri("http://john:doe@example.com:123/foo#bar");
        assertThat(instance)
                .hasFragment("bar");
    }

    /**
     * Test of isRegistryName method, of class Uri.
     */
    @Test
    public void testAddress() {
        Uri instance = UriConfigs.STRICT.buildUri("http://john:doe@example.com:123/foo?q#bar");
        assertThat(instance)
                .isRegistryName(true)
                .isIPv4address(false)
                .isIPv6reference(false);

        instance = UriConfigs.STRICT.buildUri("http://john:doe@127.0.0.1:123/foo#bar");
        assertThat(instance)
                .isRegistryName(false)
                .isIPv4address(true)
                .isIPv6reference(false);

        instance = UriConfigs.STRICT.buildUri("http://john:doe@[::1]:123/foo#bar");
        assertThat(instance)
                .isRegistryName(false)
                .isIPv4address(false)
                .isIPv6reference(true);

        instance = UriConfigs.STRICT.buildUri("mailto:john.doe@example.com");
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
        Uri instance = UriConfigs.STRICT.buildUri("http://john:doe@example.com:123/foo#bar");
        assertThat(instance)
                .isAbsolute(true);

        instance = UriConfigs.STRICT.buildUri("//example.com/foo#bar");
        assertThat(instance)
                .isAbsolute(false);

        instance = UriConfigs.STRICT.buildUri("foo#bar");
        assertThat(instance)
                .isAbsolute(false);

        instance = UriConfigs.STRICT.buildUri("mailto:john.doe@example.com");
        assertThat(instance)
                .isAbsolute(true);
    }

    /**
     * Test of isAbsolutePath method, of class Uri.
     */
    @Test
    public void testIsAbsolutePath() {
        Uri instance = UriConfigs.STRICT.buildUri("http://john:doe@example.com:123/foo#bar");
        assertThat(instance)
                .isAbsolutePath(true);

        instance = UriConfigs.STRICT.buildUri("//example.com/foo#bar");
        assertThat(instance)
                .isAbsolutePath(true);

        instance = UriConfigs.STRICT.buildUri("foo#bar");
        assertThat(instance)
                .isAbsolutePath(false);

        instance = UriConfigs.STRICT.buildUri("mailto:john.doe@example.com");
        assertThat(instance)
                .isAbsolutePath(false);
    }

    /**
     * Test of toString method, of class Uri.
     */
    @Test
    public void testToString() {
        Uri instance = UriConfigs.STRICT.buildUri("http://john:doe@example.com:123/foo#bar");
        assertThat(instance)
                .hasToString("http://john:doe@example.com:123/foo#bar");

        instance = UriConfigs.STRICT.buildUri("http://john:doe@127.0.0.1:123/foo#bar");
        assertThat(instance)
                .hasToString("http://john:doe@127.0.0.1:123/foo#bar");
    }

    /**
     * Test of toCustomString method, of class Uri.
     */
    @Test
    public void testToCustomString() {
        Uri instance = UriConfigs.STRICT.buildUri("http://john:doe@example.com:123/foo#bar");
        assertThat(instance)
                .usingCustomUriFormat(UriConfigs.DEFAULT_FORMAT)
                .hasToCustomString("http://john:doe@example.com:123/foo#bar")
                .usingCustomUriFormat(UriConfigs.HERITRIX_URI_FORMAT)
                .hasToCustomString("http://john:doe@example.com:123/foo")
                .usingCustomUriFormat(UriConfigs.CANONICALIZED_URI_FORMAT)
                .hasToCustomString("http://example.com:123/foo")
                .usingCustomUriFormat(UriConfigs.SURT_KEY_FORMAT)
                .hasToCustomString("(com,example,:123)/foo");

        instance = UriConfigs.STRICT.buildUri("http://john:doe@127.0.0.1:123/foo#bar");
        assertThat(instance)
                .usingCustomUriFormat(UriConfigs.DEFAULT_FORMAT)
                .hasToCustomString("http://john:doe@127.0.0.1:123/foo#bar")
                .usingCustomUriFormat(UriConfigs.HERITRIX_URI_FORMAT)
                .hasToCustomString("http://john:doe@127.0.0.1:123/foo")
                .usingCustomUriFormat(UriConfigs.CANONICALIZED_URI_FORMAT)
                .hasToCustomString("http://127.0.0.1:123/foo")
                .usingCustomUriFormat(UriConfigs.SURT_KEY_FORMAT)
                .hasToCustomString("(127.0.0.1:123)/foo");
    }

    /**
     * Test of toDecodedString method, of class Uri.
     */
    @Test
    public void testToDecodedString() {
        Uri instance = UriConfigs.STRICT.buildUri("http://john:doe@example.com:123/fo%20o#bar");
        assertThat(instance)
                .hasToString("http://john:doe@example.com:123/fo%20o#bar")
                .hasToDecodedString("http://john:doe@example.com:123/fo o#bar");

        instance = UriConfigs.STRICT.buildUri("http://xn--rksmrgs-5wao1o.josefßon.org/foo#bar");
        assertThat(instance)
                .hasToString("http://xn--rksmrgs-5wao1o.josefsson.org/foo#bar")
                .hasToDecodedString("http://räksmörgås.josefsson.org/foo#bar");
    }

}
