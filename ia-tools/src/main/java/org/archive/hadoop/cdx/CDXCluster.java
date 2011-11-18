package org.archive.hadoop.cdx;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.archive.util.binsearch.SortedTextFile;
import org.archive.util.binsearch.impl.HDFSSeekableLineReaderFactory;
import org.archive.util.iterator.BoundedStringIterator;
import org.archive.util.iterator.CloseableIterator;

public class CDXCluster {
	private final static Logger LOGGER = 
		Logger.getLogger(CDXCluster.class.getName());

	private Path clusterPath;
	SortedTextFile summary; 
	private FileSystem fs;
	private BlockLoader loader;
	public CDXCluster(Configuration conf, Path clusterPath) throws IOException {
		this.clusterPath = clusterPath;
		fs = clusterPath.getFileSystem(conf);
		loader = new HDFSBlockLoader(fs);
		Path summaryPath = new Path(clusterPath,"ALL.summary");
		HDFSSeekableLineReaderFactory factory = 
			new HDFSSeekableLineReaderFactory(fs, summaryPath);
		summary = new SortedTextFile(factory);
	}
	public CloseableIterator<String> getRangeBlockIterator(String start, String end) throws IOException {
		CloseableIterator<String> blocks = 
			summary.getRecordIterator(start, true);
		return new BoundedStringIterator(blocks, end);
	}
	public byte[] loadBlock(ZipNumBlock block) throws IOException {
		return loadBlock(block.shard,block.start,block.length);
	}
	public byte[] loadBlock(String name, long start, int length) throws IOException {
		LOGGER.warning(String.format("Loading(%s,%d,%d",name,start,length));
		Path shardPath = new Path(clusterPath,name + ".gz");
		return loader.readBlock(shardPath.toUri().toASCIIString(), start, length);
	}
	public CloseableIterator<String> getRange(String start, String end) throws IOException {
		return new ClusterRange(this,start,end);
	}
}
