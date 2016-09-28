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

import org.netpreserve.commons.cdx.SearchKeyTemplate;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.netpreserve.commons.cdx.CdxFormat;
import org.netpreserve.commons.cdx.cdxrecord.CdxLineFormat;
import org.netpreserve.commons.cdx.cdxrecord.CdxjLineFormat;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.util.datetime.DateFormat;
import org.netpreserve.commons.util.datetime.DateTimeRange;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ByteBufferUtil class.
 */
public class ByteBufferUtilTest {

    /**
     * Test of nextField method, of class ByteBufferUtils.
     */
    @Test
    public void testNextField() {
        ByteBuffer cdxSrc = ByteBuffer.wrap("(no,nb,)/index.html 2007-08-21T13:41:20Z response\n"
                .getBytes(StandardCharsets.UTF_8));
        ByteBuffer cdxjSrcNoArray = cdxSrc.asReadOnlyBuffer();
        int dateOffset = 20;
        int recordTypeOffset = 41;

        assertThat(cdxSrc.hasArray()).isTrue();
        assertThat(cdxjSrcNoArray.hasArray()).isFalse();

        assertThat(ByteBufferUtil.nextField(cdxSrc)).isTrue();
        assertThat(cdxSrc.position()).isEqualTo(dateOffset);
        assertThat(ByteBufferUtil.nextField(cdxSrc)).isTrue();
        assertThat(cdxSrc.position()).isEqualTo(recordTypeOffset);
        assertThat(ByteBufferUtil.nextField(cdxSrc)).isFalse();
        assertThat(cdxSrc.position()).isEqualTo(cdxSrc.limit());

        assertThat(ByteBufferUtil.nextField(cdxjSrcNoArray)).isTrue();
        assertThat(cdxjSrcNoArray.position()).isEqualTo(dateOffset);
        assertThat(ByteBufferUtil.nextField(cdxjSrcNoArray)).isTrue();
        assertThat(cdxjSrcNoArray.position()).isEqualTo(recordTypeOffset);
        assertThat(ByteBufferUtil.nextField(cdxjSrcNoArray)).isFalse();
        assertThat(cdxjSrcNoArray.position()).isEqualTo(cdxjSrcNoArray.limit());
    }

    /**
     * Test of startsWith method, of class ByteBufferUtils.
     * <p>
     * Using CDXJ formatted src buffer
     */
    @Test
    public void testStartsWithCdxj() {
        ByteBuffer cdxSrc = ByteBuffer.wrap("(no,nb,)/index.html 2007-08-21T13:41:20Z response\n"
                .getBytes(StandardCharsets.UTF_8));
        CdxFormat cdxFormat = CdxjLineFormat.DEFAULT_CDXJLINE;
        testStartsWith(cdxSrc, cdxFormat);
    }

    /**
     * Test of startsWith method, of class ByteBufferUtils.
     * <p>
     * Using legacy CDX formatted src buffer
     */
    @Test
    public void testStartsWithCdx() {
        ByteBuffer cdxSrc = ByteBuffer.wrap("no,nb)/index.html 20070821134120\n".getBytes(StandardCharsets.UTF_8));
        CdxFormat cdxFormat = CdxLineFormat.CDX09LINE;
        testStartsWith(cdxSrc, cdxFormat);
    }

    /**
     * Do the checks.
     * <p>
     * @param cdxSrc the cdx src buffer to check
     * @param cdxFormat the cdx format of the src
     */
    private void testStartsWith(ByteBuffer cdxSrc, CdxFormat cdxFormat) {
        ByteBuffer cdxSrcNoArray = cdxSrc.asReadOnlyBuffer();
        ByteBufferUtil.nextField(cdxSrc);
        int uriOffset = 0;
        int uriFieldEnd = cdxSrc.position() - 1;
        int dateOffset = cdxSrc.position();
        ByteBufferUtil.nextField(cdxSrc);
        int dateFieldEnd = cdxSrc.position() - 1;

        assertThat(cdxSrc.hasArray()).isTrue();
        assertThat(cdxSrcNoArray.hasArray()).isFalse();

        UriBuilderConfig uriConfig = cdxFormat.getKeyUriFormat();
        SearchKeyTemplate.UriMatchType matchType = SearchKeyTemplate.UriMatchType.EXACT;
        DateFormat dateFormat = cdxFormat.getKeyDateFormat();
        DateTimeRange dateRange = DateTimeRange.ofRangeExpression("2007-08-21T13:41:20;2008");
        SearchKeyFilter filter;

        cdxSrc.position(uriOffset);
        cdxSrcNoArray.position(uriOffset);
        filter = SearchKeyFilter.newUriFilter("http://www.nb.no", uriConfig, matchType);
        assertThat(ByteBufferUtil.startsWith(cdxSrc, filter)).isTrue();
        assertThat(ByteBufferUtil.startsWith(cdxSrcNoArray, filter)).isTrue();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(uriFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(uriFieldEnd);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        filter = SearchKeyFilter.newDateFilter(dateRange.getStart(), dateFormat);
        assertThat(ByteBufferUtil.startsWith(cdxSrc, filter)).isTrue();
        assertThat(ByteBufferUtil.startsWith(cdxSrcNoArray, filter)).isTrue();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        filter = SearchKeyFilter.newDateFilter(dateRange.getEnd(), dateFormat);
        assertThat(ByteBufferUtil.startsWith(cdxSrc, filter)).isFalse();
        assertThat(ByteBufferUtil.startsWith(cdxSrcNoArray, filter)).isFalse();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd);

        cdxSrc.position(uriOffset);
        cdxSrcNoArray.position(uriOffset);
        filter = SearchKeyFilter.newUriFilter("http://www.nb.no/foo", uriConfig, matchType);
        assertThat(ByteBufferUtil.startsWith(cdxSrc, filter)).isFalse();
        assertThat(ByteBufferUtil.startsWith(cdxSrcNoArray, filter)).isFalse();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(uriFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(uriFieldEnd);

        cdxSrc.position(uriOffset);
        cdxSrcNoArray.position(uriOffset);
        filter = SearchKeyFilter.newUriFilter("http://www.nb.no/index.html", uriConfig, matchType);
        assertThat(ByteBufferUtil.startsWith(cdxSrc, filter)).isTrue();
        assertThat(ByteBufferUtil.startsWith(cdxSrcNoArray, filter)).isTrue();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(uriFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(uriFieldEnd);
    }

    /**
     * Test of compareToFilter method, of class ByteBufferUtils.
     * <p>
     * Using CDXJ formatted src buffer
     */
    @Test
    public void testCompareToFilterCdxj() {
        ByteBuffer cdxSrc = ByteBuffer.wrap("(no,nb,)/index.html 2007-08-21T13:41:20Z response\n"
                .getBytes(StandardCharsets.UTF_8));
        CdxFormat cdxFormat = CdxjLineFormat.DEFAULT_CDXJLINE;
        testCompareToFilter(cdxSrc, cdxFormat);
    }

    /**
     * Test of compareToFilter method, of class ByteBufferUtils.
     * <p>
     * Using legacy CDX formatted src buffer
     */
    @Test
    public void testCompareToFilterCdx() {
        ByteBuffer cdxSrc = ByteBuffer.wrap("no,nb)/index.html 20070821134120\n".getBytes(StandardCharsets.UTF_8));
        CdxFormat cdxFormat = CdxLineFormat.CDX09LINE;
        testCompareToFilter(cdxSrc, cdxFormat);
    }

    /**
     * Do the checks.
     * <p>
     * @param cdxSrc the cdx src buffer to check
     * @param cdxFormat the cdx format of the src
     */
    public void testCompareToFilter(ByteBuffer cdxSrc, CdxFormat cdxFormat) {
        ByteBuffer cdxSrcNoArray = cdxSrc.asReadOnlyBuffer();
        ByteBufferUtil.nextField(cdxSrc);
        int uriOffset = 0;
        int uriFieldEnd = cdxSrc.position() - 1;
        int dateOffset = cdxSrc.position();
        ByteBufferUtil.nextField(cdxSrc);
        int dateFieldEnd = cdxSrc.position() - 1;

        assertThat(cdxSrc.hasArray()).isTrue();
        assertThat(cdxSrcNoArray.hasArray()).isFalse();

        UriBuilderConfig uriConfig = cdxFormat.getKeyUriFormat();
        DateFormat dateFormat = cdxFormat.getKeyDateFormat();
        SearchKeyFilter filter;

        cdxSrc.position(uriOffset);
        cdxSrcNoArray.position(uriOffset);
        filter = SearchKeyFilter.newUriFilter("http://www.nb.no", uriConfig, SearchKeyTemplate.UriMatchType.EXACT);
        assertThat(ByteBufferUtil.compareToFilter(cdxSrc, filter)).isPositive();
        assertThat(ByteBufferUtil.compareToFilter(cdxSrcNoArray, filter)).isPositive();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);

        cdxSrc.position(uriOffset);
        cdxSrcNoArray.position(uriOffset);
        filter = SearchKeyFilter.newUriFilter("http://www.nb.no/jkl", uriConfig, SearchKeyTemplate.UriMatchType.EXACT);
        assertThat(ByteBufferUtil.compareToFilter(cdxSrc, filter)).isNegative();
        assertThat(ByteBufferUtil.compareToFilter(cdxSrcNoArray, filter)).isNegative();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);

        cdxSrc.position(uriOffset);
        cdxSrcNoArray.position(uriOffset);
        filter = SearchKeyFilter.newUriFilter("http://www.nb.no/index.html", uriConfig, SearchKeyTemplate.UriMatchType.EXACT);
        assertThat(ByteBufferUtil.compareToFilter(cdxSrc, filter)).isZero();
        assertThat(ByteBufferUtil.compareToFilter(cdxSrcNoArray, filter)).isZero();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);

        cdxSrc.position(uriOffset);
        cdxSrcNoArray.position(uriOffset);
        filter = SearchKeyFilter.newUriFilter("http://www.nb.no/abc.html", uriConfig, SearchKeyTemplate.UriMatchType.EXACT);
        assertThat(ByteBufferUtil.compareToFilter(cdxSrc, filter)).isPositive();
        assertThat(ByteBufferUtil.compareToFilter(cdxSrcNoArray, filter)).isPositive();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);

        cdxSrc.position(uriOffset);
        cdxSrcNoArray.position(uriOffset);
        filter = SearchKeyFilter.newUriFilter("http://www.nb.no/jkl", uriConfig, SearchKeyTemplate.UriMatchType.PATH);
        assertThat(ByteBufferUtil.compareToFilter(cdxSrc, filter)).isNegative();
        assertThat(ByteBufferUtil.compareToFilter(cdxSrcNoArray, filter)).isNegative();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);

        cdxSrc.position(uriOffset);
        cdxSrcNoArray.position(uriOffset);
        filter = SearchKeyFilter.newUriFilter("http://www.nb.no/index.html", uriConfig, SearchKeyTemplate.UriMatchType.PATH);
        assertThat(ByteBufferUtil.compareToFilter(cdxSrc, filter)).isZero();
        assertThat(ByteBufferUtil.compareToFilter(cdxSrcNoArray, filter)).isZero();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);

        cdxSrc.position(uriOffset);
        cdxSrcNoArray.position(uriOffset);
        filter = SearchKeyFilter.newUriFilter("http://www.nb.no/abc.html", uriConfig, SearchKeyTemplate.UriMatchType.PATH);
        assertThat(ByteBufferUtil.compareToFilter(cdxSrc, filter)).isPositive();
        assertThat(ByteBufferUtil.compareToFilter(cdxSrcNoArray, filter)).isPositive();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(uriFieldEnd).isGreaterThan(uriOffset);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        filter = SearchKeyFilter.newDateFilter(
                DateTimeRange.ofRangeExpression("2007-08-21T13:41:20").getStart(), dateFormat);
        assertThat(ByteBufferUtil.compareToFilter(cdxSrc, filter)).isZero();
        assertThat(ByteBufferUtil.compareToFilter(cdxSrcNoArray, filter)).isZero();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd).isGreaterThan(dateOffset);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd).isGreaterThan(dateOffset);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        filter = SearchKeyFilter.newDateFilter(
                DateTimeRange.ofRangeExpression("2007-08-21T13:41:20.01").getStart(), dateFormat);
        assertThat(ByteBufferUtil.compareToFilter(cdxSrc, filter)).isNegative();
        assertThat(ByteBufferUtil.compareToFilter(cdxSrcNoArray, filter)).isNegative();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd).isGreaterThan(dateOffset);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd).isGreaterThan(dateOffset);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        filter = SearchKeyFilter.newDateFilter(
                DateTimeRange.ofRangeExpression("2007-08-21T13:41:19").getStart(), dateFormat);
        assertThat(ByteBufferUtil.compareToFilter(cdxSrc, filter)).isPositive();
        assertThat(ByteBufferUtil.compareToFilter(cdxSrcNoArray, filter)).isPositive();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd).isGreaterThan(dateOffset);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd).isGreaterThan(dateOffset);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        filter = SearchKeyFilter.newDateFilter(
                DateTimeRange.ofRangeExpression("2007-08-21T13:41:19.01").getStart(), dateFormat);
        assertThat(ByteBufferUtil.compareToFilter(cdxSrc, filter)).isPositive();
        assertThat(ByteBufferUtil.compareToFilter(cdxSrcNoArray, filter)).isPositive();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd).isGreaterThan(dateOffset);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd).isGreaterThan(dateOffset);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        filter = SearchKeyFilter.newDateFilter(
                DateTimeRange.ofRangeExpression("2007-08-21T13:41:21").getStart(), dateFormat);
        assertThat(ByteBufferUtil.compareToFilter(cdxSrc, filter)).isNegative();
        assertThat(ByteBufferUtil.compareToFilter(cdxSrcNoArray, filter)).isNegative();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd).isGreaterThan(dateOffset);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd).isGreaterThan(dateOffset);
    }

    /**
     * Test of between method, of class ByteBufferUtils.
     * <p>
     * Using CDXJ formatted src buffer
     */
    @Test
    public void testBetweenCdxj() {
        ByteBuffer cdxSrc = ByteBuffer.wrap("(no,nb,)/index.html 2007-08-21T13:41:20Z response\n"
                .getBytes(StandardCharsets.UTF_8));
        CdxFormat cdxFormat = CdxjLineFormat.DEFAULT_CDXJLINE;
        testBetween(cdxSrc, cdxFormat);
    }

    /**
     * Test of compareToFilter method, of class ByteBufferUtils.
     * <p>
     * Using legacy CDX formatted src buffer
     */
    @Test
    public void testBetweenCdx() {
        ByteBuffer cdxSrc = ByteBuffer.wrap("no,nb)/index.html 20070821134120\n".getBytes(StandardCharsets.UTF_8));
        CdxFormat cdxFormat = CdxLineFormat.CDX09LINE;
        testBetween(cdxSrc, cdxFormat);
    }

    /**
     * Do the checks.
     * <p>
     * @param cdxSrc the cdx src buffer to check
     * @param cdxFormat the cdx format of the src
     */
    public void testBetween(ByteBuffer cdxSrc, CdxFormat cdxFormat) {
        ByteBuffer cdxSrcNoArray = cdxSrc.asReadOnlyBuffer();
        ByteBufferUtil.nextField(cdxSrc);
        int dateOffset = cdxSrc.position();
        ByteBufferUtil.nextField(cdxSrc);
        int dateFieldEnd = cdxSrc.position() - 1;

        assertThat(cdxSrc.hasArray()).isTrue();
        assertThat(cdxSrcNoArray.hasArray()).isFalse();

        UriBuilderConfig uriConfig = cdxFormat.getKeyUriFormat();
        DateFormat dateFormat = cdxFormat.getKeyDateFormat();
        DateTimeRange dateRange;
        SearchKeyFilter fromFilter;
        SearchKeyFilter toFilter;

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        dateRange = DateTimeRange.ofRangeExpression("2007-08-21T13:41:20;2008");
        fromFilter = SearchKeyFilter.newDateFilter(dateRange.getStart(), dateFormat);
        toFilter = SearchKeyFilter.newDateFilter(dateRange.getEnd(), dateFormat);
        assertThat(ByteBufferUtil.between(cdxSrc, fromFilter, toFilter)).isTrue();
        assertThat(ByteBufferUtil.between(cdxSrcNoArray, fromFilter, toFilter)).isTrue();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        dateRange = DateTimeRange.ofRangeExpression("2007-08-21T13:41:19;2008");
        fromFilter = SearchKeyFilter.newDateFilter(dateRange.getStart(), dateFormat);
        toFilter = SearchKeyFilter.newDateFilter(dateRange.getEnd(), dateFormat);
        assertThat(ByteBufferUtil.between(cdxSrc, fromFilter, toFilter)).isTrue();
        assertThat(ByteBufferUtil.between(cdxSrcNoArray, fromFilter, toFilter)).isTrue();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        dateRange = DateTimeRange.ofRangeExpression("2007-08-21T13:41:21;2008");
        fromFilter = SearchKeyFilter.newDateFilter(dateRange.getStart(), dateFormat);
        toFilter = SearchKeyFilter.newDateFilter(dateRange.getEnd(), dateFormat);
        assertThat(ByteBufferUtil.between(cdxSrc, fromFilter, toFilter)).isFalse();
        assertThat(ByteBufferUtil.between(cdxSrcNoArray, fromFilter, toFilter)).isFalse();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        dateRange = DateTimeRange.ofRangeExpression("2007-08-21T13:41:20.001;2008");
        fromFilter = SearchKeyFilter.newDateFilter(dateRange.getStart(), dateFormat);
        toFilter = SearchKeyFilter.newDateFilter(dateRange.getEnd(), dateFormat);
        assertThat(ByteBufferUtil.between(cdxSrc, fromFilter, toFilter)).isFalse();
        assertThat(ByteBufferUtil.between(cdxSrcNoArray, fromFilter, toFilter)).isFalse();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        dateRange = DateTimeRange.ofRangeExpression("2007;2008");
        fromFilter = SearchKeyFilter.newDateFilter(dateRange.getStart(), dateFormat);
        toFilter = SearchKeyFilter.newDateFilter(dateRange.getEnd(), dateFormat);
        assertThat(ByteBufferUtil.between(cdxSrc, fromFilter, toFilter)).isTrue();
        assertThat(ByteBufferUtil.between(cdxSrcNoArray, fromFilter, toFilter)).isTrue();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        dateRange = DateTimeRange.ofRangeExpression("2007;2007-08-21T13:41:20Z");
        fromFilter = SearchKeyFilter.newDateFilter(dateRange.getStart(), dateFormat);
        toFilter = SearchKeyFilter.newDateFilter(dateRange.getEnd(), dateFormat);
        assertThat(ByteBufferUtil.between(cdxSrc, fromFilter, toFilter)).isFalse();
        assertThat(ByteBufferUtil.between(cdxSrcNoArray, fromFilter, toFilter)).isFalse();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        dateRange = DateTimeRange.ofRangeExpression("2007;2007-08-21T13:41:19.001Z");
        fromFilter = SearchKeyFilter.newDateFilter(dateRange.getStart(), dateFormat);
        toFilter = SearchKeyFilter.newDateFilter(dateRange.getEnd(), dateFormat);
        assertThat(ByteBufferUtil.between(cdxSrc, fromFilter, toFilter)).isFalse();
        assertThat(ByteBufferUtil.between(cdxSrcNoArray, fromFilter, toFilter)).isFalse();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd);

        cdxSrc.position(dateOffset);
        cdxSrcNoArray.position(dateOffset);
        dateRange = DateTimeRange.ofRangeExpression("2007;2007-08-21T13:41:20.001Z");
        fromFilter = SearchKeyFilter.newDateFilter(dateRange.getStart(), dateFormat);
        toFilter = SearchKeyFilter.newDateFilter(dateRange.getEnd(), dateFormat);
        assertThat(ByteBufferUtil.between(cdxSrc, fromFilter, toFilter)).isTrue();
        assertThat(ByteBufferUtil.between(cdxSrcNoArray, fromFilter, toFilter)).isTrue();
        assertThat(cdxSrc.position()).isLessThanOrEqualTo(dateFieldEnd);
        assertThat(cdxSrcNoArray.position()).isLessThanOrEqualTo(dateFieldEnd);
    }

}
