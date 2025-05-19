package org.archive.format.gzip;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


import org.archive.util.ByteOp;
import org.archive.util.IAUtils;
import org.archive.util.TestUtils;
import org.archive.streamcontext.ByteArrayWrappedStream;
import org.archive.streamcontext.SimpleStream;
import org.archive.streamcontext.Stream;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GZIPMemberSeriesTest {

	@Test
	public void testSingle() throws IndexOutOfBoundsException, FileNotFoundException, IOException {

		InputStream is = getClass().getResourceAsStream("abcd.gz");
		byte abcd[] = ByteStreams.toByteArray(is);
		ByteArrayInputStream bais = new ByteArrayInputStream(abcd);
		Stream stream = new SimpleStream(bais);
		GZIPMemberSeries s = new GZIPMemberSeries(stream, "unk", 0);
		GZIPSeriesMember m = s.getNextMember();
		assertNotNull(m);
		assertEquals(0,m.getRecordStartOffset());
		assertEquals(10,m.getCompressedBytesRead());
		TestUtils.assertStreamEquals(m,"abcd".getBytes(IAUtils.UTF8));
		assertNull(s.getNextMember());
	}

	@Test
	public void testSingleEmpty() throws IndexOutOfBoundsException, FileNotFoundException, IOException {

		InputStream is = getClass().getResourceAsStream("empty.gz");
		byte empty[] = ByteStreams.toByteArray(is);
		ByteArrayInputStream bais = new ByteArrayInputStream(empty);
		Stream stream = new SimpleStream(bais);
		GZIPMemberSeries s = new GZIPMemberSeries(stream, "unk", 0);
		GZIPSeriesMember m = s.getNextMember();
		assertNotNull(m);
		assertEquals(0,m.getRecordStartOffset());
		assertEquals(10,m.getCompressedBytesRead());
		int got = m.read();
		assertEquals(-1,got);
		assertTrue(m.gotEOR());
		assertFalse(m.gotGZipError());
		assertFalse(s.gotEOF());
		assertFalse(s.gotIOError());
		assertNull(s.getNextMember());
		assertTrue(s.gotEOF());
	}

	@Test
	public void testDouble() throws IndexOutOfBoundsException, FileNotFoundException, IOException {

		InputStream is = getClass().getResourceAsStream("abcd.gz");
		byte abcd[] = ByteStreams.toByteArray(is);
		byte abcd2[] = ByteOp.append(abcd, abcd);
		ByteArrayInputStream bais = new ByteArrayInputStream(abcd2);
		Stream stream = new SimpleStream(bais);
		GZIPMemberSeries s = new GZIPMemberSeries(stream, "unk", 0);
		GZIPSeriesMember m = s.getNextMember();
		assertNotNull(m);
		assertEquals(0,m.getRecordStartOffset());
		assertEquals(10,m.getCompressedBytesRead());
		TestUtils.assertStreamEquals(m,"abcd".getBytes(IAUtils.UTF8));

		m = s.getNextMember();
		assertNotNull(m);
		assertEquals(abcd.length,m.getRecordStartOffset());
		assertEquals(10,m.getCompressedBytesRead());
		TestUtils.assertStreamEquals(m,"abcd".getBytes(IAUtils.UTF8));
		assertNull(s.getNextMember());
	}

	@Test
	public void testSingleCRCStrict() throws IndexOutOfBoundsException, FileNotFoundException, IOException {

		InputStream is = getClass().getResourceAsStream("abcd.gz");
		byte abcd[] = ByteStreams.toByteArray(is);
		byte oldb = abcd[abcd.length-1];
		abcd[abcd.length-1] = (byte) (abcd[abcd.length-1] + 1);
        assertNotEquals(oldb, abcd[abcd.length - 1]);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(abcd);
		Stream stream = new SimpleStream(bais);
		GZIPMemberSeries s = new GZIPMemberSeries(stream, "unk", 0);
		GZIPSeriesMember m = s.getNextMember();
		assertNotNull(m);
		assertEquals(0,m.getRecordStartOffset());
		assertEquals(10,m.getCompressedBytesRead());
		Exception e = null;
		try {
			ByteStreams.toByteArray(m);
		} catch(IOException gotEx) {
			e = gotEx;
		}
		assertNotNull(e);
		assertTrue(m.gotEOR());
		assertTrue(m.gotGZipError());
		assertFalse(m.gotIOError());
		assertFalse(s.gotEOF());
		assertTrue(s.gotIOError());
		e = null;
		try {
			m = s.getNextMember();
		} catch(IOException gotEx) {
			e = gotEx;
		}
		assertNotNull(e);
	}

	@Test
	public void testSingleCRCLAX() throws IndexOutOfBoundsException, FileNotFoundException, IOException {

		InputStream is = getClass().getResourceAsStream("abcd.gz");
		byte abcd[] = ByteStreams.toByteArray(is);
		byte oldb = abcd[abcd.length-1];
		abcd[abcd.length-1] = (byte) (abcd[abcd.length-1] + 1);
        assertNotEquals(oldb, abcd[abcd.length - 1]);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(abcd);
		Stream stream = new SimpleStream(bais);
		GZIPMemberSeries s = new GZIPMemberSeries(stream, "unk", 0, false);
		GZIPSeriesMember m = s.getNextMember();
		assertNotNull(m);
		assertEquals(0,m.getRecordStartOffset());
		assertEquals(10,m.getCompressedBytesRead());
		Exception e = null;
		try {
			ByteStreams.toByteArray(m);
		} catch(GZIPFormatException gotEx) {
			e = gotEx;
		}
		assertNotNull(e);
		assertTrue(m.gotEOR());
		assertTrue(m.gotGZipError());
		assertFalse(m.gotIOError());
		assertFalse(s.gotEOF());
		assertFalse(s.gotIOError());
		e = null;
		try {
			m = s.getNextMember();
		} catch(IOException gotEx) {
			e = gotEx;
		}
		assertNull(e);
		assertNull(s.getNextMember());
	}

	@Test
	public void testDoubleCRC1LAX() throws IndexOutOfBoundsException, FileNotFoundException, IOException {

		InputStream is = getClass().getResourceAsStream("abcd.gz");
		byte abcd[] = ByteStreams.toByteArray(is);
		byte abcdorig[] = ByteOp.copy(abcd);
		byte oldb = abcd[abcd.length-1];
		abcd[abcd.length-1] = (byte) (abcd[abcd.length-1] + 1);
        assertNotEquals(oldb, abcd[abcd.length - 1]);
		
		byte both[] = Bytes.concat(abcd,abcdorig);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(both);
		Stream stream = new SimpleStream(bais);
		GZIPMemberSeries s = new GZIPMemberSeries(stream, "unk", 0, false);
		GZIPSeriesMember m = s.getNextMember();
		assertNotNull(m);
		assertEquals(0,m.getRecordStartOffset());
		assertEquals(10,m.getCompressedBytesRead());
		Exception e = null;
		try {
			ByteStreams.toByteArray(m);
		} catch(GZIPFormatException gotEx) {
			e = gotEx;
		}
		assertNotNull(e);
		assertTrue(m.gotEOR());
		assertTrue(m.gotGZipError());
		assertFalse(m.gotIOError());
		assertFalse(s.gotEOF());
		assertFalse(s.gotIOError());
		e = null;
		try {
			m = s.getNextMember();
		} catch(IOException gotEx) {
			e = gotEx;
		}
		assertNull(e);
		assertNotNull(m);
		TestUtils.assertStreamEquals(m,"abcd".getBytes(IAUtils.UTF8));		
	}

	@Test
	public void testSingleDeflateError() throws IndexOutOfBoundsException, IOException {

		InputStream is = getClass().getResourceAsStream("abcd.gz");
		byte abcd[] = ByteStreams.toByteArray(is);
//		int permuteIdx = 15;
//		byte oldb = abcd[permuteIdx];
//		abcd[permuteIdx] = (byte) (abcd[permuteIdx] + 1);
//		assertFalse(oldb == abcd[permuteIdx]);
		abcd[10] = 0;
		abcd[11] = 0;
		abcd[12] = 0;
		abcd[13] = 0;
		abcd[14] = 0;
		
//		ByteArrayInputStream bais = new ByteArrayInputStream(abcd);
//		Stream stream = new SimpleStream(bais);
		Stream stream = new ByteArrayWrappedStream(abcd);
		GZIPMemberSeries s = new GZIPMemberSeries(stream, "unk", 0, false);
		GZIPSeriesMember m = s.getNextMember();
		assertNotNull(m);
		assertEquals(0,m.getRecordStartOffset());
		assertEquals(10,m.getCompressedBytesRead());
		Exception e = null;
		try {
			ByteStreams.toByteArray(m);
		} catch(GZIPFormatException gotEx) {
			e = gotEx;
		}
		assertNotNull(e);
		assertFalse(m.gotEOR());
		assertTrue(m.gotGZipError());
		assertFalse(m.gotIOError());
		assertFalse(s.gotEOF());
		assertFalse(s.gotIOError());
		e = null;
		try {
			m = s.getNextMember();
		} catch(IOException gotEx) {
			e = gotEx;
		}
		assertNull(e);
		assertNull(m);
	}

	@Test
	public void testDoubleDeflateError() throws IndexOutOfBoundsException, IOException {

		InputStream is = getClass().getResourceAsStream("abcd.gz");
		byte abcd[] = ByteStreams.toByteArray(is);
		byte both[] = Bytes.concat(abcd,abcd);
//		int permuteIdx = 15;
//		byte oldb = abcd[permuteIdx];
//		abcd[permuteIdx] = (byte) (abcd[permuteIdx] + 1);
//		assertFalse(oldb == abcd[permuteIdx]);
		both[10] = 0;
		both[11] = 0;
		both[12] = 0;
		both[13] = 0;
		both[14] = 0;
		
//		ByteArrayInputStream bais = new ByteArrayInputStream(abcd);
//		Stream stream = new SimpleStream(bais);
		Stream stream = new ByteArrayWrappedStream(both);
		GZIPMemberSeries s = new GZIPMemberSeries(stream, "unk", 0, false);
		GZIPSeriesMember m = s.getNextMember();
		assertNotNull(m);
		assertEquals(0,m.getRecordStartOffset());
		assertEquals(10,m.getCompressedBytesRead());
		Exception e = null;
		try {
			ByteStreams.toByteArray(m);
		} catch(GZIPFormatException gotEx) {
			e = gotEx;
		}
		assertNotNull(e);
		assertFalse(m.gotEOR());
		assertTrue(m.gotGZipError());
		assertFalse(m.gotIOError());
		assertFalse(s.gotEOF());
		assertFalse(s.gotIOError());
		e = null;
		try {
			m = s.getNextMember();
		} catch(IOException gotEx) {
			e = gotEx;
		}
		assertNull(e);
		assertNotNull(m);
		TestUtils.assertStreamEquals(m,"abcd".getBytes(IAUtils.UTF8));
		assertNull(s.getNextMember());
		assertTrue(s.gotEOF());
		assertFalse(s.gotIOError());

	}	

	@Test
	public void testDoubleBiggerDeflateErrOnFirst() throws IOException {
		String resource = "double-single-inflate-error.gz";
		InputStream is = getClass().getResourceAsStream(resource);
		byte full[] = ByteStreams.toByteArray(is);
		Stream stream = new ByteArrayWrappedStream(full);
		GZIPMemberSeries s = new GZIPMemberSeries(stream, "unk", 0, false);
		
		GZIPSeriesMember m = s.getNextMember();
		assertNotNull(m);
		assertEquals(0,m.getRecordStartOffset());
		assertEquals(10,m.getCompressedBytesRead());
		Exception e = null;
		try {
			ByteStreams.toByteArray(m);
		} catch(GZIPFormatException gotEx) {
			e = gotEx;
		}
		assertNotNull(e);
//		assertFalse(m.gotEOR());
		assertTrue(m.gotGZipError());
		assertFalse(m.gotIOError());
		assertFalse(s.gotEOF());
		assertFalse(s.gotIOError());
		e = null;
		try {
			m = s.getNextMember();
		} catch(IOException gotEx) {
			e = gotEx;
		}
		assertNull(e);
		assertNotNull(m);
		try {
			ByteStreams.toByteArray(m);
		} catch(GZIPFormatException gotEx) {
			e = gotEx;
		}
		assertNull(e);
		assertTrue(m.gotEOR());
		assertFalse(m.gotGZipError());

		
	}

	@Test
	public void testAutoSkip() throws IOException {
		InputStream is = getClass().getResourceAsStream("abcd.gz");
		byte abcd[] = ByteStreams.toByteArray(is);
		byte both[] = Bytes.concat(abcd,abcd);

		Stream stream = new ByteArrayWrappedStream(both);
		GZIPMemberSeries s = new GZIPMemberSeries(stream, "unk", 0, false);
		GZIPSeriesMember m = s.getNextMember();
		assertNotNull(m);
		assertEquals(0,m.getRecordStartOffset());
		assertEquals(10,m.getCompressedBytesRead());
		assertFalse(m.gotEOR());
		assertTrue(-1 != m.read());
		assertFalse(m.gotEOR());
		// auto-skip
		m = s.getNextMember();
		assertNotNull(m);
		
		Exception e = null;
		try {
			TestUtils.assertStreamEquals(m,"abcd".getBytes(IAUtils.UTF8));
		} catch(GZIPFormatException gotEx) {
			e = gotEx;
		}
		assertNull(e);
		assertTrue(m.gotEOR());
		assertFalse(m.gotGZipError());
		assertFalse(m.gotIOError());
		assertFalse(s.gotEOF());
		assertFalse(s.gotIOError());
		e = null;
		try {
			m = s.getNextMember();
		} catch(IOException gotEx) {
			e = gotEx;
		}
		assertNull(e);
		assertNull(m);
		assertTrue(s.gotEOF());
	}

	@Test
	public void testWgetProblem() throws IndexOutOfBoundsException, FileNotFoundException, IOException {
		InputStream is = getClass().getResourceAsStream("IAH-urls-wget.warc.gz");
		new GZIPDecoder().parseHeader(is);
	}
}
