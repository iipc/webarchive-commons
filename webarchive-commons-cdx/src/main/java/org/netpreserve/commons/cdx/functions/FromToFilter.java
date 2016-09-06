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

import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.util.datetime.DateTimeRange;

/**
 * A filter restricting the date range for a result.
 */
public class FromToFilter implements Filter {

    final DateTimeRange dateTimeRange;

    /**
     * Constructs a new date range filter.
     * <p>
     * @param dateTimeRange the date range to compare to
     */
    public FromToFilter(final DateTimeRange dateTimeRange) {
        this.dateTimeRange = dateTimeRange;
    }

    @Override
    public boolean include(CdxRecord line) {
        return dateTimeRange.contains(line.getKey().getTimeStamp().getValue());
    }

}
