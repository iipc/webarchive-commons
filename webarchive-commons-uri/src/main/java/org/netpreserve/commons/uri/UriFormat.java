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

import org.netpreserve.commons.uri.parser.SurtEncoder;
import org.netpreserve.commons.uri.parser.StrictSurtEncoder;

/**
 * A format describing how to convert a Uri into a String.
 * <p>
 * This class is immutable and thread safe. All methods setting a value returns a fresh copy of the UriFormat.
 */
public final class UriFormat {

    private static final SurtEncoder DEFAULT_ENCODER = new StrictSurtEncoder();

    private boolean surtEncoding;

    private boolean ignoreScheme;

    private boolean ignoreAuthority;

    private boolean ignoreUser;

    private boolean ignorePassword;

    private boolean ignoreHost;

    private boolean ignorePort;

    private boolean ignorePath;

    private boolean ignoreQuery;

    private boolean ignoreFragment;

    private boolean decodeHost;

    private boolean decodePath;

    private SurtEncoder surtEncoder;

    /**
     * Use SURT encoding.
     * <p>
     * @param value true if SURT encoding should be used.
     * @return a clone of this UriFormat with the new value set
     * @see #surtEncoder(org.netpreserve.commons.uri.parser.SurtEncoder)
     * @see #getSurtEncoder()
     */
    public UriFormat surtEncoding(final boolean value) {
        UriFormat copy = new UriFormat(this);
        copy.surtEncoding = value;
        return copy;
    }

    /**
     * Returns true if SURT encoding of the authority should be used.
     * <p>
     * @return true if SURT encoding should be used
     * @see #surtEncoder(org.netpreserve.commons.uri.parser.SurtEncoder)
     * @see #getSurtEncoder()
     */
    public boolean isSurtEncoding() {
        return surtEncoding;
    }

    /**
     * Set the SurtEncoder implementation used when surtEncoding is set.
     * <p>
     * If no encoder is set, {@link StrictSurtEncoder} is used.
     * <p>
     * @param value the SurtEncoder to use.
     * @return a clone of this UriFormat with the new value set
     * @see #isSurtEncoding()
     */
    public UriFormat surtEncoder(final SurtEncoder value) {
        UriFormat copy = new UriFormat(this);
        copy.surtEncoder = value;
        return copy;
    }

    /**
     * Get the SURT encoder.
     * <p>
     * The SURT encoder to use for encoding the authority if {@link #isSurtEncoding()} is true.
     * <p>
     * @return the SURT encoder to use
     * @see #isSurtEncoding()
     */
    public SurtEncoder getSurtEncoder() {
        return surtEncoder;
    }

    /**
     * Ignore Scheme in output.
     * <p>
     * @param value true if Scheme should be ignored.
     * @return a clone of this UriFormat with the new value set
     */
    public UriFormat ignoreScheme(final boolean value) {
        UriFormat copy = new UriFormat(this);
        copy.ignoreScheme = value;
        return copy;
    }

    /**
     * Returns true if the scheme should be ignored.
     * <p>
     * @return true if the scheme should be ignored
     */
    public boolean isIgnoreScheme() {
        return ignoreScheme;
    }

    /**
     * Ignore Authority in output.
     * <p>
     * @param value true if Authority should be ignored.
     * @return a clone of this UriFormat with the new value set
     */
    public UriFormat ignoreAuthority(final boolean value) {
        UriFormat copy = new UriFormat(this);
        copy.ignoreAuthority = value;
        return copy;
    }

    /**
     * Returns true if the whole authority (user, password, host and port) should be ignored.
     * <p>
     * @return true if the whole authority should be ignored
     */
    public boolean isIgnoreAuthority() {
        return ignoreAuthority;
    }

    /**
     * Ignore User in output.
     * <p>
     * @param value true if User should be ignored.
     * @return a clone of this UriFormat with the new value set
     */
    public UriFormat ignoreUser(final boolean value) {
        UriFormat copy = new UriFormat(this);
        copy.ignoreUser = value;
        return copy;
    }

    /**
     * Returns true if the user should be ignored.
     * <p>
     * @return true if the user should be ignored
     */
    public boolean isIgnoreUser() {
        return ignoreUser;
    }

    /**
     * Ignore Password in output.
     * <p>
     * @param value true if Password should be ignored.
     * @return a clone of this UriFormat with the new value set
     */
    public UriFormat ignorePassword(final boolean value) {
        UriFormat copy = new UriFormat(this);
        copy.ignorePassword = value;
        return copy;
    }

    /**
     * Returns true if the password should be ignored.
     * <p>
     * @return true if the password should be ignored
     */
    public boolean isIgnorePassword() {
        return ignorePassword;
    }

    /**
     * Ignore Host in output.
     * <p>
     * @param value true if Host should be ignored.
     * @return a clone of this UriFormat with the new value set
     */
    public UriFormat ignoreHost(final boolean value) {
        UriFormat copy = new UriFormat(this);
        copy.ignoreHost = value;
        return copy;
    }

    /**
     * Returns true if the host should be ignored.
     * <p>
     * @return true if the host should be ignored
     */
    public boolean isIgnoreHost() {
        return ignoreHost;
    }

    /**
     * Ignore Port in output.
     * <p>
     * @param value true if Port should be ignored.
     * @return a clone of this UriFormat with the new value set
     */
    public UriFormat ignorePort(final boolean value) {
        UriFormat copy = new UriFormat(this);
        copy.ignorePort = value;
        return copy;
    }

    /**
     * Returns true if the port should be ignored.
     * <p>
     * @return true if the port should be ignored
     */
    public boolean isIgnorePort() {
        return ignorePort;
    }

    /**
     * Ignore Path in output.
     * <p>
     * @param value true if Path should be ignored.
     * @return a clone of this UriFormat with the new value set
     */
    public UriFormat ignorePath(final boolean value) {
        UriFormat copy = new UriFormat(this);
        copy.ignorePath = value;
        return copy;
    }

    /**
     * Returns true if the path should be ignored.
     * <p>
     * @return true if the path should be ignored
     */
    public boolean isIgnorePath() {
        return ignorePath;
    }

    /**
     * Ignore Query in output.
     * <p>
     * @param value true if Query should be ignored.
     * @return a clone of this UriFormat with the new value set
     */
    public UriFormat ignoreQuery(final boolean value) {
        UriFormat copy = new UriFormat(this);
        copy.ignoreQuery = value;
        return copy;
    }

    /**
     * Returns true if the query should be ignored.
     * <p>
     * @return true if the query should be ignored
     */
    public boolean isIgnoreQuery() {
        return ignoreQuery;
    }

    /**
     * Ignore Fragment in output.
     * <p>
     * @param value true if Fragment should be ignored.
     * @return a clone of this UriFormat with the new value set
     */
    public UriFormat ignoreFragment(final boolean value) {
        UriFormat copy = new UriFormat(this);
        copy.ignoreFragment = value;
        return copy;
    }

    /**
     * Returns true if fragment should be ignored.
     * <p>
     * @return true if fragment should be ignored
     */
    public boolean isIgnoreFragment() {
        return ignoreFragment;
    }

    /**
     * Decode the Host in output.
     * <p>
     * If the host is an IDN, it could be puny encoded or contain international characters in UTF-8. The default is to
     * puny encode. Setting this value to true decodes the host into UTF-8.
     * <p>
     * @param value true if Host should be decoded to UTF-8.
     * @return a clone of this UriFormat with the new value set
     */
    public UriFormat decodeHost(final boolean value) {
        UriFormat copy = new UriFormat(this);
        copy.decodeHost = value;
        return copy;
    }

    /**
     * Returns true if the host should be decoded.
     * <p>
     * If the host is an IDN, it could be puny encoded or contain international characters in UTF-8. The default is to
     * puny encode. If this value is true host is decoded into UTF-8.
     * <p>
     * @return true if the host should be decoded
     */
    public boolean isDecodeHost() {
        return decodeHost;
    }

    /**
     * Decode any percent encoded characters.
     * <p>
     * @param value true if the path should be decoded.
     * @return a clone of this UriFormat with the new value set
     */
    public UriFormat decodePath(final boolean value) {
        UriFormat copy = new UriFormat(this);
        copy.decodePath = value;
        return copy;
    }

    /**
     * Returns true if percent encoded characters in path should be decoded.
     * <p>
     * @return true if percent encoded characters in path should be decoded
     */
    public boolean isDecodePath() {
        return decodePath;
    }

    /**
     * Get the value of the URI's user field.
     * <p>
     * If URI has no user or {@code ignoreUser} is set, null is returned.
     * <p>
     * @param uri the URI to get the user from
     * @return the URI's user or null
     * @see #ignoreUser(boolean)
     */
    public String getUser(Uri uri) {
        if (!ignoreUser) {
            return uri.getUser();
        }
        return null;
    }

    /**
     * Get the value of the URI's password field.
     * <p>
     * If URI has no password or {@code ignorePassword} is set, null is returned.
     * <p>
     * @param uri the URI to get the password from
     * @return the URI's password or null
     * @see #ignorePassword(boolean)
     */
    public String getPassword(Uri uri) {
        if (!ignorePassword) {
            return uri.getPassword();
        }
        return null;
    }

    /**
     * Get the value of the URI's host field.
     * <p>
     * If URI has no host or {@code ignoreHost} is set, null is returned. If {@code decodeHost} is set, the host value
     * will be decoded before it is returned.
     * <p>
     * @param uri the URI to get the host from
     * @return the URI's host or null
     * @see #ignoreHost(boolean)
     * @see #decodeHost(boolean)
     */
    public String getHost(Uri uri) {
        if (!ignoreHost) {
            if (decodeHost) {
                return uri.getDecodedHost();
            } else {
                return uri.getHost();
            }
        }
        return null;
    }

    /**
     * Get the value of the URI's port field.
     * <p>
     * If URI has the scheme's default port or {@code ignorePort} is set, null is returned.
     * <p>
     * @param uri the URI to get the port from
     * @return the URI's port or null
     * @see #ignorePort(boolean)
     */
    public Integer getPort(Uri uri) {
        if (!ignorePort && uri.getPort() != Uri.DEFAULT_PORT_MARKER) {
            return uri.getPort();
        }
        return null;
    }

    /**
     * Constructs a new UriFormat by copying all fields from another UriFormat.
     * <p>
     * This constructor is private to ensure immutability.
     * <p>
     * @param src the UriFormat to copy from
     */
    private UriFormat(final UriFormat src) {
        this.surtEncoding = src.surtEncoding;
        this.ignoreScheme = src.ignoreScheme;
        this.ignoreAuthority = src.ignoreAuthority;
        this.ignoreUser = src.ignoreUser;
        this.ignorePassword = src.ignorePassword;
        this.ignoreHost = src.ignoreHost;
        this.ignorePort = src.ignorePort;
        this.ignorePath = src.ignorePath;
        this.ignoreQuery = src.ignoreQuery;
        this.ignoreFragment = src.ignoreFragment;
        this.decodeHost = src.decodeHost;
        this.decodePath = src.decodePath;
        if (src.surtEncoder != null) {
            this.surtEncoder = src.surtEncoder;
        } else {
            this.surtEncoder = DEFAULT_ENCODER;
        }
    }

    /**
     * Constructs a new UriFormat.
     * <p>
     * All fields are set to false. Surt encoder is set to {@link #DEFAULT_ENCODER}
     */
    public UriFormat() {
    }

}
