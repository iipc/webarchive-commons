package org.archive.hadoop.pig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.LineReader;
import org.apache.pig.tools.counters.PigCounterHelper;
import org.archive.util.zip.OpenJDK7GZIPInputStream;

public class HttpInputLineRecordReader extends RecordReader<LongWritable, Text> {
	
	private final static Logger LOGGER =
		Logger.getLogger(HttpTextLoader.class.getName());
		
	protected LongWritable key;
	protected Text value;
		
	protected int linesRead = 0;
	
	protected long maxLines = 0;
	
	protected long totalLines = 0;
	
	protected String splitInfo;
		
	protected Counter counter;
	protected String urlString;
	
	protected HttpURLConnection conn;
	
	protected LineReader reader;
	//protected CountingInputStream cis;
	
	protected PigCounterHelper counterHelper;
	
	protected final static String HTTP_INPUT_COUNTER_GROUP = "Http Input";
	protected final static String LINE_COUNTER = "Lines Read";
	protected final static String BYTE_COUNTER = "Bytes Read";
	
	public HttpInputLineRecordReader(String urlString, int split) throws IOException
	{
		this.urlString = urlString;	
		this.key = new LongWritable(0);
		this.value = new Text("");
		
		splitInfo = "Split #" + split + " ";
		
		counterHelper = new PigCounterHelper();
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
				
		linesRead++;
		incCounters(bytesRead);
		
		key.set(key.get() + bytesRead);
		
		return true;
	}

	public void incCounters(long bytesRead) {
		counterHelper.incrCounter(HTTP_INPUT_COUNTER_GROUP, LINE_COUNTER, 1);
		counterHelper.incrCounter(HTTP_INPUT_COUNTER_GROUP, BYTE_COUNTER, bytesRead);
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		if (totalLines > 0) {
			return (float)linesRead / (float)totalLines;
		}
		
		return 0;
	}

	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		
		close();
		
		Configuration conf = context.getConfiguration();
		
		boolean useGzip = conf.getBoolean(HttpTextLoader.HTTP_TEXTLOADER_GZIP, true);
		
		if (useGzip) {
			urlString += HttpTextLoader.GZIP_PARAM;
		}
		
		URL url = new URL(urlString);
		LOGGER.info("Loader initialize - " + urlString);
		
		conn = (HttpURLConnection)url.openConnection();
		conn.connect();
				
		String linesEstimate = conn.getHeaderField(HttpTextLoader.NUM_LINES_HEADER_FIELD);
		
		if (linesEstimate != null) {
			try {
				totalLines = Integer.parseInt(linesEstimate);
			} catch (NumberFormatException n) {
				
			}
		}
		
		InputStream is = conn.getInputStream();
		
		//is = cis = new CountingInputStream(is);
		
		if (useGzip) {
			is = new OpenJDK7GZIPInputStream(is);
		}
		
		reader = new LineReader(is);
	}
	
	public String getUrl()
	{
		return urlString;
	}
}
