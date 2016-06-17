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

import java.util.NoSuchElementException;

/**
 *
 */
public class SortableScratchfiles extends HeapSort {

    private final ScratchFile[] files;

    int heapSize;

    public SortableScratchfiles(final int size, final ScratchFile... files) {
        heapSize = size;
        this.files = files;
        nextRun();
    }
    
    public boolean hasMoreInRun() {
        return heapSize > 0;
    }
    
    public final void nextRun() {
        for (int i = 0; i < files.length; i++) {
            if (files[i].isEndOfRun()) {
                heapSize--;
                if (heapSize > 0) {
                    exchange(files, i, heapSize);
                }
                files[i].nextRun();
            }
        }
        heapSort(files, heapSize);
    }
    
    public String getNextInRun() {
        if (heapSize == 0) {
            throw new NoSuchElementException();
        }
        String result = files[0].getNext();
        if (files[0].isEndOfRun()) {
            heapSize--;
            exchange(files, 0, heapSize);
        }
        heapSort(files, heapSize);
        return result;
    }

    public ScratchFile[] getFiles() {
        return files;
    }

}
