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
package org.netpreserve.commons.cdx.formatter;

import org.junit.Test;
import org.netpreserve.commons.cdx.cdxrecord.CdxLine;
import org.netpreserve.commons.cdx.cdxrecord.CdxLineFormat;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.cdxrecord.CdxjLine;
import org.netpreserve.commons.cdx.cdxrecord.CdxjLineFormat;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests for CdxRecordFormatter class.
 */
public class CdxRecordFormatterTest {

    /**
     * Test of format method, of class CdxRecordFormatter.
     */
    @Test
    public void testFormat() {
        String legacyKey = "no,dagbladet)/premier2000/spiller_2519.html 20070908002541";
        String cdxjKey = "(no,dagbladet,)/premier2000/spiller_2519.html 2007-09-08T00:25:41Z response";

        String cdx11String = " http://www.dagbladet.no/premier2000/spiller_2519.html text/html 404 "
                + "4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7 - - 1506 68224437 "
                + "IAH-20070907235053-00459-heritrix.arc.gz";

        CdxRecord cdx11Record = new CdxLine(legacyKey + cdx11String, CdxLineFormat.CDX11LINE);

        String cdx09String = " http://www.dagbladet.no/premier2000/spiller_2519.html text/html 404 "
                + "4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7 - 68224437 "
                + "IAH-20070907235053-00459-heritrix.arc.gz";
        CdxRecord cdx09Record = new CdxLine(legacyKey + cdx09String, CdxLineFormat.CDX09LINE);

        String cdxjString = " {\"uri\":\"http://www.dagbladet.no/premier2000/spiller_2519.html\",\"mct\":\"text/html\","
                + "\"hsc\":404,\"digest\":\"4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7\",\"rle\":1506,"
                + "\"ref\":\"warcfile:IAH-20070907235053-00459-heritrix.arc.gz#68224437\"}";
        CdxRecord cdxjRecord = new CdxjLine(cdxjKey + cdxjString, CdxjLineFormat.DEFAULT_CDXJLINE);

        CdxRecordFormatter formatter = new CdxRecordFormatter(CdxLineFormat.CDX11LINE);
        assertThat(formatter.format(cdx11Record)).isEqualTo(legacyKey + cdx11String);
        assertThat(formatter.format(cdx09Record)).isEqualTo(legacyKey
                + " http://www.dagbladet.no/premier2000/spiller_2519.html text/html 404 "
                + "4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7 - - - 68224437 "
                + "IAH-20070907235053-00459-heritrix.arc.gz");
        assertThat(formatter.format(cdxjRecord)).isEqualTo(legacyKey + cdx11String);

        formatter = new CdxRecordFormatter(CdxLineFormat.CDX09LINE);
        assertThat(formatter.format(cdx11Record)).isEqualTo(legacyKey + cdx09String);
        assertThat(formatter.format(cdx09Record)).isEqualTo(legacyKey + cdx09String);
        assertThat(formatter.format(cdxjRecord)).isEqualTo(legacyKey + cdx09String);

        formatter = new CdxRecordFormatter(CdxjLineFormat.DEFAULT_CDXJLINE);
        assertThat(formatter.format(cdx11Record)).isEqualTo(cdxjKey + cdxjString);
        assertThat(formatter.format(cdx09Record)).isEqualTo(cdxjKey
                + " {\"uri\":\"http://www.dagbladet.no/premier2000/spiller_2519.html\",\"mct\":\"text/html\","
                + "\"hsc\":404,\"digest\":\"4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7\","
                + "\"ref\":\"warcfile:IAH-20070907235053-00459-heritrix.arc.gz#68224437\"}");

        String result = formatter.format(cdxjRecord);
        assertThat(result).startsWith(cdxjKey);
        assertThatJson(result.substring(result.indexOf("{"))).isEqualTo(cdxjString);
    }

}
