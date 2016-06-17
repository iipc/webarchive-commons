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
import java.util.BitSet;

/**
 *
 */
public class PolyphaseMergeSort {

    BufferedReader input;

    BufferedWriter output;

    final int scratchFileCount;

    final int heapSize;

    ScratchFile[] scratchFiles;

    int level;

    int lastInputFile;

    public PolyphaseMergeSort(final int scratchFileCount, final int heapSize) {
        if (scratchFileCount < 3) {
            throw new IllegalArgumentException("Need at least 3 scratchfiles");
        }
        if (heapSize < 2) {
            throw new IllegalArgumentException("Need at least a heap size of 2");
        }
        this.scratchFileCount = scratchFileCount;
        this.heapSize = heapSize;
    }

    public void sort(BufferedReader input, BufferedWriter output) {
        this.input = input;
        this.output = output;
        createScratchFiles();
        BitSet hasData = createInitialRuns();
        if (hasData.cardinality() == 1) {
            // Special case where in-memory sorting created just one run.
            copy(scratchFiles[lastInputFile], output);
        } else if (hasData.cardinality() < scratchFileCount - 1) {
            // Special case where there are to few runs to use all scratchfiles.
            ScratchFile[] inputs = new ScratchFile[hasData.cardinality()];
            int k = 0;
            for (int j = 0; j < scratchFileCount; j++) {
                if (hasData.get(j)) {
                    inputs[k++] = scratchFiles[j];
                }
            }
            SortableScratchfiles sortedInputs = new SortableScratchfiles(inputs.length, inputs);
            scratchFiles[lastInputFile].distribution++;
            merge(sortedInputs, lastInputFile, -1);
        } else {
            mergeSort();
        }

        deleteScratchFiles();
        closeOutput();
    }

    void createScratchFiles() {
        this.scratchFiles = new ScratchFile[scratchFileCount];
        for (int i = 0; i < scratchFileCount; i++) {
            scratchFiles[i] = new ScratchFile(i);
        }

        level = 1;
        scratchFiles[scratchFileCount - 1].distribution = 0;
        scratchFiles[scratchFileCount - 1].dummy = 0;
    }

    void deleteScratchFiles() {
        for (ScratchFile sf : scratchFiles) {
            sf.delete();
        }
    }

    void closeOutput() {
        try {
            output.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    void copy(ScratchFile input, BufferedWriter output) {
        try (BufferedReader source = Files.newBufferedReader(input.getPath());) {
            long nread = 0L;
            char[] buf = new char[16 * 1024];
            int n;
            while ((n = source.read(buf)) > 0) {
                output.write(buf, 0, n);
                nread += n;
            }
            output.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    BitSet createInitialRuns() {
        BitSet hasData = new BitSet(scratchFileCount);
        int fileNum = 0;
        try {
            ReplacementSelectionHeapSort heap = new ReplacementSelectionHeapSort(heapSize, input);

            scratchFiles[fileNum].dummy = 0;
            boolean hasMore = true;
            while (hasMore) {
                if (scratchFiles[fileNum].dummy < scratchFiles[fileNum + 1].dummy) {
                    fileNum++;
                } else {
                    if (scratchFiles[fileNum].dummy == 0) {
                        level++;
                        int mergesThisLevel = scratchFiles[0].distribution;
                        computeDistributionAndDummy(mergesThisLevel);
                    }
                    fileNum = 0;
                }
                hasMore = heap.writeNextRun(scratchFiles[fileNum]);
                scratchFiles[fileNum].dummy -= 1;
                hasData.set(fileNum);
            }

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        for (ScratchFile sf : scratchFiles) {
            sf.close();
        }
        lastInputFile = fileNum;

        return hasData;
    }

    void mergeSort() {
        int mergeInto = -1;
        for (int i = 1; i < level; i++) {
            mergeInto = scratchFileCount - ((i % scratchFileCount));
            if (mergeInto >= scratchFileCount) {
                mergeInto = 0;
            }

            ScratchFile[] inputs = new ScratchFile[scratchFileCount - 1];
            int k = 0;
            for (int j = 0; j < scratchFileCount; j++) {
                if (j != mergeInto) {
                    inputs[k++] = scratchFiles[j];
                }
            }
            SortableScratchfiles sortedInputs = new SortableScratchfiles(inputs.length, inputs);

            if (i < level - 1) {
                merge(sortedInputs, 1, mergeInto);

                lastInputFile = mergeInto;
                computeDistributionAndDummy(level - i);
            } else {
                merge(sortedInputs, 1, -1);
            }
        }

        for (int i = 0; i < scratchFileCount; i++) {
            scratchFiles[i].close();
        }
    }

    private void merge(SortableScratchfiles inputs, int lastInputFile, int outputFile) {
        for (int j = 1; j < scratchFiles[lastInputFile].distribution; j++) {

            // if (dummy[] > 0 for all input files) {
            boolean allDummiesGreaterThanZero = true;
            for (ScratchFile sf : inputs.getFiles()) {
                if (sf.dummy == 0) {
                    allDummiesGreaterThanZero = false;
                    break;
                }
            }

            // If outputFile is set to -1 then this is the final merge
            // and we write to final destination instead of scratchfile.
            if (outputFile >= 0) {
                if (allDummiesGreaterThanZero) {
                    scratchFiles[outputFile].dummy += 1;
                }

                while (inputs.hasMoreInRun()) {
                    scratchFiles[outputFile].write(inputs.getNextInRun());
                }
            } else {
                while (inputs.hasMoreInRun()) {
                    try {
                        output.write(inputs.getNextInRun());
                        output.write("\n");
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
            inputs.nextRun();
        }
    }

    private void computeDistributionAndDummy(int mergesThisLevel) {
        for (int i = 0; i < scratchFileCount - 1; i++) {
            scratchFiles[i].dummy = mergesThisLevel + scratchFiles[i + 1].distribution - scratchFiles[i].distribution;
            scratchFiles[i].distribution += scratchFiles[i].dummy;
        }
    }

}
