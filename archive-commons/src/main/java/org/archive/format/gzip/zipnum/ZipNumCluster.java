package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.logging.Logger;

import org.archive.format.cdx.CDXInputSource;
import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SortedTextFile;
import org.archive.util.iterator.BoundedStringIterator;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.StartBoundedStringIterator;

public class ZipNumCluster extends SortedTextFile implements CDXInputSource {
	final static Logger LOGGER = Logger.getLogger(ZipNumCluster.class.getName());

	private String clusterRoot;
		
	protected String summaryFile;
	protected String locFile;
	
	protected ZipNumBlockLoader blockLoader;
	
	//protected HashMap<String, String[]> locMap = null;
	protected LocationUpdater locationUpdater = null;
		
	protected final static boolean DEFAULT_USE_NIO = true;
	
	protected boolean useNio = DEFAULT_USE_NIO;
	
	public ZipNumCluster()
	{
		
	}
	
	public ZipNumCluster(String summaryFile) throws IOException
	{
		this.setSummaryFile(summaryFile);
		init();
	}
					
	public void init() throws IOException {
		
		super.setFactory(GeneralURIStreamFactory.createSeekableStreamFactory(summaryFile, useNio));
				
		if (locFile != null) {
			this.locationUpdater = new LocationUpdater(locFile);
		}
		
		if (blockLoader == null) {
			this.blockLoader = new ZipNumBlockLoader();
		}
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
	
	public String getClusterPart(String partId)
	{
		if (clusterRoot == null) {
			int lastSlash = summaryFile.lastIndexOf('/');
			clusterRoot = this.summaryFile.substring(0, lastSlash + 1);
		}
		
		return clusterRoot + partId + ".gz";
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
	
	public CloseableIterator<String> getCDXIterator(CloseableIterator<String> summaryIterator, String start, String end, int split, int numSplits)	
	{
		return getCDXIterator(summaryIterator, start, end, split, numSplits, null);
	}

	public CloseableIterator<String> getCDXIterator(CloseableIterator<String> summaryIterator, String start, String end, int split, int numSplits, ZipNumParams params)	
	{
		CloseableIterator<String> blocklines = this.getCDXIterator(summaryIterator, params);
		
		if (split == 0) {
			blocklines = this.wrapStartIterator(blocklines, start);
		}
		
		if (split >= (numSplits - 1)) {
			blocklines = this.wrapEndIterator(blocklines, end, false);
		}
		
		return blocklines;
	}
	
	public static String endKey(String key)
	{
		return key + "!";
	}
	
	public CloseableIterator<String> getLastBlockCDXLineIterator(String key) throws IOException {
		// the next line after last key<space> is key! so this will return last key<space> block
		CloseableIterator<String> summaryIter = super.getRecordIteratorLT(endKey(key));
		
		return wrapStartIterator(getCDXIterator(summaryIter), key);
	}
	
	
	//TODO: for CDXInputSource... this interface needs rethinking
	public CloseableIterator<String> getCDXLineIterator(String key, String prefix) throws IOException
	{
		return getCDXIterator(key, prefix, false, null);
	}
			
	public CloseableIterator<String> getCDXIterator(String key, String start, boolean exact, ZipNumParams params) throws IOException {
		
		CloseableIterator<String> summaryIter = super.getRecordIteratorLT(key);
		
		if (blockLoader.isBufferFully() && (params != null) && (params.getMaxBlocks() > 0)) {
			summaryIter = new LineBufferingIterator(summaryIter, params.getMaxBlocks());
		}
		
		if (exact) {
			summaryIter = wrapEndIterator(summaryIter, endKey(start), false);
		} else {
			summaryIter = wrapEndIterator(summaryIter, start, true);
		}
		
		return wrapStartIterator(getCDXIterator(summaryIter, params), start);
	}
	
	public CloseableIterator<String> getCDXIterator(CloseableIterator<String> summaryIterator, ZipNumParams params)
	{
		SummaryBlockIterator blockIter = new SummaryBlockIterator(summaryIterator, this, params);
		MultiBlockIterator zipIter = new MultiBlockIterator(blockIter);
		return zipIter;
	}
	
	public CloseableIterator<String> getCDXIterator(CloseableIterator<String> summaryIterator)
	{
		return getCDXIterator(summaryIterator, null);
	}
	
	public void setSummaryFile(String summaryFile) {
		this.summaryFile = summaryFile;
	}

	public String getSummaryFile() {
		return summaryFile;
	}

	public ZipNumBlockLoader getBlockLoader() {
		return blockLoader;
	}

	public void setBlockLoader(ZipNumBlockLoader blockLoader) {
		this.blockLoader = blockLoader;
	}

	public boolean isUseNio() {
		return useNio;
	}

	public void setUseNio(boolean useNio) {
		this.useNio = useNio;
	}

	public String getLocFile() {
		return locFile;
	}

	public void setLocFile(String locFile) {
		this.locFile = locFile;
	}
}
