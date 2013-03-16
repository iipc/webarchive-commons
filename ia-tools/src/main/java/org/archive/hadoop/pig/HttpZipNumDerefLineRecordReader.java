package org.archive.hadoop.pig;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.archive.format.gzip.zipnum.ZipNumCluster;
import org.archive.format.gzip.zipnum.ZipNumParams;
import org.archive.util.iterator.CloseableIterator;

public class HttpZipNumDerefLineRecordReader extends RecordReader<LongWritable, Text> {
	
	protected ZipNumCluster cluster;
	protected ZipNumParams params;
	
	protected String clusterUri;
	
	protected String start, end;
	
	protected Text nextCdxLine;
	
	protected HttpInputLineRecordReader inner;
		
	protected CloseableIterator<String> cdxReader;

	public HttpZipNumDerefLineRecordReader(String clusterUri, String summaryQueryUrl, int split, int maxAggregateBlocks)
			throws IOException {
		
		this.inner = new HttpInputLineRecordReader(summaryQueryUrl, split);
		
		this.clusterUri = clusterUri;
		
		this.nextCdxLine = new Text("");
		
		this.params = new ZipNumParams();
		this.params.setMaxAggregateBlocks(maxAggregateBlocks);
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
		
		inner.initialize(split, context);
		
		cluster = new ZipNumCluster(clusterUri);
		cluster.init();
		
		String theUrl = inner.getUrl();
		
		String query = theUrl.substring(theUrl.indexOf('?') + 1);
		start = getParam(query, "start=");
		end = getParam(query, "end=");
		
		HttpClusterInputSplit hcis = (HttpClusterInputSplit)split;
		
		cdxReader = cluster.getCDXIterator(new RecordReaderValueIterator(inner), start, end, hcis.getSplit(), hcis.getNumSplits());
	}

	@Override
	public boolean nextKeyValue() throws IOException {
		if (cdxReader != null && cdxReader.hasNext()) {
			nextCdxLine.set(cdxReader.next());
			inner.incCounters(nextCdxLine.getLength() + 2);
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
		
		inner.close();
	}

	@Override
	public LongWritable getCurrentKey() throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		return 0;
	}
}
