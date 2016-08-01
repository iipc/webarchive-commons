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
package org.netpreserve.commons.cdx;

import org.netpreserve.commons.util.datetime.DateTimeRange;

import java.nio.ByteBuffer;
import java.util.function.Predicate;

import org.netpreserve.commons.uri.Configurations;
import org.netpreserve.commons.uri.PostParseNormalizer;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.uri.UriFormat;
import org.netpreserve.commons.uri.normalization.StripSlashesAtEndOfPath;

/**
 * An instance of this class can determine if a CdxRecord is within scope for a search.
 */
public class SearchKey implements Cloneable {

    public enum UriMatchType {

        ALL,
        EXACT,
        HOST,
        PATH,
        RANGE;

    }

    private Uri uri;

    private DateTimeRange dateRange;

    private String surtUriFrom;

    private String surtUriTo;

    private UriMatchType uriMatchType = UriMatchType.ALL;

    private UriFormat SURT_HOST_FORMAT = UriFormat.builder()
            .surtEncoding(true)
            .ignoreScheme(true)
            .ignoreUserInfo(true)
            .ignorePort(true)
            .ignorePath(true)
            .ignoreFragment(true)
            .build();

    public SearchKey uri(final String value) {
        String uriString = value.trim();

        if (uriString.startsWith("*")) {
            uriMatchType = UriMatchType.HOST;
            uriString = uriString.substring(1);
        }

        if (uriString.endsWith("*")) {
            if (uriMatchType == UriMatchType.HOST) {
                throw new IllegalArgumentException("Only prefix or postfix wildcard is allowed");
            }
            uriMatchType = UriMatchType.PATH;
            uriString = uriString.substring(0, uriString.length() - 1);
            UriBuilderConfig.ConfigBuilder b = Configurations.SURT_KEY.toBuilder();
            b.getPostParseNormalizers().removeIf(new Predicate<PostParseNormalizer>() {
                @Override
                public boolean test(PostParseNormalizer t) {
                    return StripSlashesAtEndOfPath.class.isInstance(t);
                }

            });
            this.uri = UriBuilder.builder(b.build()).uri(uriString).build();
            return this;
        }

        if (uriMatchType == UriMatchType.ALL) {
            uriMatchType = UriMatchType.EXACT;
        }

        this.uri = UriBuilder.builder(Configurations.SURT_KEY).uri(uriString).build();

        return this;
    }

    public SearchKey dateRange(final DateTimeRange value) {
        this.dateRange = value;
        return this;
    }

    public SearchKey surtUriFrom(final String value) {
        uriMatchType = UriMatchType.RANGE;
        this.surtUriFrom = value;
        return this;
    }

    public SearchKey surtUriTo(final String value) {
        uriMatchType = UriMatchType.RANGE;
        this.surtUriTo = value;
        return this;
    }

    public Uri getUri() {
        return uri;
    }

    public UriMatchType getMatchType() {
        return uriMatchType;
    }

    public DateTimeRange getDateRange() {
        return dateRange;
    }

    public boolean isBefore(final String keyToTest) {
        switch (uriMatchType) {
            case ALL:
                return false;

            case EXACT:
            case PATH:
                if (keyToTest.compareTo(uri.toString()) < 0) {
                    return true;
                }
                break;

            case HOST:
                String surtHost = uri.toCustomString(SURT_HOST_FORMAT);
                surtHost = surtHost.substring(0, surtHost.length() - 1);
                if (keyToTest.compareTo(surtHost) < 0) {
                    return true;
                }
                break;

            case RANGE:
                if ((surtUriFrom != null && keyToTest.compareTo(surtUriFrom) < 0)) {
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean included(final String keyToTest) {
        switch (uriMatchType) {
            case ALL:
                return true;

            case EXACT:
                if (uri.toString().equals(keyToTest)) {
                    return true;
                }
                break;

            case PATH:
                if (keyToTest.startsWith(uri.toString())) {
                    return true;
                }
                break;

            case HOST:
                String surtHost = uri.toCustomString(SURT_HOST_FORMAT);
                surtHost = surtHost.substring(0, surtHost.length() - 1);
                if (keyToTest.startsWith(surtHost)) {
                    return true;
                }
                break;

            case RANGE:
                if ((surtUriFrom == null || surtUriFrom.compareTo(keyToTest) <= 0)
                        && (surtUriTo == null || surtUriTo.compareTo(keyToTest) > 0)) {
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean included(final ByteBuffer byteBuf) {
        switch (uriMatchType) {
            case ALL:
                return true;

            case EXACT:
                if (compareToFilter(byteBuf, uri.toString().getBytes()) == 0) {
                    if (dateRange != null && nextField(byteBuf) && byteBuf.hasRemaining()) {
                        if (dateRange.hasStartDate() && dateRange.hasEndDate()) {
                            return between(byteBuf, dateRange.getStart().toHeritrixDateString().getBytes(), dateRange
                                    .getEnd().toHeritrixDateString().getBytes());
                        } else if (dateRange.hasStartDate()) {
                            return compareToFilter(byteBuf, dateRange.getStart().toHeritrixDateString().getBytes()) >= 0;
                        } else {
                            return compareToFilter(byteBuf, dateRange.getEnd().toHeritrixDateString().getBytes()) < 0;
                        }
                    }
                    return true;
                }
                break;

            case PATH:
                if (startsWith(byteBuf, uri.toString().getBytes())) {
                    return true;
                }
                break;

            case HOST:
                String surtHost = uri.toCustomString(SURT_HOST_FORMAT);
                surtHost = surtHost.substring(0, surtHost.length() - 1);
                if (startsWith(byteBuf, surtHost.getBytes())) {
                    return true;
                }
                break;

            case RANGE:
                if (between(byteBuf, surtUriFrom.getBytes(), surtUriTo.getBytes())) {
                    return true;
                }
                break;
        }
        return false;
    }

    final boolean startsWith(final ByteBuffer byteBuf, final byte[] filter) {
        int filterLength = filter.length;

        int k = 0;

        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            byte[] buf = byteBuf.array();
            int offset = byteBuf.arrayOffset() + byteBuf.position();
            int limit = byteBuf.limit();

            for (int i = offset; i < limit && k < filterLength; i++) {
                byte c = buf[i];
                byte cf = filter[k];

                if (isLf(c)) {
                    byteBuf.position(i + 1);
                    return false;
                }
                if (c != cf) {
                    byteBuf.position(i + 1);
                    return false;
                }
                k++;
            }

        } else {
            while (k < filterLength && byteBuf.hasRemaining()) {
                byte c = byteBuf.get();
                byte cf = filter[k];

                if (isLf(c)) {
                    return false;
                }
                if (c != cf) {
                    return false;
                }
                k++;
            }
        }

        return k >= filterLength;
    }

    /**
     * Compare line starting at current position end a filter.
     * <p>
     * @param filter the filter end compare
     * @return negative number if filter is before current line, zero if equal, and positive number if filter is after
     * current line
     */
    final int compareToFilter(final ByteBuffer byteBuf, final byte[] filter) {
        int filterLength = filter.length;

        int k = 0;

        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            byte[] buf = byteBuf.array();
            int arrayOffset = byteBuf.arrayOffset();
            int offset = byteBuf.position();
            int limit = byteBuf.limit();

            for (int i = offset; i < limit && k < filterLength; i++) {
                byte c = buf[arrayOffset + i];
                byte cf = filter[k];

                if (isLf(c)) {
                    byteBuf.position(i + 1);
                    return k - filterLength;
                }
                if (c != cf) {
                    byteBuf.position(i + 1);
                    return c - cf;
                }
                k++;
            }

        } else {
            while (k < filterLength && byteBuf.hasRemaining()) {
                byte c = byteBuf.get();
                byte cf = filter[k];

                if (isLf(c)) {
                    return k - filterLength;
                }
                if (c != cf) {
                    return c - cf;
                }
                k++;
            }
        }

        if (uriMatchType != UriMatchType.PATH && byteBuf.limit() > k + byteBuf.position() && byteBuf.get(k + byteBuf
                .position()) != ' ') {
            k++;
            return 1;
        }

//        byteBuf.position(k);
        return k - filterLength;
    }

    final boolean between(final ByteBuffer byteBuf, final byte[] startFilter, final byte[] endFilter) {
        // < 0: excluded, 0: undecided (still parsing), > 0: included
        int compareToStartFilter = 0;
        int compareToEndFilter = 0;

        int startFilterLength = startFilter.length;
        int endFilterLength = endFilter.length;

        int k = 0;

        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            byte[] buf = byteBuf.array();
            int arrayOffset = byteBuf.arrayOffset();
            int offset = byteBuf.position();
            int limit = byteBuf.limit();

            for (int i = offset; i < limit && (compareToStartFilter == 0 || compareToEndFilter == 0); i++) {
                byte c = buf[arrayOffset + i];

                if (k < startFilterLength) {
                    if (compareToStartFilter == 0) {
                        byte cf = startFilter[k];

                        if (isLf(c)) {
                            compareToStartFilter = (k - startFilterLength);
                        } else if (c != cf) {
                            compareToStartFilter = (c - cf);
                        }
                    }
                } else if (compareToStartFilter == 0) {
                    compareToStartFilter = (k - startFilterLength);
                }

                if (compareToStartFilter == 0 || compareToStartFilter >= 0 && k < endFilterLength) {
                    if (compareToEndFilter == 0) {
                        byte cf = endFilter[k];

                        if (isLf(c)) {
                            compareToEndFilter = (k - endFilterLength);
                        } else if (c != cf) {
                            compareToEndFilter = (c - cf);
                        }
                    }
                } else if (compareToEndFilter == 0) {
                    compareToEndFilter = (k - endFilterLength);
                }
                k++;
            }
            byteBuf.position(Math.min(limit, offset + k + 1));

        } else {
            while (byteBuf.hasRemaining() && (compareToStartFilter == 0 || compareToEndFilter == 0)) {
                byte c = byteBuf.get();
                System.out.print((char) c);
                System.out.println(" -- SF: " + compareToStartFilter + ", EF: " + compareToEndFilter);

                if (k < startFilterLength) {
                    if (compareToStartFilter == 0) {
                        byte cf = startFilter[k];

                        if (isLf(c)) {
                            compareToStartFilter = (k - startFilterLength);
                        } else if (c != cf) {
                            compareToStartFilter = (c - cf);
                        }
                    }
                } else if (compareToStartFilter == 0) {
                    compareToStartFilter = (k - startFilterLength);
                }

                if (compareToStartFilter == 0 || compareToStartFilter >= 0 && k < endFilterLength) {
                    if (compareToEndFilter == 0) {
                        byte cf = endFilter[k];

                        if (isLf(c)) {
                            compareToEndFilter = (k - endFilterLength);
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

    final boolean nextField(ByteBuffer buf) {
        while (buf.hasRemaining()) {
            byte c = buf.get();
            if (c == ' ') {
                return true;
            }
            if (isLf(c)) {
                return false;
            }
        }
        return false;
    }

    /**
     * Check for newline characters.
     * <p>
     * @param c the character end check
     * @return true if LF or CR
     */
    final boolean isLf(int c) {
        return c == '\n' || c == '\r';
    }

    @Override
    public SearchKey clone() {
        try {
            return (SearchKey) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }

}
