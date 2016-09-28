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
package org.netpreserve.commons.cdx.cdxsource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.CdxFormat;
import org.netpreserve.commons.cdx.CdxRecordFactory;
import org.netpreserve.commons.cdx.SearchKey;

/**
 *
 */
public class CdxBuffer {

    ByteBuffer byteBuf;

    final CdxFormat lineFormat;

    final SearchKey key;

    public CdxBuffer(final SearchKey key) {

        this.lineFormat = Objects.requireNonNull(key.getCdxFormat());
        this.key = key;
    }

    public ByteBuffer getByteBuf() {
        return byteBuf;
    }

    public void setByteBuf(ByteBuffer byteBuf) {
        this.byteBuf = byteBuf;
    }

    /**
     * Skip to beginning of next line.
     */
    void skipToEndOfLine() {
        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            byte[] buf = byteBuf.array();
            int offset = byteBuf.position();
            int limit = byteBuf.limit();

            for (int i = offset; i < limit; i++) {
                if (isLf(buf[i])) {
                    byteBuf.position(i);
                    return;
                }
            }
        } else {
            while (byteBuf.position() < byteBuf.limit()) {
                if (isLf(byteBuf.get())) {
                    byteBuf.position(byteBuf.position() - 1);
                    return;
                }
            }
        }
    }

    void skipToBeginningOfLine() {
        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            byte[] buf = byteBuf.array();
            int position = byteBuf.position() + byteBuf.arrayOffset() - 1;
            int arrayOffset = byteBuf.arrayOffset();

            while (position >= arrayOffset) {
                if (isLf(buf[position])) {
                    byteBuf.position(position + 1);
                    return;
                }
                position--;
            }
            byteBuf.position(arrayOffset);

        } else {
            int position = byteBuf.position() - 1;

            while (position >= 0) {
                if (isLf(byteBuf.get(position))) {
                    byteBuf.position(position + 1);
                    return;
                }
                position--;
            }
            byteBuf.position(0);
        }
    }

    /**
     * Check for newline characters.
     * <p>
     * @param c the character to check
     * @return true if LF or CR
     */
    final boolean isLf(int c) {
        return c == '\n' || c == '\r';
    }

    /**
     * Skip to next line. Supports '\n', '\r' and '\r\n'.
     */
    void skipLF() {
        if (isLf(byteBuf.get())) {
            // Check for extra line feed
            if (byteBuf.hasRemaining() && byteBuf.get(byteBuf.position()) == '\n') {
                byteBuf.get();
            }
        }
    }

    CdxRecord readLine() {
        byteBuf.mark();

        if (hasRemaining()) {
            CdxRecord cdxLine = createCdxLine(byteBuf, getEndOfLinePosition(), lineFormat);
            moveToNextLine();
            return cdxLine;
        }

        return null;
    }

    CdxRecord readLineCheckingFilter() {
        byteBuf.mark();
        if (hasRemaining()) {
            if (key.included(byteBuf)) {
                CdxRecord cdxLine = createCdxLine(byteBuf, getEndOfLinePosition(), lineFormat);
                moveToNextLine();
                return cdxLine;
            }
        }

        return null;
    }

    boolean hasRemaining() {
        return byteBuf.hasRemaining();
    }

    int getEndOfLinePosition() {
        skipToEndOfLine();
        int endOfLine = byteBuf.position();
        byteBuf.reset();
        return endOfLine;
    }

    void moveToNextLine() {
        skipLF();
    }

    /**
     * If a start filter exist, skip lines not matching filter.
     *
     * @return true when the first line to be included is reached
     */
    boolean skipLines() {
        while (byteBuf.hasRemaining()) {
            byteBuf.mark();
            if (!key.included(byteBuf)) {
                skipToEndOfLine();
                skipLF();
            } else {
                byteBuf.reset();
                return true;
            }
        }

        return false;
    }

    int countLines() {
        int count = 0;
        while (byteBuf.hasRemaining()) {
            skipToEndOfLine();
            skipLF();
            count++;
        }

        return count;
    }

    int countLinesCheckingFilter() {
        int count = 0;
        while (byteBuf.hasRemaining()) {
            if (key.included(byteBuf)) {
                skipToEndOfLine();
                skipLF();
                count++;
            } else {
                return count;
            }
        }

        return count;
    }

    static CdxRecord createCdxLine(final ByteBuffer byteBuf, final int startOfNextLine,
            final CdxFormat lineFormat) {

        return CdxRecordFactory.create(convertToCharBuffer(byteBuf, startOfNextLine), lineFormat);
    }

    static char[] convertToCharBuffer(final ByteBuffer byteBuf,
            final int startOfNextLine) {

        int lineLength = startOfNextLine - byteBuf.position();
        char[] out = new char[lineLength];

        if (byteBuf.hasArray()) {
            // When buffer is backed by an array, this runs faster than using ByteBuffer API
            byte[] buf = byteBuf.array();
            int offset = byteBuf.position() + byteBuf.arrayOffset();

            for (int i = 0; i < lineLength; i++) {
                byte c = buf[i + offset];
                if (c < 0) {
                    // Line contains non ascii character, must decode line.
                    return convertToCharBufferNonAscii(byteBuf, lineLength);
                }
                out[i] = (char) c;
            }
            byteBuf.position(startOfNextLine);
        } else {
            for (int i = 0; i < lineLength; i++) {
                byte c = byteBuf.get();
                if (c < 0) {
                    // Line contains non ascii character, must decode line.
                    byteBuf.reset();
                    return convertToCharBufferNonAscii(byteBuf, lineLength);
                }
                out[i] = (char) c;
            }
        }
        return out;
    }

    static char[] convertToCharBufferNonAscii(final ByteBuffer byteBuf,
            final int lineLength) {

        ByteBuffer line = byteBuf.slice();
        line.limit(lineLength);
        return StandardCharsets.UTF_8.decode(line).array();
    }

}
