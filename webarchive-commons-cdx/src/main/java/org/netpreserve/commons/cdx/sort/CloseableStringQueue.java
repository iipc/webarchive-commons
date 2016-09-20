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
package org.netpreserve.commons.cdx.sort;

import java.io.Closeable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class CloseableStringQueue extends ArrayBlockingQueue<String> implements Closeable {

    private boolean endOfInput = false;

    public CloseableStringQueue(int capacity) {
        super(capacity);
    }

    @Override
    public String take() throws InterruptedException {
        while (!(endOfInput && isEmpty())) {
            String value = super.poll(5, TimeUnit.SECONDS);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    public void put(String value) throws InterruptedException {
        while (!endOfInput) {
            if (super.offer(value, 5, TimeUnit.SECONDS)) {
                return;
            }
        }
        throw new IllegalStateException("Queue is closed");
    }

    @Override
    public void close() {
        endOfInput = true;
    }

}
