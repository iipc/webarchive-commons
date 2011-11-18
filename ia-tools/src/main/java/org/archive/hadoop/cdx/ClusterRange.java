package org.archive.hadoop.cdx;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.BoundedStringIterator;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.StartBoundedStringIterator;


public class ClusterRange extends AbstractPeekableIterator<String> {
	private final static Logger LOG =
		Logger.getLogger(ClusterRange.class.getName());
	private boolean isFirst;
	private boolean done;
	private String start;
	private String end;
	private CDXCluster cluster;
	private CloseableIterator<String> blocks;
	private Iterator<String> current;
	public ClusterRange(CDXCluster cluster, String start, String end) throws IOException {
		this.cluster = cluster;
		this.start = start;
		this.end = end;
		blocks = cluster.getRangeBlockIterator(start, end);
		done = false;
		isFirst = true;
	}
	@Override
	public String getNextInner() {
		try {
			return getNextWrapper();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public String getNextWrapper() throws IOException {
		if(done) {
			return null;
		}
		if(current != null) {
			if(current.hasNext()) {
				return current.next();
			}
			// done with current:
		}
		while(blocks.hasNext()) {
			String nextLine = blocks.next();
			ZipNumBlock block = new ZipNumBlock(nextLine);
			byte[] compressed = cluster.loadBlock(block);
			LOG.fine(String.format("Loaded block:%s (%d)(%d)",
					block.shard,block.start,block.length));
			ZipNumBlockIterator zbi = new ZipNumBlockIterator(compressed);
			Iterator<String> itr = zbi.iterator();
			if(isFirst) {
				isFirst = false;
				current = new BoundedStringIterator(new StartBoundedStringIterator(itr,start),end);
			} else {
				current = new BoundedStringIterator(itr, end);
			}
			if(current.hasNext()) {
				return current.next();
			}
		}
		done = true;
		return null;
	}
	@Override
	public void close() throws IOException {
		blocks.close();
	}

}
