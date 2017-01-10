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
package org.netpreserve.commons.cdx.formatter;

import java.io.IOException;
import java.io.Writer;

import org.netpreserve.commons.cdx.CdxFormat;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.CdxRecordKey;
import org.netpreserve.commons.cdx.cdxrecord.CdxjLineRecordKey;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriBuilder;

/**
 * Formats a cdx record in the cdxj format.
 */
public class CdxjLineFormatter implements CdxFormatter {

    private static final char[] JSON_START = " {".toCharArray();
    private static final char JSON_END = '}';
    private static final char COMMA = ',';
    private static final char FIELD_NAME_START = '"';
    private static final char[] FIELD_NAME_END = "\":".toCharArray();

    @Override
    public void format(final Writer out, final CdxRecord record,
            final CdxFormat outputFormat) throws IOException {

        CdxRecordKey key = record.getKey();
        if (key instanceof CdxjLineRecordKey) {
            out.write(((CdxjLineRecordKey) key).getUnparsed());
        } else {
            Uri surt = outputFormat.getKeyUriFormat().builder(record.get(FieldName.ORIGINAL_URI).getValue()).build();

            out.write(surt.toString());
            out.write(' ');
            out.write(record.get(FieldName.TIMESTAMP).getValue().toFormattedString(outputFormat.getKeyDateFormat()));
            out.write(' ');
            out.write(record.get(FieldName.RECORD_TYPE).getValue());
        }

        out.write(JSON_START);

        boolean notFirst = false;
        for (CdxRecord.Field entry : record) {
            if (!entry.getFieldName().equals(FieldName.URI_KEY)
                    && !entry.getFieldName().equals(FieldName.TIMESTAMP)
                    && !entry.getFieldName().equals(FieldName.FILENAME)
                    && !entry.getFieldName().equals(FieldName.OFFSET)
                    && !entry.getFieldName().equals(FieldName.RECORD_TYPE)) {

                if (notFirst) {
                    out.write(COMMA);
                }

                FieldName name = entry.getFieldName();

                out.write(FIELD_NAME_START);
                out.write(name.getName());
                out.write(FIELD_NAME_END);
                entry.getValue().toJson(out);

                notFirst = true;
            }
        }

        if (!record.hasField(FieldName.RESOURCE_REF)) {
            if (notFirst) {
                out.write(COMMA);
            }
            out.write(FIELD_NAME_START);
            out.write(FieldName.RESOURCE_REF.getName());
            out.write(FIELD_NAME_END);
            out.write("\"warcfile:");
            out.write(record.get(FieldName.FILENAME).toString());
            out.write('#');
            out.write(record.get(FieldName.OFFSET).toString());
            out.write('"');
        }

        out.write(JSON_END);
    }

}
