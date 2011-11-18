package org.archive.format.gzip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.archive.util.IAUtils;

import junit.framework.TestCase;

public class GZIPMemberWriterTest extends TestCase {

	public void testWrite() throws IOException {
		String outPath = "/tmp/tmp.gz";
		GZIPMemberWriter gzw = new GZIPMemberWriter(new FileOutputStream(new File(outPath)));
		gzw.write(new ByteArrayInputStream("Here is record 1".getBytes(IAUtils.UTF8)));
		gzw.write(new ByteArrayInputStream("Here is record 2".getBytes(IAUtils.UTF8)));
	}

}
