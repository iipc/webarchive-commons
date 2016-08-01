/*
 * Copyright 2015 IIPC.
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
package org.netpreserve.commons.cdx.functions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.netpreserve.commons.cdx.CdxFormat;
import org.netpreserve.commons.cdx.cdxrecord.CdxLineFormat;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.CdxRecordFactory;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.functions.CollapseFieldProvider.CollapseField;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
@Ignore
public class CollapseFieldProviderTest {

    private CdxFormat format = new CdxLineFormat(' ', FieldName.URI_KEY, FieldName.TIMESTAMP);

    /**
     * Test of apply method, of class CollapseField.
     */
    @Test
    public void testApplySubpartOfField() {
        // test with 4 digit timestamp
        CdxRecord line1 = CdxRecordFactory.create(
                "no,dagbladet)/spiller_2519.html 20070908002541", format);
        CdxRecord line2 = CdxRecordFactory.create(
                "no,dagbladet)/spiller_2520.html 20070908002532", format);
        CdxRecord line3 = CdxRecordFactory.create(
                "no,dagbladet)/spiller_2520.html 20080908002532", format);
        CollapseField cf = new CollapseFieldProvider(
                Collections.singletonList(FieldName.TIMESTAMP + ":4")).newFunction();

        assertThat((Comparable) cf.apply(null, line1)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002541");
        assertThat((Comparable) cf.apply(line1, line2)).isNull();
        assertThat((Comparable) cf.apply(line2, line3)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20080908002532");

        // test with 12 digit timestamp
        line1 = CdxRecordFactory.create("no,dagbladet)/spiller_2519.html 20070908002541", format);
        line2 = CdxRecordFactory.create("no,dagbladet)/spiller_2520.html 20070908002532", format);
        line3 = CdxRecordFactory.create("no,dagbladet)/spiller_2520.html 20080908002532", format);
        cf = new CollapseFieldProvider(
                Collections.singletonList(FieldName.TIMESTAMP + ":12")).newFunction();

        assertThat((Comparable) cf.apply(null, line1)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002541");
        assertThat((Comparable) cf.apply(line1, line2)).isNull();
        assertThat((Comparable) cf.apply(line2, line3)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20080908002532");

        // test with 13 digit timestamp
        line1 = CdxRecordFactory.create("no,dagbladet)/spiller_2519.html 20070908002541", format);
        line2 = CdxRecordFactory.create("no,dagbladet)/spiller_2520.html 20070908002532", format);
        line3 = CdxRecordFactory.create("no,dagbladet)/spiller_2520.html 20080908002532", format);
        cf = new CollapseFieldProvider(
                Collections.singletonList(FieldName.TIMESTAMP + ":13")).newFunction();

        assertThat((Comparable) cf.apply(null, line1)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002541");
        assertThat((Comparable) cf.apply(line1, line2)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20070908002532");
        assertThat((Comparable) cf.apply(line2, line3)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20080908002532");

        // test with 14 digit timestamp
        line1 = CdxRecordFactory.create("no,dagbladet)/spiller_2519.html 20070908002541", format);
        line2 = CdxRecordFactory.create("no,dagbladet)/spiller_2520.html 20070908002532", format);
        line3 = CdxRecordFactory.create("no,dagbladet)/spiller_2520.html 20080908002532", format);
        cf = new CollapseFieldProvider(
                Collections.singletonList(FieldName.TIMESTAMP + ":14")).newFunction();

        assertThat((Comparable) cf.apply(null, line1)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002541");
        assertThat((Comparable) cf.apply(line1, line2)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20070908002532");
        assertThat((Comparable) cf.apply(line2, line3)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20080908002532");

        // test with 18 digit timestamp (larger than timestamp field)
        line1 = CdxRecordFactory.create("no,dagbladet)/spiller_2519.html 20070908002541", format);
        line2 = CdxRecordFactory.create("no,dagbladet)/spiller_2520.html 20070908002532", format);
        line3 = CdxRecordFactory.create("no,dagbladet)/spiller_2520.html 20080908002532", format);
        cf = new CollapseFieldProvider(
                Collections.singletonList(FieldName.TIMESTAMP + ":18")).newFunction();

        assertThat((Comparable) cf.apply(null, line1)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002541");
        assertThat((Comparable) cf.apply(line1, line2)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20070908002532");
        assertThat((Comparable) cf.apply(line2, line3)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2520.html 20080908002532");
    }

    @Test
    public void testApplyTwoFiles() {
        CdxRecord line11 = CdxRecordFactory.create(
                "no,dagbladet)/spiller_2519.html 20070908002541", format);
        CdxRecord line12 = CdxRecordFactory.create(
                "no,dagbladet)/spiller_2520.html 20070908002532", format);
        CdxRecord line13 = CdxRecordFactory.create(
                "no,dagbladet)/spiller_2521.html 20080908002533", format);

        CdxRecord line21 = CdxRecordFactory.create(
                "no,dagbladet)/spiller_2519.html 20070908002540", format);
        CdxRecord line22 = CdxRecordFactory.create(
                "no,dagbladet)/spiller_2520.html 20070908002533", format);
        CdxRecord line23 = CdxRecordFactory.create(
                "no,dagbladet)/spiller_2521.html 20080908002534", format);
        CdxRecord line24 = CdxRecordFactory.create(
                "no,dagbladet)/spiller_2522.html 20090908002534", format);

        CollapseFieldProvider cfp = new CollapseFieldProvider(Collections
                .singletonList(FieldName.TIMESTAMP + ":4"));

        // Simulate run of cdx source 1
        CollapseField cf = cfp.newFunction();
        assertThat((Comparable) cf.apply(null, line11)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002541");
        assertThat((Comparable) cf.apply(line11, line12)).isNull();
        assertThat((Comparable) cf.apply(line12, line13)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2521.html 20080908002533");

        // Simulate run of cdx source 2
        cf = cfp.newFunction();
        assertThat((Comparable) cf.apply(null, line21)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002540");
        assertThat((Comparable) cf.apply(line21, line22)).isNull();
        assertThat((Comparable) cf.apply(line22, line23)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2521.html 20080908002534");
        assertThat((Comparable) cf.apply(line23, line24)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2522.html 20090908002534");

        // Simulate run of multi cdx source results from 1 and 2
        cf = cfp.newFunction();
        assertThat((Comparable) cf.apply(null, line21)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2519.html 20070908002540");
        assertThat((Comparable) cf.apply(line21, line11)).isNull();
        assertThat((Comparable) cf.apply(line11, line13)).isNotNull()
                .hasToString("no,dagbladet)/spiller_2521.html 20080908002533");
        assertThat((Comparable) cf.apply(line13, line23)).isNull();
        assertThat((Comparable) cf.apply(line23, line24))
                .hasToString("no,dagbladet)/spiller_2522.html 20090908002534");
    }

    @Test
    public void testApplyUrlkey() {
        CdxRecord line1 = CdxRecordFactory.create("ab 1900", format);
        CdxRecord line2 = CdxRecordFactory.create("ab 1901", format);
        CdxRecord line3 = CdxRecordFactory.create("ac 1900", format);
        CdxRecord line4 = CdxRecordFactory.create("ac 1901", format);

        CollapseField cf = new CollapseFieldProvider(toCollapseList(FieldName.URI_KEY.toString()))
                .newFunction();

        assertThat((Comparable) cf.apply(null, line1))
                .isNotNull().isSameAs(line1).hasToString("ab 1900");
        assertThat((Comparable) cf.apply(line1, line2)).isNull();
        assertThat((Comparable) cf.apply(line2, line3))
                .isNotNull().isSameAs(line3).hasToString("ac 1900");
        assertThat((Comparable) cf.apply(line3, line4)).isNull();
    }

    @Test
    public void testApplyTimestamp() {
        CdxRecord line1 = CdxRecordFactory.create("ab 1900", format);
        CdxRecord line2 = CdxRecordFactory.create("ab 1901", format);
        CdxRecord line3 = CdxRecordFactory.create("ac 1900", format);
        CdxRecord line4 = CdxRecordFactory.create("ac 1901", format);

        CollapseField cf = new CollapseFieldProvider(toCollapseList(FieldName.TIMESTAMP.toString()))
                .newFunction();

        assertThat((Comparable) cf.apply(null, line1))
                .isNotNull().isSameAs(line1).hasToString("ab 1900");
        assertThat((Comparable) cf.apply(line1, line2))
                .isNotNull().isSameAs(line2).hasToString("ab 1901");
        assertThat((Comparable) cf.apply(line2, line3)).isNull();
        assertThat((Comparable) cf.apply(line3, line4)).isNull();
    }

    @Test
    public void testApplyUrlkeyAndTimestamp() {
        CdxRecord line1 = CdxRecordFactory.create("ab 1900", format);
        CdxRecord line2 = CdxRecordFactory.create("ab 1901", format);
        CdxRecord line3 = CdxRecordFactory.create("ac 1900", format);
        CdxRecord line4 = CdxRecordFactory.create("ac 1901", format);

        CollapseField cf = new CollapseFieldProvider(
                toCollapseList(FieldName.URI_KEY + "," + FieldName.TIMESTAMP)).newFunction();

        assertThat((Comparable) cf.apply(null, line1))
                .isNotNull().isSameAs(line1).hasToString("ab 1900");
        assertThat((Comparable) cf.apply(line1, line2))
                .isNotNull().isSameAs(line2).hasToString("ab 1901");
        assertThat((Comparable) cf.apply(line2, line3))
                .isNotNull().isSameAs(line3).hasToString("ac 1900");
        assertThat((Comparable) cf.apply(line3, line4))
                .isNotNull().isSameAs(line4).hasToString("ac 1901");
    }

    @Test
    public void testApplyTimestampAndUrlkey() {
        CdxRecord line1 = CdxRecordFactory.create("ab 1900", format);
        CdxRecord line2 = CdxRecordFactory.create("ab 1901", format);
        CdxRecord line3 = CdxRecordFactory.create("ac 1900", format);
        CdxRecord line4 = CdxRecordFactory.create("ac 1901", format);

        CollapseField cf = new CollapseFieldProvider(
                toCollapseList(FieldName.TIMESTAMP + "," + FieldName.URI_KEY)).newFunction();

        assertThat((Comparable) cf.apply(null, line1))
                .isNotNull().isSameAs(line1).hasToString("ab 1900");
        assertThat((Comparable) cf.apply(line1, line2))
                .isNotNull().isSameAs(line2).hasToString("ab 1901");
        assertThat((Comparable) cf.apply(line2, line3))
                .isNotNull().isSameAs(line3).hasToString("ac 1900");
        assertThat((Comparable) cf.apply(line3, line4))
                .isNotNull().isSameAs(line4).hasToString("ac 1901");
    }

    @Test
    public void testApplyUrlkeyAndTimestampSeparateCollapseFunction() {
        CdxRecord line1 = CdxRecordFactory.create("ab 1900", format);
        CdxRecord line2 = CdxRecordFactory.create("ab 1901", format);
        CdxRecord line3 = CdxRecordFactory.create("ac 1900", format);
        CdxRecord line4 = CdxRecordFactory.create("ac 1901", format);

        CollapseField cf1 = new CollapseFieldProvider(toCollapseList(FieldName.URI_KEY.toString()))
                .newFunction();
        CollapseField cf2 = new CollapseFieldProvider(
                toCollapseList(FieldName.TIMESTAMP.toString())).newFunction();

        CdxRecord res11 = cf1.apply(null, line1);
        assertThat((Comparable) res11).isNotNull().isSameAs(line1).hasToString("ab 1900");
        CdxRecord res21 = cf2.apply(null, res11);
        assertThat((Comparable) res21).isNotNull().isSameAs(line1).hasToString("ab 1900");

        CdxRecord res12 = cf1.apply(line1, line2);
        assertThat((Comparable) res12).isNull();

        CdxRecord res13 = cf1.apply(line2, line3);
        assertThat((Comparable) res13).isNotNull().isSameAs(line3).hasToString("ac 1900");
        CdxRecord res22 = cf2.apply(res21, res13);
        assertThat((Comparable) res22).isNull();

        CdxRecord res14 = cf1.apply(line3, line4);
        assertThat((Comparable) res14).isNull();
    }

    @Test
    public void testApplyTimestampAndUrlkeySeparateCollapseFunction() {
        CdxRecord line1 = CdxRecordFactory.create("ab 1900", format);
        CdxRecord line2 = CdxRecordFactory.create("ab 1901", format);
        CdxRecord line3 = CdxRecordFactory.create("ac 1900", format);
        CdxRecord line4 = CdxRecordFactory.create("ac 1901", format);

        CollapseField cf1 = new CollapseFieldProvider(
                toCollapseList(FieldName.TIMESTAMP.toString())).newFunction();
        CollapseField cf2 = new CollapseFieldProvider(toCollapseList(FieldName.URI_KEY.toString()))
                .newFunction();

        CdxRecord res11 = cf1.apply(null, line1);
        assertThat((Comparable) res11).isNotNull().isSameAs(line1).hasToString("ab 1900");
        CdxRecord res21 = cf2.apply(null, res11);
        assertThat((Comparable) res21).isNotNull().isSameAs(line1).hasToString("ab 1900");

        CdxRecord res12 = cf1.apply(line1, line2);
        assertThat((Comparable) res12).isNotNull().isSameAs(line2).hasToString("ab 1901");

        CdxRecord res13 = cf1.apply(line2, line3);
        assertThat((Comparable) res13).isNull();

        CdxRecord res14 = cf1.apply(line3, line4);
        assertThat((Comparable) res14).isNull();
    }

    private List<String> toCollapseList(String collapseString) {
        return Arrays.asList(collapseString.split(","));
    }

}
