package org.archive.format.gzip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.archive.util.IAUtils;

import org.junit.jupiter.api.Test;

public class GZIPMemberWriterTest {

	@Test
	public void testWrite() throws IOException {
                File outFile = File.createTempFile("tmp", ".gz");
		GZIPMemberWriter gzw = new GZIPMemberWriter(new FileOutputStream(outFile));
		gzw.write(new ByteArrayInputStream("Here is record 1".getBytes(IAUtils.UTF8)));
		gzw.write(new ByteArrayInputStream("Here is record 2".getBytes(IAUtils.UTF8)));
	}

}
