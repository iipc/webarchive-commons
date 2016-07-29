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

/**
 * Gives access to the underlying data for classes that contain unparsed data.
 * <p>
 * If the underlying data structure is a character array, it is legal to return it as is. The array should therefore not
 * be modified.
 * <p>
 * This is useful for identity transformation to avoid the cost of parsing.
 */
public interface HasUnparsedData {

    /**
     * Get the unparsed data.
     * <p>
     * @return a character array containing data which is not parsed or validated.
     */
    char[] getUnparsed();

}
