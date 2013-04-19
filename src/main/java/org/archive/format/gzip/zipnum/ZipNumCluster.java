package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.logging.Logger;

import org.archive.format.cdx.CDXInputSource;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SortedTextFile;
import org.archive.util.iterator.BoundedStringIterator;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.StartBoundedStringIterator;

public class ZipNumCluster implements CDXInputSource {
	final static Logger LOGGER = Logger.getLogger(ZipNumCluster.class.getName());

	private String clusterRoot;
		
	protected String summaryFile;
	protected SortedTextFile summary;
	
	protected String locFile;
	
	protected ZipNumBlockLoader blockLoader;
	
	//protected HashMap<String, String[]> locMap = null;
	protected LocationUpdater locationUpdater = null;
		
	protected final static boolean DEFAULT_USE_NIO = true;
	
	protected boolean useNio = DEFAULT_USE_NIO;
	
	protected final static CloseableIterator<String> EMPTY_ITERATOR = new CloseableIterator<String>()
	{
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public String next() {
			return null;
		}

		@Override
		public void remove() {
			
		}

		@Override
		public void close() throws IOException {
			
		}	
	};
	
	public ZipNumCluster()
	{
		
	}
	
	public ZipNumCluster(String clusterUri) throws IOException
	{
		this.clusterRoot = clusterUri;
	}
					
	public void init() throws IOException {
		
		if (summaryFile != null) {
			this.summary = new SortedTextFile(summaryFile, useNio);
		}
						
		if (blockLoader == null) {
			this.blockLoader = new ZipNumBlockLoader();
		}
		
		if (locFile != null) {
			this.locationUpdater = new LocationUpdater(locFile, this.blockLoader);
		}
	}
				
	protected static int extractLineCount(String line)
	{
		return (int)extractLongField(line, 4);
	}
	
	protected static long extractLongField (String line, int index)
	{
		String[] parts = line.split("\t");
		
		if (parts.length <= index) {
			return -1;
		}
		
		long count = -1;
		
		try {
			count = Long.parseLong(parts[index]);
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
		
		if (!partId.endsWith(".gz")) {
			partId += ".gz";
		}
		
		return clusterRoot + partId;
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
	
	public long getTotalLength(String[] blocks)
	{	
		long size = 0;
		
		for (String block : blocks) {
			size += extractLongField(block, 3);
		}
		
		return size;
	}
	
	// Adjust from shorter blocks, if loaded
	public long getTotalLines(int cdxPerBlock)
	{
		if (locationUpdater == null) {
			return 0;
		}
		
		long numLines = 0;
		
		try {
			numLines = this.getNumLines(summary.getRange("", ""));
		} catch (IOException e) {
			LOGGER.warning(e.toString());
			return 0;
		}
		
		long adjustment = locationUpdater.getTotalAdjustment();
		numLines -= (locationUpdater.getNumBlocks() - 1);
		numLines *= cdxPerBlock;
		numLines += adjustment;
		return numLines;
	}
	
	public long getLastBlockDiff(String startKey, int startPart, int endPart, int cdxPerBlock)
	{
		if (locationUpdater == null) {
			return 0;
		}
		
		return locationUpdater.computeLastBlockDiff(startKey, startPart, endPart, cdxPerBlock);
	}
	
	public int getNumLines(String start, String end) throws IOException
	{
		SeekableLineReader slr = null;
		String startLine = null;
		String endLine = null;
		
		int startCount = 0;
		int endCount = 0;
		
		try {
			slr = summary.getSLR();
		
			long[] offsets = summary.getStartEndOffsets(slr, start, end);
			
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
			
			// Get the last line
			if (endLine == null) {
				endLine = summary.getLastLine(slr);
			}
			
			if (endLine != null) {
				endCount = extractLineCount(endLine);
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
		iter = summary.getRecordIterator(start, includePrevLine);
		return wrapEndIterator(iter, end, inclusive);
		//return wrapStartEndIterator(iter, start, end, inclusive);
	}
	
	public CloseableIterator<String> wrapStartEndIterator(CloseableIterator<String> iter, String start, String end, boolean inclusive)
	{
		return wrapEndIterator(wrapStartIterator(iter, start), end, inclusive);
	}
	
	public static CloseableIterator<String> wrapStartIterator(CloseableIterator<String> iter, String start)
	{
		return new StartBoundedStringIterator(iter, start);
	}
	
	public static CloseableIterator<String> wrapEndIterator(CloseableIterator<String> iter, String end, boolean inclusive)
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
		
		if ((split == 0) && (start != null) && !start.isEmpty()) {
			blocklines = wrapStartIterator(blocklines, start);
		}
		
		if ((split >= (numSplits - 1)) && (end != null) && !end.isEmpty()) {
			blocklines = wrapEndIterator(blocklines, end, false);
		}
		
		return blocklines;
	}
	
	public static String endKey(String key)
	{
		return key + "!";
	}
	
	public CloseableIterator<String> getLastBlockCDXLineIterator(String key) throws IOException {
		// the next line after last key<space> is key! so this will return last key<space> block
		CloseableIterator<String> summaryIter = summary.getRecordIteratorLT(endKey(key));
		
		return wrapStartIterator(getCDXIterator(summaryIter), key);
	}
	
	public static CloseableIterator<String> wrapPrefix(CloseableIterator<String> source, String prefix, boolean exact)
	{
		if (exact) {
			return wrapEndIterator(source, endKey(prefix), false);
		} else {
			return wrapEndIterator(source, prefix, true);
		}
	}
				
	public CloseableIterator<String> getCDXIterator(String key, String start, boolean exact, ZipNumParams params) throws IOException {
		
		if ((locationUpdater != null) && !locationUpdater.dateRangeCheck(key)) {
			return EMPTY_ITERATOR;
		}
		
		CloseableIterator<String> summaryIter = summary.getRecordIteratorLT(key);
		
		if (params.getTimestampDedupLength() > 0) {
			summaryIter = new TimestampDedupIterator(summaryIter, params.getTimestampDedupLength());
		}
		
		if (blockLoader.isBufferFully() && (params != null) && (params.getMaxBlocks() > 0)) {
			LineBufferingIterator lineBufferIter = new LineBufferingIterator(summaryIter, params.getMaxBlocks());
			lineBufferIter.bufferInput();
			summaryIter = lineBufferIter;
		}
		
		summaryIter = wrapPrefix(summaryIter, start, exact);
				
		return wrapStartIterator(getCDXIterator(summaryIter, params), start);
	}
	
	public CloseableIterator<String> getCDXIterator(String key, ZipNumParams params) throws IOException {
		
		CloseableIterator<String> summaryIter = summary.getRecordIteratorLT(key);		
		return wrapStartIterator(getCDXIterator(summaryIter, params), key);
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
	
	public SortedTextFile getSummary()
	{
		return summary;
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

	public boolean isDisabled() {
		if (locationUpdater != null) {
			return locationUpdater.isDisabled;
		}
		
		return false;
	}
}
