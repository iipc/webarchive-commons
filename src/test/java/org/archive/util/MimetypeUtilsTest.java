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
package org.archive.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author stack
 * @version $Date$, $Revision$
 */
public class MimetypeUtilsTest {

    @Test
	public void testStraightTruncate() {
        assertTrue(MimetypeUtils.truncate("text/html").equals("text/html"),
            "Straight broken");
	}

    @Test
    public void testWhitespaceTruncate() {
        assertTrue(MimetypeUtils.truncate(null).equals("no-type"),
            "Null broken");
        assertTrue(MimetypeUtils.truncate("").equals("no-type"),
                "Empty broken");
        assertTrue(MimetypeUtils.truncate("    ").equals("no-type"),
                "Tab broken");
        assertTrue(MimetypeUtils.truncate("    ").equals("no-type"),
                "Multispace broken");
        assertTrue(MimetypeUtils.truncate("\n").equals("no-type"),
                "NL broken");
    }

    @Test
    public void testCommaTruncate() {
        assertTrue(MimetypeUtils.truncate("text/html,text/html").equals("text/html"),
            "Comma broken");
        assertTrue(MimetypeUtils.truncate("text/html, text/html").
                equals("text/html"),
            "Comma space broken");
        assertTrue(MimetypeUtils.truncate("text/html;charset=iso9958-1").
                equals("text/html"),
            "Charset broken");
        assertTrue(MimetypeUtils.truncate("text/html; charset=iso9958-1").
                equals("text/html"),
            "Charset space broken");
        assertTrue(MimetypeUtils.
            truncate("text/html, text/html; charset=iso9958-1").
                equals("text/html"), "dbl text/html space charset broken");
    }
}
