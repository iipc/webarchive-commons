package org.archive.hadoop.mapreduce;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.archive.hadoop.cdx.HDFSBlockLoader;
import org.archive.hadoop.cdx.ZipNumBlockIterator;

public class GZIPMembersLineRecordReader extends RecordReader<Text, Text> {
	LineRecordReader internal = null;
	HDFSBlockLoader loader = null;
	Configuration conf = null;
	Text key;
	Text value;
	String context;
	Iterator<String> activeBlock = null;
	int lineNumber = 0;
	
	@Override
	public void close() throws IOException {
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		return key;
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return internal.getProgress();
	}

	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		internal = new LineRecordReader();
		internal.initialize(split, context);
		conf = context.getConfiguration();
		FileSplit fileSplit = (FileSplit) split;
		loader = new HDFSBlockLoader(fileSplit.getPath().getFileSystem(conf));
		key = new Text();
		value = new Text();
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		while(true) {
			if(activeBlock == null) {
				if(!internal.nextKeyValue()) {
					return false;
				}
				String v = internal.getCurrentValue().toString();
				String parts[] = v.split("\\s");
				if(parts.length != 3) {
					throw new IOException("Bad line:" + v);
				}
				long offset = 0;
				int len = 0;
				try {
					offset = Long.parseLong(parts[1]);
					len = Integer.parseInt(parts[2]);
				} catch(NumberFormatException e) {
					throw new IOException("Bad line:" + v);
				}
				context = String.format("%s:%d",parts[0],offset);
				byte[] compressed = loader.readBlock(parts[0], offset, len);
				activeBlock = new ZipNumBlockIterator(compressed).iterator();
			}
			if(activeBlock != null) {
				try {
					if(activeBlock.hasNext()) {
						lineNumber++;
						key.set(context+":"+lineNumber);
						value.set(activeBlock.next());
						return true;
					} else {
						activeBlock = null;
					}
				} catch(Exception e) {
					throw new IOException(context,e);
				}
			}
		}
	}
}
