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

import org.junit.Test;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.HasUnparsedData;
import org.netpreserve.commons.util.datetime.DateFormat;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class CdxjLineTest {

    /**
     * Test of get method, of class CdxjLine.
     */
    @Test
    public void testGet() {
        CdxjLineFormat format = new CdxjLineFormat();
        String keyString = "(no,dagbladet,www,)/premier2000/spiller_2519.html 2007-08-21T18:35:28 response";
        String valueString = "{\"uri\":\"http://www.dagbladet.no/premier2000/spiller_2519.html\","
                + "\"mct\":\"text/html\",\"hsc\":404,"
                + "\"sha\":\"4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7\",\"rle\":1506,"
                + "\"ref\":\"warcfile:IAH-20070907235053-00459-heritrix.arc.gz#68224437\"}";
        char[] keyValue = (keyString + ' ' + valueString).toCharArray();

        CdxjLine record = new CdxjLine(keyValue, format);

        assertThat(record.get(FieldName.URI_KEY).getValue())
                .isEqualTo("(no,dagbladet,www,)/premier2000/spiller_2519.html");
        assertThat(record.get(FieldName.TIMESTAMP).getValue().toFormattedString(DateFormat.WARC))
                .isEqualTo("2007-08-21T18:35:28");
        assertThat(record.get(FieldName.RECORD_TYPE).getValue()).isEqualTo("response");
        assertThat(record.get(FieldName.ORIGINAL_URI).getValue().toString())
                .isEqualTo("http://www.dagbladet.no/premier2000/spiller_2519.html");
        assertThat(record.get(FieldName.CONTENT_TYPE).getValue()).isEqualTo("text/html");
        assertThat(record.get(FieldName.RESPONSE_CODE).getValue().intValue()).isEqualTo(404);
        assertThat(record.get(FieldName.PAYLOAD_DIGEST).getValue()).isEqualTo("4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7");
        assertThat(record.get(FieldName.RECORD_LENGTH).getValue().intValue()).isEqualTo(1506);
        assertThat(record.get(FieldName.RESOURCE_REF).getValue().toString())
                .isEqualTo("warcfile:IAH-20070907235053-00459-heritrix.arc.gz#68224437");

        keyString = "(com,akerkvaerner,)/internet/templates/normalarticlepage.aspx"
                + "?nrcachehint=nomodifyguest&nrmode=published&nrnodeguid={bca68cf3-e218-41f4-b76e-c9be540ee25f}"
                + "&nroriginalurl=/internet/investorrelations/financereports/annualreports/annualreport2006.htm"
                + " 2007-08-23T21:51:21 response";
        valueString = "{\"hsc\":200,\"ple\":19231,"
                + "\"uri\":\"http://www.akerkvaerner.com/internet/Templates/NormalArticlePage.aspx"
                + "?NRMODE=Published&NRORIGINALURL=%2fInternet%2fInvestorRelations%2fFinanceReports%2fAnnualreports"
                + "%2fAnnualReport2006%2ehtm&NRNODEGUID=%7bBCA68CF3-E218-41F4-B76E-C9BE540EE25F%7d&NRCACHEHINT="
                + "NoModifyGuest\",\"cle\":19471,\"sha\":\"3SDGTE3OJ6HFZR6NOQJWFODV5XFVVSSE\",\"rle\":4887,"
                + "\"mct\":\"text/html; charset=utf-8\","
                + "\"ref\":\"warcfile:IAH-20070823192158-00362-heritrix2.nb.no.arc.gz#97645777\"}";
        keyValue = (keyString + ' ' + valueString).toCharArray();

        record = new CdxjLine(keyString + ' ' + valueString, format);

        assertThat(record.get(FieldName.URI_KEY).getValue())
                .isEqualTo("(com,akerkvaerner,)/internet/templates/normalarticlepage.aspx"
                        + "?nrcachehint=nomodifyguest&nrmode=published&nrnodeguid={bca68cf3-e218-41f4-b76e-c9be540ee25f}"
                        + "&nroriginalurl=/internet/investorrelations/financereports/annualreports/annualreport2006.htm");
        assertThat(record.get(FieldName.TIMESTAMP).getValue().toFormattedString(DateFormat.WARC))
                .isEqualTo("2007-08-23T21:51:21");
        assertThat(record.get(FieldName.RECORD_TYPE).getValue()).isEqualTo("response");
        assertThat(record.get(FieldName.CONTENT_TYPE).getValue()).isEqualTo("text/html; charset=utf-8");
        assertThat(record.get(FieldName.RESPONSE_CODE).getValue().intValue()).isEqualTo(200);
        assertThat(record.get(FieldName.PAYLOAD_DIGEST).getValue()).isEqualTo("3SDGTE3OJ6HFZR6NOQJWFODV5XFVVSSE");
        assertThat(record.get(FieldName.CONTENT_LENGTH).getValue().intValue()).isEqualTo(19471);
        assertThat(record.get(FieldName.RECORD_LENGTH).getValue().intValue()).isEqualTo(4887);
    }

    /**
     * Test of getUnparsed method, of class CdxjLine.
     */
    @Test
    public void testGetUnparsed() {
        CdxjLineFormat format = new CdxjLineFormat();
        String keyString = "(no,dagbladet,www,)/premier2000/spiller_2519.html 2007-08-21T18:35:28 response";
        String valueString = "{\"uri\":\"http://www.dagbladet.no/premier2000/spiller_2519.html\","
                + "\"mct\":\"text/html\",\"hsc\":404,"
                + "\"sha\":\"4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7\",\"rle\":1506,"
                + "\"ref\":\"warcfile:IAH-20070907235053-00459-heritrix.arc.gz#68224437\"}";
        char[] keyValue = (keyString + ' ' + valueString).toCharArray();

        CdxjLine record = new CdxjLine(keyValue, format);

        assertThat(record.getUnparsed()).containsExactly(keyValue);
        assertThat(record.getKey())
                .isNotNull()
                .isInstanceOf(HasUnparsedData.class)
                .hasFieldOrPropertyWithValue("unparsed", keyString.toCharArray());

        keyString = "(com,akerkvaerner,)/internet/templates/normalarticlepage.aspx"
                + "?nrcachehint=nomodifyguest&nrmode=published&nrnodeguid={bca68cf3-e218-41f4-b76e-c9be540ee25f}"
                + "&nroriginalurl=/internet/investorrelations/financereports/annualreports/annualreport2006.htm"
                + " 2007-08-23T21:51:21 response";
        valueString = "{\"hsc\":200,\"ple\":19231,"
                + "\"uri\":\"http://www.akerkvaerner.com/internet/Templates/NormalArticlePage.aspx"
                + "?NRMODE=Published&NRORIGINALURL=%2fInternet%2fInvestorRelations%2fFinanceReports%2fAnnualreports"
                + "%2fAnnualReport2006%2ehtm&NRNODEGUID=%7bBCA68CF3-E218-41F4-B76E-C9BE540EE25F%7d&NRCACHEHINT="
                + "NoModifyGuest\",\"cle\":19471,\"sha\":\"3SDGTE3OJ6HFZR6NOQJWFODV5XFVVSSE\",\"rle\":4887,"
                + "\"mct\":\"text/html; charset=utf-8\","
                + "\"ref\":\"warcfile:IAH-20070823192158-00362-heritrix2.nb.no.arc.gz#97645777\"}";
        keyValue = (keyString + ' ' + valueString).toCharArray();

        record = new CdxjLine(keyString + ' ' + valueString, format);

        assertThat(record.getUnparsed()).containsExactly(keyValue);
        assertThat(record.getKey())
                .isNotNull()
                .isInstanceOf(HasUnparsedData.class)
                .hasFieldOrPropertyWithValue("unparsed", keyString.toCharArray());
    }

}
