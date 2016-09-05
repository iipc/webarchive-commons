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

import java.util.List;

import org.netpreserve.commons.cdx.functions.Filter;
import org.netpreserve.commons.cdx.cdxsource.CdxIterator;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.HasUnparsedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processor taking a set of {@link Filter}'s and returning only those CDX lines matching all of the filters.
 */
public class FilterProcessor extends AbstractProcessor<Filter> {
    private static final Logger LOG = LoggerFactory.getLogger(FilterProcessor.class);

    @Override
    public CdxIterator processorIterator(CdxIterator wrappedIterator) {
        final List<Filter> filters = getInstanciatedFunctions();
        if (filters.isEmpty()) {
            return wrappedIterator;
        }

        return new AbstractProcessorIterator<Filter>(wrappedIterator) {

            @Override
            protected CdxRecord computeNext() {
                if (wrappedCdxIterator.hasNext()) {
                    CdxRecord input = wrappedCdxIterator.next();
                    boolean include = true;
                    for (Filter filter : filters) {
                        try {
                            if (!filter.include(input)) {
                                include = false;
                                break;
                            }
                        } catch (Exception e) {
                            // An error was thrown while fetching the next CdxRecord. Log a warning and skip to next.
                            if (input instanceof HasUnparsedData) {
                                LOG.warn("Error while processing: '"
                                        + String.valueOf(((HasUnparsedData) input).getUnparsed()) + "', skipping", e);
                            } else {
                                LOG.warn("Error while processing a record, skipping", e);
                            }
                            include = false;
                            break;
                        }
                    }
                    if (include) {
                        return input;
                    }
                    return null;
                } else {
                    return endOfData();
                }
            }

        };
    }

}
