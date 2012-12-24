package org.archive.format.gzip.zipnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.archive.util.iterator.CloseableIterator;
import org.mortbay.io.RuntimeIOException;

/**
 * @author brad
 *
 */
public class ZipNumStreamingBlockIterator implements CloseableIterator<String> {
	private static final Logger LOGGER = Logger.getLogger(
			ZipNumStreamingBlockIterator.class.getName());

	private BufferedReader br = null;
	private ZipNumStreamingBlock currBlock = null;
	private Iterator<ZipNumStreamingBlock> blockItr = null;
	private String cachedNext = null;
	private boolean truncated = false;
	/**
	 * @param blocks which should be fetched and unzipped, one after another
	 */
	public ZipNumStreamingBlockIterator(List<ZipNumStreamingBlock> blocks) {
		LOGGER.info("initialized with " + blocks.size() + " blocks");
		blockItr = blocks.iterator();
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if(cachedNext != null) {
			return true;
		}
		while(cachedNext == null) {
			if(br != null) {
				// attempt to read the next line from this:
				try {
					cachedNext = br.readLine();
					if(cachedNext == null) {
						currBlock.closeIfLast(br);
						currBlock = null;
						br = null;
						// next loop:
					} else {
						return true;
					}
				} catch (IOException e) {
					e.printStackTrace();
					currBlock = null;
					br = null;
				}
			} else {
				// do we have more blocks to use?
				if(blockItr.hasNext()) {
					try {
						currBlock = blockItr.next();
						br = currBlock.readBlock();
					} catch (IOException e) {
						throw new RuntimeIOException();
					}
				} else {
					return false;
				}
			}
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public String next() {
		String tmp = cachedNext;
		cachedNext = null;
		return tmp;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		if(br != null) {
			br.close();
		}
	}
//	public static void main(String[] args) {
//		if(args.length != 1) {
//			System.err.println("Usage: ZIPLINES_PATH");
//			System.exit(1);
//		}
//		File f = new File(args[0]);
//		long size = f.length();
//		long numBlocks = (long) (size / ZiplinedBlock.BLOCK_SIZE);
//		long size2 = numBlocks * ZiplinedBlock.BLOCK_SIZE;
//		if(size != size2) {
//			System.err.println("File size of " + args[0] + " is not a mulitple"
//					+ " of " + ZiplinedBlock.BLOCK_SIZE);
//		}
//		try {
//			RandomAccessFile raf = new RandomAccessFile(f, "r");
//			for(int i = 0; i < numBlocks; i++) {
//				long offset = i * ZiplinedBlock.BLOCK_SIZE;
//				raf.seek(offset);
////				BufferedReader br = new BufferedReader(new InputStreamReader(
////						new GZIPInputStream(new FileInputStream(raf.getFD())),ByteOp.UTF8));
//				BufferedReader br = new BufferedReader(new InputStreamReader(
//						new OpenJDK7GZIPInputStream(new FileInputStream(raf.getFD())),ByteOp.UTF8));
//				String line = br.readLine();
//				if(line == null) {
//					System.err.println("Bad block at " + offset + " in " + args[0]);
//					System.exit(1);
//				}
//				System.out.println(args[0] + " " + offset + " " + line);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
//	}
	/**
	 * @return the truncated
	 */
	public boolean isTruncated() {
		return truncated;
	}
	/**
	 * @param truncated the truncated to set
	 */
	public void setTruncated(boolean truncated) {
		this.truncated = truncated;
	}
}
