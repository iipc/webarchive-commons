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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Representation of a temporary file used for polyphase merge sorting.
 */
class ScratchFile implements Comparable<ScratchFile> {

    private Path file;
    private BufferedReader in;
    private BufferedWriter out;
    private boolean endOfRun = false;
    private boolean endOfFile = false;
    private String next;
    int distribution;
    int dummy;

    ScratchFile(int fileNum) {
        try {
            this.file = Files.createTempFile("sort-" + fileNum + "-", null);
            file.toFile().deleteOnExit();
            distribution = 1;
            dummy = 1;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Path getPath() {
        return file;
    }

    public void delete() {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public String peek() {
        try {
            if (in == null) {
                close();
                endOfFile = false;
                endOfRun = false;
                in = Files.newBufferedReader(file);

                next = in.readLine();
            }
            return next;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public String getNext() {
        try {
            String result = peek();
            next = in.readLine();
            if (next == null) {
                endOfRun = true;
                endOfFile = true;
                distribution--;
                close();
            } else if (result != null && result.compareTo(next) > 0) {
                endOfRun = true;
                distribution--;
            }
            return result;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void nextRun() {
        endOfRun = false;
        if (dummy > 0) {
            dummy--;
            distribution--;
            endOfRun = true;
        } else {
            if (peek() == null) {
                endOfRun = true;
            }
        }
    }

    public boolean isEndOfRun() {
        return endOfRun;
    }

    public boolean isEndOfFile() {
        return endOfFile;
    }

    public void write(String record) {
        try {
            if (out == null) {
                close();
                endOfFile = false;
                endOfRun = false;
                out = Files.newBufferedWriter(file);
            }

            out.write(record);
            out.write("\n");
            out.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void close() {
        try {
            if (out != null) {
                out.close();
                out = null;
            }
            if (in != null) {
                in.close();
                in = null;
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public int compareTo(ScratchFile o) {
        return peek().compareTo(o.peek());
    }

    @Override
    public String toString() {
        return "ScratchFile{" + "file=" + file + '}';
    }

}
