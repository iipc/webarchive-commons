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

package org.archive.util.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.archive.util.ArchiveUtils;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GZIPMembersInputStream
 * @author gojomo
 * @version $ $
 */
public class GZIPMembersInputStreamTest {
    byte[] noise1k_gz;
    byte[] noise32k_gz; 
    byte[] a_gz;
    byte[] hello_gz;
    byte[] allfour_gz;
    byte[] sixsmall_gz;
    {
        Random rand = new Random(1); 
        try {
            byte[] buf = new byte[1024];
            rand.nextBytes(buf); 
            noise1k_gz = ArchiveUtils.gzip(buf);
            buf = new byte[32*1024];
            rand.nextBytes(buf);
            noise32k_gz = ArchiveUtils.gzip(buf);
            a_gz = ArchiveUtils.gzip("a".getBytes(StandardCharsets.US_ASCII));
            hello_gz = ArchiveUtils.gzip("hello".getBytes(StandardCharsets.US_ASCII));
            allfour_gz = Bytes.concat(noise1k_gz,noise32k_gz,a_gz,hello_gz);
            sixsmall_gz = Bytes.concat(a_gz,hello_gz,a_gz,hello_gz,a_gz,hello_gz); 
        }  catch (IOException e) {
            // should not happen
        }
    }

    @Test
    public void testFullReadAllFour() throws IOException {
        GZIPMembersInputStream gzin = 
            new GZIPMembersInputStream(new ByteArrayInputStream(allfour_gz));
        int count = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
        assertEquals(1024+(32*1024)+1+5, count, "wrong length uncompressed data");
    }

    @Test
    public void testFullReadSixSmall() throws IOException {
        GZIPMembersInputStream gzin = 
            new GZIPMembersInputStream(new ByteArrayInputStream(sixsmall_gz));
        int count = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
        assertEquals(1+5+1+5+1+5, count, "wrong length uncompressed data");
    }

    @Test
    public void testReadPerMemberAllFour() throws IOException {
        GZIPMembersInputStream gzin = 
            new GZIPMembersInputStream(new ByteArrayInputStream(allfour_gz));
        gzin.setEofEachMember(true); 
        int count0 = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
        assertEquals(1024, count0, "wrong 1k member count");
        assertEquals(0, gzin.getMemberNumber(), "wrong member number");
        assertEquals(0, gzin.getCurrentMemberStart(), "wrong member0 start");
        assertEquals(noise1k_gz.length, gzin.getCurrentMemberEnd(), "wrong member0 end");
        gzin.nextMember(); 
        int count1 = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
        assertEquals((32*1024), count1, "wrong 32k member count");
        assertEquals(1, gzin.getMemberNumber(), "wrong member number");
        assertEquals(noise1k_gz.length,  gzin.getCurrentMemberStart(), "wrong member1 start");
        assertEquals(noise1k_gz.length+noise32k_gz.length, gzin.getCurrentMemberEnd(), "wrong member1 end");
        gzin.nextMember(); 
        int count2 = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
        assertEquals(1, count2, "wrong 1-byte member count");
        assertEquals(2, gzin.getMemberNumber(), "wrong member number");
        assertEquals(noise1k_gz.length+noise32k_gz.length,  gzin.getCurrentMemberStart(), "wrong member2 start");
        assertEquals(noise1k_gz.length+noise32k_gz.length+a_gz.length, gzin.getCurrentMemberEnd(), "wrong member2 end");
        gzin.nextMember(); 
        int count3 = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
        assertEquals(5, count3, "wrong 5-byte member count");
        assertEquals(3, gzin.getMemberNumber(), "wrong member number");
        assertEquals(noise1k_gz.length+noise32k_gz.length+a_gz.length, gzin.getCurrentMemberStart(), "wrong member3 start");
        assertEquals(noise1k_gz.length+noise32k_gz.length+a_gz.length+hello_gz.length, gzin.getCurrentMemberEnd(), "wrong member3 end");
        gzin.nextMember();
        int countEnd = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
        assertEquals(0, countEnd, "wrong eof count");
    }

    @Test
    public void testReadPerMemberSixSmall() throws IOException {
        GZIPMembersInputStream gzin = 
            new GZIPMembersInputStream(new ByteArrayInputStream(sixsmall_gz));
        gzin.setEofEachMember(true); 
        for(int i = 0; i < 3; i++) {
            int count2 = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
            assertEquals(1, count2, "wrong 1-byte member count");
            gzin.nextMember(); 
            int count3 = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
            assertEquals(5, count3, "wrong 5-byte member count");
            gzin.nextMember();
        }
        int countEnd = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
        assertEquals(0, countEnd, "wrong eof count");
    }
    
    @Test
    public void testByteReadPerMember() throws IOException {
        GZIPMembersInputStream gzin = 
            new GZIPMembersInputStream(new ByteArrayInputStream(allfour_gz));
        gzin.setEofEachMember(true); 
        int count0 = 0;
        while(gzin.read()>-1) count0++;
        assertEquals(1024, count0, "wrong 1k member count");
        assertEquals(0, gzin.getMemberNumber(), "wrong member number");
        assertEquals(0, gzin.getCurrentMemberStart(), "wrong member0 start");
        assertEquals(noise1k_gz.length, gzin.getCurrentMemberEnd(), "wrong member0 end");
        gzin.nextMember(); 
        int count1 = 0; 
        while(gzin.read()>-1) count1++;
        assertEquals((32*1024), count1, "wrong 32k member count");
        assertEquals(1, gzin.getMemberNumber(), "wrong member number");
        assertEquals(noise1k_gz.length,  gzin.getCurrentMemberStart(), "wrong member1 start");
        assertEquals(noise1k_gz.length+noise32k_gz.length, gzin.getCurrentMemberEnd(), "wrong member1 end");
        gzin.nextMember(); 
        int count2 = 0;
        while(gzin.read()>-1) count2++;
        assertEquals(1, count2, "wrong 1-byte member count");
        assertEquals(2, gzin.getMemberNumber(), "wrong member number");
        assertEquals(noise1k_gz.length+noise32k_gz.length,  gzin.getCurrentMemberStart(), "wrong member2 start");
        assertEquals(noise1k_gz.length+noise32k_gz.length+a_gz.length, gzin.getCurrentMemberEnd(), "wrong member2 end");
        gzin.nextMember(); 
        int count3 = 0;
        while(gzin.read()>-1) count3++;
        assertEquals(5, count3, "wrong 5-byte member count");
        assertEquals(3, gzin.getMemberNumber(), "wrong member number");
        assertEquals(noise1k_gz.length+noise32k_gz.length+a_gz.length, gzin.getCurrentMemberStart(), "wrong member3 start");
        assertEquals(noise1k_gz.length+noise32k_gz.length+a_gz.length+hello_gz.length, gzin.getCurrentMemberEnd(), "wrong member3 end");
        gzin.nextMember();
        int countEnd = 0;
        while(gzin.read()>-1) countEnd++;
        assertEquals(0, countEnd, "wrong eof count");
    }

    @Test
    public void testMemberSeek() throws IOException {
        GZIPMembersInputStream gzin = 
            new GZIPMembersInputStream(new ByteArrayInputStream(allfour_gz));
        gzin.setEofEachMember(true); 
        gzin.compressedSeek(noise1k_gz.length+noise32k_gz.length);
        int count2 = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
        assertEquals(1, count2, "wrong 1-byte member count");
//        assertEquals("wrong Member number", 2, gzin.getMemberNumber());
        assertEquals(noise1k_gz.length+noise32k_gz.length,  gzin.getCurrentMemberStart(), "wrong Member2 start");
        assertEquals(noise1k_gz.length+noise32k_gz.length+a_gz.length, gzin.getCurrentMemberEnd(), "wrong Member2 end");
        gzin.nextMember(); 
        int count3 = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
        assertEquals(5, count3, "wrong 5-byte member count");
//        assertEquals("wrong Member number", 3, gzin.getMemberNumber());
        assertEquals(noise1k_gz.length+noise32k_gz.length+a_gz.length, gzin.getCurrentMemberStart(), "wrong Member3 start");
        assertEquals(noise1k_gz.length+noise32k_gz.length+a_gz.length+hello_gz.length, gzin.getCurrentMemberEnd(), "wrong Member3 end");
        gzin.nextMember();
        int countEnd = IOUtils.copy(gzin, ByteStreams.nullOutputStream());
        assertEquals(0, countEnd, "wrong eof count");
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testMemberIterator() throws IOException {
        GZIPMembersInputStream gzin = 
            new GZIPMembersInputStream(new ByteArrayInputStream(allfour_gz));
        Iterator<GZIPMembersInputStream> iter = gzin.memberIterator();
        assertTrue(iter.hasNext());
        GZIPMembersInputStream gzMember0 = iter.next();
        int count0 = IOUtils.copy(gzMember0, ByteStreams.nullOutputStream());
        assertEquals(1024, count0, "wrong 1k member count");
        assertEquals(0, gzin.getMemberNumber(), "wrong member number");
        assertEquals(0, gzin.getCurrentMemberStart(), "wrong member0 start");
        assertEquals(noise1k_gz.length, gzin.getCurrentMemberEnd(), "wrong member0 end");
        
        assertTrue(iter.hasNext());
        GZIPMembersInputStream gzMember1 = iter.next();
        int count1 = IOUtils.copy(gzMember1, ByteStreams.nullOutputStream());
        assertEquals((32*1024), count1, "wrong 32k member count");
        assertEquals(1, gzin.getMemberNumber(), "wrong member number");
        assertEquals(noise1k_gz.length,  gzin.getCurrentMemberStart(), "wrong member1 start");
        assertEquals(noise1k_gz.length+noise32k_gz.length, gzin.getCurrentMemberEnd(), "wrong member1 end");
        
        assertTrue(iter.hasNext());
        GZIPMembersInputStream gzMember2 = iter.next();
        int count2 = IOUtils.copy(gzMember2, ByteStreams.nullOutputStream()); 
        assertEquals(1, count2, "wrong 1-byte member count");
        assertEquals(2, gzin.getMemberNumber(), "wrong member number");
        assertEquals(noise1k_gz.length+noise32k_gz.length,  gzin.getCurrentMemberStart(), "wrong member2 start");
        assertEquals(noise1k_gz.length+noise32k_gz.length+a_gz.length, gzin.getCurrentMemberEnd(), "wrong member2 end");
        
        assertTrue(iter.hasNext());
        GZIPMembersInputStream gzMember3 = iter.next();
        int count3 = IOUtils.copy(gzMember3, ByteStreams.nullOutputStream());
        assertEquals(5, count3, "wrong 5-byte member count");
        assertEquals(3, gzin.getMemberNumber(), "wrong member number");
        assertEquals(noise1k_gz.length+noise32k_gz.length+a_gz.length, gzin.getCurrentMemberStart(), "wrong member3 start");
        assertEquals(noise1k_gz.length+noise32k_gz.length+a_gz.length+hello_gz.length, gzin.getCurrentMemberEnd(), "wrong member3 end");
        
        assertFalse(iter.hasNext());
    }
    
}