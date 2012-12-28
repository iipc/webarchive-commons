package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.util.iterator.AbstractPeekableIterator;

/**
 * @author brad, ilya
 *
 */
public class ZipNumStreamingBlockIterator extends AbstractPeekableIterator<String> {
	private static final Logger LOGGER = Logger.getLogger(
			ZipNumStreamingBlockIterator.class.getName());

	private ZipNumStreamingBlock currBlock = null;
	private Iterator<ZipNumStreamingBlock> blockItr = null;

	/**
	 * @param blocks which should be fetched and unzipped, one after another
	 */
	public ZipNumStreamingBlockIterator(List<ZipNumStreamingBlock> blocks) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Iterating over " + blocks.size() + " blocks");
		}
		blockItr = blocks.iterator();
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
		
				currBlock.closeIfLast();
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
		}
	}
}
