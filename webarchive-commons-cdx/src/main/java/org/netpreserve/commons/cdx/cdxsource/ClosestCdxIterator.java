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

import java.time.Duration;
import java.util.List;

import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;
import org.netpreserve.commons.util.datetime.DateTimeRange;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.CdxSource;
import org.netpreserve.commons.cdx.SearchKey;
import org.netpreserve.commons.cdx.SearchResult;
import org.netpreserve.commons.cdx.processor.Processor;

/**
 * An iterator over a CdxSource with CdxLines sorted by distance to a timeStamp.
 */
public class ClosestCdxIterator implements CdxIterator {

    final SearchResult forwardResult;

    final SearchResult backwardResult;

    final CdxIterator forwardIterator;

    final CdxIterator backwardIterator;

    final VariablePrecisionDateTime timeStamp;

    CdxRecord nextLine;

    Candidate nextForwardCandidate;

    Candidate nextBackwardCandidate;

    /**
     * Construct a new ClosestCdxIterator.
     * <p>
     * @param source the CdxSource to wrap
     * @param key the key containing the Uri to search for
     * @param timeStamp the timeStamp to sort around
     * @param processors processors to apply before sorting, might be empty
     */
    public ClosestCdxIterator(CdxSource source, SearchKey key, VariablePrecisionDateTime timeStamp,
            List<Processor> processors) {

        if (key.getMatchType() != SearchKey.UriMatchType.EXACT) {
            throw new IllegalArgumentException("Closest match not allowed for wildcard uri");
        }

        SearchKey forwardKey = key.clone().dateRange(DateTimeRange.start(timeStamp));
        SearchKey backwardKey = key.clone().dateRange(DateTimeRange.end(timeStamp));

        forwardResult = source.search(forwardKey, processors, false);
        backwardResult = source.search(backwardKey, processors, true);

        forwardIterator = forwardResult.iterator();
        backwardIterator = backwardResult.iterator();
        this.timeStamp = timeStamp;
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

        if (nextForwardCandidate == null && forwardIterator.hasNext()) {
            nextForwardCandidate = new Candidate(forwardIterator.next());
        }
        if (nextBackwardCandidate == null && backwardIterator.hasNext()) {
            nextBackwardCandidate = new Candidate(backwardIterator.next());
        }

        if (nextForwardCandidate == null && nextBackwardCandidate == null) {
            return false;
        }

        if (nextForwardCandidate == null
                || nextForwardCandidate.greaterDistanceThan(nextBackwardCandidate)) {
            nextLine = nextBackwardCandidate.line;
            nextBackwardCandidate = null;
        } else {
            nextLine = nextForwardCandidate.line;
            nextForwardCandidate = null;
        }

        return true;
    }

    @Override
    public CdxIterator limit(long maxSize) {
        return new SizeLimitingCdxIterator(this, maxSize);
    }

    @Override
    public void close() {
        forwardIterator.close();
        forwardResult.close();
        backwardIterator.close();
        backwardResult.close();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * A candidate for the next value.
     * <p>
     * These are chosen based on distance to the time stamp
     */
    private class Candidate {

        final CdxRecord line;

        final Duration distance;

        /**
         * Construct a new candidate.
         * <p>
         * @param line the CdxRecord to be turned into a Candidate
         */
        Candidate(CdxRecord line) {
            this.line = line;
            this.distance = line.getKey().getTimeStamp().getValue().distanceTo(timeStamp);
        }

        /**
         * Compare distances.
         * <p>
         * @param o the candidate to compare this to
         * @return true if this has greater distance than o
         */
        public boolean greaterDistanceThan(Candidate o) {
            if (o == null) {
                return false;
            }
            return distance.compareTo(o.distance) > 0;
        }

        @Override
        public String toString() {
            return "Candidate{" + "line=" + line.getKey() + ", distance=" + distance + '}';
        }

    }
}
