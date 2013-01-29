package org.archive.hadoop.pig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.builtin.TextLoader;

public class HttpTextLoader extends TextLoader {
	
	private final static Logger LOGGER =
		Logger.getLogger(HttpTextLoader.class.getName());
	
	protected int maxLinesPerSplit = 0;
	protected int numSplits = 1;
	
	protected final static String HTTP_TEXTLOADER_URL = "httptextloader.url";
	protected final static String HTTP_TEXTLOADER_NUM_SPLITS = "httptextloader.numSplits";
	protected final static String HTTP_TEXTLOADER_MAX_LINES = "httptextloader.maxLines";
	protected final static String HTTP_TEXTLOADER_GZIP = "httptextloader.gzip";
	protected final static String HTTP_TEXTLOADER_ZIPNUM_CLUSTER = "httptextloader.clusterUri";
	protected final static String HTTP_TEXTLOADER_MAX_AGGREGATE_BLOCKS = "httptextloader.maxAggregateBlocks";
	
	protected final static String COUNT_LINES_PARAM = "&countLines=true";
	
	protected final static String GZIP_PARAM = "&output=gzip";
	
	protected final static String CDX_PARAM = "&cdx=true";
	
	protected final static String SPLIT_PARAM = "&split=";
	protected final static String NUM_SPLIT_PARAM = "&numSplits=";
	
	protected final static String NUM_LINES_HEADER_FIELD = "X-Cluster-Num-Lines";
	
	public HttpTextLoader()
	{
		super();
	}
	
	public HttpTextLoader(String option, String param)
	{
		if (option != null) {
			if (option.equalsIgnoreCase("splits")) {
				this.numSplits = Integer.parseInt(param);
				this.maxLinesPerSplit = 0;
			} else if (option.equalsIgnoreCase("maxLines")) {
				this.numSplits = 1;
				this.maxLinesPerSplit = Integer.parseInt(param);
			}
		}
	}
	
	public static String getSplitUrl(String url, int split, int numSplits)
	{
		StringBuilder builder = new StringBuilder(url);
		builder.append(SPLIT_PARAM);
		builder.append(split);
		builder.append(NUM_SPLIT_PARAM);
		builder.append(numSplits);
		return builder.toString();
	}
	
	@Override
	public InputFormat<LongWritable, Text> getInputFormat() {
		return new InputFormat<LongWritable, Text>()
		{
			@Override
			public RecordReader<LongWritable, Text> createRecordReader(InputSplit split,
					TaskAttemptContext job) {
				
				if (!(split instanceof HttpClusterInputSplit)) {
					throw new RuntimeException("Wrong Input Split, must be HttpClusterInputSplit");
				}
				
				Configuration conf = job.getConfiguration();
				
				String clusterUri = conf.get(HTTP_TEXTLOADER_ZIPNUM_CLUSTER);
				
				
				HttpClusterInputSplit clusterSplit = (HttpClusterInputSplit)split;
				
				try {
					if (clusterUri != null) {
						int maxAggBlocks = Integer.parseInt(conf.get(HTTP_TEXTLOADER_MAX_AGGREGATE_BLOCKS, "1"));
						return new HttpZipNumDerefLineRecordReader(clusterUri, clusterSplit.getUrl(), clusterSplit.getSplit(), maxAggBlocks);
					} else {
						return new HttpInputLineRecordReader(clusterSplit.getUrl() + CDX_PARAM, clusterSplit.getSplit());
					}
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
					array.add(new HttpClusterInputSplit(getSplitUrl(url, i, numSplits), i, numSplits));
				}
				
				return array;
			}
		};
	}

	@Override
	public void setLocation(String location, Job job) throws IOException {
		Configuration conf = job.getConfiguration();
		
		String savedLoc = conf.get(HTTP_TEXTLOADER_URL);
		
		if (savedLoc == null) {
			location = URLDecoder.decode(location, "UTF-8");
			
			conf.set(HTTP_TEXTLOADER_URL, location);	
		} else {
			location = savedLoc;
		}
				
		if (maxLinesPerSplit > 0) {
			
			int totalLineCount = Integer.parseInt(conf.get(HTTP_TEXTLOADER_MAX_LINES, "-1"));
			
			if (totalLineCount == -1) {
				totalLineCount = queryLineCount(location);
				conf.set(HTTP_TEXTLOADER_MAX_LINES, String.valueOf(totalLineCount));
			}
			
			if (totalLineCount > 0) {
				numSplits = totalLineCount / maxLinesPerSplit;
				LOGGER.info("Total Line Count / maxLinesPerSplit = " + totalLineCount + " / " + maxLinesPerSplit + " = " + numSplits);
			} else {
				LOGGER.info("Total Line Count Not Available");
			}
		}
		
		conf.setInt(HTTP_TEXTLOADER_NUM_SPLITS, numSplits);
		
		LOGGER.info("setLocation - " + numSplits + " " + location);
	}
	
	protected int queryLineCount(String url)
	{
		HttpURLConnection conn = null;
		int numLines = 0;
		
		try {
			URL theURL = new URL(url + COUNT_LINES_PARAM);
			
			conn = (HttpURLConnection)theURL.openConnection();
			conn.setRequestMethod("HEAD");
			conn.connect();
			
			numLines = conn.getHeaderFieldInt(NUM_LINES_HEADER_FIELD, 0);
			
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		
		return numLines;
	}
	
	@Override
	public void prepareToRead(RecordReader reader, PigSplit split) {
		super.prepareToRead(reader, split);
	}

	@Override
	public String relativeToAbsolutePath(String location, Path curDir)
			throws IOException {
		
		return location;
	}
}
