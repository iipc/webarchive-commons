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
package org.netpreserve.commons.cdx.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 *
 * @param <T> The Java type encapsulated by this value
 */
public interface Value<T> {

    void toJson(Writer out) throws IOException;

    T getValue();

    default String toJson() {
        try {
            StringWriter sw = new StringWriter();
            toJson(sw);
            return sw.toString();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
