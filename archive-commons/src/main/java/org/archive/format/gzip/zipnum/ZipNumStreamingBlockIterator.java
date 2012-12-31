package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.logging.Logger;

import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.CloseableIterator;

/**
 * @author brad, ilya
 *
 */
public class ZipNumStreamingBlockIterator extends AbstractPeekableIterator<String> {
	private static final Logger LOGGER = Logger.getLogger(
			ZipNumStreamingBlockIterator.class.getName());

	private ZipNumStreamingBlock currBlock = null;
	private CloseableIterator<ZipNumStreamingBlock> blockItr = null;

	/**
	 * @param blocks which should be fetched and unzipped, one after another
	 */
	public ZipNumStreamingBlockIterator(CloseableIterator<ZipNumStreamingBlock> blockItr) {
//		if (LOGGER.isLoggable(Level.INFO)) {
//			LOGGER.info("Iterating over " + blocks.size() + " blocks");
//		}
		this.blockItr = blockItr;
	}
	
	@Override
	public String getNextInner() {
				
		try {		
			while (true) {
				if (currBlock == null) {
					if (blockItr.hasNext()) {
						currBlock = blockItr.next();
					} else {
						return null;
					}
				}
				
				// attempt to read the next line from this:
				String next = currBlock.readLine();
				
				if (next != null) {
					return next;
				}
		
				currBlock.close();
				currBlock = null;
			}
		} catch (IOException io) {
			LOGGER.warning(io.toString());
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		if (currBlock != null) {
			currBlock.close();
			currBlock = null;
		}
		
		if (blockItr != null) {
			blockItr.close();
			blockItr = null;
		}
	}
}
