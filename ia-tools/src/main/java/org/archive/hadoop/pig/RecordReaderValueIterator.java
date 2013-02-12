package org.archive.hadoop.pig;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordReader;
import org.archive.util.iterator.CloseableIterator;

class RecordReaderValueIterator implements CloseableIterator<String>
{
	
	protected RecordReader<LongWritable, Text> recordReader;
	
	public RecordReaderValueIterator(RecordReader<LongWritable, Text> recordReader)
	{
		this.recordReader = recordReader;
	}
	
	public boolean hasNext() {
		try {
			return recordReader.nextKeyValue();
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String next() {
		try {
			return recordReader.getCurrentValue().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public void remove() {
		
	}
	
	public void close()
	{
					
	}
}