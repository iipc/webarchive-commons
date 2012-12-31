package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.format.cdx.CDXInputSource;
import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.SeekableLineReaderIterator;
import org.archive.util.binsearch.SortedTextFile;
import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.StartBoundedStringIterator;

public class ZipNumCluster implements CDXInputSource {
	private final static Logger LOGGER = 
		Logger.getLogger(ZipNumCluster.class.getName());

	protected String clusterUri;
		
	protected String summaryFile;
	
	protected SortedTextFile summary;
	
	protected HashMap<String, String[]> locMap = null;
	
	public ZipNumCluster(String clusterUri) throws IOException
	{
		this(clusterUri, "ALL.summary");
	}
	
	public ZipNumCluster(String clusterUri, String summaryFile) throws IOException {
		
		this.clusterUri = clusterUri;
		this.summaryFile = summaryFile;
		
		SeekableLineReaderFactory summaryFactory;
		
		if (summaryFile.startsWith("/")) {
			summaryFactory = GeneralURIStreamFactory.createSeekableStreamFactory(summaryFile);
		} else {
			summaryFactory = GeneralURIStreamFactory.createSeekableStreamFactory(clusterUri + "/" + summaryFile);
		}
		
		summary = new SortedTextFile(summaryFactory);
	}
	
	public ZipNumCluster(String clusterUri, String summaryFile, String locUri) throws IOException {
		this(clusterUri, summaryFile);
		loadPartLocations(locUri);
	}
	
	protected void loadPartLocations(String locUri) throws IOException
	{
		locMap = new HashMap<String, String[]>();
		SeekableLineReaderIterator lines = null;
		
		try {
			SeekableLineReaderFactory readerFactory = GeneralURIStreamFactory.createSeekableStreamFactory(locUri);
			
			lines = new SeekableLineReaderIterator(readerFactory.get());
			
			while(lines.hasNext()) {
				String line = lines.next();
				String[] parts = line.split("\\s");
				if (parts.length < 2) {
					String msg = "Bad line(" + line +") in (" + locUri + ")";
					throw new IOException(msg);
				}
				
				String locations[] = new String[parts.length - 1];
			
				for (int i = 1; i < parts.length; i++) {
					locations[i-1] = parts[i];
				}
				
				locMap.put(parts[0], locations);
			}
		} finally {
			lines.close();	
		}
	}
	
	class ZipNumBlockIterator extends AbstractPeekableIterator<ZipNumStreamingBlock>
	{
		protected CloseableIterator<String> summaryIterator;
		protected ZipNumStreamingLoader currLoader = null;
		protected int numBlocks = 0;
		
		protected boolean exact;
		protected String key;

		protected boolean first = true;
		
		ZipNumBlockIterator(String key, boolean exact) throws IOException
		{
			this.key = key;
			this.exact = exact;
			
			if (key.equals("-")) {
				this.summaryIterator = summary.getRecordIterator(0);
			} else {
				this.summaryIterator = summary.getRecordIteratorLT(key);
			}
		}

		@Override
		public ZipNumStreamingBlock getNextInner() {
//			if (numBlocks >= maxBlocks) {
//				truncated = true;
//				if (LOGGER.isLoggable(Level.INFO)) {
//					LOGGER.info("Truncated by blocks for " + key);
//				}
//				return null;
//			}
						
			if (LOGGER.isLoggable(Level.INFO)) {
				if ((currLoader != null) && (numBlocks > 0) && (numBlocks % 10) == 0) {
					LOGGER.info("So far, read " + currLoader.toString());
				}
			}
			
			numBlocks++;
			
			String blockDescriptor = summaryIterator.next();
			String parts[] = blockDescriptor.split("\t");
			if ((parts.length < 3) || (parts.length > 4)) {
				LOGGER.severe("Bad line(" + blockDescriptor +") ");
				//throw new RecoverableRecordFormatException("Bad line(" + blockDescriptor + ")");
				return null;
			}
			// only compare the correct length:
			String prefCmp = key;
			String blockCmp = parts[0];
			
			if (first) {
				// always add first:
				first = false;
			} else if (key.equals("-")) {
				//
			} else if (exact && !blockCmp.equals(prefCmp)) {
				return null;
			} else if (!exact && !blockCmp.startsWith(prefCmp)) {
				return null;
			}
			
			if (parts.length < 3) {
				LOGGER.severe("No size for multi-block load at offset(" + parts[2] + ")");
				return null;
			}
			
			String partId = parts[1];
			
			String locations[] = null;
			
			if (locMap != null) {
				locations = locMap.get(partId);
				if (locations == null) {
					LOGGER.severe("No locations for block(" + partId +")");
				}
			} else {
				partId = clusterUri + "/" + partId + ".gz";
			}
	
			long offset = Long.parseLong(parts[2]);
			int length = Integer.parseInt(parts[3]);
			
			if ((currLoader == null) || !currLoader.isSameBlock(offset, partId)) {
				closeLoader();
				currLoader = new ZipNumStreamingLoader(offset, partId, locations);
			}
			
			return new ZipNumStreamingBlock(length, currLoader);
		}
		
		protected void closeLoader()
		{
			if (currLoader != null) {
				if (LOGGER.isLoggable(Level.INFO)) {
					LOGGER.info("Read " + currLoader.toString());
				}
				try {
					currLoader.close();
				} catch (IOException e) {
					LOGGER.warning(e.toString());
				}
				currLoader = null;
			}
		}

		@Override
		public void close() throws IOException {
			if (summaryIterator != null) {
				summaryIterator.close();
			}
			closeLoader();
		}
	}
	
	public CloseableIterator<String> getLineIterator(String key, boolean exact) throws IOException {		
		ZipNumBlockIterator blockIter = new ZipNumBlockIterator(key, exact);
		ZipNumStreamingBlockIterator zipIter = new ZipNumStreamingBlockIterator(blockIter);
		
		if (key.equals("-")) {
			return zipIter;
		}
		
		return new StartBoundedStringIterator(zipIter, key);
	}
	
//	public CDXSearchResult getLineIterator(String key, boolean exact) throws IOException {
//		ArrayList<ZipNumStreamingBlock> blocks = new ArrayList<ZipNumStreamingBlock>();
//		
//		boolean first = true;
//		int numBlocks = 0;
//		boolean truncated = false;
//		
//		CloseableIterator<String> itr = null;
//		
//		try {			
//			
//			itr = summary.getRecordIteratorLT(key);
//			
//			ZipNumStreamingLoader currLoader = null;
//			
//			String blockDescriptor = null;
//			
//			while(itr.hasNext()) {
//				if (numBlocks >= maxBlocks) {
//					truncated = true;
//					if (LOGGER.isLoggable(Level.INFO)) {
//						LOGGER.info("Truncated by blocks for " + key);
//					}
//					break;
//				}
//				
//				blockDescriptor = itr.next();
//				numBlocks++;
//				String parts[] = blockDescriptor.split("\t");
//				if((parts.length < 3) || (parts.length > 4)) {
//					LOGGER.severe("Bad line(" + blockDescriptor +") ");
//					throw new RecoverableRecordFormatException("Bad line(" + blockDescriptor + ")");
//				}
//				// only compare the correct length:
//				String prefCmp = key;
//				String blockCmp = parts[0];
//				
//				if (first) {
//					// always add first:
//					first = false;
//				} else if (exact && !blockCmp.equals(prefCmp)) {
//					break;
//				} else if (!exact && !blockCmp.startsWith(prefCmp)) {
//					break;
//				}
//							
//				// add this and keep lookin...
////				BlockLocation bl = chunkMap.get(parts[1]);
////				if(bl == null) {
////					LOGGER.severe("No locations for block(" + parts[1] +")");
////					throw new ResourceIndexNotAvailableException(
////							"No locations for block(" + parts[1] + ")");
////				}
//				
//				if(parts.length < 3) {
//					throw new RecoverableRecordFormatException(
//							"No size for multi-block load at offset(" + parts[2] + ")");
//				}
//				
//				String partId = parts[1];
//				
//				String locations[] = null;
//				
//				if (locMap != null) {
//					locations = locMap.get(partId);
//				} else {
//					partId = clusterUri + "/" + partId + ".gz";
//				}
//		
//				long offset = Long.parseLong(parts[2]);
//				int length = Integer.parseInt(parts[3]);
//				
//				if ((currLoader == null) || !currLoader.isSameBlock(offset, partId)) {
//					if ((currLoader != null) && LOGGER.isLoggable(Level.INFO)) {
//						LOGGER.info("Added " + currLoader.toString());
//					}
//					currLoader = new ZipNumStreamingLoader(offset, partId, locations);
//				}
//				
//				blocks.add(new ZipNumStreamingBlock(length, currLoader));
//			}
//			
//			if ((currLoader != null) && LOGGER.isLoggable(Level.INFO)) {
//				LOGGER.info("Added " + currLoader.toString());		
//			}
//			
//			ZipNumStreamingBlockIterator zipIter = new ZipNumStreamingBlockIterator(blocks.iterator());
//			return new CDXSearchResult(new StartBoundedStringIterator(zipIter, key), truncated);
//			
//		} finally {
//			if(itr != null) {
//				itr.close();
//			}
//		}
//	}
	
	public String getClusterUri() {
		return clusterUri;
	}
	
	public String getSummaryFile() {
		return summaryFile;
	}
}
