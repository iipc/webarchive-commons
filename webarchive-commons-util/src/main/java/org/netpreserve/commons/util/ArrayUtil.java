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
     * @param fromIndex where in the src to start, searching backwards. If &lt;= 0, the beggining of the array is
     * assumed. If &gt;= src.length, then -1 is returned.
     * @return the index of the last occurence of ch or -1 if not found.
     */
    public static int lastIndexOf(char[] src, char ch, int fromIndex) {
        if (fromIndex < 0) {
            fromIndex = 0;
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

}
