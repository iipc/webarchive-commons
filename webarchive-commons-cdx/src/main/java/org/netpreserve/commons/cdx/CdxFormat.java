/*
 * Copyright 2015 IIPC.
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

/**
 * Information specific to a cdx format.
 */
public interface CdxFormat {

    /**
     * Get the file name suffix used for files of this format.
     * <p>
     * This is the suffix excluding the last dot e.g. {@code cdxj}.
     * <p>
     * @return the file name suffix or null if this format has no file representation
     */
    String getFileSuffix();

    /**
     * Get the header used to identify the format of this format.
     * <p>
     * e.g. {@code !OpenWayback-CDXJ 1.0}
     * <p>
     * @return the file header line or null if this format has no file representation
     */
    String getFileHeader();

}
