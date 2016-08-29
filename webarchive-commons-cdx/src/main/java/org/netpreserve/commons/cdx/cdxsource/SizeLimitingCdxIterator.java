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
package org.netpreserve.commons.cdx.cdxsource;

import org.netpreserve.commons.cdx.CdxRecord;

/**
 * A CdxIterator which cuts off a wrapped CdxIterator when a predefined number of elements are returned.
 */
public class SizeLimitingCdxIterator implements CdxIterator {

    private final long limit;

    private final CdxIterator src;

    private long count;

    public SizeLimitingCdxIterator(CdxIterator src, long limit) {
        this.limit = limit;
        this.src = src;
    }

    @Override
    public CdxRecord next() {
        if (count < limit) {
            count++;
            return src.next();
        } else {
            return null;
        }
    }

    @Override
    public CdxRecord peek() {
        if (count < limit) {
            return src.peek();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        if (count < limit) {
            return src.hasNext();
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        src.close();
    }

    @Override
    public CdxIterator limit(long maxSize) {
        return new SizeLimitingCdxIterator(this, maxSize);
    }

}
