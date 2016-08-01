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

import org.netpreserve.commons.cdx.cdxrecord.CdxLine;
import org.netpreserve.commons.cdx.cdxrecord.CdxLineFormat;
import org.netpreserve.commons.cdx.cdxrecord.CdxjLine;
import org.netpreserve.commons.cdx.cdxrecord.CdxjLineFormat;
import org.netpreserve.commons.cdx.cdxrecord.NonCdxLineFormat;
import org.netpreserve.commons.cdx.cdxrecord.UnconnectedCdxRecord;

/**
 * Factory for creating CdxRecords.
 */
public final class CdxRecordFactory {

    /**
     * Private constructor to avoid instantiation.
     */
    private CdxRecordFactory() {
    }

    /**
     * Factory method for creating CdxRecords.
     * <p>
     * @param data a character array containing raw record formatted according to the submitted format. Might be null if
     * format allows.
     * @param format a format for the unparsed CDX data.
     * @return the newly created record.
     * @throws IllegalArgumentException if CdxFormat is not recognized by this method.
     */
    public static final CdxRecord create(final char[] data, final CdxFormat format) {
        if (format instanceof CdxLineFormat) {
            return new CdxLine(data, (CdxLineFormat) format);
        } else if (format instanceof CdxjLineFormat) {
            return new CdxjLine(data, (CdxjLineFormat) format);
        } else if (format instanceof NonCdxLineFormat) {
            return new UnconnectedCdxRecord();
        }

        throw new IllegalArgumentException("Unknow CdxFormat: " + format.getClass());
    }

    /**
     * Factory method for creating CdxRecords.
     * <p>
     * @param data a string containing raw record formatted according to the submitted format. Might be null if format
     * allows.
     * @param format a format for the unparsed CDX data.
     * @return the newly created record.
     * @throws IllegalArgumentException if CdxFormat is not recognized by this method.
     */
    public static final CdxRecord create(final String data, final CdxFormat format) {
        return create(data.toCharArray(), format);
    }

}
