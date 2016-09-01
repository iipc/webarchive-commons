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

import org.netpreserve.commons.uri.PostParseNormalizer;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.uri.normalization.StripSlashesAtEndOfPath;

/**
 * An instance of this class can determine if a CdxRecord is within scope for a search.
 */
public final class SearchKey {

    public enum UriMatchType {

        ALL,
        EXACT,
        HOST,
        PATH,
        RANGE;

    }

    private final String uriString;

    private final DateTimeRange dateRange;

    private final String fromUriString;

    private final String toUriString;

    private final UriMatchType uriMatchType;

    private final CdxFormat cdxFormat;

    private final Uri parsedUri;

    public SearchKey() {
        this(null, null, null, null, UriMatchType.ALL, null, null, false);
    }

    public SearchKey(final String uri) {
        this(uri.trim(), null, null, null, detectMatchType(uri.trim()), null, null, true);
    }

    public SearchKey(final String uri, final DateTimeRange dateRange) {
        this(uri.trim(), dateRange, null, null, detectMatchType(uri.trim()), null, null, true);
    }

    private SearchKey(String uri, DateTimeRange dateRange, String fromUri, String toUri,
            UriMatchType uriMatchType, CdxFormat cdxFormat, Uri parsedUri, boolean parseUri) {

        if (parseUri && uri != null && cdxFormat != null) {
            UriBuilderConfig uriBuilderConfig = cdxFormat.getKeyUriFormat();

            if (uriMatchType == UriMatchType.HOST) {
                uri = uri.substring(1);
            }

            if (uriMatchType == UriMatchType.PATH) {
                uri = uri.substring(0, uri.length() - 1);

                // If match type is PATH, we need to keep ending slashes because we removed the final '*'.
                UriBuilderConfig.ConfigBuilder builder = uriBuilderConfig.toBuilder();
                builder.getPostParseNormalizers().removeIf(new Predicate<PostParseNormalizer>() {
                    @Override
                    public boolean test(PostParseNormalizer t) {
                        return StripSlashesAtEndOfPath.class.isInstance(t);
                    }

                });
                uriBuilderConfig = builder.build();
            }

            this.uriString = uri;
            this.uriMatchType = uriMatchType;
            this.parsedUri = UriBuilder.builder(uriBuilderConfig).uri(uri).build();

        } else {
            this.uriString = uri;
            this.uriMatchType = uriMatchType;
            this.parsedUri = parsedUri;
        }

        this.cdxFormat = cdxFormat;
        this.dateRange = dateRange;
        this.fromUriString = fromUri;
        this.toUriString = toUri;
    }

    public SearchKey uri(final String uri) {
        String tmpUri = uri.trim();
        UriMatchType tmpMatchType = detectMatchType(tmpUri);
        return new SearchKey(tmpUri, dateRange, fromUriString, toUriString, tmpMatchType, cdxFormat, parsedUri, true);
    }

    public SearchKey dateRange(final DateTimeRange dateRange) {
        return new SearchKey(uriString, dateRange, fromUriString, toUriString, uriMatchType, cdxFormat, parsedUri,
                false);
    }

    public SearchKey surtUriFrom(final String fromUri) {
        return new SearchKey(uriString, dateRange, fromUri, toUriString, UriMatchType.RANGE, cdxFormat, parsedUri,
                false);
    }

    public SearchKey surtUriTo(final String toUri) {
        return new SearchKey(uriString, dateRange, fromUriString, toUri, UriMatchType.RANGE, cdxFormat, parsedUri,
                false);
    }

    public SearchKey cdxFormat(final CdxFormat format) {
        return new SearchKey(uriString, dateRange, fromUriString, toUriString, uriMatchType, format, parsedUri, true);
    }

    public Uri getUri() {
        if (cdxFormat == null) {
            throw new IllegalStateException("Cannot get parsed URI when CdxFormat is not set");
        }

        if (parsedUri == null) {
            throw new IllegalStateException("Cannot get parsed URI when uri is not set");
        }

        return parsedUri;
    }

    public UriMatchType getMatchType() {
        return uriMatchType;
    }

    public DateTimeRange getDateRange() {
        return dateRange;
    }

    public CdxFormat getCdxFormat() {
        return cdxFormat;
    }

    public boolean isBefore(final String keyToTest) {
        switch (uriMatchType) {
            case ALL:
                return false;

            case EXACT:
            case PATH:
                if (keyToTest.compareTo(getUri().toString()) < 0) {
                    return true;
                }
                break;

            case HOST:
                String surtHost = getUri().getFormattedAuthority();
                surtHost = surtHost.substring(0, surtHost.length() - 1);
                if (keyToTest.compareTo(surtHost) < 0) {
                    return true;
                }
                break;

            case RANGE:
                if ((fromUriString != null && keyToTest.compareTo(fromUriString) < 0)) {
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
                if (getUri().toString().equals(keyToTest)) {
                    return true;
                }
                break;

            case PATH:
                if (keyToTest.startsWith(getUri().toString())) {
                    return true;
                }
                break;

            case HOST:
                String surtHost = getUri().getFormattedAuthority();
                surtHost = surtHost.substring(0, surtHost.length() - 1);
                if (keyToTest.startsWith(surtHost)) {
                    return true;
                }
                break;

            case RANGE:
                if ((fromUriString == null || fromUriString.compareTo(keyToTest) <= 0)
                        && (toUriString == null || toUriString.compareTo(keyToTest) > 0)) {
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
                if (compareToFilter(byteBuf, getUri().toString().getBytes()) == 0) {
                    if (dateRange != null && nextField(byteBuf) && byteBuf.hasRemaining()) {
                        if (dateRange.hasStartDate() && dateRange.hasEndDate()) {
                            String startDate = dateRange.getStart().toFormattedString(cdxFormat.getKeyDateFormat());
                            String endDate = dateRange.getEnd().toFormattedString(cdxFormat.getKeyDateFormat());
                            return between(byteBuf, startDate.getBytes(), endDate.getBytes());
                        } else if (dateRange.hasStartDate()) {
                            String startDate = dateRange.getStart().toFormattedString(cdxFormat.getKeyDateFormat());
                            return compareToFilter(byteBuf, startDate.getBytes()) >= 0;
                        } else {
                            String endDate = dateRange.getEnd().toFormattedString(cdxFormat.getKeyDateFormat());
                            return compareToFilter(byteBuf, endDate.getBytes()) < 0;
                        }
                    }
                    return true;
                }
                break;

            case PATH:
                if (startsWith(byteBuf, getUri().toString().getBytes())) {
                    return true;
                }
                break;

            case HOST:
                String surtHost = getUri().getFormattedAuthority();
                surtHost = surtHost.substring(0, surtHost.length() - 1);
                if (startsWith(byteBuf, surtHost.getBytes())) {
                    return true;
                }
                break;

            case RANGE:
                if (between(byteBuf, fromUriString.getBytes(), toUriString.getBytes())) {
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

    private static UriMatchType detectMatchType(String uri) {
        UriMatchType matchType = UriMatchType.ALL;

        if (uri.startsWith("*")) {
            matchType = UriMatchType.HOST;
        }

        if (uri.endsWith("*")) {
            if (matchType == UriMatchType.HOST) {
                throw new IllegalArgumentException("Only prefix or postfix wildcard is allowed");
            }
            matchType = UriMatchType.PATH;
        }

        if (matchType == UriMatchType.ALL) {
            matchType = UriMatchType.EXACT;
        }

        return matchType;
    }

}
