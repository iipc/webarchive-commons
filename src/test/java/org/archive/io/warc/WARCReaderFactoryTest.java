package org.archive.io.warc;

import java.io.FileInputStream;
import java.io.IOException;

import org.archive.format.warc.WARCConstants;
import org.archive.format.warc.WARCConstants.WARCRecordType;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WARCReaderFactoryTest {
	
	// Test files:
	String[] files = new String[] {
			"src/test/resources/org/archive/format/gzip/IAH-urls-wget.warc.gz",
			"src/test/resources/org/archive/format/warc/IAH-urls-wget.warc"
	};

	@Test
	public void testGetStringInputstreamBoolean() throws IOException {
		// Check the test files can be opened:
		for( String file : files ) {
			FileInputStream is = new FileInputStream(file);
			ArchiveReader ar = WARCReaderFactory.get(file, is, true);
			ArchiveRecord r = ar.get();
			String type = (String) r.getHeader().getHeaderValue(WARCConstants.HEADER_KEY_TYPE);
			// Check the first record comes out as a 'warcinfo' record.
			assertEquals(WARCRecordType.warcinfo.name(), type);
		}
	}
	

}
