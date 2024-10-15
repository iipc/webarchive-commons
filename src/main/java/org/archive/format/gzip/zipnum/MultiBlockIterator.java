package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.logging.Logger;

import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.CloseableIterator;

/**
 * @author brad, ilya
 *
 */
public class MultiBlockIterator extends AbstractPeekableIterator<String> {
	private static final Logger LOGGER = Logger.getLogger(
			MultiBlockIterator.class.getName());

	private CloseableIterator<String> currLoader = null;
	private CloseableIterator<CloseableIterator<String>> blockItr = null;

	/**
	 * @param blockItr blocks which should be fetched and unzipped, one after another
	 */
	public MultiBlockIterator(CloseableIterator<CloseableIterator<String>> blockItr) {
		this.blockItr = blockItr;
	}
	
	@Override
	public String getNextInner() {
		
		while (true) {
			if (currLoader == null) {
				if (blockItr.hasNext()) {
					currLoader = blockItr.next();
				} else {
					return null;
				}
			}
			
			if (currLoader.hasNext()) {
				String next = currLoader.next();
				return next;
			}
			
			// attempt to read the next line from this:
	//			String next = currLoader.readLine();
	//			
	//			if (next != null) {
	//				return next;
	//			}
	
			try {
				currLoader.close();
			} catch (IOException exc) {
				LOGGER.warning(exc.toString());				
			}
			
			currLoader = null;
		}
	}

	@Override
	public void close() throws IOException {
		if (currLoader != null) {
			try {
				currLoader.close();
			} catch (IOException exc) {
				LOGGER.warning(exc.toString());				
			}
			currLoader = null;
		}
		
		if (blockItr != null) {
			blockItr.close();
			blockItr = null;
		}
	}
}
