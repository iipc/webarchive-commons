/*
 *  This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.archive.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test suite for ArchiveUtils
 *
 * @author <a href="mailto:me@jamesc.net">James Casey</a>
 * @version $Id$
 */
public class ArchiveUtilsTest {

    /** check the getXXDigitDate() methods produce valid dates*/
    @Test
    public void testGetXXDigitDate() {
        // TODO - we only really test the date lengths here.  How to test
        // other stuff well ?
        final String date12 = ArchiveUtils.get12DigitDate();
        assertEquals(12, (Object) date12.length(), "12 digits");

        final String date14 = ArchiveUtils.get14DigitDate();
        assertEquals(14, (Object) date14.length(), "14 digits");

        final String date17 = ArchiveUtils.get17DigitDate();
        assertEquals(17, (Object) date17.length(), "17 digits");

        // now parse, and check they're all within 1 minute

        try {
            final long long12 = ArchiveUtils.parse12DigitDate(date12).getTime();
            long long14 = ArchiveUtils.parse14DigitDate(date14).getTime();
            long long17 = ArchiveUtils.parse17DigitDate(date17).getTime();

            assertClose("12 and 14 close", long12, long14, 600000);
            assertClose("12 and 17 close", long12, long17, 600000);
            assertClose("14 and 17 close", long14, long17, 600000);
        } catch (ParseException e) {
            fail("Could not parse a date : " + e.getMessage());
        }
    }

    /** check that getXXDigitDate(long) does the right thing */
    @Test
    public void testGetXXDigitDateLong() {
        final long now = System.currentTimeMillis();
        final String date12 = ArchiveUtils.get12DigitDate(now);
        assertEquals(12, (Object) date12.length(), "12 digits");

        final String date14 = ArchiveUtils.get14DigitDate(now);
        assertEquals(14, (Object) date14.length(), "14 digits");
        assertEquals(date12, date14.substring(0, 12), "first twelve digits same as date12");
        final String date17 = ArchiveUtils.get17DigitDate(now);
        assertEquals(17, (Object) date17.length(), "17 digits");
        assertEquals(date12, date17.substring(0, 12), "first twelve digits same as date12");
        assertEquals(date14, date17.substring(0, 14), "first fourteen digits same as date14");
    }

    /**
     * Check that parseXXDigitDate() works
     *
     * @throws ParseException
     */
    @Test
    public void testParseXXDigitDate() throws ParseException {
        // given a date, check it get resolved properly
        // It's 02 Jan 2004, 12:40:02.111
        final String date = "20040102124002111";
        try {
            final long long12 = ArchiveUtils.parse12DigitDate(date.substring(0, 12)).getTime();
            final long long14 = ArchiveUtils.parse14DigitDate(date.substring(0, 14)).getTime();
            final long long17 = ArchiveUtils.parse17DigitDate(date).getTime();

            assertClose("12 and 14 close", long12, long14, 600000);
            assertClose("12 and 17 close", long12, long17, 600000);
            assertClose("14 and 17 close", long14, long17, 600000);
        } catch (ParseException e) {
            fail("Could not parse a date : " + e.getMessage());
        }
    }

    @Test
    public void testTooShortParseDigitDate() throws ParseException {
        String d = "X";
        boolean b = false;
        try {
            ArchiveUtils.getDate(d);
        } catch (ParseException e) {
            b = true;
        }
        assertTrue(b);
        
        Date date = ArchiveUtils.getDate("1999");
        assertTrue(date.getTime() == 915148800000L);
        
        b = false;
        try {
            ArchiveUtils.getDate("19991");
        } catch (ParseException e) {
            b = true;
        }
        assertTrue(b);
        
        ArchiveUtils.getDate("19990101");
        ArchiveUtils.getDate("1999010101");
        ArchiveUtils.getDate("19990101010101"); 
        ArchiveUtils.getDate("1960"); 
    }

    /** check that parse12DigitDate doesn't accept a bad date */
    @Test
    public void testBad12Date() {
        // now try a badly formed dates
        assertBad12DigitDate("a-stringy-digit-date");
        assertBad12DigitDate("20031201"); // too short
    }

    /**
     * check that parse14DigitDate doesn't accept a bad date
     */
    @Test
    public void testBad14Date() {
        // now try a badly formed dates
        assertBad14DigitDate("a-stringy-digit-date");
        assertBad14DigitDate("20031201"); // too short
        assertBad14DigitDate("200401021240");  // 12 digit
    }
    /**
     * check that parse12DigitDate doesn't accept a bad date
     */
    @Test
    public void testBad17Date() {
        // now try a badly formed dates
        assertBad17DigitDate("a-stringy-digit-date");
        assertBad17DigitDate("20031201"); // too short
        assertBad17DigitDate("200401021240");  // 12 digit
        assertBad17DigitDate("20040102124002");  // 14 digit
    }

    /** check that padTo(String) works */
    @Test
    public void testPadToString() {
        assertEquals("foo", ArchiveUtils.padTo("foo", 1), "pad to one (smaller)");
        assertEquals("foo", ArchiveUtils.padTo("foo", 0), "pad to 0 (no sense)");
        assertEquals("foo", ArchiveUtils.padTo("foo", 0), "pad to neg (nonsense)");
        assertEquals(" foo", ArchiveUtils.padTo("foo", 4), "pad to 4");
        assertEquals("       foo", ArchiveUtils.padTo("foo", 10), "pad to 10");
    }

    /**
     * check that padTo(int) works
     */
    @Test
    public void testPadToInt() {
        assertEquals("123", ArchiveUtils.padTo(123, 1), "pad to one (smaller)");
        assertEquals("123", ArchiveUtils.padTo(123, 0), "pad to 0 (no sense)");
        assertEquals("123", ArchiveUtils.padTo(123, 0), "pad to neg (nonsense)");
        assertEquals(" 123", ArchiveUtils.padTo(123, 4), "pad to 4");
        assertEquals("       123", ArchiveUtils.padTo(123, 10), "pad to 10");
        assertEquals("      -123", ArchiveUtils.padTo(-123, 10), "pad -123 to 10");
    }

    /** check that byteArrayEquals() works */
    @Test
    public void testByteArrayEquals() {
        // foo == foo2, foo != bar, foo != bar2
        byte[] foo = new byte[10], bar = new byte[20];
        byte[] foo2 = new byte[10], bar2 = new byte[10];

        for (byte i = 0; i < 10 ; ++i) {
            foo[i] = foo2[i] = bar[i] = i;
            bar2[i] = (byte)(01 + i);
        }
        assertTrue(ArchiveUtils.byteArrayEquals(null, null), "two nulls");
        assertFalse(ArchiveUtils.byteArrayEquals(null, foo), "lhs null");
        assertFalse(ArchiveUtils.byteArrayEquals(foo, null), "rhs null");

        // now check with same length, with same (foo2) and different (bar2)
        // contents
        assertFalse(ArchiveUtils.byteArrayEquals(foo, bar), "different lengths");

        assertTrue(ArchiveUtils.byteArrayEquals(foo, foo), "same to itself");
        assertTrue(ArchiveUtils.byteArrayEquals(foo, foo2), "same contents");
        assertFalse(ArchiveUtils.byteArrayEquals(foo, bar2), "different contents");
    }

    /** test doubleToString() */
    @Test
    public void testDoubleToString(){
        double test = 12.121d;
        assertEquals("12", ArchiveUtils.doubleToString(test, 0), "cecking zero precision");
        assertEquals("12.12", ArchiveUtils.doubleToString(test, 2), "cecking 2 character precision");
        assertEquals("12.121", ArchiveUtils.doubleToString(test, 65), "cecking precision higher then the double has");
    }


    @Test
    public void testFormatBytesForDisplayPrecise(){
        assertEquals("0 B", ArchiveUtils
                .formatBytesForDisplay(-1), "formating negative number");
        assertEquals("0 B", ArchiveUtils
                .formatBytesForDisplay(0), "0 bytes");
        Object a2 = ArchiveUtils.formatBytesForDisplay(1);
        assertEquals("1 B", a2);
        Object a1 = ArchiveUtils.formatBytesForDisplay(9);
        assertEquals("9 B", a1);
        Object a = ArchiveUtils.formatBytesForDisplay(512);
        assertEquals( "512 B", a);
        assertEquals("1,023 B", ArchiveUtils
                .formatBytesForDisplay(1023), "1023 bytes");
        assertEquals("1.0 KiB", ArchiveUtils
                .formatBytesForDisplay(1025), "1025 bytes");
        // expected display values taken from Google calculator
        assertEquals("9.8 KiB", ArchiveUtils.formatBytesForDisplay(10000), "10,000 bytes");
        assertEquals("977 KiB", ArchiveUtils.formatBytesForDisplay(1000000), "1,000,000 bytes");
        assertEquals("95 MiB", ArchiveUtils.formatBytesForDisplay(100000000), "100,000,000 bytes");
        assertEquals("93 GiB", ArchiveUtils.formatBytesForDisplay(100000000000L), "100,000,000,000 bytes");
        assertEquals("91 TiB", ArchiveUtils.formatBytesForDisplay(100000000000000L), "100,000,000,000,000 bytes");
        assertEquals("90,949 TiB", ArchiveUtils.formatBytesForDisplay(100000000000000000L), "100,000,000,000,000,000 bytes");
    }

    /*
     * helper methods
     */

    /** check that this is a bad date, and <code>fail()</code> if so.
     *
     * @param date the 12digit date to check
     */
    private void assertBad12DigitDate(final String date) {
        try {
            ArchiveUtils.parse12DigitDate(date);
        } catch (ParseException e) {
            return;
        }
        fail("Expected exception on parse of : " + date);

    }
    /**
     * check that this is a bad date, and <code>fail()</code> if so.
     *
     * @param date the 14digit date to check
     */
    private void assertBad14DigitDate(final String date) {
        try {
            ArchiveUtils.parse14DigitDate(date);
        } catch (ParseException e) {
            return;
        }
        fail("Expected exception on parse of : " + date);

    }

    /**
     * check that this is a bad date, and <code>fail()</code> if so.
     *
     * @param date the 17digit date to check
     */
    private void assertBad17DigitDate(final String date) {
        try {
            ArchiveUtils.parse17DigitDate(date);
        } catch (ParseException e) {
            return;
        }
        fail("Expected exception on parse of : " + date);

    }

    /** check that two longs are within a given <code>delta</code> */
    private void assertClose(String desc, long date1, long date2, long delta) {
        assertTrue(date1 == date2 ||
                    (date1 < date2 && date2 < (date1 + delta)) ||
                    (date2 < date1 && date1 < (date2 + delta)), desc);
    }

    @Test
    public void testArrayToLong() {
        testOneArrayToLong(-1);
        testOneArrayToLong(1);
        testOneArrayToLong(1000);
        testOneArrayToLong(Integer.MAX_VALUE);
    }
    
    private void testOneArrayToLong(final long testValue) {
        byte [] a = new byte[8];
        ArchiveUtils.longIntoByteArray(testValue, a, 0);
        final long l = ArchiveUtils.byteArrayIntoLong(a, 0);
        assertEquals(testValue, l);
    }

    @Test
    public void testSecondsSinceEpochCalculation() throws ParseException {
        String m6 = ArchiveUtils.secondsSinceEpoch("20010909014640");
        assertEquals("1000000000", m6);
        String m5 = ArchiveUtils.secondsSinceEpoch("20010909014639");
        assertEquals("0999999999", m5);
        String m4 = ArchiveUtils.secondsSinceEpoch("19700101");
        assertEquals("0000000000", m4);
        String m3 = ArchiveUtils.secondsSinceEpoch("2005");
        assertEquals("1104537600", m3);
        String m2 = ArchiveUtils.secondsSinceEpoch("200501");
        assertEquals("1104537600", m2);
        String m1 = ArchiveUtils.secondsSinceEpoch("20050101");
        assertEquals("1104537600", m1);
        String m = ArchiveUtils.secondsSinceEpoch("2005010100");
        assertEquals("1104537600", m);
        boolean eThrown = false;
        try {
            ArchiveUtils.secondsSinceEpoch("20050");
        } catch (IllegalArgumentException e) {
            eThrown = true;
        }
        assertTrue(eThrown);
    }

    @Test
    public void testZeroPadInteger() {
        String m1 = ArchiveUtils.zeroPadInteger(1);
        assertEquals("0000000001", m1);
        String m = ArchiveUtils.zeroPadInteger(1000000000);
        assertEquals("1000000000", m);
    }
    
    /**
     * Test stable behavior of date formatting under heavy concurrency. 
     * 
     * @throws InterruptedException
     */
    @Test
    @EnabledIfSystemProperty(named = "runSlowTests", matches = "true")
    public void testDateFormatConcurrency() throws InterruptedException {
        final int COUNT = 1000;
        Thread [] ts = new Thread[COUNT];
        final Semaphore allDone = new Semaphore(-COUNT+1);
        final AtomicInteger failures = new AtomicInteger(0); 
        for (int i = 0; i < COUNT; i++) {
            Thread t = new Thread() {
                public void run() {
                    long n = System.currentTimeMillis();
                    final String d = ArchiveUtils.get17DigitDate(n);
                    for (int i = 0; i < 1000; i++) {
                        try {
                            sleep(10);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        String d2 = ArchiveUtils.get17DigitDate(n);
                        if(!d.equals(d2)) {
                            failures.incrementAndGet();
                            break; 
                        }
                    }
                    allDone.release();
                }
            };
            ts[i] = t;
            ts[i].setName(Integer.toString(i));
            ts[i].start();
            while(!ts[i].isAlive()) /* Wait for thread to spin up*/;
        }
        allDone.acquire(); // wait for all threads to finish
        String m = failures.get()+" format mismatches";
        assertEquals(0, (Object) failures.get(), m);
    }

    @Test
    public void testIsTld() {
        assertTrue(ArchiveUtils.isTld("com"), "TLD test problem");
        assertTrue(ArchiveUtils.isTld("COM"), "TLD test problem");
    }

    @Test
    public void testUnique17() {
        HashSet<String> uniqueTimestamps = new HashSet<String>();
        for(int i = 0; i<10; i++) {
            assertTrue(uniqueTimestamps.add(ArchiveUtils.getUnique17DigitDate()),"timestamp17 repeated");
        }
    }

    @Test
    public void testUnique14() {
        HashSet<String> uniqueTimestamps = new HashSet<String>();
        for(int i = 0; i<10; i++) {
            assertTrue(uniqueTimestamps.add(ArchiveUtils.getUnique14DigitDate()),"timestamp14 repeated");
        }
    }
}

