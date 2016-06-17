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

/**
 *
 */
public class ReplacementSelectionHeapSort extends HeapSort {

    private final BufferedReader input;

    private final String[] data;

    int heapSize;

    int notHeapSize;

    public ReplacementSelectionHeapSort(final int size, final BufferedReader input) throws IOException {
        heapSize = size;
        notHeapSize = 0;
        data = new String[size];
        this.input = input;
        fillHeap();
    }

    private void fillHeap() throws IOException {
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

        heapSort(data, heapSize);
    }

    public boolean writeNextRun(ScratchFile out) throws IOException {
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
                    heapSort(data, heapSize);
                    break;
                } else {
                    return false;
                }
            } else {
                if (record.compareTo(data[0]) < 0) {
                    heapSize--;
                    notHeapSize++;
                    exchange(data, 0, heapSize);
                    data[heapSize] = record;

                    if (heapSize == 0) {
                        // No more space on heap -> end of run
                        heapSize = notHeapSize;
                        notHeapSize = 0;
                        heapSort(data, heapSize);
                        break;
                    }
                } else {
                    data[0] = record;
                }
                heapSort(data, heapSize);
            }
        }
        return true;
    }

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
