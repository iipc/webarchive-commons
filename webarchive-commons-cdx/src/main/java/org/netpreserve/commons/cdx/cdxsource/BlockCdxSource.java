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
package org.netpreserve.commons.cdx.cdxsource;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.netpreserve.commons.cdx.processor.Processor;
import org.netpreserve.commons.cdx.CdxSource;
import org.netpreserve.commons.cdx.SearchKey;
import org.netpreserve.commons.cdx.SearchKeyTemplate;
import org.netpreserve.commons.cdx.SearchResult;

/**
 *
 */
public class BlockCdxSource implements CdxSource {

    final SourceDescriptor sourceDescriptor;

    public BlockCdxSource(SourceDescriptor sourceDescriptor) throws IOException {
        this.sourceDescriptor = sourceDescriptor;
    }

    @Override
    public SearchResult search(final SearchKeyTemplate searchKey, final List<Processor> processors, final boolean reverse) {
        SearchKey key = searchKey.createSearchKey(sourceDescriptor.getInputFormat());

        return new AbstractSearchResult() {
            final List<SourceBlock> blocks = sourceDescriptor.calculateBlocks(key);

            @Override
            public CdxIterator newIterator() {
                CdxIterator iterator;

                if (reverse) {
                    iterator = new BlockCdxSourceReverseIterator(sourceDescriptor, reverseIterator(blocks), key).init();
                } else {
                    iterator = new BlockCdxSourceIterator(sourceDescriptor, blocks.iterator(), key).init();
                }

                if (processors != null) {
                    for (Processor processorProvider : processors) {
                        iterator = processorProvider.processorIterator(iterator);
                    }
                }

                return iterator;
            }

        };
    }

    @Override
    public void close() {
        try {
            sourceDescriptor.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static <E> Iterator<E> reverseIterator(final List<E> list) {
        return new Iterator<E>() {
            ListIterator<E> src = list.listIterator(list.size());

            @Override
            public boolean hasNext() {
                return src.hasPrevious();
            }

            @Override
            public E next() {
                return src.previous();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public long count(final SearchKeyTemplate searchKey) {
        SearchKey key = searchKey.createSearchKey(sourceDescriptor.getInputFormat());

        final List<SourceBlock> blocks = sourceDescriptor.calculateBlocks(key);

        BlockCdxSourceLineCounter counter = new BlockCdxSourceLineCounter(
                sourceDescriptor, blocks.iterator(), key).init();

        return counter.count();
    }

}
