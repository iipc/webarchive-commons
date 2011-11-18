package org.archive.server;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import junit.framework.TestCase;

public class GZRangeClientTest extends TestCase {

	public void testAppend() throws ParseException, IOException {
		File output = new File("/tmp/gzrclient-tmp");
		output.mkdirs();
		String prefix = "gzr-prefix-";
		String timestamp14 = "20110101000000";
		GZRangeClient gz = new GZRangeClient(output, prefix, timestamp14);
		String[] urls = {"http://ia500204.us.archive.org/0/items/10-20050904211432-00001-crawling018/10-20050904211432-00001-crawling018.arc.gz"};
		long offset = 507280;
		gz.append(offset, Arrays.asList(urls));
		gz.finish();
		
		GZRangeClient gz2 = new GZRangeClient(output, prefix, timestamp14);
		gz2.setExitOnError(true);
		Exception caught = null;
		try {
			gz2.append(offset+1, Arrays.asList(urls));
			gz2.finish();
		} catch(Exception e) {
			caught = e;
		}
		assertNotNull(caught);
		
		caught = null;
		GZRangeClient gz3 = new GZRangeClient(output, prefix, timestamp14);
		gz3.setExitOnError(false);
		try {
			gz3.append(offset+1, Arrays.asList(urls));
			gz3.finish();
		} catch(Exception e) {
			caught = e;
		}
		assertNull(caught);
	}

}
