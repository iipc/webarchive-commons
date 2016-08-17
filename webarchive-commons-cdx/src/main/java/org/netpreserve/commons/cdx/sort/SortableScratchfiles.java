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

import org.netpreserve.commons.util.ArrayUtil;

/**
 * Class holding a set of scratch files and sorting them according to the next run in each.
 */
class SortableScratchfiles {

    private final ScratchFile[] files;

    int size;

    SortableScratchfiles(final ScratchFile... files) {
        size = files.length;
        this.files = files;
    }

    public int getAndRemoveNumberOfCommonDummies() {
        int lowestDummyCount = Integer.MAX_VALUE;
        for (ScratchFile sf : files) {
            if (sf.dummy < lowestDummyCount) {
                lowestDummyCount = sf.dummy;
            }
        }
        if (lowestDummyCount > 0) {
            for (ScratchFile sf : files) {
                sf.dummy -= lowestDummyCount;
                sf.distribution -= lowestDummyCount;
            }
        }
        return lowestDummyCount;
    }

    public boolean hasMoreInRun() {
        return size > 0;
    }

    public final void nextRun() {
        size = files.length;

        for (int i = 0; i < size; i++) {
            files[i].nextRun();
            if (files[i].isEndOfRun()) {
                size--;
                if (size > i) {
                    ArrayUtil.swap(files, i, size);
                    i--;
                }
            }
        }
        if (size > 0) {
            ArrayUtil.heapSort(files, 0, size);
        }
    }

    public String getNextInRun() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        String result = files[0].getNext();
        if (files[0].isEndOfRun()) {
            size--;
            ArrayUtil.swap(files, 0, size);
        }
        ArrayUtil.heapSort(files, 0, size);
        return result;
    }

    public ScratchFile[] getFiles() {
        return files;
    }

}