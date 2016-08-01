/*
 * Copyright 2016 IIPC.
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

package org.netpreserve.commons.cdx.cdxrecord;


import org.netpreserve.commons.cdx.cdxrecord.CdxjLine;
import org.netpreserve.commons.cdx.cdxrecord.CdxjLineFormat;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class CdxjLineTest {

    /**
     * Test of getValue method, of class CdxjLine.
     */
    @Test
    public void testGetValue() {
    }

    /**
     * Test of get method, of class CdxjLine.
     */
    @Test
    public void testGet() {
    }

    /**
     * Test of parseFields method, of class CdxjLine.
     */
    @Test
    public void testParseFields() {
        CdxjLineFormat format = new CdxjLineFormat();
        String keyString = "(no,dagbladet,www,)/premier2000/spiller_2519.html 20070821183528 response";
        String valueString = "{\"url\":\"http://www.dagbladet.no/premier2000/spiller_2519.html\","
                + "\"mime\":\"text/html\",\"statuscode\":\"404\","
                + "\"digest\":\"4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7\",\"length\":1506,"
                + "\"loc\":\"warcfile:IAH-20070907235053-00459-heritrix.arc.gz#68224437\"}";
        char[] key = keyString.toCharArray();
        char[] value = valueString.toCharArray();
        char[] keyValue = (keyString + ' ' + valueString).toCharArray();

        CdxjLine line = new CdxjLine(keyValue, format);
        line.parseFields();

        System.out.println("GET");
        System.out.println("surt = " + line.getKey().getUriKey());
        System.out.println("loc = " + line.get("loc"));
        System.out.println("---");

        line = new CdxjLine(keyValue, format);
        line.parseFields();

//        fail("prototype");
    }

}