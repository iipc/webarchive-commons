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
package org.netpreserve.commons.cdx.processor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;
import org.netpreserve.commons.cdx.cdxrecord.CdxLine;
import org.netpreserve.commons.cdx.cdxsource.CdxIterator;
import org.netpreserve.commons.cdx.CdxFormat;
import org.netpreserve.commons.cdx.cdxrecord.CdxLineFormat;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.CdxRecordFactory;
import org.netpreserve.commons.cdx.CdxSource;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.SearchKey;
import org.netpreserve.commons.cdx.SearchResult;
import org.netpreserve.commons.cdx.CdxSourceFactory;
import org.netpreserve.commons.cdx.cdxsource.BlockCdxSource;
import org.netpreserve.commons.cdx.cdxsource.MockCdxIterator;
import org.netpreserve.commons.cdx.cdxsource.MultiCdxIterator;
import org.netpreserve.commons.cdx.cdxsource.MultiCdxSource;
import org.netpreserve.commons.cdx.cdxsource.SourceDescriptor;
import org.netpreserve.commons.cdx.functions.BiFunction;
import org.netpreserve.commons.cdx.functions.CollapseFieldProvider;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for BiFunctionProcessor.
 */
@Ignore
public class BiFunctionProcessorTest {

    /**
     * Test of processorIterator method, of class BiFunctionProcessor.
     */
    @Test
    public void testProcessorIterator() {
        CdxRecord line1 = new CdxLine("no,dagbladet)/premier2000/spiller_2519.html 20070908002541 "
                + "http://www.dagbladet.no/premier2000/spiller_2519.html text/html 404 "
                + "4GYIEA43CYREJWAD2NSGSIWYVGXJNGB7 - - 1506 68224437 "
                + "IAH-20070907235053-00459-heritrix2.nb.no.arc.gz", CdxLineFormat.CDX11LINE);

        CdxRecord line2 = new CdxLine("no,dagbladet)/premier2000/spiller_2520.html 20070908002532 "
                + "http://www.dagbladet.no/premier2000/spiller_2520.html text/html 200 "
                + "5RRATYEFXZV5V64KA6AP3KFDK7LGF7TT - - 4014 89051462 "
                + "IAH-20070907231717-00457-heritrix2.nb.no.arc.gz", CdxLineFormat.CDX11LINE);

        BiFunctionProcessor fp = new BiFunctionProcessor();
        fp.addFunction(new BiFunction() {
            @Override
            public CdxRecord apply(CdxRecord previousLine, CdxRecord currentLine) {
                if (previousLine == null || !currentLine.equals(previousLine)) {
                    return currentLine;
                } else {
                    return null;
                }
            }

        });

        MockCdxIterator iter = new MockCdxIterator();
        iter.add(line1).add(line2).add(line2).add(line2);

        CdxIterator processedIterator = fp.processorIterator(iter);

        assertThat(processedIterator).hasSize(2);
    }

    @Test
    public void testProcessorIteratorWithCollapse() {
        CdxFormat format = new CdxLineFormat(' ', FieldName.URI_KEY, FieldName.TIMESTAMP);

        CdxRecord line11 = CdxRecordFactory
                .create("no,dagbladet)/spiller_2519.html 20070908002541", format);
        CdxRecord line12 = CdxRecordFactory
                .create("no,dagbladet)/spiller_2520.html 20070908002532", format);
        CdxRecord line13 = CdxRecordFactory
                .create("no,dagbladet)/spiller_2521.html 20080908002533", format);

        CdxRecord line21 = CdxRecordFactory
                .create("no,dagbladet)/spiller_2519.html 20070908002540", format);
        CdxRecord line22 = CdxRecordFactory
                .create("no,dagbladet)/spiller_2520.html 20070908002533", format);
        CdxRecord line23 = CdxRecordFactory
                .create("no,dagbladet)/spiller_2521.html 20080908002534", format);
        CdxRecord line24 = CdxRecordFactory
                .create("no,dagbladet)/spiller_2522.html 20090908002534", format);

        CollapseFieldProvider cf = new CollapseFieldProvider(
                Collections.singletonList(FieldName.TIMESTAMP + ":4"));

        BiFunctionProcessor fp = new BiFunctionProcessor();
        fp.addFunctionProvider(cf);

        MockCdxIterator iter1 = new MockCdxIterator()
                .add(line11).add(line12).add(line13);

        MockCdxIterator iter2 = new MockCdxIterator()
                .add(line21).add(line22).add(line23).add(line24);

        MultiCdxIterator multiIter = new MultiCdxIterator(false, false, iter1, iter2);
        CdxIterator processedIterator = fp.processorIterator(multiIter);

        assertThat(processedIterator).hasSize(3).contains(line21, line13, line24);
        System.out.println(line21);
        System.out.println(line13);
        System.out.println(line24);
    }

    @Test
    public void testProcessorIteratorWithCollapse2() {
        CdxFormat format = new CdxLineFormat(' ', FieldName.URI_KEY, FieldName.TIMESTAMP);

        CdxRecord line1 = CdxRecordFactory.create("ab 00", format);
        CdxRecord line2 = CdxRecordFactory.create("ab 01", format);
        CdxRecord line3 = CdxRecordFactory.create("ac 00", format);
        CdxRecord line4 = CdxRecordFactory.create("ac 01", format);

        CollapseFieldProvider cf1 = new CollapseFieldProvider(
                Collections.singletonList(FieldName.URI_KEY.toString()));
        CollapseFieldProvider cf2 = new CollapseFieldProvider(
                Collections.singletonList(FieldName.TIMESTAMP.toString()));

        BiFunctionProcessor fp = new BiFunctionProcessor();
        fp.addFunctionProvider(cf1).addFunctionProvider(cf2);

        MockCdxIterator iter = new MockCdxIterator()
                .add(line1).add(line2).add(line3).add(line4);

        CdxIterator processedIterator = fp.processorIterator(iter);

        assertThat(processedIterator).hasSize(1).contains(line1);
    }

    @Test
    public void testProcessorIteratorWithCollapse3() {
        CdxFormat format = new CdxLineFormat(' ', FieldName.URI_KEY, FieldName.TIMESTAMP);

        CdxRecord line1 = CdxRecordFactory.create("ab 00", format);
        CdxRecord line2 = CdxRecordFactory.create("ab 01", format);
        CdxRecord line3 = CdxRecordFactory.create("ac 00", format);
        CdxRecord line4 = CdxRecordFactory.create("ac 01", format);

        CollapseFieldProvider cf1 = new CollapseFieldProvider(
                Collections.singletonList(FieldName.URI_KEY.toString()));
        CollapseFieldProvider cf2 = new CollapseFieldProvider(
                Collections.singletonList(FieldName.TIMESTAMP.toString()));

        BiFunctionProcessor fp1 = new BiFunctionProcessor();
        BiFunctionProcessor fp2 = new BiFunctionProcessor();
        fp1.addFunctionProvider(cf1);
        fp2.addFunctionProvider(cf2);

        MockCdxIterator iter = new MockCdxIterator()
                .add(line1).add(line2).add(line3).add(line4);

        CdxIterator processedIterator = fp2.processorIterator(fp1.processorIterator(iter));

        assertThat(processedIterator).hasSize(1).contains(line1).hasToString("[ab 00]");
    }

    @Test
    public void testProcessorIteratorWithCollapseFromFile() throws URISyntaxException, IOException {
        CollapseFieldProvider cf = new CollapseFieldProvider(
                Collections.singletonList(FieldName.TIMESTAMP + ":6"));

        ArrayList<Processor> processors = new ArrayList<>();
        BiFunctionProcessor fp = new BiFunctionProcessor();
        processors.add(fp);
        fp.addFunctionProvider(cf);

        CdxSource cdxSource = CdxSourceFactory.getCdxSource("cdxfile:src/test/resources/cdxfile3.cdx");

        SearchKey key = new SearchKey();
        SearchResult result = cdxSource.search(key, processors, false);

//        assertThat(result).hasSize(113);
        assertThat(result).hasSize(2);

        ArrayList<String> collapseStrings = new ArrayList<>();
        collapseStrings.add(FieldName.TIMESTAMP + ":6");
        collapseStrings.add(FieldName.URI_KEY + ":10");
        cf = new CollapseFieldProvider(collapseStrings);

        processors = new ArrayList<>();
        fp = new BiFunctionProcessor();
        processors.add(fp);
        fp.addFunctionProvider(cf);

        result = cdxSource.search(key, processors, false);

//        assertThat(result).hasSize(113);
        assertThat(result).hasSize(2);
    }

    @Test
    public void testProcessorIteratorWithCollapseFromTwoFiles() throws URISyntaxException, IOException {
        CollapseFieldProvider cf = new CollapseFieldProvider(
                Collections.singletonList(FieldName.TIMESTAMP + ":6"));

        ArrayList<Processor> processors = new ArrayList<>();
        BiFunctionProcessor fp = new BiFunctionProcessor();
        processors.add(fp);
        fp.addFunctionProvider(cf);

        CdxSource cdxSource1 = CdxSourceFactory.getCdxSource("cdxfile:src/test/resources/cdxfile4.cdx");
        CdxSource cdxSource2 = CdxSourceFactory.getCdxSource("cdxfile:src/test/resources/cdxfile5.cdx");

        SearchKey key = new SearchKey();
        MultiCdxSource cdxSource = new MultiCdxSource(cdxSource1, cdxSource2);
        SearchResult result = cdxSource.search(key, processors, false);

//        assertThat(result).hasSize(113);
        assertThat(result).hasSize(2);

        ArrayList<String> collapseStrings = new ArrayList<>();
        collapseStrings.add(FieldName.TIMESTAMP + ":6");
        collapseStrings.add(FieldName.URI_KEY + ":10");
        cf = new CollapseFieldProvider(collapseStrings);

        processors = new ArrayList<>();
        fp = new BiFunctionProcessor();
        processors.add(fp);
        fp.addFunctionProvider(cf);

//        sd = CdxSourceFactory.getDescriptor("cdxfile",
//                ClassLoader.getSystemResource("cdxfile3.cdx").toURI(), null);
//
//        cdxSource = new BlockCdxSource(sd);
        result = cdxSource.search(key, processors, false);

//        assertThat(result).hasSize(113);
        assertThat(result).hasSize(2);
    }

}
