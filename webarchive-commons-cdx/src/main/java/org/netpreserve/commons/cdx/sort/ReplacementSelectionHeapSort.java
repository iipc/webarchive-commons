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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.netpreserve.commons.util.ArrayUtil;

/**
 * Internal class used for sorting.
 * <p>
 * This class fills a heap and sorts it. A run is a series of lines in ascending order. When {@link #writeNextRun} is
 * called it writes the next line in the heap to a scratch file. Then it tries to fill up the heap with new lines from
 * input and add it in the correct position in the heap. If the new line is less than what already is written, it is
 * added to the part outside the heap and the heap size is reduced by one. When the heap size is 0 then all elements
 * fitting in the current run is written. The heap size is restored to contain all the lines outside the heap and it is
 * ready for a new call to writeNextRun.
 */
class ReplacementSelectionHeapSort {

    private final BufferedReader input;

    private final String[] data;

    int heapSize;

    int notHeapSize;

    /**
     * Create a new ReplacementSelectionHeapSort.
     * <p>
     * @param size the number of lines in the heap
     * @param input the source for new lines
     */
    ReplacementSelectionHeapSort(final int size, final BufferedReader input) {
        heapSize = size;
        notHeapSize = 0;
        data = new String[size];
        this.input = input;
        fillHeap();
    }

    /**
     * Initially fill the heap from the input source.
     */
    private void fillHeap() {
        int i = 0;
        while (i < heapSize) {
            String record = readNextNonEmpty();
            if (record == null) {
                heapSize = i;
                break;
            }
            data[i] = record;
            i++;
        }

        ArrayUtil.heapSort(data, 0, heapSize);
    }

    /**
     * Write the next run.
     *
     * See the class description for what it does.
     *
     * @param out the scratch file to write this run to
     * @return false if no more data i.e. writeNextRun should not be called anymore
     */
    public boolean writeNextRun(ScratchFile out) {
        while (heapSize > 0) {
            out.write(data[0]);

            String record = readNextNonEmpty();
            if (record == null) {
                // No more input ......
                for (int i = 1; i < heapSize; i++) {
                    out.write(data[i]);
                }
                // No more space on heap -> end of run
                if (notHeapSize > 0) {
                    for (int i = 0; i < notHeapSize; i++) {
                        data[i] = data[i + heapSize];
                    }
                    heapSize = notHeapSize;
                    notHeapSize = 0;
                    ArrayUtil.heapSort(data, 0, heapSize);
                    break;
                } else {
                    return false;
                }
            } else {
                if (record.compareTo(data[0]) < 0) {
                    heapSize--;
                    notHeapSize++;
                    ArrayUtil.swap(data, 0, heapSize);
                    data[heapSize] = record;

                    if (heapSize == 0) {
                        // No more space on heap -> end of run
                        heapSize = notHeapSize;
                        notHeapSize = 0;
                        ArrayUtil.heapSort(data, 0, heapSize);
                        break;
                    }
                } else {
                    data[0] = record;
                }
                ArrayUtil.heapSort(data, 0, heapSize);
            }
        }
        return true;
    }

    /**
     * Read the next line from the input source skipping empty lines.
     * <p>
     * @return the next non-empty line from the input source
     */
    private String readNextNonEmpty() {
        try {
            String record = input.readLine();
            while ("".equals(record)) {
                record = input.readLine();
            }
            return record;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
