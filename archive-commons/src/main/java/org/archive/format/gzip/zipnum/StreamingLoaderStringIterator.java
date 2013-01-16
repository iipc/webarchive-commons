package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.CloseableIteratorUtil;

/**
 * @author brad, ilya
 *
 */
public class StreamingLoaderStringIterator extends AbstractPeekableIterator<String> {
	private static final Logger LOGGER = Logger.getLogger(
			StreamingLoaderStringIterator.class.getName());

	private ZipNumStreamingLoader currLoader = null;
	private Iterator<ZipNumStreamingLoader> blockItr = null;

	/**
	 * @param blocks which should be fetched and unzipped, one after another
	 */
	public StreamingLoaderStringIterator(Iterator<ZipNumStreamingLoader> blockItr) {
//		if (LOGGER.isLoggable(Level.INFO)) {
//			LOGGER.info("Iterating over " + blocks.size() + " blocks");
//		}
		this.blockItr = blockItr;
	}
	
	@Override
	public String getNextInner() {
				
		try {		
			while (true) {
				if (currLoader == null) {
					if (blockItr.hasNext()) {
						currLoader = blockItr.next();
					} else {
						return null;
					}
				}
				
				// attempt to read the next line from this:
				String next = currLoader.readLine();
				
				if (next != null) {
					return next;
				}
		
				//currLoader.close();
				currLoader = null;
			}
		} catch (IOException io) {
			LOGGER.warning(io.toString());
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		if (currLoader != null) {
			currLoader.close();
			currLoader = null;
		}
		
		if (blockItr != null) {
			CloseableIteratorUtil.attemptClose(blockItr);
			blockItr = null;
		}
	}
}
