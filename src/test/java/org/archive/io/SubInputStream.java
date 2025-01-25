/*
 * $Header: $
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
/*
 *
 */
package org.archive.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Encapsulates another stream, keeping track of local as well as global offsets.
 * The SubStream has a max size and close() ensures that the encapsulated Stream
 * is fast-forwarded to that point.
 * </p><p>
 * Note: Calling close on the SubInputStream does not close the wrapped stream.
 * Note 2: This implementation relies on the wrapped InputStream not to return 0
 * in {@link java.io.InputStream#available()} until that stream has been depleted.
 */
public class SubInputStream extends InputStream {
    private final InputStream inner;
    private final long globalPosOrigo;

    private long length;
    private long mark = -1;
    private long pos = 0;

    /**
     * Wraps the inner Stream with no max on bytes read. Reduces functionality to tracking position.
     * </p><p>
     * Note: The length can be specified later with {@link #setLength(long)}.
     * @param inner data source.
     */
    public SubInputStream(InputStream inner) {
        this(inner, Long.MAX_VALUE, 0);
    }
    /**
     * Wraps the inner InputStream with a max on bytes read.
     * @param inner  data source.
     * @param length the number of bytes that at a maximum can be read from inner.
     */
    public SubInputStream(InputStream inner, long length) {
        this(inner, length, 0);
    }
    /**
     * Wraps the inner InputStream with a max on bytes read.
     * @param inner  data source.
     * @param length the number of bytes that at a maximum can be read from inner.
     * @param globalPosition the position in the inner stream.
     */
    public SubInputStream(InputStream inner, long length, long globalPosition) {
        this.inner = inner;
        globalPosOrigo = globalPosition;
        this.length = length;
    }

    /**
     * @return the position from the virtual stream.
     */
    public long getPosition() {
        return pos;
    }

    /**
     * @return the position in the wrapped stream, if the starting point was stated during construction.
     */
    public long getGlobalPosition() {
        return globalPosOrigo + pos;
    }

    public void setLength(long length) {
        if (length <= pos) {
            throw new IllegalStateException(
                    "The position is " + pos + " which is past the allowed virtual length " + length);
        }
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    /**
     * Reads to the next '\n' and returns the line as an UTF-8 string, excluding trailing
     * carriage returns {@code '\r'} and newlines {@code '\n'}.
     * @return the next line or null if EOF.
     * @throws IOException if there was a problem reading bytes from inner.
     */
    public String readLine() throws IOException {
        byte[] bytes = readLineBytes();
        if (bytes == null) {
            return null;
        }
        int length = bytes.length;
        while (length > 0 && (bytes[length-1] == '\n' || bytes[length-1] == '\r')) {
            length--;
        }
        return new String(bytes, 0, length, "utf-8");
    }
    /**
     * Reads to the next '\n' and returns the line as raw bytes, including the delimiting '\n'.
     * @return the next line.
     * @throws IOException if there was a problem reading bytes from inner.
     */
    public byte[] readLineBytes() throws IOException {
        ByteArrayOutputStream by = new ByteArrayOutputStream();
        int b;
        while ((b = read()) != -1) {
            by.write(b);
            if (b == '\n') {
                break;
            }
        }
        return by.size() == 0 && b == -1 ? null : by.toByteArray();
    }

    /* Delegates from inner InputStream */

    @Override
    public int read() throws IOException {
        if (available() == 0) {
            return -1;
        }
        int c = inner.read();
        if (c != -1) {
            pos++;
        }
        return c;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        if (available() == 0) {
            return -1;
        }
        len = Math.min(len, available());
        int r = inner.read(b, off, len);
        if (r != -1) { // EOF
            pos += r;
        }
        return r;
    }

    @Override
    public long skip(long n) throws IOException {
        n = Math.min(n, available());
        if (n <= 0) {
            return 0;
        }
        long s = inner.skip(n);
        pos += s;
        return s;
    }

    @Override
    public int available() throws IOException {
        return (int) Math.min(inner.available(), length - pos);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void close() throws IOException {
        if (pos < length) {
            skip(length - pos);
        }
    }

    @Override
    public void mark(int readlimit) {
        mark = pos;
        inner.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        if (mark == -1) {
            throw new IOException("A mark must be set before reset is called");
        }
        inner.reset();
        pos = mark;
    }

    @Override
    public boolean markSupported() {
        return inner.markSupported();
    }
}
