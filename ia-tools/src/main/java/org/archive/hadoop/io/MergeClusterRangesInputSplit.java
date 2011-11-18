package org.archive.hadoop.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

public class MergeClusterRangesInputSplit extends InputSplit implements Writable {

	private long length;
	private String start;
	private String end;
	private String[] clusterPaths;
	
	// punting - likely the inputs will be spread all over
	private static final String[] locations = new String[0];

	public MergeClusterRangesInputSplit() {}
	public MergeClusterRangesInputSplit(long length, String start, String end,
			String[] clusterPaths) {
		this.length = length;
		this.start = start;
		this.end = end;
		this.clusterPaths = clusterPaths;
//		this.locations = new String[0];
	}	
	
	public void write(DataOutput out) throws IOException {
		out.writeLong(length);
		out.writeUTF(start);
		out.writeUTF(end);
		out.writeInt(clusterPaths.length);
		for(String p : clusterPaths) {
			out.writeUTF(p);
		}
//		out.writeInt(locations.length);
//		for(String l : locations) {
//			out.writeUTF(l);
//		}
	}

	public void readFields(DataInput in) throws IOException {
		length = in.readLong();
		start = in.readUTF();
		end = in.readUTF();
		int cl = in.readInt();
		clusterPaths = new String[cl];
		for(int i = 0; i < cl; i++) {
			clusterPaths[i] = in.readUTF();
		}
//		int ll = in.readInt();
//		locations = new String[ll];
//		for(int i = 0; i < ll; i++) {
//			locations[i] = in.readUTF();
//		}
	}

	@Override
	public long getLength() throws IOException, InterruptedException {
		return length;
	}

	@Override
	public String[] getLocations() throws IOException, InterruptedException {
		return locations;
	}
	/**
	 * @return the start
	 */
	public String getStart() {
		return start;
	}
	/**
	 * @return the end
	 */
	public String getEnd() {
		return end;
	}
	/**
	 * @return the clusterPaths
	 */
	public String[] getClusterPaths() {
		return clusterPaths;
	}
}
