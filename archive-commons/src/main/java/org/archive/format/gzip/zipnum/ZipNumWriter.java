package org.archive.format.gzip.zipnum;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.archive.format.gzip.GZIPMemberWriter;
import org.archive.format.gzip.GZIPMemberWriterCommittedOutputStream;

public class ZipNumWriter extends GZIPMemberWriterCommittedOutputStream {
	int limit;
	int count;
	OutputStream manifestOut;
	ByteArrayOutputStream manifestBuffer;
	char delimiter = '\t';
	private static final Charset UTF8 = Charset.forName("utf-8");
	public ZipNumWriter(OutputStream main, OutputStream manifest, int limit) {
		super(new GZIPMemberWriter(main));
		manifestOut = manifest;
		this.limit = limit;
		count = 0;
		manifestBuffer = new ByteArrayOutputStream();
	}

	public void addRecord(byte[] bytes) throws IOException {
		if(count == 0) {
			manifestBuffer.write(bytes);
		}
		write(bytes);
		count++;
		if(count == limit) {
			finishCurrent();
		}
	}
	
	public void close() throws IOException {
		finishCurrent();
	}

	private void finishCurrent() throws IOException {
		if(count == 0) {
			return;
		}
		long start = getBytesWritten();
		commit();
		long end = getBytesWritten();
		long len = end - start;
		StringBuilder sb = new StringBuilder();
		sb.append(start);
		sb.append(delimiter);
		sb.append(len);
		sb.append(delimiter);
		manifestOut.write(sb.toString().getBytes(UTF8));
		manifestBuffer.writeTo(manifestOut);
		manifestOut.flush();
		count = 0;
		manifestBuffer.reset();
	}
}
