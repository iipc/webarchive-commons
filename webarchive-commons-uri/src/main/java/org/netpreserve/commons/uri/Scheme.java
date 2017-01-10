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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta data about schemes.
 */
public enum Scheme {

    /**
     * The empty scheme.
     */
    UNDEFINED(null, -1, false),
    /**
     * Not a well known scheme.
     */
    UNKNOWN(null, -1, false),
    /**
     * The http scheme.
     */
    HTTP("http", 80, true),
    /**
     * The https scheme.
     */
    HTTPS("https", 443, true),
    /**
     * The ftp scheme.
     */
    FTP("ftp", 21, true),
    /**
     * The ftps scheme.
     */
    FTPS("ftps", 990, true),
    /**
     * The dns scheme.
     */
    DNS("dns", 53, false),
    /**
     * The gopher scheme.
     */
    GOPHER("gopher", 70, false),
    /**
     * The ws scheme.
     */
    WS("ws", 80, true),
    /**
     * The wss scheme.
     */
    WSS("wss", 443, true),
    /**
     * The file scheme.
     */
    FILE("file", -1, false),
    /**
     * The about scheme.
     */
    ABOUT("about", -1, false),
    /**
     * The blob scheme.
     */
    BLOB("blob", -1, false),
    /**
     * The data scheme.
     */
    DATA("data", -1, false),
    /**
     * The filesystem scheme.
     */
    FILESYSTEM("filesystem", -1, false);

    private static final Map<String, Scheme> SCHEME_MAP;

    static {
        SCHEME_MAP = new HashMap<>();

        for (Scheme sc : values()) {
            SCHEME_MAP.put(sc.name, sc);
        }
    }

    private static final EnumSet<Scheme> SPECIAL_SCHEMES = EnumSet.of(FTP, FILE, GOPHER, HTTP, HTTPS, WS, WSS);

    private static final EnumSet<Scheme> LOCAL_SCHEMES = EnumSet.of(ABOUT, BLOB, DATA, FILESYSTEM);

    private static final EnumSet<Scheme> HTTP_SCHEMES;

    private static final EnumSet<Scheme> NETWORK_SCHEMES;

    private static final EnumSet<Scheme> FETCH_SCHEMES;

    static {
        HTTP_SCHEMES = EnumSet.of(HTTP, HTTPS);

        NETWORK_SCHEMES = EnumSet.copyOf(HTTP_SCHEMES);
        NETWORK_SCHEMES.add(FTP);

        FETCH_SCHEMES = EnumSet.copyOf(NETWORK_SCHEMES);
        FETCH_SCHEMES.addAll(LOCAL_SCHEMES);
        FETCH_SCHEMES.add(FILE);
    }

    private final String name;

    private final int defaultPort;

    private final boolean punycodedHost;

    /**
     * Constructor for Scheme.
     * <p>
     * @param name the normalized name of this scheme.
     * @param defaultPort The default port for this scheme or {@code -1} if the scheme has no default port.
     * @param punycodedHost True if this scheme supports punycoded IDN for host.
     */
    Scheme(final String name, final int defaultPort, boolean punycodedHost) {
        this.name = name;
        this.defaultPort = defaultPort;
        this.punycodedHost = punycodedHost;
    }

    /**
     * The normalized name of this scheme.
     * <p>
     * @return the normalized name
     */
    public String normalizedName() {
        return name;
    }

    /**
     * The default port for this scheme or {@code -1} if the scheme has no default port.
     * <p>
     * @return the default port or -1 if no port is defined
     */
    public int defaultPort() {
        return defaultPort;
    }

    /**
     * True if this scheme supports punycoded IDN for host.
     * <p>
     * @return true if this scheme supports punycoded IDN for host
     */
    public boolean isPunycodedHost() {
        return punycodedHost;
    }

    /**
     * Get the Scheme for a scheme name.
     * <p>
     * If name is null or empty {@link #UNDEFINED} is returned. If name is not a well known scheme {@link #UNKNOWN} is
     * returned.
     * <p>
     * @param name the scheme name. Can be null.
     * @return the Scheme.
     */
    public static Scheme forName(String name) {
        if (name == null || name.isEmpty()) {
            return UNDEFINED;
        }

        Scheme result = SCHEME_MAP.get(name.toLowerCase());
        if (result == null) {
            result = UNKNOWN;
        }
        return result;
    }

    /**
     * Check if this Scheme is one of the listed types.
     * <p>
     * @param type one or more schemes to check.
     * @return true if this Scheme matches one of the submitted types
     */
    public boolean isType(Scheme... type) {
        for (Scheme sd : type) {
            if (sd == this) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if this Scheme is special.
     * <p>
     * Returns true if this Scheme is one of the special schemes defined by whatwg.org:
     * <a href="https://url.spec.whatwg.org/#special-scheme">https://url.spec.whatwg.org/#special-scheme</a>.
     * <p>
     * The special schemes are:
     * <ul>
     * <li>ftp</li>
     * <li>file</li>
     * <li>gopher</li>
     * <li>http</li>
     * <li>https</li>
     * <li>ws</li>
     * <li>wss</li>
     * </ul>
     * <p>
     * @return true if scheme is special
     */
    public boolean isSpecial() {
        return SPECIAL_SCHEMES.contains(this);
    }

    /**
     * Check if this Scheme is local.
     * <p>
     * Returns true if this Scheme is one of the local schemes defined by whatwg.org:
     * <a href="https://url.spec.whatwg.org/#local-scheme">https://url.spec.whatwg.org/#local-scheme</a>.
     * <p>
     * The local schemes are:
     * <ul>
     * <li>about</li>
     * <li>blob</li>
     * <li>data</li>
     * <li>filesystem</li>
     * </ul>
     * <p>
     * @return true if scheme is local
     */
    public boolean isLocal() {
        return LOCAL_SCHEMES.contains(this);
    }

    /**
     * Check if this Scheme is http(s).
     * <p>
     * Returns true if this Scheme is one of the http(s) schemes defined by whatwg.org:
     * <a href="https://url.spec.whatwg.org/#http-scheme">https://url.spec.whatwg.org/#http-scheme</a>.
     * <p>
     * The http schemes are:
     * <ul>
     * <li>http</li>
     * <li>https</li>
     * </ul>
     * <p>
     * @return true if scheme is http(s)
     */
    public boolean isHttp() {
        return HTTP_SCHEMES.contains(this);
    }

    /**
     * Check if this Scheme is network scheme.
     * <p>
     * Returns true if this Scheme is one of the network schemes defined by whatwg.org:
     * <a href="https://url.spec.whatwg.org/#network-scheme">https://url.spec.whatwg.org/#network-scheme</a>.
     * <p>
     * The network schemes are:
     * <ul>
     * <li>http</li>
     * <li>https</li>
     * <li>ftp</li>
     * </ul>
     * <p>
     * @return true if scheme is a network scheme
     */
    public boolean isNetwork() {
        return NETWORK_SCHEMES.contains(this);
    }

    /**
     * Check if this Scheme is fetch scheme.
     * <p>
     * Returns true if this Scheme is one of the fetch schemes defined by whatwg.org:
     * <a href="https://url.spec.whatwg.org/#fetch-scheme">https://url.spec.whatwg.org/#fetch-scheme</a>.
     * <p>
     * The fetch schemes are:
     * <ul>
     * <li>about</li>
     * <li>blob</li>
     * <li>data</li>
     * <li>file</li>
     * <li>filesystem</li>
     * <li>http</li>
     * <li>https</li>
     * <li>ftp</li>
     * </ul>
     * <p>
     * @return true if scheme is a fetch scheme
     */
    public boolean isFetch() {
        return FETCH_SCHEMES.contains(this);
    }

}
