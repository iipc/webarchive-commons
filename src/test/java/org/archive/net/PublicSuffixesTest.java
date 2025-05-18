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

package org.archive.net;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;

import org.archive.net.PublicSuffixes.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for PublicSuffixes utility. Confirm expected matches/nonmatches
 * from constructed regex.
 * 
 * @author gojomo
 */
public class PublicSuffixesTest {
    // test of low level implementation
    private final String NL = System.getProperty("line.separator");

    @Test
    public void testCompare() {
        Node n = new Node("hoge");
        assertTrue(n.compareTo('a') > 0);
        assertEquals(-1, n.compareTo('*'));
        assertEquals(-1, n.compareTo('!'));
        assertEquals(-1, n.compareTo(new Node("*,")));
        assertEquals(-1, n.compareTo(new Node("!muga,")));
        assertEquals(-1, n.compareTo(new Node("")));
        
        n = new Node("*,");
        assertEquals(1, n.compareTo('a'));
        assertEquals(0, n.compareTo('*'));
        assertEquals(1, n.compareTo('!'));
        assertEquals(0, n.compareTo(new Node("*,")));
        assertEquals(1, n.compareTo(new Node("!muga,")));
        assertEquals(-1, n.compareTo(new Node("")));
        
        n = new Node("!hoge");
        assertEquals(1, n.compareTo('a'));
        assertEquals(-1, n.compareTo('*'));
        assertEquals(0, n.compareTo('!'));
        assertEquals(-1, n.compareTo(new Node("*,")));
        assertEquals(0, n.compareTo(new Node("!muga,")));
        assertEquals(-1, n.compareTo(new Node("")));
        
        n = new Node("");
        assertEquals(1, n.compareTo('a'));
        assertEquals(1, n.compareTo('*'));
        assertEquals(1, n.compareTo('!'));
        assertEquals(0, n.compareTo(new Node("")));
    }
    
    protected String dump(Node alt) {
        StringWriter w = new StringWriter();
        PublicSuffixes.dump(alt, 0, new PrintWriter(w));
        return w.toString();
    }

    @Test
    public void testTrie1()  {
        Node alt = new Node(null, new ArrayList<Node>());
        alt.addBranch("ac,");
        // specifically, should not have empty string as match.
        assertEquals("(null)" + NL + "  \"ac,\"" + NL, dump(alt));
        alt.addBranch("ac,com,");
        assertEquals("(null)" + NL +
        		"  \"ac,\"" + NL +
        		"    \"com,\"" + NL +
        		"    \"\"" + NL, dump(alt));
        alt.addBranch("ac,edu,");
        assertEquals("(null)" + NL +
        		"  \"ac,\"" + NL +
        		"    \"com,\"" + NL +
        		"    \"edu,\"" + NL +
        		"    \"\"" + NL, dump(alt));
    }

    @Test
    public void testTrie2() {
        Node alt = new Node(null, new ArrayList<Node>());
        alt.addBranch("ac,");
        alt.addBranch("*,");
        assertEquals("(null)" + NL +
        		"  \"ac,\"" + NL +
        		"  \"*,\"" + NL, dump(alt));
    }

    @Test
    public void testTrie3() {
        Node alt = new Node(null, new ArrayList<Node>());
        alt.addBranch("ac,");
        alt.addBranch("ac,!hoge,");
        alt.addBranch("ac,*,");
        // exception goes first.
        assertEquals("(null)" + NL +
        		"  \"ac,\"" + NL +
        		"    \"!hoge,\"" + NL +
        		"    \"*,\"" + NL +
        		"    \"\"" + NL, dump(alt));
    }

    // test of higher-level functionality
    
    Matcher m = PublicSuffixes.getTopmostAssignedSurtPrefixPattern()
            .matcher("");

    @Test
    public void testBasics() {
        matchPrefix("com,example,www,", "com,example,");
        matchPrefix("com,example,", "com,example,");
        matchPrefix("org,archive,www,", "org,archive,");
        matchPrefix("org,archive,", "org,archive,");
        matchPrefix("fr,yahoo,www,", "fr,yahoo,");
        matchPrefix("fr,yahoo,", "fr,yahoo,");
        matchPrefix("au,com,foobar,www,", "au,com,foobar,");
        matchPrefix("au,com,foobar,", "au,com,foobar,");
        matchPrefix("uk,co,virgin,www,", "uk,co,virgin,");
        matchPrefix("uk,co,virgin,", "uk,co,virgin,");
        matchPrefix("au,com,example,www,", "au,com,example,");
        matchPrefix("au,com,example,", "au,com,example,");
        matchPrefix("jp,yokohama,public,assigned,www,",
                "jp,yokohama,public,assigned,");
        matchPrefix("jp,yokohama,public,assigned,", "jp,yokohama,public,assigned,");
    }

    @Test
    public void testDomainWithDash() {
        matchPrefix("de,bad-site,www", "de,bad-site,");
    }

    @Test
    public void testDomainWithNumbers() {
        matchPrefix("de,archive4u,www", "de,archive4u,");
    }

    @Test
    public void testIPV4() {
        assertEquals("1.2.3.4",
                PublicSuffixes.reduceSurtToAssignmentLevel("1.2.3.4"),
                "unexpected reduction");
    }

    @Test
    public void testIPV6() {
        assertEquals("[2001:0db8:85a3:08d3:1319:8a2e:0370:7344]",
                PublicSuffixes.reduceSurtToAssignmentLevel(
                        "[2001:0db8:85a3:08d3:1319:8a2e:0370:7344]"),
                "unexpected reduction");
    }

    @Test
    public void testExceptions() {
        matchPrefix("uk,bl,www,", "uk,bl,");
        matchPrefix("uk,bl,", "uk,bl,");
        matchPrefix("jp,tokyo,city,subdomain,", "jp,tokyo,city,");
        matchPrefix("jp,tokyo,city,", "jp,tokyo,city,");
    }

    @Test
    public void testFakeTLD() {
        // we assume any new/unknonwn TLD should be assumed as 2-level;
        // this is preferable for our grouping purpose but might not be
        // for a cookie-assigning browser (original purpose of publicsuffixlist)
        matchPrefix("zzz,example,www,", "zzz,example,");
    }

    @Test
    public void testUnsegmentedHostname() {
        m.reset("example");
        assertFalse(m.find(), "unexpected match found in 'example'");
    }

    @Test
    public void testTopmostAssignedCaching() {
        assertSame(PublicSuffixes.getTopmostAssignedSurtPrefixPattern(),PublicSuffixes.getTopmostAssignedSurtPrefixPattern(),"topmostAssignedSurtPrefixPattern not cached");
        assertSame(PublicSuffixes.getTopmostAssignedSurtPrefixRegex(),PublicSuffixes.getTopmostAssignedSurtPrefixRegex(),"topmostAssignedSurtPrefixRegex not cached");
    }
    
    // TODO: test UTF domains?

    protected void matchPrefix(String surtDomain, String expectedAssignedPrefix) {
        m.reset(surtDomain);
        assertTrue(m.find(), "expected match not found in '" + surtDomain);
        assertEquals(expectedAssignedPrefix, m.group(), "expected match not found");
    }
}
