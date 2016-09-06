/*
 * Copyright 2016 IIPC.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;

/**
 * Representation of a field name.
 * <p>
 * A field can have both a name and a code. The code is used to support the legacy cdx format
 * and the name is used as json keys in the cdxj format.
 * <p>
 * This class also contains static definitions for the most commonly used fields.
 * <p>
 * @param <T> the Java type allowed for this field
 */
public final class FieldName<T extends Object> {

    private static final Map<String, FieldName> FIELDS_BY_NAME = new HashMap<>();

    private static final Map<Character, FieldName> FIELDS_BY_CODE = new HashMap<>();

    /**
     * Ceta tags (AIF).
     * <p>
     * Code 'M' in legacy CDX format
     */
    public static final FieldName<String> ROBOT_FLAGS = forNameAndCode("robotflags", 'M');

    /**
     * A searchable version of the URI.
     * <p>
     * Code 'N' in legacy CDX format
     */
    public static final FieldName<String> URI_KEY = forNameAndCodeAndType("ssu", 'N', String.class);

    /**
     * Language string.
     * <p>
     * Code 'Q' in legacy CDX format
     */
    public static final FieldName<String> LANGUAGE = forNameAndCode("lang", 'Q');

    /**
     * Payload Length.
     * <p>
     * The length of the payload (uncompressed). The exact meaning will vary by content type, but the common case is the
     * length of the document, excluding any HTTP headers in a HTTP response record.
     */
    public static final FieldName<Number> PAYLOAD_LENGTH = forNameAndType("ple", Number.class);

    /**
     * Content Length.
     * <p>
     * The length of the content (uncompressed), ignoring WARC headers, but including any HTTP headers or similar.
     */
    public static final FieldName<Number> CONTENT_LENGTH = forNameAndType("cle", Number.class);

    /**
     * Record length.
     * <p>
     * The length of the record. This is the entire record (including e.g. WARC headers) as written on disk (compressed
     * if stored compressed).
     * <p>
     * Code 'S' in legacy CDX format
     */
    public static final FieldName<Number> RECORD_LENGTH = forNameAndCodeAndType("rle", 'S', Number.class);

    /**
     * ARC/WARC file offset.
     * <p>
     * Code 'V' in legacy CDX format
     */
    public static final FieldName<Number> OFFSET = forNameAndCodeAndType("offset", 'V', Number.class);

    /**
     * Original Url.
     * <p>
     * Code 'a' in legacy CDX format
     */
    public static final FieldName<Uri> ORIGINAL_URI = forNameAndCodeAndType("uri", 'a', Uri.class);

    /**
     * Timestamp.
     * <p>
     * Code 'b' in legacy CDX format
     */
    public static final FieldName<VariablePrecisionDateTime> TIMESTAMP = forNameAndCodeAndType("sts", 'b', VariablePrecisionDateTime.class);

    /**
     * File name.
     * <p>
     * Code 'g' in legacy CDX format
     */
    public static final FieldName<String> FILENAME = forNameAndCodeAndType("filename", 'g', String.class);

    /**
     * New style checksum.
     * <p>
     * Code 'k' in legacy CDX format
     */
    public static final FieldName<String> DIGEST = forNameAndCodeAndType("digest", 'k', String.class);

    /**
     * A Base32 encoded SHA-1 digest of the payload.
     * <p>
     * Omit if the URI has no intrinsic payload. For revisit records, this is the digest of the original payload. The
     * algorithm prefix (e.g. sha-1) is not included in this field.
     * <p>
     */
    public static final FieldName<String> PAYLOAD_DIGEST = forNameAndType("sha", String.class);

    /**
     * Media Content Type (MIME type).
     * <p>
     * For HTTP(S) response records this is typically the “Content-Type” from the HTTP header. This field, however, does
     * not specify the origin of the information. It may be used to include content type that was derived from content
     * analysis or other sources.
     * <p>
     * Code 'm' in legacy CDX format
     */
    public static final FieldName<String> CONTENT_TYPE = forNameAndCodeAndType("mct", 'm', String.class);

    /**
     * Redirect.
     * <p>
     * Code 'r' in legacy CDX format
     */
    public static final FieldName<String> REDIRECT = forNameAndCode("redirect", 'r');

    /**
     * HTTP Status Code.
     * <p>
     * Applicable for response records for HTTP(S) URIs.
     * <p>
     * Code 's' in legacy CDX format
     */
    public static final FieldName<Number> RESPONSE_CODE = forNameAndCodeAndType("hsc", 's', Number.class);

    /**
     * Record ID.
     * <p>
     * Typically WARC-Record-ID or equivalent if not using WARCs. In a mixed environment, you should ensure that record
     * ID is unique.
     */
    public static final FieldName<String> RECORD_ID = forNameAndType("rid", String.class);

    /**
     * Record Concurrent To.
     * <p>
     * The record ID of another record that the current record is considered to be ‘concurrent’ to. See further WARC
     * chapter 5.7 (WARC-Concurrent-To).
     */
    public static final FieldName<String> RECORD_CONCURRENT_TO = forNameAndType("rct", String.class);

    /**
     * Revisit Original URI.
     * <p>
     * Only valid for records of type revisit. Contains the URI of the record that this record is considered a revisit
     * of.
     */
    public static final FieldName<Uri> REVISIT_ORIGINAL_URI = forNameAndType("rou", Uri.class);

    /**
     * Revisit Original Date.
     * <p>
     * Only valid for records of type revisit. Contains the timestamp of the record that this record is considered a
     * revisit of.
     */
    public static final FieldName<VariablePrecisionDateTime> REVISIT_ORIGINAL_DATE = forNameAndType("rod", VariablePrecisionDateTime.class);

    /**
     * Revisit Original record ID.
     * <p>
     * Only valid for records of type revisit. Contains the record ID of the record that this record is considered a
     * revisit of.
     */
    public static final FieldName<String> REVISIT_ORIGINAL_ID = forNameAndType("roi", String.class);

    /**
     * Reference used to fetch the record.
     */
    public static final FieldName<Uri> RESOURCE_REF = forNameAndType("ref", Uri.class);

    /**
     * Comment.
     */
    public static final FieldName<String> COMMENT = forNameAndCode("comment", '#');

    /**
     * Record type.
     * <p>
     * Indicates what type of record the current line refers to. This field is fully compatible with WARC 1.0 definition
     * of WARC-Type (chapter 5.5 and chapter 6). For content not stored in WARCs, a reasonable equivalent should be
     * chosen.
     * <p>
     * E.g.
     * <ul>
     * <li><b>response</b> - Suitable for any record that contains the response from a server to a specific request
     * (irrespective of protocol).</li>
     * <li><b>request</b> - Suitable for any record containing a request made to a server.</li>
     * <li><b>revisit</b> - Suitable for any record of a response from a server to a specific request, where the content
     * body is equal to that of another record.</li>
     * </ul>
     */
    public static final FieldName<String> RECORD_TYPE = forNameAndType("srt", String.class);

    private final String name;

    private final char code;

    private final T type;

    private FieldName(String name, char code, T type) {
        this.name = Objects.requireNonNull(name);
        this.code = code;
        this.type = Objects.requireNonNull(type);
    }

    public static <T> FieldName<T> forName(String name) {
        FieldName field = FIELDS_BY_NAME.get(name);
        if (field == null) {
            field = new FieldName(name, '?', Object.class);
            FIELDS_BY_NAME.put(name, field);
        }
        return field;
    }

    public static <T> FieldName<T> forCode(char code) {
        FieldName<T> field = FIELDS_BY_CODE.get(code);
        if (field == null) {
            throw new IllegalArgumentException("Illegal field code: " + code);
        }
        return field;
    }

    private static FieldName<String> forNameAndCode(String name, char code) {
        return forNameAndCodeAndType(name, code, String.class);
    }

    private static <T> FieldName<T> forNameAndType(String name, Class<T> type) {
        return forNameAndCodeAndType(name, '?', type);
    }

    private static <T> FieldName<T> forNameAndCodeAndType(String name, char code, Class<T> type) {
        FieldName field = FIELDS_BY_NAME.get(name);
        if (field == null) {
            field = new FieldName(name, code, type);
            FIELDS_BY_NAME.put(name, field);
            FIELDS_BY_CODE.put(code, field);
        }
        return field;
    }

    public String getName() {
        return name;
    }

    public char getCode() {
        return code;
    }

    public T getType() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final FieldName other = (FieldName) obj;
        return Objects.equals(this.name, other.name);
    }

}
