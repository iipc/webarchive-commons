/*
 *  This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.url;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UsableURITest {
    @Test
    public void testHasScheme() {
        assertTrue(UsableURI.hasScheme("http://www.archive.org"));
        assertTrue(UsableURI.hasScheme("http:"));
        assertFalse(UsableURI.hasScheme("ht/tp://www.archive.org"));
        assertFalse(UsableURI.hasScheme("/tmp"));
    }

    @Test
    public void testGetFileName() throws URISyntaxException {
        final String filename = "x.arc.gz";
        assertEquals(filename,
            UsableURI.parseFilename("/tmp/one.two/" + filename));
        assertEquals(filename,
            UsableURI.parseFilename("http://archive.org/tmp/one.two/" +
                    filename));
        assertEquals(filename,
            UsableURI.parseFilename("rsync://archive.org/tmp/one.two/" +
                    filename)); 
    }

    @Test
    public void testSchemalessRelative() throws URIException {
        UsableURI base = new UsableURI("http://www.archive.org/a", true, "UTF-8");
        UsableURI relative = new UsableURI("//www.facebook.com/?href=http://www.archive.org/a", true, "UTF-8");
        assertNull(relative.getScheme());
        assertEquals("www.facebook.com", relative.getAuthority());
        UsableURI test = new UsableURI(base, relative);
        assertEquals("http://www.facebook.com/?href=http://www.archive.org/a", test.toString());
    }

    /**
     * Test of toUnicodeHostString method, of class UsableURI.
     */
    @Test
    public void testToUnicodeHostString() throws URIException {
        assertEquals("http://øx.dk", new UsableURI("http://xn--x-4ga.dk", true, "UTF-8").toUnicodeHostString());
        assertEquals("xn--x-4ga.dk", new UsableURI("xn--x-4ga.dk", true, "UTF-8").toUnicodeHostString());
        assertEquals("http://user:pass@øx.dk:8080", new UsableURI("http://user:pass@xn--x-4ga.dk:8080", true, "UTF-8").toUnicodeHostString());
        assertEquals("http://user@øx.dk:8080", new UsableURI("http://user@xn--x-4ga.dk:8080", true, "UTF-8").toUnicodeHostString());
        assertEquals("http://øx.dk/foo/bar?query=q", new UsableURI("http://xn--x-4ga.dk/foo/bar?query=q", true, "UTF-8").toUnicodeHostString());
        assertEquals("http://127.0.0.1/foo/bar?query=q", new UsableURI("http://127.0.0.1/foo/bar?query=q", true, "UTF-8").toUnicodeHostString());

        // test idn round trip
        // XXX fails because idn is not handled here (it is converted to punycode in UsableURIFactory.fixupDomainlabel())
        // assertEquals("http://øx.dk", new UsableURI("http://øx.dk", false, "UTF-8").toUnicodeHostString());
        // To check the round trip it is then necessary to use the factory method in UsableURIFactory.
        assertEquals("http://øx.dk/", UsableURIFactory.getInstance("http://øx.dk/", "UTF-8").toUnicodeHostString());

        // non-idn domain name
        assertEquals("http://example.org", new UsableURI("http://example.org", true, "UTF-8").toUnicodeHostString());

        // ensure a call to toUnicodeHostString() has no effect on toString()
        UsableURI uri = new UsableURI("http://xn--x-4ga.dk", true, "UTF-8");
        assertEquals("http://øx.dk", uri.toUnicodeHostString());
        uri.setPath(uri.getPath()); // force toString() cached value to be recomputed
        assertEquals("http://xn--x-4ga.dk", uri.toString());
    }
}
