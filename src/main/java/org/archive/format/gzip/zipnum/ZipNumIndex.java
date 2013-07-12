package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.logging.Logger;

import org.archive.format.cdx.CDXInputSource;
import org.archive.util.binsearch.FieldExtractingSLR;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SortedTextFile;
import org.archive.util.iterator.BoundedStringIterator;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.StartBoundedStringIterator;

public class ZipNumIndex implements CDXInputSource {
	final static Logger LOGGER = Logger.getLogger(ZipNumIndex.class.getName());

	protected String pathRoot;
		
	protected String summaryFile;
	protected SortedTextFile summary;
	
	protected ZipNumBlockLoader blockLoader;
	
	// Used only for reference / user info
	protected int cdxLinesPerBlock = 3000;
	
	//protected HashMap<String, String[]> locMap = null;
		
	protected final static boolean DEFAULT_USE_NIO = true;

	private static final int LINE_COUNT_FIELD = 4;
	
	protected boolean useNio = DEFAULT_USE_NIO;
	
	public ZipNumIndex()
	{
		
	}
					
	public void init() throws IOException {
		
		if (summaryFile != null) {
			this.summary = new SortedTextFile(summaryFile, useNio);
		}
						
		if (blockLoader == null) {
			this.blockLoader = new ZipNumBlockLoader();
		}
	}
	
	public static ZipNumIndex createIndexWithSummaryPath(String summaryFile) throws IOException
	{
		ZipNumIndex zipIndex = new ZipNumIndex();
		zipIndex.setSummaryFile(summaryFile);
		zipIndex.init();
		return zipIndex;
	}
	
	public static ZipNumIndex createIndexWithBasePath(String pathRoot) throws IOException
	{
		ZipNumIndex zipIndex = new ZipNumIndex();
		zipIndex.setPathRoot(pathRoot);
		zipIndex.init();
		return zipIndex;
	}
				
	public static int extractLineCount(String line)
	{
		return (int)extractLongField(line, LINE_COUNT_FIELD);
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
	
	public static class PageResult
	{
		final public CloseableIterator<String> iter;
		final public int numPages;
		
		PageResult(CloseableIterator<String> iter, int numPages)
		{
			this.iter = iter;
			this.numPages = numPages;
		}
	}
	
	public PageResult getNthPage(String[] startEnd, int page, int pageSize, boolean numPagesOnly) throws IOException
	{
		String startEndIdx[] = getSummary().getRange(startEnd[0], startEnd[1]);
		
		int firstLineNumber = extractLineCount(startEndIdx[0]);
		int endLineNumber = extractLineCount(startEndIdx[1]) + 1;
		int totalLines = endLineNumber - firstLineNumber;
		
		int numPages = (totalLines / pageSize) + 1;
		
		if (numPagesOnly) {
			return new PageResult(null, numPages);
		}
		
		if (page >= numPages) {
			return new PageResult(null, numPages);
		}
		
		int firstPageLineNumber = (page * pageSize) + firstLineNumber;
		int lastPageLineNumber = Math.min(firstPageLineNumber + pageSize, endLineNumber);
		
		if (page > 0) {
			startEndIdx[0] = getNthLine("" + firstPageLineNumber, LINE_COUNT_FIELD);
		}
		
		boolean endInclusive = false;
		
		if (page < (numPages - 1)) {
			startEndIdx[1] = getNthLine("" + lastPageLineNumber, LINE_COUNT_FIELD);
		} else {
			endInclusive = true;
		}
	
    	CloseableIterator<String> blocklines = getClusterRange(startEndIdx[0], startEndIdx[1], endInclusive, false);
    	return new PageResult(blocklines, numPages);
	}
	
	public String getNthLine(String lineNumber, int lineField) throws IOException
	{
		SeekableLineReader slr = null;
		
		try {
			slr = summary.getSLR();
			FieldExtractingSLR lineCountReader = new FieldExtractingSLR(slr, lineField, "\t");
			
			long offset = summary.binaryFindOffset(lineCountReader, lineNumber, SortedTextFile.numericComparator);
			slr.seek(offset);
			
			if (offset > 0) {
				slr.skipLine();
			}
			
			String fullLine = null;
			String prevLine = null;
			
		    while (true) {
		    	prevLine = fullLine;
		    	fullLine = slr.readLine();
		    	
		    	if (fullLine == null) {
		    		fullLine = prevLine;
		    		break;
		    	}
		    	
		    	String currLineNumber = fullLine.split("\t")[lineField];
		    	
		    	if (SortedTextFile.numericComparator.compare(lineNumber, currLineNumber) <= 0) {
		    		break;
		    	}
		    }
		    
			return fullLine;
			
		} finally {
			if (slr != null) {
				slr.close();
			}
		}
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
	
	public CloseableIterator<String> getCDXIterator(String key, String start, String end, ZipNumParams params) throws IOException {	
		CloseableIterator<String> summaryIter = summary.getRecordIteratorLT(key);
		
		if (params.getTimestampDedupLength() > 0) {
			summaryIter = new TimestampDedupIterator(summaryIter, params.getTimestampDedupLength());
		}
		
		if (end != null && !end.isEmpty()) {
			summaryIter = wrapEndIterator(summaryIter, end, false);
		}
		
		if (blockLoader.isBufferFully() && (params != null) && (params.getMaxBlocks() > 0)) {
			LineBufferingIterator lineBufferIter = new LineBufferingIterator(summaryIter, params.getMaxBlocks());
			lineBufferIter.bufferInput();
			summaryIter = lineBufferIter;
		}
				
		return wrapStartEndIterator(getCDXIterator(summaryIter, params), start, end, false);
	}
	
	
	//TODO: replace with matchType version
	public CloseableIterator<String> getCDXIterator(String key, String start, boolean exact, ZipNumParams params) throws IOException {
		
		CloseableIterator<String> summaryIter = summary.getRecordIteratorLT(key);
		
		if (params.getTimestampDedupLength() > 0) {
			summaryIter = new TimestampDedupIterator(summaryIter, params.getTimestampDedupLength());
		}
		
		summaryIter = wrapPrefix(summaryIter, start, exact);
		
		if (blockLoader.isBufferFully() && (params != null) && (params.getMaxBlocks() > 0)) {
			LineBufferingIterator lineBufferIter = new LineBufferingIterator(summaryIter, params.getMaxBlocks());
			lineBufferIter.bufferInput();
			summaryIter = lineBufferIter;
		}
				
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

	public int getCdxLinesPerBlock() {
		return cdxLinesPerBlock;
	}

	public void setCdxLinesPerBlock(int cdxLinesPerBlock) {
		this.cdxLinesPerBlock = cdxLinesPerBlock;
	}

	SeekableLineReader createReader(String partId) throws IOException {
		if (pathRoot == null) {
			int lastSlash = summaryFile.lastIndexOf('/');
			pathRoot = this.summaryFile.substring(0, lastSlash + 1);
		}
		
		if (!partId.endsWith(".gz")) {
			partId += ".gz";
		}
		
		String gzFile = pathRoot + partId;
		
		return blockLoader.createBlockReader(gzFile);
	}

	public String getPathRoot() {
		return pathRoot;
	}

	public void setPathRoot(String pathRoot) {
		this.pathRoot = pathRoot;
	}
}
