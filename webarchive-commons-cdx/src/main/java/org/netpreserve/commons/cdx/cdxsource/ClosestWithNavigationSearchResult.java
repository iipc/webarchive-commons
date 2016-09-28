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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;
import org.netpreserve.commons.util.datetime.DateTimeRange;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.CdxSource;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.SearchKeyTemplate;
import org.netpreserve.commons.cdx.SearchResult;
import org.netpreserve.commons.cdx.cdxrecord.UnconnectedCdxRecord;
import org.netpreserve.commons.cdx.json.StringValue;
import org.netpreserve.commons.cdx.processor.Processor;

/**
 * A SearchResult which is used to find the closest record to a timestamp.
 * <p>
 * In addition this SearchResult will also return the first and last record for the URI and the record immediately
 * before and after the closest record.
 * <p>
 * The result should contain enough information to create a Memento timegate response.
 */
public class ClosestWithNavigationSearchResult extends AbstractSearchResult {

    final CdxSource source;

    final SearchKeyTemplate key;

    final VariablePrecisionDateTime timestamp;

    final List<Processor> processors;

    /**
     * Construct a new ClosestWithNavigationSearchResult.
     * <p>
     * @param source the CdxSource to query
     * @param key the key to use for search
     * @param timestamp the timestamp to find the closest record to
     * @param processors processors to apply before sorting, might be empty or null
     */
    public ClosestWithNavigationSearchResult(final CdxSource source, final SearchKeyTemplate key,
            final VariablePrecisionDateTime timestamp, final List<Processor> processors) {
        this.source = source;
        this.key = key;
        this.timestamp = timestamp;
        this.processors = processors;
    }

    @Override
    protected CdxIterator newIterator() {
        return new ClosestWithNavigationCdxIterator(source, key, timestamp, processors);
    }

    /**
     * The iterator.
     */
    private class ClosestWithNavigationCdxIterator implements CdxIterator {

        CdxRecord nextLine;

        final Iterator<CdxRecord> resultIterator;

        /**
         * Construct a new ClosestWithNavigationCdxIterator.
         * <p>
         * @param source the CdxSource to wrap
         * @param key the key containing the Uri to search for
         * @param timeStamp the timeStamp to get closest to
         * @param processors processors to apply before sorting, might be empty
         */
        ClosestWithNavigationCdxIterator(final CdxSource source, final SearchKeyTemplate key,
                final VariablePrecisionDateTime timeStamp, final List<Processor> processors) {

            if (key.getMatchType() != SearchKeyTemplate.UriMatchType.EXACT) {
                throw new IllegalArgumentException("Closest match not allowed for wildcard uri");
            }

            SearchKeyTemplate outerBoundsKey = key.dateRange(null);

            SearchKeyTemplate forwardKey = key.dateRange(DateTimeRange.start(timeStamp));
            SearchKeyTemplate backwardKey = key.dateRange(DateTimeRange.end(timeStamp));

            try (SearchResult forwardResult = source.search(forwardKey, processors, false);
                    SearchResult backwardResult = source.search(backwardKey, processors, true);
                    SearchResult firstResult = source.search(outerBoundsKey, processors, false);
                    SearchResult lastResult = source.search(outerBoundsKey, processors, true);
                    CdxIterator forwardIterator = forwardResult.iterator();
                    CdxIterator backwardIterator = backwardResult.iterator();
                    CdxIterator firstIterator = firstResult.iterator();
                    CdxIterator lastIterator = lastResult.iterator();) {

                List<CdxRecord> results = new ArrayList<>();
                if (firstIterator.hasNext()) {
                    results.add(new UnconnectedCdxRecord(firstIterator.next()).set(FieldName.forName("rel"),
                            StringValue.valueOf("first")));
                }
                if (lastIterator.hasNext()) {
                    findPrevClosestAndNext(results, timestamp, forwardIterator, backwardIterator);
                    results.add(new UnconnectedCdxRecord(lastIterator.next()).set(FieldName.forName("rel"),
                            StringValue.valueOf("last")));
                }

                resultIterator = results.iterator();
            }
        }

        @Override
        public CdxRecord next() {
            if (nextLine != null || hasNext()) {
                CdxRecord line = nextLine;
                nextLine = null;
                return line;
            } else {
                return null;
            }
        }

        @Override
        public CdxRecord peek() {
            if (hasNext()) {
                return nextLine;
            } else {
                return null;
            }
        }

        @Override
        public boolean hasNext() {
            if (nextLine != null) {
                return true;
            }

            if (resultIterator.hasNext()) {
                nextLine = resultIterator.next();
                return true;
            }

            return false;
        }

        /**
         * Find the record which is closest to the timestamp and also the previous, next, first and last records.
         * <p>
         * @param results the list to add the results to
         * @param timeStamp the timestamp to find the closest record to
         * @param forwardIterator an iterator initialized to move forward from the timestamp
         * @param backwardIterator an iterator initialized to move backward from the timestamp
         */
        private void findPrevClosestAndNext(final List<CdxRecord> results, final VariablePrecisionDateTime timeStamp,
                final CdxIterator forwardIterator, final CdxIterator backwardIterator) {

            CdxRecord nextForwardCandidate = forwardIterator.next();
            CdxRecord nextBackwardCandidate = backwardIterator.next();

            if (nextForwardCandidate == null && nextBackwardCandidate == null) {
                return;
            }

            CdxRecord prev;
            CdxRecord closest;
            CdxRecord next;

            if (nextForwardCandidate == null
                    || nextBackwardCandidate != null
                    && nextForwardCandidate.get(FieldName.TIMESTAMP).getValue().distanceTo(timeStamp)
                    .compareTo(nextBackwardCandidate.get(FieldName.TIMESTAMP).getValue().distanceTo(timeStamp)) > 0) {
                closest = nextBackwardCandidate;
                next = nextForwardCandidate;
                prev = backwardIterator.next();
            } else {
                closest = nextForwardCandidate;
                next = forwardIterator.next();
                prev = nextBackwardCandidate;
            }

            if (prev != null) {
                results.add(new UnconnectedCdxRecord(prev).set(FieldName.forName("rel"), StringValue.valueOf("prev")));
            }
            if (closest != null) {
                results.add(new UnconnectedCdxRecord(closest).set(FieldName.forName("rel"),
                        StringValue.valueOf("memento")));
            }
            if (next != null) {
                results.add(new UnconnectedCdxRecord(next).set(FieldName.forName("rel"), StringValue.valueOf("next")));
            }
        }

        @Override
        public CdxIterator limit(long maxSize) {
            return new SizeLimitingCdxIterator(this, maxSize);
        }

        @Override
        public void close() {
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}
