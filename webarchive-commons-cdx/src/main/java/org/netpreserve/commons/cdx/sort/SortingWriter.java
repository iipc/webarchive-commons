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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * Writer which sorts the output.
 * <p>
 * This Writer act as a filter writer. It uses an external polyphase merge sort and is capable of sorting amounts of
 * data bigger than physical memory.
 * <p>
 * Sorting is partly done while writing, but the final result is only available after the writer is closed.
 * <p>
 * Resource usage can be controlled by two parameters:
 * <ul>
 * <li> The number of scratch files which constrains how many temporary files will be used. A higher number means
 * smaller files and a reduced number of iterations at the cost of more file handles used.
 * <li> The heap size which is the number of lines in the input sorted in memory. A higher number increases speed at the
 * cost of higher memory usage. Since the heap is defined in number of lines, the actual memory consumption is dependent
 * on the average line size in the input.
 * </ul>
 */
public class SortingWriter extends Writer {

    private static final int DEFAULT_SCRATCHFILE_COUNT = 5;

    private static final int DEFAULT_HEAP_SIZE = 100;

    private static final int PIPE_BUFFER_SIZE = 1024 * 8;

    private final PolyphaseMergeSort pms;

    private final PipedReader reader;

    private final PipedWriter writer;

    private final BufferedWriter output;

    private final Thread sortingThread;

    /**
     * Construct a new SortingWriter using default values for number of scratch files and heap size.
     * <p>
     * @param output the writer to write the result to.
     */
    public SortingWriter(Writer output) {
        this(output, DEFAULT_SCRATCHFILE_COUNT, DEFAULT_HEAP_SIZE);
    }

    /**
     * Construct a new SortingWriter.
     * <p>
     * @param output the writer to write the result to.
     * @param scratchFileCount the number of scratch files to use
     * @param heapSize the number of lines sorted in memory
     */
    public SortingWriter(Writer output, int scratchFileCount, int heapSize) {
        try {
            this.writer = new PipedWriter();
            this.reader = new PipedReader(writer, PIPE_BUFFER_SIZE);
            if (output instanceof BufferedWriter) {
                this.output = (BufferedWriter) output;
            } else {
                this.output = new BufferedWriter(output);
            }

            this.pms = new PolyphaseMergeSort(scratchFileCount, heapSize);
            sortingThread = new Thread(new SortingThread(), "Sorting thread");
            sortingThread.start();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void write(int c) throws IOException {
        writer.write(c);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
        output.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
        try {
            // Wait for sort to finish
            sortingThread.join();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        output.close();
    }

    /**
     * The thread executing the sort.
     */
    private class SortingThread implements Runnable {

        @Override
        public void run() {
            pms.sort(new BufferedReader(reader), output);
        }

    }
}
