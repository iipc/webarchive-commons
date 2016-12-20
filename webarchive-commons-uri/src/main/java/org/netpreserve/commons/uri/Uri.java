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
 * Immutable representation of a URI.
 */
public class Uri {

    /**
     * Value to use to indicate that the default port for the protocol (if any) should be used.
     */
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
        if (uriBuilder.scheme() != null) {
            this.scheme = uriBuilder.scheme().intern();
        } else {
            this.scheme = null;
        }

        this.user = uriBuilder.user();
        this.password = uriBuilder.password();
        this.host = uriBuilder.host();
        this.port = uriBuilder.port();
        this.path = uriBuilder.path();
        this.query = uriBuilder.query();
        this.parsedQuery = uriBuilder.parsedQuery;
        this.fragment = uriBuilder.fragment();
        this.isRegName = uriBuilder.isRegName();
        this.isIPv4address = uriBuilder.isIPv4address();
        this.isIPv6reference = uriBuilder.isIPv6reference();
        this.isAbsPath = uriBuilder.isAbsPath();

        this.charset = uriBuilder.charset();
        this.defaultFormat = uriBuilder.config().getDefaultFormat();
    }

    /**
     * Get the URI's scheme.
     * <p>
     * @return the scheme or null is URI is not absolute
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Get the URI's user info.
     * <p>
     * The userinfo consists of user + ':' + password.
     * <p>
     * @return the user info
     */
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

    /**
     * Get the URI's user.
     * <p>
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Get the URI's password.
     * <p>
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the hostname or ip address from this URI's authority component.
     * <p>
     * @return the hostname from this URI's authority component
     * @see #getDecodedHost()
     * @see #isRegistryName()
     * @see #isIPv4address()
     * @see #isIPv6reference()
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the decoded hostname or ip address from this URI's authority component.
     * <p>
     * <p>
     * @return the hostname from this URI's authority component
     * @see #getHost()
     * @see #isRegistryName()
     * @see #isIPv4address()
     * @see #isIPv6reference()
     */
    public String getDecodedHost() {
        if (isIPv4address || isIPv6reference) {
            return host;
        }
        if (Scheme.forName(scheme).isPunycodedHost()) {
            return IDN.toUnicode(host);
        }
        return decode(host);
    }

    /**
     * Get the URI's port.
     * <p>
     * If the URI uses the scheme's default port, {@link #DEFAULT_PORT_MARKER} is returned.
     * <p>
     * @return the port or {@link #DEFAULT_PORT_MARKER} if scheme's default port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the URI's decoded port.
     * <p>
     * If this URI has no port, but the scheme defines a default port, the scheme's default port is returned.
     * <p>
     * @return the port
     */
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

    /**
     * Get the URI's path component.
     * <p>
     * The path might be empty, but never null.
     * <p>
     * @return the URI's path
     * @see #getDecodedPath()
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the URI's decoded path component.
     * <p>
     * All percent encoded characters are decoded to Unicode characters. The path might be empty, but never null.
     * <p>
     * @return the URI's decoded path
     * @see #getPath()
     */
    public String getDecodedPath() {
        return decode(path);
    }

    /**
     * Get the URI's query component.
     * <p>
     * The query is the part after a question mark. Not all schemes support queries. The RFC 3986 doesn't specify a
     * format for queries for those schemas that supports it, but it is common to take the form:
     * {@code ?var1=val1&var2=val2}. For schemas using this form, {@link #getParsedQuery()} can be used to get the query
     * in a parsed format.
     * <p>
     * @return the URI's query. Can be null
     * @see #getParsedQuery()
     */
    public String getQuery() {
        return query;
    }

    /**
     * Get a parsed version of the URI's query component.
     * <p>
     * The query is the part after a question mark. Not all schemes support queries. The RFC 3986 doesn't specify a
     * format for queries for those schemas that supports it. This method only supports queries of the form:
     * {@code ?var1=val1&var2=val2} which is commonly used for example by the http scheme. For schemas not using this
     * form, {@link #getQuery()} can be used to get the query as a string.
     * <p>
     * @return the URI's query. Can be null
     * @see #getQuery()
     */
    public ParsedQuery getParsedQuery() {
        if (parsedQuery == null) {
            parsedQuery = new ParsedQuery(query);
        }
        return parsedQuery;
    }

    /**
     * Get the URI's fragment component.
     * <p>
     * The fragment is the part after a '#'. It is also often referred to as the hash component of the URI.
     * <p>
     * @return the fragment component
     */
    public String getFragment() {
        return fragment;
    }

    /**
     * Returns true if this URI contains an authority component.
     * <p>
     * @return true if this URI contains an authority component
     * @see #getAuthority()
     * @see #getUserinfo()
     * @see #getUser()
     * @see #getPassword()
     * @see #getHost()
     * @see #getDecodedHost()
     * @see #getPort()
     * @see #getDecodedPort()
     * @see #isRegistryName()
     * @see #isIPv4address()
     * @see #isIPv6reference()
     */
    public boolean isAuthority() {
        return host != null || user != null || password != null || port != DEFAULT_PORT_MARKER;
    }

    public boolean isRegistryName() {
        return isRegName;
    }

    /**
     * Returns true if this URI's host is an IPv4 address.
     * <p>
     * @return true if this URI's host is an IPv4 address
     */
    public boolean isIPv4address() {
        return isIPv4address;
    }

    /**
     * Returns true if this URI's host is an IPv6 reference.
     * <p>
     * @return true if this URI's host is an IPv6 reference
     */
    public boolean isIPv6reference() {
        return isIPv6reference;
    }

    /**
     * Returns true if this URI is absolute.
     * <p>
     * RFC 3986 defines an absolute URI to be a URI with a scheme. This is equal to {@code getScheme() != null}.
     * <p>
     * @return true if this URI is absolute
     */
    public boolean isAbsolute() {
        return scheme != null;
    }

    /**
     * Returns true if this URI's path is absolute.
     * <p>
     * A URI's path is absolute if it starts with the character {@code '/'}. This method is only relevant for URIs with
     * a scheme which defines a hierarchical path (eg. http,ftp).
     * <p>
     * @return true if this URI's path is absolute
     */
    public boolean isAbsolutePath() {
        return isAbsPath;
    }

    public Charset getCharset() {
        return charset;
    }

    /**
     * Get the default formatting as specified when creating the URI.
     * <p>
     * @return the default format
     */
    public UriFormat getDefaultFormat() {
        return defaultFormat;
    }

    /**
     * Returns a string with this URI's fields for debugging.
     * <p>
     * @return a descriptive string of this URI's fields
     */
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

    /**
     * Get this URI as a string using the default format.
     * <p>
     * @return this URI as a string
     */
    @Override
    public String toString() {
        if (toStringCache == null) {
            toStringCache = toCustomString(defaultFormat);
        }
        return toStringCache;
    }

    public String toDecodedString() {
        UriFormat format;
        if (defaultFormat.isDecodeHost() && defaultFormat.isDecodePath()) {
            format = defaultFormat;
        } else {
            format = defaultFormat.decodeHost(true).decodePath(true);
        }
        return toCustomString(format);
    }

    /**
     * Get this URI as a string using a custom format.
     * <p>
     * @param format the format used for creating the string.
     * @return this URI as a string
     */
    public String toCustomString(UriFormat format) {
        try {
            StringBuilder buf = new StringBuilder();

            if (!format.isIgnoreScheme() && scheme != null) {
                buf.append(scheme);
                buf.append(':');
            }

            if (!format.isIgnoreAuthority() && isAuthority()) {
                if (!format.isIgnoreScheme() && scheme != null) {
                    buf.append("//");
                }

                if (format == defaultFormat) {
                    buf.append(getAuthority());
                } else {
                    formatAuthority(format, buf);
                }
            }

            if (!format.isIgnorePath() && path != null) {
                if (!path.isEmpty()) {
                    if (format.isDecodePath()) {
                        buf.append(getDecodedPath());
                    } else {
                        buf.append(path);
                    }
                }
            }

            if (!format.isIgnoreQuery() && query != null) {
                buf.append('?');
                buf.append(query);
            }

            if (!format.isIgnoreFragment() && fragment != null) {
                buf.append('#');
                buf.append(fragment);
            }

            return buf.toString();
        } catch (UriException ex) {
            throw new RuntimeException(ex);
        }
    }

    private StringBuilder formatAuthority(UriFormat format, StringBuilder buf) {
        if (format.isSurtEncoding()) {
            format.getSurtEncoder().encode(buf, this, format);
        } else {

            boolean isUserInfo = false;
            if (!format.isIgnoreUser() && user != null) {
                buf.append(user);
                isUserInfo = true;
            }
            if (!format.isIgnorePassword() && password != null) {
                buf.append(':').append(password);
                isUserInfo = true;
            }
            if (isUserInfo) {
                buf.append('@');
            }

            if (!format.isIgnoreHost() && host != null) {
                if (isIPv6reference) {
                    buf.append('[');
                }
                if (format.isDecodeHost()) {
                    buf.append(getDecodedHost());
                } else {
                    buf.append(host);
                }
                if (isIPv6reference) {
                    buf.append(']');
                }
            }

            if (!format.isIgnorePort() && port > DEFAULT_PORT_MARKER) {
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
