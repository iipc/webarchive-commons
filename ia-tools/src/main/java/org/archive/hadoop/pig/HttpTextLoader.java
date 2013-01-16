package org.archive.hadoop.pig;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.pig.builtin.TextLoader;

public class HttpTextLoader extends TextLoader {
	
	private final static Logger LOGGER =
		Logger.getLogger(HttpTextLoader.class.getName());
	
	protected int maxLines = 0;
	protected int numSplits = 0;
	
	private final static String HTTP_TEXTLOADER_URL = "httptextloader.url";
	private final static String HTTP_TEXTLOADER_NUM_SPLITS = "httptextloader.numSplits";
	
	public HttpTextLoader()
	{
		super();
	}
	
	public HttpTextLoader(String numSplitsString, String maxLinesString)
	{
		this.numSplits = Integer.parseInt(numSplitsString);
		this.maxLines = Integer.parseInt(maxLinesString);
	}
	
	public static class HttpClusterInputSplit extends InputSplit implements Writable
	{
		protected long estLength;
		
		protected String url;
		protected int split;
		protected int numSplits;
		
		public HttpClusterInputSplit()
		{
			numSplits = 1;
		}
		
		public HttpClusterInputSplit(String url, int split, int numSplits)
		{
			this.estLength = 0;

			this.split = split;
			this.numSplits = numSplits;
			
			// Fill in split and numSplits
			this.url = String.format(url, split, numSplits);
		}
		
		public void setLength(long length)
		{
			estLength = length;
		}
		
		public String getUrl()
		{
			return this.url;
		}
				
		@Override
		public long getLength() {
			return estLength;
		}

		@Override
		public String[] getLocations() throws IOException, InterruptedException {
			return new String[0];
		}

		public void readFields(DataInput in) throws IOException {
			url = in.readUTF();
			
			split = in.readInt();
			numSplits = in.readInt();
			
			estLength = in.readLong();
		}

		public void write(DataOutput out) throws IOException {
			out.writeUTF(url);
			
			out.writeInt(split);
			out.writeInt(numSplits);
			
			out.writeLong(estLength);
		}
	}

	@Override
	public InputFormat<LongWritable, Text> getInputFormat() {
		return new InputFormat<LongWritable, Text>()
		{
			@Override
			public RecordReader<LongWritable, Text> createRecordReader(InputSplit split,
					TaskAttemptContext job) {
				
				//String theUrl = job.getConfiguration().get(HTTP_TEXTLOADER_URL);
				
				if (!(split instanceof HttpClusterInputSplit)) {
					throw new RuntimeException("Wrong Input Split, must be HttpClusterInputSplit");
				}
				
				HttpClusterInputSplit clusterSplit = (HttpClusterInputSplit)split;
				
				try {
					return new HttpInputLineRecordReader(clusterSplit.getUrl());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public List<InputSplit> getSplits(JobContext context)
					throws IOException {
				ArrayList<InputSplit> array = new ArrayList<InputSplit>();
				
				Configuration conf = context.getConfiguration();
				
				String url = conf.get(HTTP_TEXTLOADER_URL);
				int numSplits = conf.getInt(HTTP_TEXTLOADER_NUM_SPLITS, 1);
				
				LOGGER.info("getSplits - " + numSplits + " " + url);
				
				for (int i = 0; i < numSplits; i++) {
					array.add(new HttpClusterInputSplit(url, i, numSplits));
				}
				
				return array;
			}
		};
	}

	@Override
	public void setLocation(String location, Job job) throws IOException {
		Configuration conf = job.getConfiguration();
		
		location = URLDecoder.decode(location, "UTF-8");
		
		conf.set(HTTP_TEXTLOADER_URL, location);
		conf.setInt(HTTP_TEXTLOADER_NUM_SPLITS, numSplits);
		
		LOGGER.info("setLocation - " + numSplits + " " + location);
	}
}
