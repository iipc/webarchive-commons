package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.archive.format.cdx.CDXFile;
import org.archive.format.gzip.zipnum.blockloader.BlockLoader;
import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.SeekableLineReaderIterator;
import org.archive.util.iterator.BoundedStringIterator;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.StartBoundedStringIterator;

public class ZipNumCluster extends CDXFile {
	final static Logger LOGGER = 
		Logger.getLogger(ZipNumCluster.class.getName());

	protected String clusterUri;
		
	protected String summaryFile;
	
	protected BlockLoader blockLoader;
	
	protected HashMap<String, String[]> locMap = null;
	
	//protected HashMap<String, SeekableLineReaderFactory> factoryPool = new HashMap<String, SeekableLineReaderFactory>();
	
	public ZipNumCluster(String clusterUri) throws IOException
	{
		this(clusterUri, "ALL.summary");
	}
	
	public ZipNumCluster(String clusterUri, String summaryFile, String locUri) throws IOException {
		this(clusterUri, summaryFile);
		
		loadPartLocations(locUri);
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
		
		this.blockLoader = GeneralURIStreamFactory.createBlockLoader(clusterUri, true);
	}
		
	protected void loadPartLocations(String locUri) throws IOException
	{
		locMap = new HashMap<String, String[]>();
		SeekableLineReaderIterator lines = null;
		
		try {
			SeekableLineReaderFactory readerFactory = GeneralURIStreamFactory.createSeekableStreamFactory(locUri, true);
			
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
	
	protected SeekableLineReaderFactory createFactory(String partName, String[] partLocations) throws IOException
	{					
			// Either load from specified location, or from partName path
		if (partLocations != null && partLocations.length > 0) {
			for (String location : partLocations) {
				try {
					return GeneralURIStreamFactory.createSeekableStreamFactory(location, false);
				} catch (IOException io) {
					continue;
				}
			}
		}
		
		return GeneralURIStreamFactory.createSeekableStreamFactory(partName, false);
	}
	
	protected static int extractLineCount(String line)
	{
		String[] parts = line.split("\t");
		
		if (parts.length < 5) {
			return -1;
		}
		
		int count = -1;
		
		try {
			count = Integer.parseInt(parts[4]);
		} catch (NumberFormatException n) {

		}
		
		return count;
	}
	
	public int getNumLines(String[] blocks)
	{
		if (blocks.length < 2) {
			return 0;
		}
		
		int lastLine = -1;
		int line = -1;
		
		int size = 0;
		
		for (String block : blocks) {
			lastLine = line;
			line = extractLineCount(block);
				
			if (lastLine >= 0) {
				size += (line - lastLine);
			}
		}
		
		return size;
	}
	
	public int getNumLines(String start, String end) throws IOException
	{
		SeekableLineReader slr = null;
		String startLine = null;
		String endLine = null;
		
		int startCount = 0;
		int endCount = 0;
		
		try {
			slr = factory.get();
		
			long[] offsets = getStartEndOffsets(slr, start, end);
			
			if (offsets[0] > 0) {
				slr.seek(offsets[0]);
				slr.readLine();
				
				startLine = slr.readLine();
			}
			
			if (offsets[1] < slr.getSize()) {
				slr.seek(offsets[1]);
				slr.readLine();
			
				endLine = slr.readLine();
			}
			
			if (endLine != null) {
				endCount = extractLineCount(endLine);
			} else {
				//TODO: A bit hacky, try to get last field of last line
				slr.seek(slr.getSize() - 100);
				endLine = slr.readLine();
				int lastSp = endLine.lastIndexOf(' ');
				endCount = Integer.parseInt(endLine.substring(lastSp + 1));
			}
			
			if (startLine != null) {
				startCount = extractLineCount(startLine);
			}
			
		} finally {
			if (slr != null) {
				slr.close();
			}
		}
		
		return endCount - startCount;
	}
	
	//TODO: Experimental?
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
		
//		SummaryBlockIterator blockIter = new SummaryBlockIterator(super.getRecordIteratorLT(key), this);
//		
//		MultiBlockIterator zipIter = new MultiBlockIterator(blockIter);
//				
//		return new StartBoundedStringIterator(zipIter, prefix);
		
		return wrapStartIterator(getCDXIterator(super.getRecordIteratorLT(key)), prefix);
	}
	
	public CloseableIterator<String> getCDXIterator(CloseableIterator<String> summaryIterator)
	{
		SummaryBlockIterator blockIter = new SummaryBlockIterator(summaryIterator, this);
		MultiBlockIterator zipIter = new MultiBlockIterator(blockIter);
		return zipIter;
	}
		
	public String getClusterUri() {
		return clusterUri;
	}
	
	public String getSummaryFile() {
		return summaryFile;
	}
}
