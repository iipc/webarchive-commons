package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.RecoverableRecordFormatException;
import org.archive.format.cdx.CDXInputSource;
import org.archive.format.cdx.CDXSearchResult;
import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.SortedTextFile;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.StartBoundedStringIterator;

public class ZipNumCluster implements CDXInputSource {
	private final static Logger LOGGER = 
		Logger.getLogger(ZipNumCluster.class.getName());

	protected String clusterUri;
	
	protected int maxBlocks = 100;
	
	public int getMaxBlocks() {
		return maxBlocks;
	}

	public void setMaxBlocks(int maxBlocks) {
		this.maxBlocks = maxBlocks;
	}

	protected String summaryFile;
	
	protected SortedTextFile summary;
	
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
	
	public CDXSearchResult getLineIterator(String key, boolean exact) throws IOException {
		ArrayList<ZipNumStreamingBlock> blocks = new ArrayList<ZipNumStreamingBlock>();
		
		boolean first = true;
		int numBlocks = 0;
		boolean truncated = false;
		
		CloseableIterator<String> itr = null;
		
		try {			
			
			itr = summary.getRecordIteratorLT(key);
			
			ZipNumStreamingLoader currLoader = null;
			
			String blockDescriptor = null;
			
			while(itr.hasNext()) {
				if (numBlocks >= maxBlocks) {
					truncated = true;
					if (LOGGER.isLoggable(Level.INFO)) {
						LOGGER.info("Truncated by blocks for " + key);
					}
					break;
				}
				
				blockDescriptor = itr.next();
				numBlocks++;
				String parts[] = blockDescriptor.split("\t");
				if((parts.length < 3) || (parts.length > 4)) {
					LOGGER.severe("Bad line(" + blockDescriptor +") ");
					throw new RecoverableRecordFormatException("Bad line(" + blockDescriptor + ")");
				}
				// only compare the correct length:
				String prefCmp = key;
				String blockCmp = parts[0];
				
				if (first) {
					// always add first:
					first = false;
				} else if (exact && !blockCmp.equals(prefCmp)) {
					break;
				} else if (!exact && !blockCmp.startsWith(prefCmp)) {
					break;
				}
							
				// add this and keep lookin...
//				BlockLocation bl = chunkMap.get(parts[1]);
//				if(bl == null) {
//					LOGGER.severe("No locations for block(" + parts[1] +")");
//					throw new ResourceIndexNotAvailableException(
//							"No locations for block(" + parts[1] + ")");
//				}
				
				if(parts.length < 3) {
					throw new RecoverableRecordFormatException(
							"No size for multi-block load at offset(" + parts[2] + ")");
				}
				
				String partUri = clusterUri + "/" + parts[1] + ".gz";
								
				long offset = Long.parseLong(parts[2]);
				int length = Integer.parseInt(parts[3]);
				
				if ((currLoader == null) || !currLoader.isSameBlock(offset, partUri)) {
					if ((currLoader != null) && LOGGER.isLoggable(Level.INFO)) {
						LOGGER.info("Added " + currLoader.toString());
					}
					currLoader = new ZipNumStreamingLoader(offset, partUri);
				}
				
				blocks.add(new ZipNumStreamingBlock(length, currLoader));
			}
			
			if ((currLoader != null) && LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Added " + currLoader.toString());			
			}
			
			ZipNumStreamingBlockIterator zipIter = new ZipNumStreamingBlockIterator(blocks);
			return new CDXSearchResult(new StartBoundedStringIterator(zipIter, key), truncated);
			
		} finally {
			if(itr != null) {
				itr.close();
			}
		}
	}
	
	public String getClusterUri() {
		return clusterUri;
	}

	public void setClusterUri(String clusterUri) {
		this.clusterUri = clusterUri;
	}
	
	public String getSummaryFile() {
		return summaryFile;
	}

	public void setSummaryFile(String summaryFile) {
		this.summaryFile = summaryFile;
	}
}
