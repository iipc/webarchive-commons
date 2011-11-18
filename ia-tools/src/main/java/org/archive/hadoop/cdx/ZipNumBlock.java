package org.archive.hadoop.cdx;

public class ZipNumBlock {
	String url;
	String shard;
	long start;
	int length;
	public ZipNumBlock(String line) {
		String parts[] = line.split("\t");
		if(parts.length != 4) {
			throw new IllegalArgumentException("Bad block:" + line);
		}
		url = parts[0];
		shard = parts[1];
		start = Long.parseLong(parts[2]);
		length = Integer.parseInt(parts[3]);		
	}
}
