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
 * Test methods for ParsedQuery.
 */
public class ParsedQueryTest {

    /**
     * Test of size method, of class ParsedQuery.
     */
    @Test
    public void testSize() {
        ParsedQuery pq = new ParsedQuery("foo&bar=qwe&iop=fgh&bar=xyz");
        assertThat(pq.size()).isEqualTo(3);

        pq = new ParsedQuery("");
        assertThat(pq.size()).isEqualTo(0);

        pq = new ParsedQuery(null);
        assertThat(pq.size()).isEqualTo(0);
    }

    /**
     * Test of isEmpty method, of class ParsedQuery.
     */
    @Test
    public void testIsEmpty() {
        ParsedQuery pq = new ParsedQuery("foo&bar=qwe&iop=fgh&bar=xyz");
        assertThat(pq.isEmpty()).isFalse();

        pq = new ParsedQuery("");
        assertThat(pq.isEmpty()).isTrue();

        pq = new ParsedQuery(null);
        assertThat(pq.isEmpty()).isTrue();
    }

    /**
     * Test of containsKey method, of class ParsedQuery.
     */
    @Test
    public void testContainsKey() {
        ParsedQuery pq = new ParsedQuery("foo&bar=qwe&iop=fgh&bar=xyz");
        assertThat(pq.containsKey("foo")).isTrue();
        assertThat(pq.containsKey("abc")).isFalse();

        pq = new ParsedQuery(null);
        assertThat(pq.containsKey("foo")).isFalse();
    }

    /**
     * Test of get method, of class ParsedQuery.
     */
    @Test
    public void testGet() {
        ParsedQuery pq = new ParsedQuery("foo&bar=qwe&iop=fgh&bar=xyz");
        assertThat(pq.get("foo")).isNotNull();
        assertThat(pq.get("abc")).isNull();

        pq = new ParsedQuery(null);
        assertThat(pq.get("foo")).isNull();
    }

    /**
     * Test of put method, of class ParsedQuery.
     */
    @Test
    public void testPut() {
        ParsedQuery pq = new ParsedQuery("foo&bar=qwe&iop=fgh&bar=xyz");
        assertThat(pq.get("foo")).isNotNull();
        assertThat(pq.get("foo").size()).isEqualTo(0);
        pq = pq.put(new ParsedQuery.Entry("foo", "def"));
        assertThat(pq.get("foo")).isNotNull();
        assertThat(pq.get("foo").size()).isEqualTo(1);
        assertThat(pq.get("foo").get(0)).isNotNull().isEqualTo("def");
        pq = pq.put(new ParsedQuery.Entry("foo", "aaa"));
        assertThat(pq.get("foo")).isNotNull();
        assertThat(pq.get("foo").size()).isEqualTo(1);
        assertThat(pq.get("foo").get(0)).isNotNull().isEqualTo("aaa");

        assertThat(pq.get("abc")).isNull();
        pq = pq.put(new ParsedQuery.Entry("abc", "def"));
        assertThat(pq.get("abc")).isNotNull();

        assertThat(pq).hasToString("abc=def&bar=qwe&bar=xyz&foo=aaa&iop=fgh");
    }

    /**
     * Test of put method, of class ParsedQuery.
     */
    @Test
    public void testAdd() {
        ParsedQuery pq = new ParsedQuery("foo&bar=qwe&iop=fgh&bar=xyz");
        assertThat(pq.get("foo")).isNotNull();
        assertThat(pq.get("foo").size()).isEqualTo(0);
        pq = pq.add(new ParsedQuery.Entry("foo", "def"));
        assertThat(pq.get("foo")).isNotNull();
        assertThat(pq.get("foo").size()).isEqualTo(1);
        assertThat(pq.get("foo").get(0)).isNotNull().isEqualTo("def");
        pq = pq.add(new ParsedQuery.Entry("foo", "aaa"));
        assertThat(pq.get("foo")).isNotNull();
        assertThat(pq.get("foo").size()).isEqualTo(2);
        assertThat(pq.get("foo").get(0)).isNotNull().isEqualTo("aaa");
        assertThat(pq.get("foo").get(1)).isNotNull().isEqualTo("def");

        assertThat(pq.get("abc")).isNull();
        pq = pq.add(new ParsedQuery.Entry("abc", "def"));
        assertThat(pq.get("abc")).isNotNull();

        assertThat(pq).hasToString("abc=def&bar=qwe&bar=xyz&foo=aaa&foo=def&iop=fgh");
    }

    /**
     * Test of remove method, of class ParsedQuery.
     */
    @Test
    public void testRemove() {
        ParsedQuery pq = new ParsedQuery("foo&bar=qwe&iop=fgh&bar=xyz");
        assertThat(pq.get("foo")).isNotNull();
        pq = pq.remove("foo");
        assertThat(pq.get("foo")).isNull();
        assertThat(pq.get("abc")).isNull();
        pq = pq.remove("abc");
        assertThat(pq.get("abc")).isNull();

        assertThat(pq).hasToString("bar=qwe&bar=xyz&iop=fgh");
    }

    /**
     * Test of format method, of class ParsedQuery.
     */
    @Test
    public void testToString() {
        ParsedQuery pq = new ParsedQuery("foo&bar=qwe&iop=fgh&bar=xyz");
        assertThat(pq).hasToString("bar=qwe&bar=xyz&foo&iop=fgh");

        pq = new ParsedQuery("");
        assertThat(pq.toString()).isNull();

        pq = new ParsedQuery(null);
        assertThat(pq.toString()).isNull();
    }

}
