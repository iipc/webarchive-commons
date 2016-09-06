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

    private final String primaryUriString;

    private final DateTimeRange dateRange;

    private final String secondaryUriString;

    private final UriMatchType uriMatchType;

    private final CdxFormat cdxFormat;

    private final Uri parsedPrimaryUri;

    private final Uri parsedSecondaryUri;

    public SearchKey() {
        this(null, null, null, null, null, UriMatchType.ALL, null, false);
    }

    public SearchKey(final String uri, final UriMatchType matchType) {
        this(uri.trim(), null, null, null, null, matchType, null, true);
    }

    public SearchKey(final String uri, final UriMatchType matchType, final DateTimeRange dateRange) {
        this(uri.trim(), null, null, null, dateRange, matchType, null, true);
    }

    private SearchKey(String primaryUri, Uri parsedPrimaryUri, String secondaryUri, Uri parsedSecondaryUri,
            DateTimeRange dateRange, UriMatchType uriMatchType, CdxFormat cdxFormat, boolean parseUri) {

        if (parseUri && cdxFormat != null) {

            UriBuilderConfig uriBuilderConfig = cdxFormat.getKeyUriFormat();
            if (parsedPrimaryUri == null && primaryUri != null) {
                if (uriMatchType == UriMatchType.PATH) {
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

                parsedPrimaryUri = UriBuilder.builder(uriBuilderConfig).uri(primaryUri).build();
            }
            if (parsedSecondaryUri == null && secondaryUri != null) {
                parsedSecondaryUri = UriBuilder.builder(uriBuilderConfig).uri(secondaryUri).build();
            }
        }

        this.primaryUriString = primaryUri;
        this.parsedPrimaryUri = parsedPrimaryUri;
        this.secondaryUriString = secondaryUri;
        this.parsedSecondaryUri = parsedSecondaryUri;
        this.uriMatchType = uriMatchType;
        this.cdxFormat = cdxFormat;
        this.dateRange = dateRange;
    }

    public SearchKey uri(final String uri) {
        return uri(uri, UriMatchType.EXACT);
    }

    public SearchKey uri(final String uri, final UriMatchType matchType) {
        return new SearchKey(uri.trim(), null, null, null, dateRange, matchType, cdxFormat, true);
    }

    public SearchKey dateRange(final DateTimeRange dateRange) {
        return new SearchKey(primaryUriString, parsedPrimaryUri, secondaryUriString, parsedSecondaryUri, dateRange,
                uriMatchType, cdxFormat, false);
    }

    public SearchKey uriRange(final String fromUri, final String toUri) {
        return new SearchKey(fromUri.trim(), null, toUri.trim(), null, dateRange,
                UriMatchType.RANGE, cdxFormat, true);
    }

    public SearchKey cdxFormat(final CdxFormat format) {
        return new SearchKey(primaryUriString, null, secondaryUriString, null, dateRange, uriMatchType, format, true);
    }

    public Uri getUri() {
        if (cdxFormat == null) {
            throw new IllegalStateException("Cannot get parsed URI when CdxFormat is not set");
        }

        if (parsedPrimaryUri == null) {
            throw new IllegalStateException("Cannot get parsed URI when uri is not set");
        }

        return parsedPrimaryUri;
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
        if (cdxFormat == null) {
            throw new IllegalStateException("CdxFormat must be set");
        }

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
                if ((parsedPrimaryUri != null && keyToTest.compareTo(parsedPrimaryUri.toString()) < 0)) {
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean included(final String keyToTest) {
        if (cdxFormat == null) {
            throw new IllegalStateException("CdxFormat must be set");
        }

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
                if ((parsedPrimaryUri == null || parsedPrimaryUri.toString().compareTo(keyToTest) <= 0)
                        && (parsedSecondaryUri == null || parsedSecondaryUri.toString().compareTo(keyToTest) > 0)) {
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean included(final ByteBuffer byteBuf) {
        if (cdxFormat == null) {
            throw new IllegalStateException("CdxFormat must be set");
        }

        switch (uriMatchType) {
            case ALL:
                return true;

            case EXACT:
                if (compareToFilter(byteBuf, getUri().toString().getBytes()) == 0) {
                    if (dateRange != null && dateRange.hasStartDateOrEndDate()
                            && nextField(byteBuf) && byteBuf.hasRemaining()) {
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
                if (parsedPrimaryUri == null && parsedSecondaryUri == null) {
                    return true;
                }

                if (parsedPrimaryUri != null && parsedSecondaryUri != null) {
                    return between(byteBuf, parsedPrimaryUri.toString().getBytes(),
                            parsedSecondaryUri.toString().getBytes());
                } else if (parsedPrimaryUri != null) {
                    return compareToFilter(byteBuf, parsedPrimaryUri.toString().getBytes()) >= 0;
                } else {
                    return compareToFilter(byteBuf, parsedSecondaryUri.toString().getBytes()) < 0;
                }

            default:
                return false;
        }
        return false;
    }

    final boolean startsWith(final ByteBuffer byteBuf, final byte[] filter) {
        if (cdxFormat == null) {
            throw new IllegalStateException("CdxFormat must be set");
        }

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
     * Compare line starting at current position to a filter.
     * <p>
     * @param filter the filter to compare
     * @return negative number if filter is before current line, zero if equal, and positive number if filter is after
     * current line
     */
    final int compareToFilter(final ByteBuffer byteBuf, final byte[] filter) {
        if (cdxFormat == null) {
            throw new IllegalStateException("CdxFormat must be set");
        }

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
        if (cdxFormat == null) {
            throw new IllegalStateException("CdxFormat must be set");
        }

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

}
