package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.archive.format.cdx.CDXFile;
import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.SeekableLineReaderIterator;
import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.BoundedStringIterator;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.StartBoundedStringIterator;

public class ZipNumCluster extends CDXFile {
	private final static Logger LOGGER = 
		Logger.getLogger(ZipNumCluster.class.getName());

	protected String clusterUri;
		
	protected String summaryFile;
	
	//protected SortedTextFile summary;
	
	protected HashMap<String, String[]> locMap = null;
	
	public ZipNumCluster(String clusterUri) throws IOException
	{
		this(clusterUri, "ALL.summary");
	}
	
	protected static String getStreamFactoryUri(String clusterUri, String summaryFile)
	{
		if (summaryFile.startsWith("/")) {
			return (summaryFile);
		} else {
			return (clusterUri + "/" + summaryFile);
		}
	}
	
	public ZipNumCluster(String clusterUri, String summaryFile) throws IOException {
		
		super(getStreamFactoryUri(clusterUri, summaryFile));
		
		this.clusterUri = clusterUri;
		this.summaryFile = summaryFile;
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
	
	class SummaryStreamingLoaderIterator extends AbstractPeekableIterator<ZipNumStreamingLoader>
	{
		protected CloseableIterator<String> summaryIterator;
		
		protected ZipNumStreamingLoader currLoader = null;
		
		protected String currLine;
		protected String nextLine;
		protected boolean isFirst = true;
						
		SummaryStreamingLoaderIterator(CloseableIterator<String> summaryIterator)
		{
			this.summaryIterator = summaryIterator;
			this.isFirst = true;
		}

		@Override
		public ZipNumStreamingLoader getNextInner() {
						
			if (summaryIterator.hasNext()) {
				nextLine = summaryIterator.next();
			}
			
			while (nextLine != null) {
				
				currLine = nextLine;
				
				if (summaryIterator.hasNext()) {
					nextLine = summaryIterator.next();
				} else {
					nextLine = null;
				}
				
				String blockDescriptor = currLine;
				
				String parts[] = blockDescriptor.split("\t");
			
				if ((parts.length < 3) || (parts.length > 4)) {
					LOGGER.severe("Bad line(" + blockDescriptor +") ");
					//throw new RecoverableRecordFormatException("Bad line(" + blockDescriptor + ")");
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
					if (currLoader != null) {
						currLoader.close();
					}
					
					currLoader = new ZipNumStreamingLoader(offset, length, partId, locations);
					
					if (isFirst) {
						currLoader.setIsFirst(true);
						isFirst = false;
					}
				} else {
					currLoader.addBlock(length);
				}
				
				if (nextLine == null) {
					currLoader.setIsLast(true);
				}
				
				return currLoader;
			}
			
			return null;
		}
		
		@Override
		public void close() throws IOException
		{
			if (summaryIterator != null) {
				summaryIterator.close();
				summaryIterator = null;
			}
			
			if (currLoader != null) {
				currLoader.close();
				currLoader = null;
			}
		}
	}
	
	public long getEstimateSplitSize(String[] blocks)
	{
		String parts[] = null, lastParts[] = null;
		
		long totalSize = 0;
		
		for (String block : blocks) {
			lastParts = parts;
			parts = block.split("\t");
			
			if ((lastParts != null) && (parts.length >= 3) && (lastParts.length >= 3)) {
				// If same shard, simply subtract
				long newOffset = Long.parseLong(parts[2]);
				
				if (parts[1].equals(lastParts[1])) {
					long lastOffset = Long.parseLong(lastParts[2]);
					totalSize += (newOffset - lastOffset);
				} else {
					totalSize += newOffset;
					//TODO: Compute size of all in between shards
					//computeBlockSizeDiff();
				}
			}
		}
		
		return totalSize;
	}
	
	public CloseableIterator<String> getClusterRange(String start, String end, boolean inclusive, boolean includePrevLine) throws IOException
	{
		CloseableIterator<String> iter = null;
		iter = super.getRecordIterator(start, includePrevLine);
		return wrapEndIterator(iter, end, inclusive);
		//return wrapStartEndIterator(iter, start, end, inclusive);
	}
	
	public CloseableIterator<String> wrapStartEndIterator(CloseableIterator<String> iter, String start, String end, boolean inclusive)
	{
		return wrapEndIterator(wrapStartIterator(iter, start), end, inclusive);
	}
	
	public CloseableIterator<String> wrapStartIterator(CloseableIterator<String> iter, String start)
	{
		return new StartBoundedStringIterator(iter, start);
	}
	
	public CloseableIterator<String> wrapEndIterator(CloseableIterator<String> iter, String end, boolean inclusive)
	{		
		if (end.isEmpty()) {
			return iter;
		} else {
			return new BoundedStringIterator(iter, end, inclusive);	
		}
	}
	
	public CloseableIterator<String> getCDXLineIterator(String key) throws IOException {
		return getCDXLineIterator(key, key);
	}
	
	public CloseableIterator<String> getLastBlockCDXLineIterator(String key) throws IOException {
		// the next line after last key<space> is key! so this will return last key<space> block
		return getCDXLineIterator(key + "!", key);
	}
			
	public CloseableIterator<String> getCDXLineIterator(String key, String prefix) throws IOException {
			
		//PrefixMatchStringIterator startIter = new PrefixMatchStringIterator(summary.getRecordIteratorLT(key), key, true);
		
		SummaryStreamingLoaderIterator blockIter = new SummaryStreamingLoaderIterator(super.getRecordIteratorLT(key));
		
		StreamingLoaderStringIterator zipIter = new StreamingLoaderStringIterator(blockIter);
				
		return new StartBoundedStringIterator(zipIter, prefix);
	}
	
	public CloseableIterator<String> getCDXIterator(CloseableIterator<String> summaryIterator)
	{
		SummaryStreamingLoaderIterator blockIter = new SummaryStreamingLoaderIterator(summaryIterator);
		StreamingLoaderStringIterator zipIter = new StreamingLoaderStringIterator(blockIter);
		return zipIter;
	}
		
	public String getClusterUri() {
		return clusterUri;
	}
	
	public String getSummaryFile() {
		return summaryFile;
	}
}
