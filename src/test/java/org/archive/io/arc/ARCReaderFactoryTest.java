package org.archive.io.arc;

import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;

import junit.framework.TestCase;

/**
 * 
 * Based on https://github.com/iipc/openwayback/pull/104/files
 * 
 * @author csr@statsbiblioteket.dk (Colin Rosenthal)
 *
 */
public class ARCReaderFactoryTest extends TestCase {

    private File testfile1 = new File("src/test/resources/org/archive/format/arc/IAH-20080430204825-00000-blackbook-truncated.arc");
    //private File testfile_nl = new File("src/test/resources/org/archive/format/arc/137542-153-20111129020925-00316-kb-prod-har-003.kb.dk_truncated.arc");
    private File testfile_nl = getResource(
            "org/archive/format/arc/137542-153-20111129020925-00316-kb-prod-har-003.kb.dk_truncated.arc");

    /**
     * Test reading uncompressed arcfile for issue
     * https://github.com/iipc/openwayback/issues/101
     * @throws Exception
     */
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
		assertEquals("URL of record is not as expected.", uri, url);
		
        final long position = record.getPosition();
        final long recordLength = record.getHeader().getLength();
        assertTrue("Position " + position + " is after end of record " + recordLength, position <= recordLength);

        // Clean up:
        if( raf != null )
        	raf.close();
    }

    public void testBaseSampleARC() throws IOException {
        testIteration(testfile1);
    }
    // Independent of the ARCReader code
    public void testBaseSampleIntegrity() throws IOException {
        List<String> urls = ARCTestHelper.getURLs(testfile1);
        assertEquals("The correct number of URLs should be extracted", 8, urls.size());
    }

    // Independent of the ARCReader code
    public void testVerifyNewlinedSampleIntegrity() throws IOException {
        List<String> urls = ARCTestHelper.getURLs(testfile_nl);
        assertEquals("The correct number of URLs should be extracted", 3, urls.size());
    }

    /*
    This fails, but the independent {@link ARCTestHelper} is able to process it.
    Logically one of the implementations is faulty.
     */
    public void testNewlinedSampleARC() throws IOException {
        testIteration(testfile_nl);
    }

    private void testIteration(File arc) throws IOException {
        ARCReader reader = ARCReaderFactory.get(arc);
        Iterator<ArchiveRecord> ir = reader.iterator();
        while (ir.hasNext()) {
            System.out.println(ir.next().getHeader().getHeaderValue("subject-uri"));
        }
        reader.close();
    }

    private static File getResource(String resource) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null) {
            throw new RuntimeException("The resource '" + resource + "' could not be located in the class path");
        }
        return new File(url.getFile());
    }
}
