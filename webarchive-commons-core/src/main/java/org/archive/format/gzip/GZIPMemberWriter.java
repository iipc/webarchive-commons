package org.archive.format.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.archive.util.StreamCopy;
import org.archive.util.io.CRCInputStream;

import com.google.common.io.CountingOutputStream;

public class GZIPMemberWriter implements GZIPConstants {
	private static final int MAX_RAM_BUFFER = 1024 * 1024;
	private byte slRecordName[] = SL_RECORD;
	public int maxBuffer = MAX_RAM_BUFFER;
	private CountingOutputStream out;
	
	public GZIPMemberWriter(OutputStream out) {
		this.out = new CountingOutputStream(out);
	}

	public void write(InputStream is) throws IOException {
		CRCInputStream crc = new CRCInputStream(is);
		GZIPHeader gzHeader = new GZIPHeader();
		// TODO: add fields...
		gzHeader.writeBytes(out);
		Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
		DeflaterOutputStream deflateOut = new DeflaterOutputStream(out,deflater);
		StreamCopy.copy(crc, deflateOut);
		deflateOut.finish();
		GZIPFooter gzFooter = new GZIPFooter(crc.getCRCValue(), crc.getByteCount());
		gzFooter.writeBytes(out);
		out.flush();
	}

	public long getBytesWritten() {
		return out.getCount();
	}
}
