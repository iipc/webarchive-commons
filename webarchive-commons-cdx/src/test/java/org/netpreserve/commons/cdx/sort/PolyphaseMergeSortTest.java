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
import java.io.StringReader;
import java.io.StringWriter;

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
    public void testCreateInitialRuns() {
        BufferedReader input = new BufferedReader(
                new StringReader("B\nD\nE\nC\nF\nA\nG\nZ\nX\nH\nI\nO\nN\nL\nJ\nK\nL"));
        pms = new PolyphaseMergeSort(3, 3);
        pms.createScratchFiles();
        ReplacementSelectionHeapSort inputHeap = new SortingReaderSource(pms.heapSize, input);
        pms.createInitialRuns(inputHeap);

        assertThat(pms.scratchFiles[0]).hasFieldOrPropertyWithValue("distribution", 2);
        assertThat(pms.scratchFiles[0]).hasFieldOrPropertyWithValue("dummy", 0);
        assertThat(pms.scratchFiles[0].getPath()).hasContent("B\nC\nD\nE\nF\nG\nX\nZ\nJ\nK\nL");

        assertThat(pms.scratchFiles[1]).hasFieldOrPropertyWithValue("distribution", 1);
        assertThat(pms.scratchFiles[1]).hasFieldOrPropertyWithValue("dummy", 0);
        assertThat(pms.scratchFiles[1].getPath()).hasContent("A\nH\nI\nL\nN\nO");

        assertThat(pms.scratchFiles[2]).hasFieldOrPropertyWithValue("distribution", 0);
        assertThat(pms.scratchFiles[2]).hasFieldOrPropertyWithValue("dummy", 0);


        input = new BufferedReader(new StringReader("Z\nY\nX\nW\nV\nU\nT\nS\nR\nQ\nP\nO\nN\nM\nL\nK\nJ\nI\nH\nG\n"
                + "F\nE\nD\nC\nB\nA\n9\n8\n7\n6\n5\n4\n3\n2\n"));
        inputHeap = new SortingReaderSource(pms.heapSize, input);
        pms.createScratchFiles();
        pms.createInitialRuns(inputHeap);
        assertThat(pms.scratchFiles[0]).hasFieldOrPropertyWithValue("distribution", 8);
        assertThat(pms.scratchFiles[0]).hasFieldOrPropertyWithValue("dummy", 0);
        assertThat(pms.scratchFiles[0].getPath()).hasContent(
                "X\nY\nZ\nR\nS\nT\nO\nP\nQ\nI\nJ\nK\nF\nG\nH\n9\nA\nB\n6\n7\n8\n2");

        assertThat(pms.scratchFiles[1]).hasFieldOrPropertyWithValue("distribution", 5);
        assertThat(pms.scratchFiles[1]).hasFieldOrPropertyWithValue("dummy", 1);
        assertThat(pms.scratchFiles[1].getPath()).hasContent("U\nV\nW\nL\nM\nN\nC\nD\nE\n3\n4\n5");

        assertThat(pms.scratchFiles[2]).hasFieldOrPropertyWithValue("distribution", 0);
        assertThat(pms.scratchFiles[2]).hasFieldOrPropertyWithValue("dummy", 0);
    }

    /**
     * Test of mergeSort method, of class PolyphaseMergeSort.
     */
    @Test
    public void testMergeSort1() {
        BufferedReader input = new BufferedReader(
                new StringReader("B\nD\nE\nC\nF\nA\nG\nZ\nX\nH\nI\nO\nN\nL\nJ\nK\nL"));
        StringWriter writer = new StringWriter();
        BufferedWriter output = new BufferedWriter(writer);

        pms = new PolyphaseMergeSort(3, 3);
        pms.createScratchFiles();
        pms.output = output;
        ReplacementSelectionHeapSort inputHeap = new SortingReaderSource(pms.heapSize, input);
        pms.createInitialRuns(inputHeap);
        pms.mergeSort();
        pms.closeOutput();

        assertThat(writer).hasToString("A\nB\nC\nD\nE\nF\nG\nH\nI\nJ\nK\nL\nL\nN\nO\nX\nZ\n");
    }

    @Test
    public void testMergeSort2() {
        BufferedReader input = new BufferedReader(
                new StringReader("Z\nY\nX\nW\nV\nU\nT\nS\nR\nQ\nP\nO\nN\nM\nL\nK\nJ\nI\nH\nG\nF\nE\nD\nC\nB\nA\n"
                        + "9\n8\n7\n6\n5\n4\n3\n2\n"));
        StringWriter writer = new StringWriter();
        BufferedWriter output = new BufferedWriter(writer);

        pms = new PolyphaseMergeSort(4, 2);
        pms.createScratchFiles();
        pms.output = output;
        ReplacementSelectionHeapSort inputHeap = new SortingReaderSource(pms.heapSize, input);
        pms.createInitialRuns(inputHeap);
        pms.mergeSort();
        pms.closeOutput();

        assertThat(writer).hasToString("2\n3\n4\n5\n6\n7\n8\n9\nA\nB\nC\nD\nE\nF\nG\nH\nI\nJ\nK\nL\nM\nN\nO\nP\nQ\n"
                + "R\nS\nT\nU\nV\nW\nX\nY\nZ\n");
    }

    @Test
    public void testMergeSort3() {
        BufferedReader input = new BufferedReader(
                new StringReader("Z\nY\nX\nW\nV\nU\nT\nS\nR\nQ\nP\nO\nN\nM\nL\nK\nJ\nI\nH\nG\nF\nE\nD\nC\nB\nA\n"
                        + "9\n8\n7\n6\n5\n4\n3\n2\n1\n"));
        StringWriter writer = new StringWriter();
        BufferedWriter output = new BufferedWriter(writer);

        pms = new PolyphaseMergeSort(4, 2);
        pms.createScratchFiles();
        pms.output = output;
        ReplacementSelectionHeapSort inputHeap = new SortingReaderSource(pms.heapSize, input);
        pms.createInitialRuns(inputHeap);
        pms.mergeSort();
        pms.closeOutput();

        assertThat(writer).hasToString("1\n2\n3\n4\n5\n6\n7\n8\n9\nA\nB\nC\nD\nE\nF\nG\nH\nI\nJ\nK\nL\nM\nN\nO\nP\nQ\n"
                + "R\nS\nT\nU\nV\nW\nX\nY\nZ\n");
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

        input = new BufferedReader(new StringReader(""));
        writer = new StringWriter();
        output = new BufferedWriter(writer);
        pms = new PolyphaseMergeSort(5, 3);
        pms.sort(input, output);
        assertThat(writer).hasToString("");
    }
}
