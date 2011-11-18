package org.archive.format.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.archive.util.StreamCopy;
import org.archive.util.io.CRCInputStream;

import com.google.common.io.CountingOutputStream;
import com.google.common.io.FileBackedOutputStream;

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

	public void writeWithLengthHeader(InputStream is) throws IOException {

		// stuff all the deflate data into the file backed OS:
		FileBackedOutputStream outTmp = new FileBackedOutputStream(maxBuffer);
		
		CRCInputStream crc = new CRCInputStream(is);
		Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
		DeflaterOutputStream deflateOut = new DeflaterOutputStream(outTmp,deflater);
		StreamCopy.copy(crc, deflateOut);
		deflateOut.finish();
		outTmp.flush();

		// now calculate and write the gzip header:
		GZIPHeader gzHeader = new GZIPHeader();
		gzHeader.addRecord(slRecordName, deflater.getBytesWritten() + GZIP_FOOTER_BYTES);
		gzHeader.writeBytes(out);

		StreamCopy.copy(outTmp.getSupplier().getInput(), out);

		GZIPFooter gzFooter = new GZIPFooter(crc.getCRCValue(), crc.getByteCount());
		gzFooter.writeBytes(out);
		out.flush();
	}
	public void writeWithAlexaHeader(InputStream is) throws IOException {

		// stuff all the deflate data into the file backed OS:
		FileBackedOutputStream outTmp = new FileBackedOutputStream(maxBuffer);
		
		CRCInputStream crc = new CRCInputStream(is);
		Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
		DeflaterOutputStream deflateOut = new DeflaterOutputStream(outTmp,deflater);
		StreamCopy.copy(crc, deflateOut);
		deflateOut.finish();
		outTmp.flush();

		// now calculate and write the gzip header:
		GZIPHeader gzHeader = new GZIPHeader();
		gzHeader.addRecord(LX_RECORD, LX_RECORD_VALUE);
		gzHeader.writeBytes(out);

		StreamCopy.copy(outTmp.getSupplier().getInput(), out);

		GZIPFooter gzFooter = new GZIPFooter(crc.getCRCValue(), crc.getByteCount());
		gzFooter.writeBytes(out);
		out.flush();
	}
	public long getBytesWritten() {
		return out.getCount();
	}
}
