package org.archive.hadoop.mapreduce;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.archive.format.gzip.GZIPConstants;
import org.archive.format.gzip.GZIPFooter;
import org.archive.format.gzip.GZIPHeader;
import org.archive.util.io.CRCOutputStream;

public class ZipNumRecordWriterOld  extends RecordWriter<Text, Text>{
    protected DataOutputStream outMain;
    protected DataOutputStream outSummary;
    protected int limit;
    
    private int count;
    private long offset;
    private ByteArrayOutputStream mainBuffer;
    private ByteArrayOutputStream summaryBuffer;
    private ByteArrayOutputStream gzBuffer;
    public static int DEFAULT_MAX_GZ_BUFFER = 1024 * 1024 * 2;
    public static int DEFAULT_MAX_BUFFER = 1024 * 1024 * 10;
    public static int DEFAULT_DELIM = 32;
    
    public static int newline = 10;
    public int delim = DEFAULT_DELIM;
    private final static Charset UTF8 = Charset.forName("utf-8");

    public ZipNumRecordWriterOld(int limit,
    		DataOutputStream outMain, DataOutputStream outSummary) {
    	this.outMain = outMain;
    	this.outSummary = outSummary;
    	this.limit = limit;
    	count = 0;
    	offset = 0;
    	mainBuffer = new ByteArrayOutputStream(DEFAULT_MAX_BUFFER);
    	summaryBuffer = new ByteArrayOutputStream(DEFAULT_MAX_BUFFER);
    	gzBuffer = new ByteArrayOutputStream(DEFAULT_MAX_GZ_BUFFER);
    }

	@Override
	public void close(TaskAttemptContext arg0) throws IOException,
			InterruptedException {
		finishCurrent();
		outMain.close();
		outSummary.close();
	}

	public void writeBytes(byte[] key, int delim, byte[] value) throws IOException {
		if(count == 0) {
			summaryBuffer.write(key);
			summaryBuffer.write(delim);
			summaryBuffer.write(value);
			summaryBuffer.write(newline);
		}
		mainBuffer.write(key);
		mainBuffer.write(delim);
		mainBuffer.write(value);
		mainBuffer.write(newline);
		count++;
		if(count == limit) {
			finishCurrent();
		}		
	}

	public void writeLineBytes(byte[] line) throws IOException {
		if(count == 0) {
			summaryBuffer.write(line);
			summaryBuffer.write(newline);
		}
		mainBuffer.write(line);
		mainBuffer.write(newline);
		count++;
		if(count == limit) {
			finishCurrent();
		}		
	}
	@Override
	public void write(Text key, Text val) throws IOException,
			InterruptedException {
		writeBytes(key.toString().getBytes(UTF8), delim, 
				val.toString().getBytes(UTF8));
	}
	private void finishCurrent() throws IOException {
		if(count == 0) {
			return;
		}
		gzBuffer.reset();

		// deflate the main buffer into the temp gzBuffer:

		Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
		DeflaterOutputStream deflateOut = new DeflaterOutputStream(gzBuffer,deflater);
		CRCOutputStream crcOut = new CRCOutputStream(deflateOut);
		mainBuffer.writeTo(crcOut);
		deflateOut.finish();

		// now calculate the gzip header and footer:
		GZIPHeader gzHeader = new GZIPHeader();
		gzHeader.addRecord(GZIPConstants.SL_RECORD, 
				deflater.getBytesWritten() + GZIPConstants.GZIP_FOOTER_BYTES);

		GZIPFooter gzFooter = new GZIPFooter(crcOut.getCRCValue(), 
				crcOut.getBytesWritten());

		// write the header, the deflated bytes, and the footer:
		int len = gzHeader.getLength() + gzBuffer.size()
			+ GZIPConstants.GZIP_FOOTER_BYTES;
		long startOffset = offset;
		offset += len;
		gzHeader.writeBytes(outMain);
		gzBuffer.writeTo(outMain);
		gzFooter.writeBytes(outMain);
		outMain.flush();

		// write the summary buffer:
		String offsetAndLength = String.format("%d\t%d\t",startOffset, len);
		outSummary.writeBytes(offsetAndLength);
		summaryBuffer.writeTo(outSummary);
		outSummary.flush();

		// reset the main and summary buffers for the next block:
		mainBuffer.reset();
		summaryBuffer.reset();
		count = 0;
	}

	/**
	 * @return the delim
	 */
	public int getDelim() {
		return delim;
	}

	/**
	 * @param delim the delim to set
	 */
	public void setDelim(int delim) {
		this.delim = delim;
	}

}
