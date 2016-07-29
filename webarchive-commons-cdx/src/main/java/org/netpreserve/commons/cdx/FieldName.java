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

/**
 * Representation of a field name.
 */
public final class FieldName {

    public enum Type {

        STRING,
        NUMBER,
        BOOLEAN,
        URI,
        TIMESTAMP,
        ANY

    }

    private static final Map<String, FieldName> FIELDS_BY_NAME = new HashMap<>();

    private static final Map<Character, FieldName> FIELDS_BY_CODE = new HashMap<>();

    /**
     * Ceta tags (AIF).
     * <p>
     * Code 'M' in legacy CDX format
     */
    public static final FieldName ROBOT_FLAGS = forNameAndCode("robotflags", 'M');

    /**
     * A searchable version of the URI.
     * <p>
     * Code 'N' in legacy CDX format
     */
    public static final FieldName URI_KEY = forNameAndCodeAndType("ssu", 'N', Type.STRING);

    /**
     * Language string.
     * <p>
     * Code 'Q' in legacy CDX format
     */
    public static final FieldName LANGUAGE = forNameAndCode("lang", 'Q');

    /**
     * Payload Length.
     * <p>
     * The length of the payload (uncompressed). The exact meaning will vary by content type, but the common case is the
     * length of the document, excluding any HTTP headers in a HTTP response record.
     */
    public static final FieldName PAYLOAD_LENGTH = forNameAndType("ple", Type.NUMBER);

    /**
     * Content Length.
     * <p>
     * The length of the content (uncompressed), ignoring WARC headers, but including any HTTP headers or similar.
     */
    public static final FieldName CONTENT_LENGTH = forNameAndType("cle", Type.NUMBER);

    /**
     * Record length.
     * <p>
     * The length of the record. This is the entire record (including e.g. WARC headers) as written on disk (compressed
     * if stored compressed).
     * <p>
     * Code 'S' in legacy CDX format
     */
    public static final FieldName RECORD_LENGTH = forNameAndCodeAndType("rle", 'S', Type.NUMBER);

    /**
     * ARC/WARC file offset.
     * <p>
     * Code 'V' in legacy CDX format
     */
    public static final FieldName OFFSET = forNameAndCodeAndType("offset", 'V', Type.NUMBER);

    /**
     * Original Url.
     * <p>
     * Code 'a' in legacy CDX format
     */
    public static final FieldName ORIGINAL_URI = forNameAndCodeAndType("uri", 'a', Type.URI);

    /**
     * Timestamp.
     * <p>
     * Code 'b' in legacy CDX format
     */
    public static final FieldName TIMESTAMP = forNameAndCodeAndType("sts", 'b', Type.TIMESTAMP);

    /**
     * File name.
     * <p>
     * Code 'g' in legacy CDX format
     */
    public static final FieldName FILENAME = forNameAndCodeAndType("filename", 'g', Type.STRING);

    /**
     * New style checksum.
     * <p>
     * Code 'k' in legacy CDX format
     */
    public static final FieldName DIGEST = forNameAndCodeAndType("digest", 'k', Type.STRING);

    /**
     * A Base32 encoded SHA-1 digest of the payload.
     * <p>
     * Omit if the URI has no intrinsic payload. For revisit records, this is the digest of the original payload. The
     * algorithm prefix (e.g. sha-1) is not included in this field.
     * <p>
     */
    public static final FieldName PAYLOAD_DIGEST = forNameAndType("sha", Type.STRING);

    /**
     * Media Content Type (MIME type).
     * <p>
     * For HTTP(S) response records this is typically the “Content-Type” from the HTTP header. This field, however, does
     * not specify the origin of the information. It may be used to include content type that was derived from content
     * analysis or other sources.
     * <p>
     * Code 'm' in legacy CDX format
     */
    public static final FieldName CONTENT_TYPE = forNameAndCodeAndType("mct", 'm', Type.STRING);

    /**
     * Redirect.
     * <p>
     * Code 'r' in legacy CDX format
     */
    public static final FieldName REDIRECT = forNameAndCode("redirect", 'r');

    /**
     * HTTP Status Code.
     * <p>
     * Applicable for response records for HTTP(S) URIs.
     * <p>
     * Code 's' in legacy CDX format
     */
    public static final FieldName RESPONSE_CODE = forNameAndCodeAndType("hsc", 's', Type.NUMBER);

    /**
     * Record ID.
     * <p>
     * Typically WARC-Record-ID or equivalent if not using WARCs. In a mixed environment, you should ensure that record
     * ID is unique.
     */
    public static final FieldName RECORD_ID = forNameAndType("rid", Type.STRING);

    /**
     * Reference used to fetch the record.
     */
    public static final FieldName RESOURCE_REF = forNameAndType("ref", Type.URI);

    /**
     * Comment.
     */
    public static final FieldName COMMENT = forNameAndCode("comment", '#');

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
    public static final FieldName RECORD_TYPE = forNameAndType("srt", Type.STRING);

    private final String name;

    private final char code;

    private final Type type;

    private FieldName(String name, char code, Type type) {
        this.name = Objects.requireNonNull(name);
        this.code = code;
        this.type = Objects.requireNonNull(type);
    }

    public static FieldName forName(String name) {
        FieldName field = FIELDS_BY_NAME.get(name);
        if (field == null) {
            field = new FieldName(name, '?', Type.ANY);
            FIELDS_BY_NAME.put(name, field);
        }
        return field;
    }

    public static FieldName forCode(char code) {
        FieldName field = FIELDS_BY_CODE.get(code);
        if (field == null) {
            throw new IllegalArgumentException("Illegal field code: " + code);
        }
        return field;
    }

    private static FieldName forNameAndCode(String name, char code) {
        return forNameAndCodeAndType(name, code, Type.STRING);
    }

    private static FieldName forNameAndType(String name, Type type) {
        return forNameAndCodeAndType(name, '?', type);
    }

    private static FieldName forNameAndCodeAndType(String name, char code, Type type) {
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

    public Type getType() {
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
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

}
