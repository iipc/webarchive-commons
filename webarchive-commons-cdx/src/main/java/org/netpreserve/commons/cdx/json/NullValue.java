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
import java.io.Writer;

/**
 * A representation of a null value.
 * <p>
 * This class encapsulate a null value so that it still can have a Java type.
 * <p>
 * @param <T> The Java type for this null value.
 */
public final class NullValue<T> implements Value<T> {

    public static final NullValue NULL = new NullValue();

    private NullValue() {
    }

    @Override
    public T getValue() {
        return null;
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public void toJson(Writer out) throws IOException {
        out.write("null");
    }

}
