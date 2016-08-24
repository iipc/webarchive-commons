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

import java.io.ByteArrayOutputStream;
import java.net.IDN;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 *
 */
public class Uri {

    final String scheme;

    final String authority;

    final String userinfo;

    final String host;

    final int port;

    final String path;

    final String query;

    final String fragment;

    final boolean isRegName;

    final boolean isIPv4address;

    final boolean isIPv6reference;

    final boolean isAbsPath;

    final Charset charset;

    final UriFormat defaultFormat;

    transient ParsedQuery parsedQuery;

    private transient String toStringCache;

    Uri(final UriBuilder uriBuilder) {
        // There are usually only a little number of schemes. Interning the field reuses the same
        // string to reduce memory footprint.
        this.scheme = uriBuilder.scheme != null ? uriBuilder.scheme.intern() : null;

        this.authority = uriBuilder.authority;
        this.userinfo = uriBuilder.userinfo;
        this.port = uriBuilder.port;
        this.path = uriBuilder.path;
        this.query = uriBuilder.query;
        this.parsedQuery = uriBuilder.parsedQuery;
        this.fragment = uriBuilder.fragment;
        this.isRegName = uriBuilder.isRegName;
        this.isIPv4address = uriBuilder.isIPv4address;
        this.isIPv6reference = uriBuilder.isIPv6reference;
        this.isAbsPath = uriBuilder.isAbsPath;

        // Coalesce the getHost and getAuthority fields where possible.
        //
        // In the web crawl/http domain, most URIs have an identical getHost and getAuthority. (There is
        // no getPort or user info.)
        //
        // Notably, the lengths of these fields are equal if and only if their values are identical.
        // This code makes use of this fact to reduce the two instances to one where possible,
        // slimming instances.
        if (uriBuilder.host != null && uriBuilder.authority != null
                && uriBuilder.host.length() == uriBuilder.authority.length()) {
            this.host = uriBuilder.authority;
        } else {
            this.host = uriBuilder.host;
        }

        this.charset = uriBuilder.charset;
        this.defaultFormat = uriBuilder.config.getDefaultFormat();
    }

    public String getScheme() {
        return scheme;
    }

    public String getUserinfo() {
        return userinfo;
    }

    public String getHost() {
        return host;
    }

    public String getDecodedHost() {
        if (Schemes.forName(scheme).punycodedHost) {
            return IDN.toUnicode(host);
        }
        return decode(host);
    }

    public int getPort() {
        return port;
    }

    public int getDecodedPort() {
        if (port == -1) {
            return Schemes.forName(scheme).defaultPort;
        } else {
            return port;
        }
    }

    public String getAuthority() {
        return authority;
    }

    public String getPath() {
        return path;
    }

    public String getDecodedPath() {
        return decode(path);
    }

    public String getQuery() {
        return query;
    }

    public ParsedQuery getParsedQuery() {
        if (parsedQuery == null) {
            parsedQuery = new ParsedQuery(query);
        }
        return parsedQuery;
    }

    public String getFragment() {
        return fragment;
    }

    public boolean isRegistryName() {
        return isRegName;
    }

    public boolean isIPv4address() {
        return isIPv4address;
    }

    public boolean isIPv6reference() {
        return isIPv6reference;
    }

    public boolean isAbsolute() {
        return scheme != null;
    }

    public boolean isAbsolutePath() {
        return isAbsPath;
    }

    public String toDebugString() {
        StringBuilder buf = new StringBuilder("Details for '" + toString() + "'\n");
        buf.append("  Scheme: ").append(scheme).append('\n');
        buf.append("  Authority: ").append(authority).append('\n');
        buf.append("  UserInfo: ").append(userinfo).append('\n');
        buf.append("  Host: ").append(host).append('\n');
        buf.append("  Port: ").append(port).append('\n');
        buf.append("  Path: ").append(path).append('\n');
        buf.append("  Query: ").append(query).append('\n');
        buf.append("  Fragment: ").append(fragment).append('\n');
        buf.append("  Is absolute: ").append(isAbsolute()).append('\n');
        buf.append("  Is absolute path: ").append(isAbsolutePath()).append('\n');
        buf.append("  Is regname: ").append(isRegName).append('\n');
        buf.append("  Is IPv4: ").append(isIPv4address).append('\n');
        buf.append("  Is IPv6: ").append(isIPv6reference).append('\n');

        return buf.toString();
    }

    @Override
    public String toString() {
        if (toStringCache == null) {
            toStringCache = toCustomString(defaultFormat);
        }
        return toStringCache;
    }

    public String toDecodedString() {
        UriFormat format;
        if (defaultFormat.decodeHost && defaultFormat.decodePath) {
            format = defaultFormat;
        } else {
            format = UriFormat.builder(defaultFormat).decodeHost(true).decodePath(true).build();
        }
        return toCustomString(format);
    }

    public String toCustomString(UriFormat format) {
        try {
            StringBuilder buf = new StringBuilder();

            if (!format.ignoreScheme && scheme != null) {
                buf.append(scheme);
                buf.append(':');
            }

            if (!format.ignoreAuthority && authority != null) {
                if (!format.ignoreScheme && scheme != null) {
                    buf.append("//");
                }
                if (format.surtEncoding) {
                    toSurtAuthority(buf, format);
                } else if ((format.ignoreUserInfo && userinfo != null)
                        || (host != null && (format.ignoreHost || format.decodeHost))
                        || (format.ignorePort && port != -1)) {

                    if (!format.ignoreUserInfo && userinfo != null) {
                        buf.append(userinfo).append('@');
                    }
                    if (!format.ignoreHost && host != null) {
                        if (format.decodeHost) {
                            buf.append(getDecodedHost());
                        } else {
                            buf.append(host);
                        }
                    }
                    if (!format.ignorePort && port >= 0) {
                        buf.append(':').append(port);
                    }
                } else {
                    buf.append(authority);
                }
            }

            if (!format.ignorePath && path != null) {
                if (!path.isEmpty()) {
                    if (format.decodePath) {
                        buf.append(getDecodedPath());
                    } else {
                        buf.append(path);
                    }
                }
            }

            if (!format.ignoreQuery && query != null) {
                buf.append('?');
                buf.append(query);
            }

            if (!format.ignoreFragment && fragment != null) {
                buf.append('#');
                buf.append(fragment);
            }

            return buf.toString();
        } catch (UriException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void toSurtAuthority(StringBuilder buf, UriFormat format) {
        buf.append("(");

        if (!format.ignoreHost && host != null) {
            if (isRegName) {
                // other hostname match: do reverse
                int hostSegEnd = host.length();
                for (int i = hostSegEnd; i >= 0; i--) {
                    if (i > 0 && host.charAt(i - 1) != '.') {
                        continue;
                    }
                    buf.append(host, i, hostSegEnd); // rev getHost segment
                    buf.append(',');     // ','
                    hostSegEnd = i - 1;
                }
            } else {
                buf.append(host);
            }
        }

        if (!format.ignorePort && port != -1) {
            buf.append(':').append(port);
        }

        if (!format.ignoreUserInfo && userinfo != null) {
            buf.append('@');
            buf.append(userinfo);
        }

        buf.append(')');
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.scheme);
        hash = 53 * hash + Objects.hashCode(this.authority);
        hash = 53 * hash + Objects.hashCode(this.path);
        hash = 53 * hash + Objects.hashCode(this.query);
        hash = 53 * hash + Objects.hashCode(this.fragment);
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
        final Uri other = (Uri) obj;
        if (!Objects.equals(this.scheme, other.scheme)) {
            return false;
        }
        if (!Objects.equals(this.authority, other.authority)) {
            return false;
        }
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        if (!Objects.equals(this.query, other.query)) {
            return false;
        }
        if (!Objects.equals(this.fragment, other.fragment)) {
            return false;
        }
        return true;
    }

    final String decode(String string) {
        if (string == null) {
            return null;
        }
        if (string.isEmpty()) {
            return string;
        }

        byte[] bytes = string.getBytes();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            if (b == '+') {
                buffer.write(' ');
                continue;
            }
            if (b == '%') {
                if (i + 2 < bytes.length) {
                    int u = Character.digit((char) bytes[i + 1], 16);
                    int l = Character.digit((char) bytes[i + 2], 16);
                    if (u > -1 && l > -1) {
                        // good encoding
                        int c = ((u << 4) + l);
                        buffer.write((char) c);
                        i += 2;
                        continue;
                    } // else: bad encoding digits, leave '%' in place
                } // else: insufficient encoding digits, leave '%' in place
            }
            buffer.write(b);
        }
        return new String(buffer.toByteArray(), charset);
    }

}
