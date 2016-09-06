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

/**
 * Serializes a CDX record to a Writer.
 */
public interface CdxFormatter {

    /**
     * Format a CDX record.
     * @param out the {@link Writer} to write the record to
     * @param record the record to format
     * @param outputFormat the format definition describe the serialized format
     * @throws IOException is thrown if the underlying IO throws an exception
     */
    void format(final Writer out, final CdxRecord record,
            final CdxFormat outputFormat) throws IOException;

}
