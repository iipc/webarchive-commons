package org.archive.hadoop.pig;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.archive.format.gzip.zipnum.ZipNumCluster;
import org.archive.format.gzip.zipnum.ZipNumParams;
import org.archive.util.iterator.CloseableIterator;

public class ZipNumRecordReader extends RecordReader<Text, Text> {
	
	protected ZipNumCluster cluster = null;
	
	protected Text nextCdxLine;
	protected Text key;
	
	protected CloseableIterator<String> cdxReader;
	
	protected LineRecordReader inner;
	protected ZipNumParams params;
	

	@Override
	public Text getCurrentKey() {
		return key;
	}

	@Override
	public float getProgress() {
		return inner.getProgress();
	}

	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException {
						
		
		FileSplit fileSplit = (FileSplit)split;
		inner = new LineRecordReader();
		inner.initialize(split, context);
		
		Path summaryPath = fileSplit.getPath();
		
		String summaryFile = summaryPath.toString();
		
		if (summaryFile.startsWith("file:/")) {
			summaryFile = summaryFile.substring(5);
		}
		
		cluster = new ZipNumCluster();
		cluster.setSummaryFile(summaryFile);
		cluster.init();
		
		key = new Text("");
		nextCdxLine = new Text("");
		
		params = new ZipNumParams();
		params.setMaxAggregateBlocks(0);
		params.setMaxBlocks(0);		
		cdxReader = cluster.getCDXIterator(new RecordReaderValueIterator(inner), params);
		
		//cdxReader = cluster.getCDXIterator(clusterSplit.createSummaryIterator());
	}

	@Override
	public boolean nextKeyValue() throws IOException {
		if (cdxReader != null && cdxReader.hasNext()) {
			
			String cdxLine = cdxReader.next();
			int spaceIndex = cdxLine.indexOf(' ');
			
			if (spaceIndex >= 0) {
				key.set(cdxLine.substring(0, spaceIndex));
			} else {
				key.set(cdxLine);
			}
			
			nextCdxLine.set(cdxLine);			
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

	public void seekNear(String key) {
		
		try {
			if (cdxReader != null) {
				cdxReader.close();
				cdxReader = null;
			}
			
			cdxReader = cluster.getCDXIterator(key, null);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
