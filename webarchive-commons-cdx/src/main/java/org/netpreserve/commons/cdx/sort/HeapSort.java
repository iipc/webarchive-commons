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
public class HeapSort {

    public static <T extends Comparable> void heapSort(T[] data, int heapSize) {
        // Turn the array into a maxheap
        for (int i = heapSize / 2; i > 0; i--) {
            percolateDown(data, heapSize, i);
        }
        // Convert the maxheap into a sorted array
        for (int i = heapSize; i > 0; i--) {
            // Put the correct key in slot n by exchanging with the
            // key in slot 0; then percolate the key in slot 1 down.
            // We use i as the size of the heap, because after an item is in
            // place it is no longer considered part of the heap
            exchange(data, 0, i - 1);
            percolateDown(data, i - 1, 1);
        }
    }

    static <T extends Comparable> void percolateDown(T[] data, int size, int hole) {
        int child;
        T temp = data[hole - 1];
        for (; hole * 2 <= size; hole = child) {
            child = hole * 2;
            if (child < size && data[child].compareTo(data[child - 1]) > 0) {
                child++;
            }
            if (data[child - 1].compareTo(temp) > 0) {
                data[hole - 1] = data[child - 1];
            } else {
                break;
            }
        }
        data[hole - 1] = temp;
    }

    static <T extends Comparable> void exchange(T[] data, int idx1, int idx2) {
        T tmp = data[idx1];
        data[idx1] = data[idx2];
        data[idx2] = tmp;
    }

}
