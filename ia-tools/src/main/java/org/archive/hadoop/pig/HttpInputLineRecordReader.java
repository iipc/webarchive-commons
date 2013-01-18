package org.archive.hadoop.pig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.LineReader;
import org.apache.pig.tools.pigstats.PigStatusReporter;
import org.archive.util.zip.OpenJDK7GZIPInputStream;

public class HttpInputLineRecordReader extends RecordReader<LongWritable, Text> {
	
	private final static Logger LOGGER =
		Logger.getLogger(HttpTextLoader.class.getName());
	
	protected LongWritable key;
	protected Text value;
		
	protected int linesRead = 0;
	protected long maxLines = 0;
	
	protected long totalLines = 0;
		
	protected PigStatusReporter reporter;
	protected Counter counter;
	protected String urlString;
	
	protected HttpURLConnection conn;
	
	protected LineReader reader;
	//protected CountingInputStream cis;
	
	enum StreamReaderCounters
	{
		BYTES_READ;
	}

	public HttpInputLineRecordReader(String urlString) throws IOException
	{
		this.urlString = urlString;
		reporter = PigStatusReporter.getInstance();
		
		if (reporter != null) {
			counter = reporter.getCounter("StreamReader", urlString);
			if (counter != null) {
				counter.setValue(0);
			}
		}
		
		this.key = new LongWritable(0);
		this.value = new Text("");
	}

	@Override
	public synchronized void close() throws IOException {
		if (reader != null) {
			reader.close();
			reader = null;
		}
		
		if (conn != null) {
			conn.disconnect();
			conn = null;
		}
	}

	@Override
	public LongWritable getCurrentKey() {
		return key;
	}

	@Override
	public Text getCurrentValue() {
		return value;
	}

	@Override
	public boolean nextKeyValue() throws IOException {
		
		if ((maxLines > 0) && (linesRead >= maxLines)) {
			return false;
		}
		
		long bytesRead = reader.readLine(value);
		
		if (bytesRead <= 0) {
			return false;
		}
		
//		if (reporter != null && counter == null) {
//			counter = reporter.getCounter("StreamReader", urlString);
//			if (counter == null) {
//				counter = reporter.getCounter(StreamReaderCounters.BYTES_READ);
//			}
//		}
//		
//		if (counter != null) {
//			counter.increment(bytesRead);
//		}
				
		linesRead++;
		
		key.set(key.get() + bytesRead);
		
		return true;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		if (totalLines > 0) {
			return (float)linesRead / (float)totalLines;
		}
		
		return 0;
	}

	@Override
	public void initialize(InputSplit split, TaskAttemptContext arg1)
			throws IOException, InterruptedException {
		
		close();
		
		LOGGER.info("Loader initialize - " + urlString);
		
		URL url = new URL(urlString);
		
		conn = (HttpURLConnection)url.openConnection();
		conn.connect();
				
		String linesEstimate = conn.getHeaderField("X-Cluster-Num-Lines");
		
		if (linesEstimate != null) {
			try {
				totalLines = Integer.parseInt(linesEstimate);
			} catch (NumberFormatException n) {
				
			}
		}
		
		InputStream is = conn.getInputStream();
		
		//is = cis = new CountingInputStream(is);
		
		if (urlString.contains("&output=gzip")) {
			is = new OpenJDK7GZIPInputStream(is);
		}
		
		reader = new LineReader(is);
	}
}
