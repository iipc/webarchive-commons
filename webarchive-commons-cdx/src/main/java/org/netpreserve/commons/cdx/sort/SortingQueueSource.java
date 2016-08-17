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
package org.netpreserve.commons.cdx.sort;

/**
 *
 */
public class SortingQueueSource extends ReplacementSelectionHeapSort {

    private final CloseableStringQueue input;

    public SortingQueueSource(int size, CloseableStringQueue input) {
        super(size);
        this.input = input;
        fillHeap();
    }

    @Override
    protected String readNext() {
        try {
            String record = input.take();
            while ("".equals(record)) {
                record = input.take();
            }
            return record;
        } catch (InterruptedException ex) {
            return null;
        }
    }

}
