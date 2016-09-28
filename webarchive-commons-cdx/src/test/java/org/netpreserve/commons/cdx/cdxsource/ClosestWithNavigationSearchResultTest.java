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
package org.netpreserve.commons.cdx.cdxsource;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.netpreserve.commons.cdx.CdxFormat;
import org.netpreserve.commons.cdx.CdxSource;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.SearchKeyTemplate;
import org.netpreserve.commons.cdx.SearchResult;
import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Test of class ClosestWithNavigationCdxIterator.
 */
public class ClosestWithNavigationSearchResultTest {

    /**
     * Test of next method, of class ClosestWithNavigationCdxIterator.
     * <p>
     * @throws java.lang.Exception exception is not expected
     */
    @Test
    public void testNext() throws Exception {
        Path path = Paths.get(ClassLoader.getSystemResource("closestTest.cdxj").toURI());
        try (SourceDescriptor sourceDescriptor = new CdxFileDescriptor(path);) {
            CdxFormat format = sourceDescriptor.getInputFormat();
            SearchKeyTemplate searchKey = new SearchKeyTemplate().uri("www.adobe.com/favicon.ico");
            String surtKey = "(com,adobe,)/favicon.ico";

            CdxSource cdxSource = new BlockCdxSource(sourceDescriptor);
            SearchResult searchResult;

            // Test with timestamp equal to one of the lines
            searchResult = new ClosestWithNavigationSearchResult(cdxSource, searchKey,
                    VariablePrecisionDateTime.valueOf("2007-09-04T15:04:45"), null);
            assertThat(searchResult).hasSize(4)
                    .extracting(r -> {
                        return new Tuple(
                                r.get(FieldName.TIMESTAMP).toString(),
                                r.get(FieldName.forName("rel")).toString());
                    })
                    .containsExactly(
                            new Tuple("2007-08-21T13:31:36Z", "first"),
                            new Tuple("2007-08-21T13:31:36Z", "prev"),
                            new Tuple("2007-09-04T15:04:45Z", "memento"),
                            new Tuple("2007-09-04T15:04:45Z", "last"));

            // Test with timestamp in between the lines
            searchResult = new ClosestWithNavigationSearchResult(cdxSource, searchKey,
                    VariablePrecisionDateTime.valueOf("20070827225413"), null);
            assertThat(searchResult).hasSize(4)
                    .extracting(r -> {
                        return new Tuple(
                                r.get(FieldName.TIMESTAMP).toString(),
                                r.get(FieldName.forName("rel")).toString());
                    })
                    .containsExactly(
                            new Tuple("2007-08-21T13:31:36Z", "first"),
                            new Tuple("2007-08-21T13:31:36Z", "memento"),
                            new Tuple("2007-09-04T15:04:45Z", "next"),
                            new Tuple("2007-09-04T15:04:45Z", "last"));

            // Test with timestamp earlier than all the lines
            searchResult = new ClosestWithNavigationSearchResult(cdxSource, searchKey,
                    VariablePrecisionDateTime.valueOf("20060823173549"), null);
            assertThat(searchResult).hasSize(4)
                    .extracting(r -> {
                        return new Tuple(
                                r.get(FieldName.TIMESTAMP).toString(),
                                r.get(FieldName.forName("rel")).toString());
                    })
                    .containsExactly(
                            new Tuple("2007-08-21T13:31:36Z", "first"),
                            new Tuple("2007-08-21T13:31:36Z", "memento"),
                            new Tuple("2007-09-04T15:04:45Z", "next"),
                            new Tuple("2007-09-04T15:04:45Z", "last"));

            // Test with timestamp later than all the lines
            searchResult = new ClosestWithNavigationSearchResult(cdxSource, searchKey,
                    VariablePrecisionDateTime.valueOf("20090823173549"), null);
            assertThat(searchResult).hasSize(4)
                    .extracting(r -> {
                        return new Tuple(
                                r.get(FieldName.TIMESTAMP).toString(),
                                r.get(FieldName.forName("rel")).toString());
                    })
                    .containsExactly(
                            new Tuple("2007-08-21T13:31:36Z", "first"),
                            new Tuple("2007-08-21T13:31:36Z", "prev"),
                            new Tuple("2007-09-04T15:04:45Z", "memento"),
                            new Tuple("2007-09-04T15:04:45Z", "last"));
        }
    }

}
