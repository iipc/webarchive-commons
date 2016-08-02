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
package org.netpreserve.commons.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Test methods for ArrayUtil.
 */
public class ArrayUtilTest {

    /**
     * Test of indexOf method, of class ArrayUtil.
     */
    @Test
    public void testIndexOf() {
        char[] array = new char[]{'g', 'a', 'c', 'b', 'a', 'l', 'm', 'a'};
        assertThat(ArrayUtil.indexOf(array, 'a', 0)).isEqualTo(1);
        assertThat(ArrayUtil.indexOf(array, 'a', 1)).isEqualTo(1);
        assertThat(ArrayUtil.indexOf(array, 'a', 2)).isEqualTo(4);
    }

    /**
     * Test of lastIndexOf method, of class ArrayUtil.
     */
    @Test
    public void testLastIndexOf() {
        char[] array = new char[]{'g', 'a', 'c', 'b', 'a', 'l', 'm', 'a'};
        assertThat(ArrayUtil.lastIndexOf(array, 'a', 0)).isEqualTo(-1);
        assertThat(ArrayUtil.lastIndexOf(array, 'a', 1)).isEqualTo(1);
        assertThat(ArrayUtil.lastIndexOf(array, 'a', array.length - 1)).isEqualTo(7);
        assertThat(ArrayUtil.lastIndexOf(array, 'a', array.length - 2)).isEqualTo(4);
        assertThat(ArrayUtil.lastIndexOf(array, 'a', -1)).isEqualTo(7);
    }

    /**
     * Test of swap method, of class ArrayUtil.
     */
    @Test
    public void testSwap() {
        String[] array = new String[]{"g", "a", "c", "b"};
        ArrayUtil.swap(array, 2, 0);
        assertThat(array).containsExactly("c", "a", "g", "b");
    }

    /**
     * Test of heapSort method, of class ArrayUtil.
     */
    @Test
    public void testHeapSort() {
        String[] array;

        array = new String[]{"g", "a", "c", "b", "a", "l", "m", "a"};
        ArrayUtil.heapSort(array, 0, array.length);
        assertThat(array).containsExactly("a", "a", "a", "b", "c", "g", "l", "m");

        array = new String[]{"g", "a", "c", "b", "a", "l", "m", "a", "x"};
        ArrayUtil.heapSort(array, 1, array.length - 1);
        assertThat(array).containsExactly("g", "a", "a", "a", "b", "c", "l", "m", "x");
    }

}
