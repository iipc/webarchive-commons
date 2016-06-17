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

package org.netpreserve.commons.cdx;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class CdxDateTest {

    public CdxDateTest() {
    }
    /**
     * Test of fromWarcDate method, of class CdxDate.
     */
    @Test
    public void testFromWarcDate() {
        CdxDate date = CdxDate.fromWarcDate("2016-12");
        CdxDate date2 = CdxDate.fromWarcDate("2016-12-01T12:13:54.335Z");
        CdxDate date3 = CdxDate.fromWarcDate("2016-12-01T12");
        CdxDate date4 = CdxDate.fromWarcDate("2016-12-01T12:13Z");
        CdxDate date5 = CdxDate.fromWarcDate("2016-12-01T12:13:00Z");

        System.out.println(date);
        System.out.println(date2);
        System.out.println(date3);
        System.out.println(date4);
        System.out.println(date5);
        fail("XXX");
    }

    /**
     * Test of fromHeritrixDate method, of class CdxDate.
     */
    @Test
    public void testFromHeritrixDate() {
    }
}