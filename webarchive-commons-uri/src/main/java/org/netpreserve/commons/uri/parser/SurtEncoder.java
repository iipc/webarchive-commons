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
package org.netpreserve.commons.uri.parser;

import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriFormat;

/**
 * An encoder capable of turning a host name/ip-address into SURT format.
 *
 * This is used internally in the Uri class. The reason for the possibility of
 * having more than one implementation of the SURT encoder is that Heritrix and
 * Legacy wayback did this in different ways and we want to be able to support
 * old files.
 */
public interface SurtEncoder {

    /**
     * Encode the host part of a Uri into SURT format.
     * <p>
     * @param sb A StringBuilder to append the result to.
     * @param uri the Uri whose host name should be SURT encoded.
     * @param uriFormat the uriFormat used to format the Uri.
     */
    abstract void encode(StringBuilder sb, Uri uri, UriFormat uriFormat);
}
