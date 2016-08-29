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
package org.netpreserve.commons.cdx.cdxsource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.junit.Test;
import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;
import org.netpreserve.commons.cdx.CdxFormat;
import org.netpreserve.commons.cdx.cdxrecord.CdxLineFormat;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.CdxRecordFactory;
import org.netpreserve.commons.cdx.CdxSource;
import org.netpreserve.commons.cdx.SearchKey;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class ClosestCdxIteratorTest {

    private final CdxFormat format = CdxLineFormat.CDX11LINE;

    private final Comparator<CdxRecord> comparator = new CdxLineComparator();

    /**
     * Test of next method, of class ClosestCdxIterator.
     */
    @Test
    public void testNext() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("cdxfile3.cdx").toURI());
        try (SourceDescriptor sourceDescriptor = new CdxFileDescriptor(path);) {
            SearchKey searchKey = new SearchKey().uri("http://vg.no/din_verden/assets/images/himmel.gif");
            SearchKey missingUrl = new SearchKey().uri("http://vg.no/din_verden/assets/images/hinder.gif");

            CdxSource cdxSource = new BlockCdxSource(sourceDescriptor);
            CdxIterator it;

            // Test with url not in cdx
            it = new ClosestCdxIterator(cdxSource, missingUrl, VariablePrecisionDateTime.of("20070905173550"), null);
            assertThat(it).isEmpty();

            // Test with timestamp equal to one of the lines
            it = new ClosestCdxIterator(cdxSource, searchKey, VariablePrecisionDateTime.of("20070905173550"), null);
            assertThat(it).hasSize(2).usingElementComparator(comparator).containsExactly(
                    CdxRecordFactory.create(searchKey.getUri() + " 20070905173550", format),
                    CdxRecordFactory.create(searchKey.getUri() + " 20070822103939", format));

            // Test with timestamp in between the lines
            it = new ClosestCdxIterator(cdxSource, searchKey, VariablePrecisionDateTime.of("20070823173549"), null);
            assertThat(it).hasSize(2).usingElementComparator(comparator).containsExactly(
                    CdxRecordFactory.create(searchKey.getUri() + " 20070822103939", format),
                    CdxRecordFactory.create(searchKey.getUri() + " 20070905173550", format));

            // Test with timestamp earlier than all the lines
            it = new ClosestCdxIterator(cdxSource, searchKey, VariablePrecisionDateTime.of("20060823173549"), null);
            assertThat(it).hasSize(2).usingElementComparator(comparator).containsExactly(
                    CdxRecordFactory.create(searchKey.getUri() + " 20070822103939", format),
                    CdxRecordFactory.create(searchKey.getUri() + " 20070905173550", format));

            // Test with timestamp later than all the lines
            it = new ClosestCdxIterator(cdxSource, searchKey, VariablePrecisionDateTime.of("20090823173549"), null);
            assertThat(it).hasSize(2).usingElementComparator(comparator).containsExactly(
                    CdxRecordFactory.create(searchKey.getUri() + " 20070905173550", format),
                    CdxRecordFactory.create(searchKey.getUri() + " 20070822103939", format));
        }
    }

    @Test
    public void testNextCdxj() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("closestTest.cdxj").toURI());
        try (SourceDescriptor sourceDescriptor = new CdxFileDescriptor(path);) {
            SearchKey searchKey = new SearchKey().uri("www.adobe.com/favicon.ico");

            CdxSource cdxSource = new BlockCdxSource(sourceDescriptor);
            CdxIterator it;

            // Test with timestamp equal to one of the lines
            it = new ClosestCdxIterator(cdxSource, searchKey, VariablePrecisionDateTime.of("2007-09-04T15:04:45"), null);
            assertThat(it).hasSize(2).usingElementComparator(comparator).containsExactly(
                    CdxRecordFactory.create(searchKey.getUri() + " 2007-09-04T15:04:45", format),
                    CdxRecordFactory.create(searchKey.getUri() + " 2007-08-21T13:31:36", format));

            // Test with timestamp in between the lines
            it = new ClosestCdxIterator(cdxSource, searchKey, VariablePrecisionDateTime.of("20070827225413"), null);
            assertThat(it).hasSize(2).usingElementComparator(comparator).containsExactly(
                    CdxRecordFactory.create(searchKey.getUri() + " 2007-08-21T13:31:36", format),
                    CdxRecordFactory.create(searchKey.getUri() + " 2007-09-04T15:04:45", format));

            // Test with timestamp earlier than all the lines
            it = new ClosestCdxIterator(cdxSource, searchKey, VariablePrecisionDateTime.of("20060823173549"), null);
            assertThat(it).hasSize(2).usingElementComparator(comparator).containsExactly(
                    CdxRecordFactory.create(searchKey.getUri() + " 2007-08-21T13:31:36", format),
                    CdxRecordFactory.create(searchKey.getUri() + " 2007-09-04T15:04:45", format));

            // Test with timestamp later than all the lines
            it = new ClosestCdxIterator(cdxSource, searchKey, VariablePrecisionDateTime.of("20090823173549"), null);
            assertThat(it).hasSize(2).usingElementComparator(comparator).containsExactly(
                    CdxRecordFactory.create(searchKey.getUri() + " 2007-09-04T15:04:45", format),
                    CdxRecordFactory.create(searchKey.getUri() + " 2007-08-21T13:31:36", format));
        }
    }

    /**
     * Test of peek method, of class ClosestCdxIterator.
     */
    @Test
    public void testPeek() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("cdxfile3.cdx").toURI());
        try (SourceDescriptor sourceDescriptor = new CdxFileDescriptor(path);) {

            SearchKey searchKey = new SearchKey().uri("http://vg.no/din_verden/assets/images/himmel.gif");
            SearchKey missingUrl = new SearchKey().uri("http://vg.no/din_verden/assets/images/hinder.gif");

            CdxSource cdxSource = new BlockCdxSource(sourceDescriptor);
            CdxIterator it;

            it = new ClosestCdxIterator(cdxSource, missingUrl, VariablePrecisionDateTime.of("20070905173550"), null);
            assertThat((Comparable) it.peek()).isNull();

            it = new ClosestCdxIterator(cdxSource, searchKey, VariablePrecisionDateTime.of("20070905173550"), null);
            assertThat((Comparable) it.peek())
                    .isEqualByComparingTo(CdxRecordFactory.create(searchKey.getUri() + " 20070905173550", format));

            it = new ClosestCdxIterator(cdxSource, searchKey, VariablePrecisionDateTime.of("20070823173549"), null);
            assertThat((Comparable) it.peek())
                    .isEqualByComparingTo(CdxRecordFactory.create(searchKey.getUri() + " 20070822103939", format));
        }
    }

    /**
     * Test of hasNext method, of class ClosestCdxIterator.
     */
    @Test
    public void testHasNext() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("cdxfile3.cdx").toURI());
        try (SourceDescriptor sourceDescriptor = new CdxFileDescriptor(path);) {

            SearchKey searchKey = new SearchKey().uri("http://vg.no/din_verden/assets/images/himmel.gif");
            SearchKey missingUrl = new SearchKey().uri("http://vg.no/din_verden/assets/images/hinder.gif");

            CdxSource cdxSource = new BlockCdxSource(sourceDescriptor);
            CdxIterator it;

            it = new ClosestCdxIterator(cdxSource, missingUrl, VariablePrecisionDateTime.of("20070905173550"), null);
            assertThat(it.hasNext()).isFalse();

            it = new ClosestCdxIterator(cdxSource, searchKey, VariablePrecisionDateTime.of("20070905173550"), null);
            assertThat(it.hasNext()).isTrue();

            it = new ClosestCdxIterator(cdxSource, searchKey, VariablePrecisionDateTime.of("20070823173549"), null);
            assertThat(it.hasNext()).isTrue();
        }
    }

}
