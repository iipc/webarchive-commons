package org.archive.hadoop.mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

public class SortMergeInputSplit extends InputSplit implements Writable {
	private long length;
	private String outputName;
	private String[] locations;
	private String[] inputPaths;
	
	public SortMergeInputSplit() {}
	
	public SortMergeInputSplit(long length, String outputName,
			String[] locations, String[] inputPaths) {
		this.length = length;
		this.outputName = outputName;
		this.locations = locations;
		this.inputPaths = inputPaths;
	}

	/**
	 * @return the outputName
	 */
	public String getOutputName() {
		return outputName;
	}


	@Override
	public long getLength() throws IOException, InterruptedException {
		return length;
	}

	@Override
	public String[] getLocations() throws IOException, InterruptedException {
		return locations;
	}

	public String[] getPaths() {
		return inputPaths;
	}

	public void write(DataOutput out) throws IOException {
		out.writeLong(length);
		out.writeUTF(outputName);
		out.writeInt(locations.length);
		for(String l : locations) {
			out.writeUTF(l);
		}
		out.writeInt(inputPaths.length);
		for(String p : inputPaths) {
			out.writeUTF(p);
		}
		
	}

	public void readFields(DataInput in) throws IOException {
		length = in.readLong();
		outputName = in.readUTF();
		int c = in.readInt();
		locations = new String[c];
		for(int i = 0; i < c; i++) {
			locations[i] = in.readUTF();
		}
		c = in.readInt();
		inputPaths = new String[c];
		for(int i = 0; i < c; i++) {
			inputPaths[i] = in.readUTF();
		}
	}
}
