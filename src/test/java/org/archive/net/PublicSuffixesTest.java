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


import junit.framework.TestCase;

/**
 * Test cases for PublicSuffixes utility. Confirm expected matches/nonmatches
 * from constructed regex.
 * 
 * @author gojomo
 */
public class PublicSuffixesTest extends TestCase {
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
        
        // Test UTF-8 domain
        matchPrefix("com,øx,ũber,", "com,øx,");
    }

    public void testDomainWithDash() {
        matchPrefix("de,bad-site,www", "de,bad-site,");
    }
    
    public void testDomainWithNumbers() {
        matchPrefix("de,archive4u,www", "de,archive4u,");
    }
    
    public void testIPV4() {
        assertEquals("unexpected reduction", 
                "1.2.3.4",
                PublicSuffixes.reduceSurtToAssignmentLevel("1.2.3.4"));
    }
    
    public void testIPV6() {
        assertEquals("unexpected reduction", 
                "[2001:0db8:85a3:08d3:1319:8a2e:0370:7344]",
                PublicSuffixes.reduceSurtToAssignmentLevel(
                        "[2001:0db8:85a3:08d3:1319:8a2e:0370:7344]"));
    }
    
    public void testExceptions() {
        matchPrefix("uk,bl,www,", "uk,bl,");
        matchPrefix("uk,bl,", "uk,bl,");
        matchPrefix("jp,tokyo,city,subdomain,", "jp,tokyo,city,");
        matchPrefix("jp,tokyo,city,", "jp,tokyo,city,");
    }

    public void testFakeTLD() {
        // we assume any new/unknonwn TLD should be assumed as 2-level;
        // this is preferable for our grouping purpose but might not be
        // for a cookie-assigning browser (original purpose of publicsuffixlist)
        matchPrefix("zzz,example,www,", "zzz,example,");
        matchPrefix("zzz,example,", "zzz,example,");
    }

    public void testUnsegmentedHostname() {
        assertEquals("Unexpected rewriting of unsegmented hostname", "example", PublicSuffixes.reduceSurtToAssignmentLevel("example"));
    }

    protected void matchPrefix(String surtDomain, String expectedAssignedPrefix) {
        assertEquals("expected match not found", expectedAssignedPrefix, PublicSuffixes.reduceSurtToAssignmentLevel(surtDomain));
    }
}
