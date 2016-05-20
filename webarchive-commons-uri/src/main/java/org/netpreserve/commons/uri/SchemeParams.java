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
package org.netpreserve.commons.uri;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public final class SchemeParams {
    public static final SchemeParams UNDEFINED = new SchemeParams(null, -1, false);
    public static final SchemeParams HTTP = new SchemeParams("http", 80, true);
    public static final SchemeParams HTTPS = new SchemeParams("https", 443, true);

    private static final Map<String, SchemeParams> SCHEME_MAP;
    static {
        SCHEME_MAP = new HashMap<>();

        SCHEME_MAP.put(HTTP.name, HTTP);
        SCHEME_MAP.put(HTTPS.name, HTTPS);
    }

    public final String name;
    public final int defaultPort;
    public final boolean punycodedHost;

    private SchemeParams(final String name, final int defaultPort, boolean punycodedHost) {
        this.name = name != null ? name.intern() : null;
        this.defaultPort = defaultPort;
        this.punycodedHost = punycodedHost;
    }

    public static SchemeParams forName(String name) {
        if (name == null) {
            return UNDEFINED;
        }

        SchemeParams result = SCHEME_MAP.get(name.toLowerCase());
        if (result == null) {
            result = UNDEFINED;
        }
        return result;
    }

    public static boolean isType(String scheme, SchemeParams... type) {
        for (SchemeParams sd : type) {
            if (sd.name.equals(scheme)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "SchemeDefaults{" + name + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchemeParams other = (SchemeParams) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

}
