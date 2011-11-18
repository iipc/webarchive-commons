package org.archive.hadoop.cdx;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSBlockLoader implements BlockLoader {
	private FileSystem fs;
	FSDataInputStream fsdis;
	String currentUrl;
	long currentOffset = -1;
	public HDFSBlockLoader(FileSystem fs) {
		this.fs = fs;
		fsdis = null;
		currentUrl = null;
		currentOffset = -1;
	}
	public byte[] readBlock(String url, long start, int length) throws IOException {
		byte[] buffer = new byte[length];
		if(url.equals(currentUrl)) {
			if(start != currentOffset) {
				fsdis.seek(start);
				currentOffset = start;
			}
			fsdis.readFully(buffer);
			currentOffset += length;
			return buffer;
		}
		if(fsdis != null) {
			fsdis.close();
		}
		fsdis = fs.open(new Path(url));
		currentUrl = url;
		fsdis.seek(start);
		fsdis.readFully(buffer);
		currentOffset = start + length;
		return buffer;
	}
	
}
