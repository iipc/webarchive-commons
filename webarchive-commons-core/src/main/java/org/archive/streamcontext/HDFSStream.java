package org.archive.streamcontext;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;

public class HDFSStream extends AbstractBufferingStream {
	FSDataInputStream hdfs;
	public HDFSStream(FSDataInputStream hdfs) {
		this.hdfs = hdfs;
	}
	public HDFSStream(FSDataInputStream hdfs, long offset) throws IOException {
		this.hdfs = hdfs;
		doSeek(offset);
	}

	@Override
	public int doRead(byte[] b, int off, int len) throws IOException {
		return hdfs.read(b, off, len);
	}

	@Override
	public void doSeek(long offset) throws IOException {
//		System.err.format("HDFSdoSeek(%d)\n", offset);
		hdfs.seek(offset);
	}

	@Override
	public void doClose() throws IOException {
		hdfs.close();
	}
}
