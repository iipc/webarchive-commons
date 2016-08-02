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
package org.netpreserve.commons.util;

/**
 * Utility methods for working with arrays.
 */
public final class ArrayUtil {

    /**
     * Private constructor to avoid instantiation.
     */
    private ArrayUtil() {
    }

    /**
     * Helper method to find the index of a character in a char array.
     * <p>
     * @param src the array to search
     * @param ch the char to look for
     * @param fromIndex where in the src to start. If &lt;= 0, the beggining of the array is assumed. If &gt;=
     * src.length, then -1 is returned.
     * @return the index of the first occurence of ch or -1 if not found.
     */
    public static int indexOf(char[] src, char ch, int fromIndex) {
        final int max = src.length;
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= max) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }

        final char[] value = src;
        for (int i = fromIndex; i < max; i++) {
            if (value[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Helper method to find the last index of a character in a char array.
     * <p>
     * @param src the array to search
     * @param ch the char to look for
     * @param fromIndex where in the src to start, searching backwards. If = 0, the beggining of the array is
     * assumed. If &lt; 0, the end of the array is assumed. If &gt;= src.length, then -1 is returned.
     * @return the index of the last occurence of ch or -1 if not found.
     */
    public static int lastIndexOf(char[] src, char ch, int fromIndex) {
        if (fromIndex < 0) {
            fromIndex = src.length - 1;
        } else if (fromIndex >= src.length) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }

        final char[] value = src;
        for (int i = fromIndex; i >= 0; i--) {
            if (value[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Swap two values in an array.
     * <p>
     * @param <T> the type of the elements in the array
     * @param data the array containing the values to swap
     * @param idx1 index of the first element
     * @param idx2 index of the second element
     */
    public static <T> void swap(T[] data, int idx1, int idx2) {
        T tmp = data[idx1];
        data[idx1] = data[idx2];
        data[idx2] = tmp;
    }

    /**
     * Sort an array using the heapsort algorithm.
     * <p>
     * The range to be sorted extends from the index fromIndex, inclusive, to the index toIndex, exclusive. If fromIndex
     * == toIndex, the range to be sorted is empty. The elements of the array must implement the {@link Comparable}
     * interface.
     * <p>
     * @param <T> the type of the elements in the array
     * @param data the array to be sorted
     * @param fromIndex the index of the first element, inclusive, to be sorted
     * @param toIndex the index of the last element, exclusive, to be sorted
     * @throws ArrayIndexOutOfBoundsException if fromIndex &lt; 0 or toIndex &gt; data.length
     */
    public static <T extends Comparable> void heapSort(T[] data, int fromIndex, int toIndex) {
        int size = toIndex - fromIndex;
        int offset = fromIndex;

        // Turn the array into a maxheap
        for (int i = size / 2; i > 0; i--) {
            percolateDown(data, offset, size, i);
        }
        // Convert the maxheap into a sorted array
        for (int i = size - 1; i >= 0; i--) {
            // Put the correct key in slot n by exchanging with the
            // key in slot 0; then percolate the key in slot 1 down.
            // We use i as the size of the heap, because after an item is in
            // place it is no longer considered part of the heap
            swap(data, offset, i + offset);
            percolateDown(data, offset, i, 1);
        }
    }

    /**
     * Part of the heapSort implementation.
     * <p>
     * @param <T> the type of the elements in the heap
     * @param data the heap
     * @param offset where in the heap to start
     * @param size size of the heap
     * @param hole the element to percolate down
     */
    private static <T extends Comparable> void percolateDown(T[] data, int offset, int size, int hole) {
        int child;
        T temp = data[hole - 1 + offset];
        for (; hole * 2 <= size; hole = child) {
            child = hole * 2;
            if (child < size && data[child + offset].compareTo(data[child - 1 + offset]) > 0) {
                child++;
            }
            if (data[child - 1 + offset].compareTo(temp) > 0) {
                data[hole - 1 + offset] = data[child - 1 + offset];
            } else {
                break;
            }
        }
        data[hole - 1 + offset] = temp;
    }

}
