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

import org.netpreserve.commons.cdx.SearchKey;

/**
 *
 */
public class ReverseCdxBuffer extends CdxBuffer {

    /**
     * End of buffer.
     * <p>
     * Actually reached the beginning of buffer since we are going backwards.
     */
    private boolean eob;

    private int endOfLine;

    public ReverseCdxBuffer(final SearchKey searchKey) {
        super(searchKey);
    }

    @Override
    public void setByteBuf(ByteBuffer byteBuf) {
        this.byteBuf = byteBuf;

        // Set the position at the beginning of the last line to be ready for backward iteration.
        eob = false;
        byteBuf.position(byteBuf.limit());
        skipLF();
        endOfLine = byteBuf.position();
        skipToBeginningOfLine();
    }

    @Override
    void skipLF() {
        int position = byteBuf.position() - 1;
        while (position >= 0 && isLf(byteBuf.get(position))) {
            position--;
        }
        byteBuf.position(position + 1);
    }

    @Override
    boolean hasRemaining() {
        return !eob;
    }

    @Override
    int getEndOfLinePosition() {
        byteBuf.reset();
        return endOfLine;
    }

    @Override
    void moveToNextLine() {
        byteBuf.reset();
        if (byteBuf.position() == 0) {
            eob = true;
        }
        if (!eob) {
            skipLF();
            endOfLine = byteBuf.position();
            skipToBeginningOfLine();
        }
    }

    @Override
    boolean skipLines() {
        byteBuf.mark();
        while (!key.included(byteBuf)) {
            byteBuf.reset();
            skipLF();
            endOfLine = byteBuf.position();
            skipToBeginningOfLine();
            if (endOfLine == byteBuf.position()) {
                eob = true;
                return false;
            }
            byteBuf.mark();
        }

        byteBuf.reset();
        return true;
    }

}
