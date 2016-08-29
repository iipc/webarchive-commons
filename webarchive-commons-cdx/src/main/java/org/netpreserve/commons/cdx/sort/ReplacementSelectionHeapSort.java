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

import java.util.Arrays;

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
abstract class ReplacementSelectionHeapSort {

    private final String[] data;

    int heapSize;

    int takePtr;

    /**
     * Create a new ReplacementSelectionHeapSort.
     * <p>
     * @param size the number of lines in the heap
     * @param input the source for new lines
     */
    ReplacementSelectionHeapSort(final int size) {
        heapSize = size;
        data = new String[size];
    }

    /**
     * Initially fill the heap from the input source.
     */
    protected void fillHeap() {
        int i = 0;
        while (i < heapSize) {
            String record = readNext();
            if (record == null) {
                heapSize = i;
                break;
            }
            data[i] = record;
            i++;
        }

        Arrays.parallelSort(data, 0, heapSize);
    }

    /**
     * Write the next run.
     * <p>
     * See the class description for what it does.
     * <p>
     * @param out the scratch file to write this run to
     * @return false if no more data i.e. writeNextRun should not be called anymore
     */
    public boolean writeNextRun(ScratchFile out) {
        while (heapSize > 0) {
            out.write(data[takePtr]);

            String record = readNext();
            if (record == null) {
                // No more input ......
                for (int i = takePtr + 1; i < takePtr + heapSize; i++) {
                    out.write(data[i]);
                }
                // No more space on heap -> end of run
                if (takePtr > 0) {
                    heapSize = takePtr;
                    takePtr = 0;
                    break;
                }
                return false;
            } else {
                if (record.compareTo(data[takePtr]) < 0) {
                    // Does not fit into current run
                    heapSize--;
                    takePtr++;

                    // Insert into right place in next run
                    int insertPt = Math.abs(Arrays.binarySearch(data, 0, takePtr - 1, record)) - 1;
                    for (int i = takePtr - 1; i > insertPt; i--) {
                        data[i] = data[i - 1];
                    }
                    data[insertPt] = record;

                    if (heapSize == 0) {
                        // No more space on heap -> end of run
                        heapSize = takePtr;
                        takePtr = 0;
                        break;
                    }
                } else {
                    // Insert into right place in current run
                    int insertPt = Math.abs(Arrays.binarySearch(data, takePtr, takePtr + heapSize, record)) - 2;
                    for (int i = takePtr; i < insertPt; i++) {
                        data[i] = data[i + 1];
                    }
                    data[insertPt] = record;
                }
            }
        }
        return heapSize > 0;
    }

    /**
     * Read the next line from the input source skipping empty lines.
     * <p>
     * @return the next non-empty line from the input source
     */
    protected abstract String readNext();

}
