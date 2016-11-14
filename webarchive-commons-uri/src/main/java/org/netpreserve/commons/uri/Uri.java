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

    public static final int DEFAULT_PORT_MARKER = -1;

    final String scheme;

    final String user;

    final String password;

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

    private transient String authorityCache;

    Uri(final UriBuilder uriBuilder) {
        // There are usually only a little number of schemes. Interning the field reuses the same
        // string to reduce memory footprint.
        this.scheme = uriBuilder.scheme() != null ? uriBuilder.scheme().intern() : null;

        this.user = uriBuilder.user();
        this.password = uriBuilder.password();
        this.host = uriBuilder.host();
        this.port = uriBuilder.port;
        this.path = uriBuilder.path;
        this.query = uriBuilder.query;
        this.parsedQuery = uriBuilder.parsedQuery;
        this.fragment = uriBuilder.fragment;
        this.isRegName = uriBuilder.isRegName;
        this.isIPv4address = uriBuilder.isIPv4address;
        this.isIPv6reference = uriBuilder.isIPv6reference;
        this.isAbsPath = uriBuilder.isAbsPath;

        this.charset = uriBuilder.charset;
        this.defaultFormat = uriBuilder.config.getDefaultFormat();
    }

    public String getScheme() {
        return scheme;
    }

    public String getUserinfo() {
        StringBuilder sb = new StringBuilder();
        if (user != null) {
            sb.append(user);
        }
        if (password != null) {
            sb.append(':').append(password);
        }
        return sb.toString();
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public String getDecodedHost() {
        if (isIPv4address || isIPv6reference) {
            return host;
        }
        if (Scheme.forName(scheme).isPunycodedHost()) {
            return IDN.toUnicode(host);
        }
        return decode(host);
    }

    public int getPort() {
        return port;
    }

    public int getDecodedPort() {
        if (port == DEFAULT_PORT_MARKER) {
            return Scheme.forName(scheme).defaultPort();
        } else {
            return port;
        }
    }

    public String getAuthority() {
        if (isAuthority() && authorityCache == null) {
            StringBuilder authBuf = new StringBuilder();
            authorityCache = formatAuthority(defaultFormat, authBuf).toString();
        }
        return authorityCache;
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

    public boolean isAuthority() {
        return host != null || user != null || password != null || port != DEFAULT_PORT_MARKER;
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

    public Charset getCharset() {
        return charset;
    }

    public UriFormat getDefaultFormat() {
        return defaultFormat;
    }

    public String toDebugString() {
        StringBuilder buf = new StringBuilder("Details for '" + toString() + "'\n");
        buf.append("  Scheme: ").append(scheme).append('\n');
        buf.append("  Authority: ").append(getAuthority()).append('\n');
        buf.append("  User: ").append(user).append('\n');
        buf.append("  Password: ").append(password).append('\n');
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
            format = defaultFormat.toBuilder().decodeHost(true).decodePath(true).build();
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

            if (!format.ignoreAuthority && isAuthority()) {
                if (!format.ignoreScheme && scheme != null) {
                    buf.append("//");
                }

                if (format == defaultFormat) {
                    buf.append(getAuthority());
                } else {
                    formatAuthority(format, buf);
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

    private StringBuilder formatAuthority(UriFormat format, StringBuilder buf) {
        if (format.surtEncoding) {
            format.surtEncoder.encode(buf, this, format);
        } else {

            boolean isUserInfo = false;
            if (!format.ignoreUser && user != null) {
                buf.append(user);
                isUserInfo = true;
            }
            if (!format.ignorePassword && password != null) {
                buf.append(':').append(password);
                isUserInfo = true;
            }
            if (isUserInfo) {
                buf.append('@');
            }

            if (!format.ignoreHost && host != null) {
                if (isIPv6reference) {
                    buf.append('[');
                }
                if (format.decodeHost) {
                    buf.append(getDecodedHost());
                } else {
                    buf.append(host);
                }
                if (isIPv6reference) {
                    buf.append(']');
                }
            }

            if (!format.ignorePort && port > DEFAULT_PORT_MARKER) {
                buf.append(':').append(port);
            }
        }

        return buf;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.scheme);
        hash = 47 * hash + Objects.hashCode(this.user);
        hash = 47 * hash + Objects.hashCode(this.password);
        hash = 47 * hash + Objects.hashCode(this.host);
        hash = 47 * hash + this.port;
        hash = 47 * hash + Objects.hashCode(this.path);
        hash = 47 * hash + Objects.hashCode(this.query);
        hash = 47 * hash + Objects.hashCode(this.fragment);
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
        if (!Objects.equals(this.user, other.user)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        if (!Objects.equals(this.host, other.host)) {
            return false;
        }
        if (this.port != other.port) {
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
