package org.archive.hadoop.pig;

import java.io.IOException;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.pig.builtin.TextLoader;
import org.apache.pig.data.TupleFactory;

public class ZipNumLoader extends TextLoader {
	
	protected final static String ZIPNUM_SUMMARY_URI = "zipnum.summaryUri";
	protected final static String ZIPNUM_NUM_SPLITS = "zipnum.numSplits";
	protected final static String ZIPNUM_NUM_LINES_PER_SPLIT = "zipnum.numLinesPerSplit";
	protected final static String ZIPNUM_NUM_TOTAL_LINES = "zipnum.numTotalLines";
	
	protected final static String ZIPNUM_URL_START = "zipnum.url.start";
	protected final static String ZIPNUM_URL_END = "zipnum.url.end";
	
	protected TupleFactory factory;
	
	protected String clusterUriOrLoc;
	
	protected int numLinesPerSplit = 0;
	
	protected int numSplits = 0;
	
	protected ZipNumRecordReader lastReader;
	
	public ZipNumLoader()
	{
		
	}
	
	public ZipNumLoader(String numLinesPerSplit)
	{
		this.numLinesPerSplit = Integer.parseInt(numLinesPerSplit);
	}
		
//	public ZipNumLoader(String param, String clusterUriOrLoc)
//	{
//		this();
//		this.numSplits = 0;
//		this.numLinesPerSplit = Integer.parseInt(param);
//		this.clusterUriOrLoc = clusterUriOrLoc;
//	}
//	
//	@Override
//	public String relativeToAbsolutePath(String location, Path curDir)
//			throws IOException {
//		
//		if (GeneralURIStreamFactory.isHttp(location)) {		
//			return URLDecoder.decode(location, "UTF-8");
//		}
//		
//		return super.relativeToAbsolutePath(location, curDir);
//	}
//	
//	@Override
//	public void setLocation(String location, Job job) throws IOException {
//		Configuration conf = job.getConfiguration();
//		
//		conf.set(ZIPNUM_SUMMARY_URI, location);		
//		
//		if (numLinesPerSplit > 0) {
//			conf.setInt(ZIPNUM_NUM_LINES_PER_SPLIT, numLinesPerSplit);
//		}
//		
//		if (numSplits > 0) {		
//			conf.setInt(ZIPNUM_NUM_SPLITS, numSplits);
//		}
//		
//		super.setLocation(location, job);
//	}
	
	@Override
	public void setLocation(String location, Job job) throws IOException {			
		super.setLocation(location, job);
		
		if (numLinesPerSplit > 0) {
			NLineInputFormat.setNumLinesPerSplit(job, numLinesPerSplit);
		}
	}
	
	@Override
	public InputFormat getInputFormat() {
		
		return new NLineInputFormat()
		{
			@Override
			public RecordReader createRecordReader(
					InputSplit genericSplit, TaskAttemptContext context)
					throws IOException {
				
				//Path path = ((FileSplit)genericSplit).getPath();
				return new ZipNumRecordReader();
				//in = lastReader = new ZipNumRecordReader();
				//return lastReader;
			}		
		};
	}
}
