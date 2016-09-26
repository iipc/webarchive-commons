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
package org.netpreserve.commons.cdx.formatter;

import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;
import org.netpreserve.commons.cdx.CdxFormat;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.cdxrecord.CdxLine;
import org.netpreserve.commons.cdx.cdxrecord.CdxLineFormat;
import org.netpreserve.commons.cdx.cdxrecord.CdxjLine;
import org.netpreserve.commons.cdx.cdxrecord.CdxjLineFormat;
import org.netpreserve.commons.cdx.cdxrecord.UnconnectedCdxRecord;

import static org.assertj.core.api.Assertions.*;

/**
 * TestMethods for CdxLineFormatter.
 */
public class CdxLineFormatterTest {

    /**
     * Test of format method, of class CdxLineFormatter.
     * @throws java.lang.Exception should not be thrown for this test
     */
    @Test
    public void testFormat() throws Exception {
        CdxFormat outputFormat = CdxLineFormat.CDX11LINE;
        CdxLineFormatter instance = new CdxLineFormatter();
        Writer out;

        String cdxLine = "as,terra)/gfx/whitepixel.gif 20070821183528"
                + " https://www.terra.as/gfx/whitepixel.gif image/gif 200"
                + " FQ2WDWANAVUTLNKHGLRCOG4HKQWKHLVQ - - 396 36942074"
                + " IAH-20070821182921-00150.arc.gz";
        CdxRecord cdxRecord = new CdxLine(cdxLine, CdxLineFormat.CDX11LINE);
        out = new StringWriter();
        instance.format(out, cdxRecord, outputFormat);
        assertThat(out).hasToString(cdxLine);

        String cdxjLine = "(as,terra,)/gfx/whitepixel.gif 2007-08-21T18:35:28Z response "
                + "{\"uri\":\"https://www.terra.as/gfx/whitepixel.gif\","
                + "\"mct\":\"image/gif\",\"hsc\":200,"
                + "\"digest\":\"FQ2WDWANAVUTLNKHGLRCOG4HKQWKHLVQ\",\"rle\":396,"
                + "\"ref\":\"warcfile:IAH-20070821182921-00150.arc.gz#36942074\"}";
        CdxRecord cdxjRecord = new CdxjLine(cdxjLine, CdxjLineFormat.DEFAULT_CDXJLINE);
        out = new StringWriter();
        instance.format(out, cdxjRecord, outputFormat);
        assertThat(out).hasToString(cdxLine);

        String cdxjson = "{\"uri\":\"https://www.terra.as/gfx/whitepixel.gif\","
                + "\"mct\":\"image/gif\",\"hsc\":200,"
                + "\"digest\":\"FQ2WDWANAVUTLNKHGLRCOG4HKQWKHLVQ\",\"rle\":396,"
                + "\"ref\":\"warcfile:IAH-20070821182921-00150.arc.gz#36942074\","
                + "\"sts\":\"2007-08-21T18:35:28Z\"}";
        CdxRecord unconnectedRecord = new UnconnectedCdxRecord(cdxjson);
        out = new StringWriter();
        instance.format(out, unconnectedRecord, outputFormat);
        assertThat(out).hasToString(cdxLine);
    }

}
