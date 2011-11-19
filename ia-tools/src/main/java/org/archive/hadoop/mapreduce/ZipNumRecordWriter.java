package org.archive.hadoop.mapreduce;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.archive.format.gzip.zipnum.ZipNumWriter;

/**
 * Warning - this has a bug.. leaves empty SUMMARY files in some cases.
 * 
 * @author brad
 *
 */
public class ZipNumRecordWriter extends RecordWriter<Text, Text>{
	protected ZipNumWriter znw;
	
    public static char DEFAULT_DELIM = ' ';
    public static char DEFAULT_NL = 10;
    public char delim = DEFAULT_DELIM;
    private final static Charset UTF8 = Charset.forName("utf-8");

    public ZipNumRecordWriter(int limit,
    		DataOutputStream outMain, DataOutputStream outSummary) {
    	znw = new ZipNumWriter(outMain, outSummary, limit);
    }

	@Override
	public void close(TaskAttemptContext arg0) throws IOException,
			InterruptedException {
		znw.close();
	}

	@Override
	public void write(Text key, Text val) throws IOException,
			InterruptedException {
		StringBuilder sb = new StringBuilder();
		sb.append(key.toString());
		sb.append(delim);
		sb.append(val.toString());
		sb.append(DEFAULT_NL);
		write(sb.toString().getBytes(UTF8));
	}

	public void write(byte[] bytes) throws IOException {
		znw.addRecord(bytes);
	}
	
	/**
	 * @return the delim
	 */
	public char getDelim() {
		return delim;
	}

	/**
	 * @param delim the delim to set
	 */
	public void setDelim(char delim) {
		this.delim = delim;
	}

}
