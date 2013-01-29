package org.archive.hadoop.pig;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.archive.format.gzip.zipnum.ZipNumCluster;
import org.archive.util.iterator.CloseableIterator;

public class HttpZipNumDerefLineRecordReader extends HttpInputLineRecordReader {
	
	protected ZipNumCluster cluster;
	
	protected String clusterUri;
	
	protected String start, end;
	
	protected Text nextCdxLine;
	
	protected int maxAggregateBlocks = 1;
	
	protected CloseableIterator<String> cdxReader;

	public HttpZipNumDerefLineRecordReader(String clusterUri, String summaryQueryUrl, int split, int maxAggregateBlocks)
			throws IOException {
		
		super(summaryQueryUrl, split);
		this.clusterUri = clusterUri;
		
		this.nextCdxLine = new Text("");
		
		this.maxAggregateBlocks = maxAggregateBlocks;
	}
	
	protected String getParam(String query, String key)
	{
		int index = query.indexOf(key);
		if (index < 0) {
			return null;
		}
		int endIndex = query.indexOf('&', index + 1);
		
		if (endIndex < 0) {
			return query.substring(index + key.length());
		} else {
			return query.substring(index + key.length(), endIndex);
		}
	}
	
	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		super.initialize(split, context);
		
		cluster = new ZipNumCluster(clusterUri);
		cluster.setMaxAggregateBlocks(maxAggregateBlocks);
		
		String query = super.urlString.substring(super.urlString.indexOf('?') + 1);
		start = getParam(query, "start=");
		end = getParam(query, "end=");
		
		HttpClusterInputSplit hcis = (HttpClusterInputSplit)split;
		
		cdxReader = cluster.getCDXIterator(new TextLoaderDerefIterator(), start, end, hcis.getSplit(), hcis.getNumSplits());
	}

	@Override
	public boolean nextKeyValue() throws IOException {
		if (cdxReader != null && cdxReader.hasNext()) {
			nextCdxLine.set(cdxReader.next());
			
			counterHelper.incrCounter(HTTP_INPUT_COUNTER_GROUP, LINE_COUNTER, 1);
			counterHelper.incrCounter(HTTP_INPUT_COUNTER_GROUP, BYTE_COUNTER, nextCdxLine.getLength() + 2);
			
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public Text getCurrentValue()
	{
		return nextCdxLine;
	}
	
	@Override
	public synchronized void close() throws IOException
	{
		if (cdxReader != null) {
			cdxReader.close();
			cdxReader = null;
		}
		
		super.close();
	}
	
	private class TextLoaderDerefIterator implements CloseableIterator<String>
	{	
		public boolean hasNext() {
			try {
				return HttpZipNumDerefLineRecordReader.super.nextKeyValue();
			}
			catch (IOException io) {
				io.printStackTrace();
				return false;
			}
		}
	
		public String next() {
			return HttpZipNumDerefLineRecordReader.super.getCurrentValue().toString();
		}
	
		public void remove() {
			
		}
		
		public void close()
		{
						
		}
	}
}
