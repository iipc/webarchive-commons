package org.archive.io.arc;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 
 * Based on https://github.com/iipc/openwayback/pull/104/files
 * 
 * @author csr@statsbiblioteket.dk (Colin Rosenthal)
 *
 */
public class ARCReaderFactoryTest {

    private File testfile1 = new File("src/test/resources/org/archive/format/arc/IAH-20080430204825-00000-blackbook-truncated.arc");

    /**
     * Test reading uncompressed arcfile for issue
     * https://github.com/iipc/openwayback/issues/101
     * @throws Exception
     */
	@Test
    public void testGetResource() throws Exception {
    	this.offsetResourceTest(testfile1, 1515, "http://www.archive.org/robots.txt" );
    	this.offsetResourceTest(testfile1, 36420, "http://www.archive.org/services/collection-rss.php" );
    }
    
    private void offsetResourceTest( File testfile, long offset, String uri ) throws Exception {
    	RandomAccessFile raf = new RandomAccessFile(testfile, "r");
		raf.seek(offset);
		InputStream is = new FileInputStream(raf.getFD());
		String fPath = testfile.getAbsolutePath();
		ArchiveReader reader = ARCReaderFactory.get(fPath, is, false);    	
		// This one works:
		//ArchiveReader reader = ARCReaderFactory.get(testfile, offset);
		ArchiveRecord record = reader.get();

		final String url = record.getHeader().getUrl();
		assertEquals(uri, url, "URL of record is not as expected.");
		
        final long position = record.getPosition();
        final long recordLength = record.getHeader().getLength();
        assertTrue(position <= recordLength, "Position " + position + " is after end of record " + recordLength);

        // Clean up:
        if( raf != null )
        	raf.close();
    }
    
}
