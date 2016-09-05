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
        assertThat(ArrayUtil.indexOf(array, 0, 'a')).isEqualTo(1);
        assertThat(ArrayUtil.indexOf(array, 1, 'a')).isEqualTo(1);
        assertThat(ArrayUtil.indexOf(array, 2, 'a')).isEqualTo(4);

        assertThat(ArrayUtil.indexOf(array, 0, 'a', 'c')).isEqualTo(1);
        assertThat(ArrayUtil.indexOf(array, 0, 'a', 'l')).isEqualTo(4);
        assertThat(ArrayUtil.indexOf(array, 0, 'a', 'm')).isEqualTo(-1);
    }

    /**
     * Test of lastIndexOf method, of class ArrayUtil.
     */
    @Test
    public void testLastIndexOf() {
        char[] array = new char[]{'g', 'a', 'c', 'b', 'a', 'l', 'm', 'a', 'c'};
        assertThat(ArrayUtil.lastIndexOf(array, 0, 'a')).isEqualTo(-1);
        assertThat(ArrayUtil.lastIndexOf(array, 1, 'a')).isEqualTo(1);
        assertThat(ArrayUtil.lastIndexOf(array, array.length - 1, 'a')).isEqualTo(7);
        assertThat(ArrayUtil.lastIndexOf(array, array.length - 3, 'a')).isEqualTo(4);
        assertThat(ArrayUtil.lastIndexOf(array, -1, 'a')).isEqualTo(7);

        assertThat(ArrayUtil.lastIndexOf(array, -1, 'a', 'c')).isEqualTo(7);
        assertThat(ArrayUtil.lastIndexOf(array, -1, 'a', 'l')).isEqualTo(4);
        assertThat(ArrayUtil.lastIndexOf(array, -1, 'a', 'm')).isEqualTo(-1);
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
