package org.archive.format.gzip;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.archive.util.io.CommitedOutputStream;

public class GZIPMemberWriterCommittedOutputStream extends CommitedOutputStream {
	private static int DEFAULT_BUFFER_RAM = 1024 * 1024;
	private GZIPMemberWriter gzW;
	public GZIPMemberWriterCommittedOutputStream(GZIPMemberWriter gzW) {
		this(gzW,DEFAULT_BUFFER_RAM);
	}
	public GZIPMemberWriterCommittedOutputStream(GZIPMemberWriter gzW, int bufferRAM) {
                super(new ByteArrayOutputStream());
		this.gzW = gzW;
	}

	@Override
	public void commit() throws IOException {
                ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
		gzW.write(new ByteArrayInputStream(bos.toByteArray()));
	}
	public long getBytesWritten() {
		return gzW.getBytesWritten();
	}
}
