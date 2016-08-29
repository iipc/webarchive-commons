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
import org.netpreserve.commons.cdx.cdxrecord.CdxLineFormat;
import org.netpreserve.commons.cdx.cdxrecord.CdxLineRecordKey;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.CdxRecordKey;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.json.NullValue;
import org.netpreserve.commons.cdx.json.NumberValue;
import org.netpreserve.commons.cdx.json.StringValue;
import org.netpreserve.commons.cdx.json.Value;
import org.netpreserve.commons.uri.Uri;

/**
 *
 */
public class CdxLineFormatter implements CdxFormatter {

    @Override
    public void format(final Writer out, final CdxRecord<? extends CdxFormat> record,
            final CdxFormat outputFormat) throws IOException {

        CdxLineFormat format = (CdxLineFormat) outputFormat;

        CdxRecordKey key = record.getKey();
        if (key instanceof CdxLineRecordKey) {
            out.write(((CdxLineRecordKey) key).getUnparsed());
        } else {
            out.write(key.getUriKey().getValue());
            out.write(' ');
            out.write(key.getTimeStamp().getValue().toFormattedString(format.getKeyDateFormat()));
        }

        for (int i = 2; i < format.getLength(); i++) {
            FieldName fieldName = format.getField(i);
            Value value = record.get(fieldName);

            if (value == NullValue.NULL) {
                if (fieldName == FieldName.FILENAME) {
                    Uri locator = ((Uri) record.get(FieldName.RESOURCE_REF).getValue());
                    if ("warcfile".equals(locator.getScheme())) {
                        value = StringValue.valueOf(locator.getPath());
                    }
                } else if (fieldName == FieldName.OFFSET) {
                    Uri locator = ((Uri) record.get(FieldName.RESOURCE_REF).getValue());
                    if ("warcfile".equals(locator.getScheme())) {
                        value = NumberValue.valueOf(locator.getFragment());
                    }
                }
            }

            out.append(' ');
            if (value == NullValue.NULL) {
                out.write('-');
            } else {
                out.write(value.toString());
            }
        }
    }

}
