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
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

import org.junit.After;

/**
 *
 */
public class PolyphaseMergeSortTest {

    PolyphaseMergeSort pms;

    @After
    public void tearDown() throws Exception {
        pms.deleteScratchFiles();
    }

    /**
     * Test of createInitialRuns method, of class PolyphaseMergeSort.
     */
    @Test
    public void testCreateInitialRuns() throws IOException {
        BufferedReader input = new BufferedReader(new StringReader("B\nD\nE\nC\nF\nA\nG\nZ\nX\nH\nI\nO\nN\nL\nJ\nK\nL"));
        pms = new PolyphaseMergeSort(3, 3);
        pms.createScratchFiles();
        pms.input = input;
        pms.createInitialRuns();

        assertThat(pms.scratchFiles[0]).hasFieldOrPropertyWithValue("distribution", 3);
        assertThat(pms.scratchFiles[0]).hasFieldOrPropertyWithValue("dummy", 0);
        assertThat(pms.scratchFiles[0].getPath()).hasContent("A\nH\nI\nL\nN\nO\nJ\nK\nL");

        assertThat(pms.scratchFiles[1]).hasFieldOrPropertyWithValue("distribution", 2);
        assertThat(pms.scratchFiles[1]).hasFieldOrPropertyWithValue("dummy", 1);
        assertThat(pms.scratchFiles[1].getPath()).hasContent("B\nC\nD\nE\nF\nG\nX\nZ");

        assertThat(pms.scratchFiles[2]).hasFieldOrPropertyWithValue("distribution", 0);
        assertThat(pms.scratchFiles[2]).hasFieldOrPropertyWithValue("dummy", 0);
    }

    /**
     * Test of mergeSort method, of class PolyphaseMergeSort.
     */
    @Test
    public void testMergeSort() throws IOException {
        BufferedReader input = new BufferedReader(new StringReader("B\nD\nE\nC\nF\nA\nG\nZ\nX\nH\nI\nO\nN\nL\nJ\nK\nL"));
        StringWriter writer = new StringWriter();
        BufferedWriter output = new BufferedWriter(writer);

        pms = new PolyphaseMergeSort(3, 3);
        pms.createScratchFiles();
        pms.input = input;
        pms.output = output;
        pms.createInitialRuns();
        pms.mergeSort();
        pms.closeOutput();

        assertThat(writer).hasToString("A\nB\nC\nD\nE\nF\nG\nH\nI\nJ\nK\nL\nL\nN\nO\nX\nZ\n");
    }

    /**
     * Test of sort method, of class PolyphaseMergeSort.
     */
    @Test
    public void testSort() {
        String inputString = "B\nD\nE\nC\nF\nA\nG\nZ\nX\nH\nI\nO\nN\nL\nJ\nK\nL";
        String expected = "A\nB\nC\nD\nE\nF\nG\nH\nI\nJ\nK\nL\nL\nN\nO\nX\nZ\n";
        BufferedReader input;
        StringWriter writer;
        BufferedWriter output;

        input = new BufferedReader(new StringReader(inputString));
        writer = new StringWriter();
        output = new BufferedWriter(writer);
        pms = new PolyphaseMergeSort(3, 3);
        pms.sort(input, output);
        assertThat(writer).hasToString(expected);

        input = new BufferedReader(new StringReader(inputString));
        writer = new StringWriter();
        output = new BufferedWriter(writer);
        pms = new PolyphaseMergeSort(5, 30);
        pms.sort(input, output);
        assertThat(writer).hasToString(expected);

        input = new BufferedReader(new StringReader(inputString));
        writer = new StringWriter();
        output = new BufferedWriter(writer);
        pms = new PolyphaseMergeSort(5, 3);
        pms.sort(input, output);
        assertThat(writer).hasToString(expected);
    }

    @Test
    public void testSortFile() throws URISyntaxException, IOException {
        String fileName = "cdxfile3.cdx";
        Path inputPath = Paths.get(ClassLoader.getSystemResource(fileName).toURI());
        Path outputPath = Paths.get("target", "sort", fileName);
        Files.createDirectories(outputPath.getParent());

        BufferedReader input = Files.newBufferedReader(inputPath);
        BufferedWriter output = Files.newBufferedWriter(outputPath);
        pms = new PolyphaseMergeSort(3, 3);
        pms.sort(input, output);
    }
}
