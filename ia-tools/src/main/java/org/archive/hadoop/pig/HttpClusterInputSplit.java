package org.archive.hadoop.pig;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

public class HttpClusterInputSplit extends InputSplit implements Writable
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
		this.split = split;
		this.numSplits = numSplits;
		this.url = url;
		
		// Fill in split and numSplits
		//this.url = String.format(this.url, split, numSplits);
		//this.url = this.url.replace("$SPLIT", String.valueOf(split));
		//this.url = this.url.replace("$NUM_SPLITS", String.valueOf(numSplits));
	}
	
	public void setLength(long length)
	{
		estLength = length;
	}
	
	public String getUrl()
	{
		return this.url;
	}
	
	public int getSplit()
	{
		return split;
	}
	
	public int getNumSplits()
	{
		return numSplits;
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