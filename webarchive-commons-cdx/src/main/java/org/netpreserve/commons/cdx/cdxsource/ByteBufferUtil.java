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
package org.netpreserve.commons.cdx.cdxsource;

import java.nio.ByteBuffer;

/**
 * Static helper methods for working with ByteBuffers.
 */
public final class ByteBufferUtil {

    /**
     * Private constructor to avoid instantiation of utility class.
     */
    private ByteBufferUtil() {
    }

    /**
     * Check if field starting at buffer's current position starts with filter value.
     * <p>
     * The buffer's position after execution will be somewhere in the field to be tested or at the field separator
     * following the field. It will never be into the next field.
     * <p>
     * @param byteBuf a buffer positioned at the start of the field to check
     * @param filter the filter to compare
     * @return true if field starts with filter value
     */
    public static boolean startsWith(final ByteBuffer byteBuf, final SearchKeyFilter filter) {
        final byte[] filterArray = filter.getFilterArray();
        final int filterLength = filterArray.length;

        int k = 0;
        final int offset = byteBuf.position();

        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            final byte[] buf = byteBuf.array();
            final int arrayOffset = byteBuf.arrayOffset();
            final int limit = byteBuf.limit();

            for (int i = offset; i < limit && k < filterLength; i++) {
                byte c = buf[i + arrayOffset];
                byte cf = filterArray[k];

                if (c != cf || filter.isEndOfField(c)) {
                    break;
                }
                k++;
            }

        } else {
            while (k < filterLength && byteBuf.hasRemaining()) {
                byte c = byteBuf.get();
                byte cf = filterArray[k];

                if (c != cf || filter.isEndOfField(c)) {
                    break;
                }
                k++;
            }
        }

        byteBuf.position(offset + k);
        return k >= filterLength || filter.isIgnorableFrom(k);
    }

    /**
     * Compare field starting at buffer's current position to a filter.
     * <p>
     * The returned value will be a negative number if current field is before filter, zero if equal, and a positive
     * number if current field is after filter.
     * <p>
     * The buffer's position after execution will be somwhere in the field to be tested or at the field separator
     * following the field. It will never be into the next field.
     * <p>
     * @param byteBuf a buffer positioned at the start of the field to check
     * @param filter the filter to compare
     * @return negative number if current field is before filter, zero if equal, and positive number if current field is
     * after filter
     */
    public static int compareToFilter(final ByteBuffer byteBuf, final SearchKeyFilter filter) {
        byte[] filterArray = filter.getFilterArray();
        int filterLength = filterArray.length;

        int k = 0;
        final int offset = byteBuf.position();
        final int limit = byteBuf.limit();

        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            final byte[] buf = byteBuf.array();
            final int arrayOffset = byteBuf.arrayOffset();

            for (int i = offset; i < limit; i++) {
                byte c = buf[arrayOffset + i];
                if (k == filterLength) {
                    if (!filter.isEndOfField(c)) {
                        k++;
                    }
                    break;
                }
                byte cf = filterArray[k];

                if (filter.isEndOfField(c)) {
                    byteBuf.position(i);
                    int result = k - filterLength;
                    if (result < 0 && filter.isIgnorableFrom(k)) {
                        return 0;
                    }
                    return result;
                }
                if (c != cf) {
                    byteBuf.position(i);
                    return c - cf;
                }
                k++;
            }

        } else {
            while (byteBuf.hasRemaining()) {
                byte c = byteBuf.get();
                if (k == filterLength) {
                    if (!filter.isEndOfField(c)) {
                        k++;
                    }
                    break;
                }
                byte cf = filterArray[k];

                if (filter.isEndOfField(c)) {
                    byteBuf.position(offset + k);
                    int result = k - filterLength;
                    if (result < 0 && filter.isIgnorableFrom(k)) {
                        return 0;
                    }
                    return result;
                }
                if (c != cf) {
                    return c - cf;
                }
                k++;
            }
        }

        byteBuf.position(Math.min(limit, offset + k));
        return k - filterLength;
    }

    /**
     * Compare field starting at buffer's current position to a start filter and an end filter.
     * <p>
     * The buffer's position after execution will be somwhere in the field to be tested or at the field separator
     * following the field. It will never be into the next field.
     * <p>
     * @param byteBuf a buffer positioned at the start of the field to check
     * @param startFilter the startFilter
     * @param endFilter the endFilter
     * @return true if field at buffer's current position is between startFilter (inclusive) and endFilter (exclusive).
     */
    public static boolean between(final ByteBuffer byteBuf, final SearchKeyFilter startFilter, final SearchKeyFilter endFilter) {
        // compareToStartFilter: < 0: excluded, 0: undecided (still parsing), > 0: included
        // compareToEndFilter: < 0: included, 0: undecided (still parsing), > 0: excluded
        int compareToStartFilter = 0;
        int compareToEndFilter = 0;

        byte[] startFilterArray = startFilter.getFilterArray();
        byte[] endFilterArray = endFilter.getFilterArray();
        int startFilterLength = startFilterArray.length;
        int endFilterLength = endFilterArray.length;

        int k = 0;

        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            final byte[] buf = byteBuf.array();
            final int arrayOffset = byteBuf.arrayOffset();
            final int offset = byteBuf.position();
            final int limit = byteBuf.limit();

            for (int i = offset; i < limit && (compareToStartFilter == 0 || compareToEndFilter == 0); i++) {
                byte c = buf[arrayOffset + i];

                if (k < startFilterLength) {
                    if (compareToStartFilter == 0) {
                        byte cf = startFilterArray[k];

                        if (startFilter.isEndOfField(c)) {
                            compareToStartFilter = (k - startFilterLength);
                            if (compareToStartFilter < 0 && startFilter.isIgnorableFrom(k)) {
                                compareToStartFilter = 1;
                            }
                            k--;
                        } else if (c != cf) {
                            compareToStartFilter = (c - cf);
                        }
                    }
                } else if (compareToStartFilter == 0) {
                    compareToStartFilter = (k - startFilterLength);
                }

                if (compareToStartFilter >= 0 && k < endFilterLength) {
                    if (compareToEndFilter == 0) {
                        byte cf = endFilterArray[k];

                        if (endFilter.isEndOfField(c)) {
                            compareToEndFilter = (k - endFilterLength);
                            if (compareToEndFilter < 0 && endFilter.isIgnorableFrom(k)) {
                                compareToEndFilter = 1;
                            }
                            k--;
                        } else if (c != cf) {
                            compareToEndFilter = (c - cf);
                        }
                    }
                } else if (compareToEndFilter == 0) {
                    compareToEndFilter = (k - endFilterLength);
                }
                k++;
            }
            byteBuf.position(Math.min(limit, offset + k));

        } else {
            while (byteBuf.hasRemaining() && (compareToStartFilter == 0 || compareToEndFilter == 0)) {
                byte c = byteBuf.get();

                if (k < startFilterLength) {
                    if (compareToStartFilter == 0) {
                        byte cf = startFilterArray[k];

                        if (startFilter.isEndOfField(c)) {
                            compareToStartFilter = (k - startFilterLength);
                            if (compareToStartFilter < 0 && startFilter.isIgnorableFrom(k)) {
                                compareToStartFilter = 1;
                            }
                            byteBuf.position(byteBuf.position() - 1);
                        } else if (c != cf) {
                            compareToStartFilter = (c - cf);
                        }
                    }
                } else if (compareToStartFilter == 0) {
                    compareToStartFilter = (k - startFilterLength);
                }

                if (compareToStartFilter == 0 || compareToStartFilter >= 0 && k < endFilterLength) {
                    if (compareToEndFilter == 0) {
                        byte cf = endFilterArray[k];

                        if (endFilter.isEndOfField(c)) {
                            compareToEndFilter = (k - endFilterLength);
                            if (compareToEndFilter < 0 && endFilter.isIgnorableFrom(k)) {
                                compareToEndFilter = 1;
                            }
                            byteBuf.position(byteBuf.position() - 1);
                        } else if (c != cf) {
                            compareToEndFilter = (c - cf);
                        }
                    }
                } else if (compareToEndFilter == 0) {
                    compareToEndFilter = (k - endFilterLength);
                }
                k++;
            }
        }

        return (compareToStartFilter >= 0)
                && (compareToEndFilter < 0);
    }

    /**
     * Advance buffer position to next field.
     * <p>
     * @param byteBuf the buffer which position is to be advanced to the next field
     * @return return true if a new field was found, false if end of line or end of buffer
     */
    public final static boolean nextField(ByteBuffer byteBuf) {
        if (byteBuf.hasArray()) {
            final byte[] buf = byteBuf.array();
            final int arrayOffset = byteBuf.arrayOffset();
            final int offset = byteBuf.position();
            final int limit = byteBuf.limit();
            for (int i = offset; i < limit; i++) {
                byte c = buf[arrayOffset + i];
                if (c == ' ') {
                    byteBuf.position(i + 1);
                    return true;
                }
                if (c == '\n' || c == '\r') {
                    byteBuf.position(i + 1);
                    return false;
                }
            }
        } else {
            while (byteBuf.hasRemaining()) {
                byte c = byteBuf.get();
                if (c == ' ') {
                    return true;
                }
                if (c == '\n' || c == '\r') {
                    return false;
                }
            }
        }
        return false;
    }

}
