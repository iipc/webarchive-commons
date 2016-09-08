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
 *
 */
public final class BooleanValue implements Value<Boolean> {

    public static final BooleanValue TRUE = new BooleanValue(Boolean.TRUE);

    public static final BooleanValue FALSE = new BooleanValue(Boolean.FALSE);

    private static final char[] TRUE_VALUE_LC = "true".toCharArray();

    private static final char[] TRUE_VALUE_UC = "TRUE".toCharArray();

    private final Boolean value;

    private BooleanValue(Boolean value) {
        this.value = value;
    }

    public static BooleanValue valueOf(char[] src, int start, int end) {
        int length = end - start;
        if (length == 4 && src.length - start >= 4) {
            for (int i = 0; i < TRUE_VALUE_LC.length; i++) {
                if (src[start + i] != TRUE_VALUE_LC[i]
                        && src[start + i] != TRUE_VALUE_UC[i]) {
                    return FALSE;
                }
            }
        } else {
            return FALSE;
        }
        return TRUE;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public void toJson(Writer out) throws IOException {
        out.write(value.toString());
    }

}
