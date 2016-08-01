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
package org.netpreserve.commons.cdx.cdxrecord;

import org.netpreserve.commons.cdx.CdxFormat;

/**
 * A format indicating that the CdxRecord is not connected to an unparsed line format.
 */
public final class NonCdxLineFormat implements CdxFormat {
    /**
     * The single instance of this format.
     */
    public static final NonCdxLineFormat FORMAT = new NonCdxLineFormat();

    /**
     * Private constructor to avoid instantiation.
     */
    private NonCdxLineFormat() {
    }

}
