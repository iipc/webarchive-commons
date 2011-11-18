package org.archive.format.gzip;

import java.io.IOException;

import org.archive.util.io.CommitedOutputStream;

import com.google.common.io.FileBackedOutputStream;

public class GZIPMemberWriterCommittedOutputStream extends CommitedOutputStream {
	private static int DEFAULT_BUFFER_RAM = 1024 * 1024;
	private GZIPMemberWriter gzW;
	public GZIPMemberWriterCommittedOutputStream(GZIPMemberWriter gzW) {
		this(gzW,DEFAULT_BUFFER_RAM);
	}
	public GZIPMemberWriterCommittedOutputStream(GZIPMemberWriter gzW, int bufferRAM) {
		super(new FileBackedOutputStream(bufferRAM,true));
		this.gzW = gzW;
	}

	@Override
	public void commit() throws IOException {
		FileBackedOutputStream fbos = (FileBackedOutputStream) out;
		gzW.writeWithLengthHeader(fbos.getSupplier().getInput());
		fbos.reset();
	}
	public long getBytesWritten() {
		return gzW.getBytesWritten();
	}
}
