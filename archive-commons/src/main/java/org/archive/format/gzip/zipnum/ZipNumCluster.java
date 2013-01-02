package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.archive.format.cdx.CDXInputSource;
import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.SeekableLineReaderIterator;
import org.archive.util.binsearch.SortedTextFile;
import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.BoundedStringIterator;
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
	
	class SummaryStreamingLoaderIterator extends AbstractPeekableIterator<ZipNumStreamingLoader>
	{
		protected CloseableIterator<String> summaryIterator;
		protected String lastPartId = null;
						
		SummaryStreamingLoaderIterator(CloseableIterator<String> summaryIterator)
		{
			this.summaryIterator = summaryIterator;
		}

		@Override
		public ZipNumStreamingLoader getNextInner() {
			
			while (summaryIterator.hasNext()) {
				
//				if (LOGGER.isLoggable(Level.INFO)) {
//					if ((currLoader != null) && (numBlocks > 0) && (numBlocks % 10) == 0) {
//						LOGGER.info("So far, read " + currLoader.toString());
//					}
//				}
				
//				numBlocks++;
				
				String blockDescriptor = summaryIterator.next();
				
				String parts[] = blockDescriptor.split("\t");
			
				if ((parts.length < 3) || (parts.length > 4)) {
					LOGGER.severe("Bad line(" + blockDescriptor +") ");
					//throw new RecoverableRecordFormatException("Bad line(" + blockDescriptor + ")");
					return null;
				}
				
				String partId = parts[1];
				
				if ((lastPartId == null) || !lastPartId.equals(partId)) {
					
					lastPartId = partId;
					
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
					
					return new ZipNumStreamingLoader(offset, partId, locations);
				}
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
		}
	}
	
	public CloseableIterator<String> getClusterRange(String start, String end, boolean inclusive) throws IOException
	{
		return new BoundedStringIterator(summary.getRecordIteratorLT(start), end, inclusive);
	}
			
	public CloseableIterator<String> getCDXLineIterator(String key) throws IOException {
			
		//PrefixMatchStringIterator startIter = new PrefixMatchStringIterator(summary.getRecordIteratorLT(key), key, true);
		
		SummaryStreamingLoaderIterator blockIter = new SummaryStreamingLoaderIterator(summary.getRecordIteratorLT(key));
		
		StreamingLoaderStringIterator zipIter = new StreamingLoaderStringIterator(blockIter);
				
		return new StartBoundedStringIterator(zipIter, key);
	}
		
	public String getClusterUri() {
		return clusterUri;
	}
	
	public String getSummaryFile() {
		return summaryFile;
	}
}
