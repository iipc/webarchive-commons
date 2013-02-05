package org.archive.util.binsearch;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.util.iterator.CloseableIterator;

public class SortedTextFile {
	private final static Logger LOGGER =
		Logger.getLogger(SortedTextFile.class.getName());
	
	protected SeekableLineReaderFactory factory;
	
	public SortedTextFile(SeekableLineReaderFactory factory) {
		setFactory(factory);
	}
	
	protected SortedTextFile()
	{
		this.factory = null;
	}
	
	protected void setFactory(SeekableLineReaderFactory factory)
	{
		this.factory = factory;
	}

	public CloseableIterator<String> getRecordIteratorLT(final String prefix) 
	throws IOException {
		return getRecordIterator(prefix, true);
	}

	public CloseableIterator<String> getRecordIterator(final String prefix) 
	throws IOException {
		return getRecordIterator(prefix, false);
	}
	
	public CloseableIterator<String> getRecordIterator(final long offset) throws IOException
	{
		SeekableLineReader slr = factory.get();
		slr.seek(offset);
		return new SeekableLineReaderIterator(slr);
	}

	public CloseableIterator<String> getRecordIterator(final String prefix, 
			boolean lessThan) throws IOException {
		return search(factory.get(),prefix,lessThan);
	}
	
	protected long findOffset(SeekableLineReader slr, final String key) throws IOException
	{
		int blockSize = SeekableLineReaderFactory.BINSEARCH_BLOCK_SIZE;
		long fileSize = slr.getSize();
		long min = 0;
		long max = (long) fileSize / blockSize;
		long mid;
		String line;

		// TODO: implement a cache of midpoints - will make a HUGE difference
		//       on both HTTP and HDFS
	    while (max - min > 1) {
	    	mid = min + (long)((max - min) / 2);
	    	slr.seek(mid * blockSize);
	    	if(mid > 0) line = slr.readLine(); // probably a partial line
	    	line = slr.readLine();
	    	if (key.compareTo(line) > 0) {

	    		if(LOGGER.isLoggable(Level.FINE)) {
	    			LOGGER.fine(String.format("Search(%d) (%s)/(%s) : After",
	    					mid * blockSize, key,line));
	    		}
	    		min = mid;
	    	} else {

	    		if(LOGGER.isLoggable(Level.FINE)) {
	    			LOGGER.fine(String.format("Search(%d) (%s)/(%s) : Before",
					mid * blockSize, key,line));
	    		}
	    		max = mid;
	    	}
	    }
	    // find the right line
	    min = min * blockSize;
	    return min;
	}
	
	protected long[] getStartEndOffsets(SeekableLineReader slr, String start, String end) throws IOException
	{
		long endOffset = 0;
		
		if ((end != null) && !end.isEmpty()) {
			//endOffset = this.findOffset(slr, end);
			endOffset = this.searchOffset(slr, end, false);
		} else {
			endOffset = slr.getSize();
		}
		
		long startOffset = 0;
		
		if ((start != null) && !start.isEmpty()) {
			startOffset = this.searchOffset(slr, start, true);
		}
		
		return new long[]{startOffset, endOffset};
	}
	

//	public CloseableIterator<String> getSplitIterator(long startOffset, long endOffset, int numSplits) throws IOException
//	{
//		SeekableLineReader slr = factory.get();		
//		return new StepSeekingIterator(slr, startOffset, endOffset, numSplits);
//	}
	
	public CloseableIterator<String> getSplitIterator(String start, String end, int numSplits) throws IOException
	{
		SeekableLineReader slr = factory.get();
		
		long[] offsets = getStartEndOffsets(slr, start, end);
		
		return new StepSeekingIterator(slr, offsets[0], offsets[1], numSplits);
	}
	
	public String[] getNthSplit(String start, String end, int split, int numSplits) throws IOException
	{
		SeekableLineReader slr = null;
		String startLine = null;
		String endLine = null;
		
		try {
			slr = factory.get();
			
			long[] offsets = getStartEndOffsets(slr, start, end);
			long startOffset = offsets[0];
			long diff = offsets[1] - offsets[0];
				
			long seekDiff = (diff * split) / numSplits;
			
			slr.seek(startOffset + seekDiff);
			
			if ((startOffset + seekDiff) > 0) {
				slr.readLine();
			}
			
			startLine = slr.readLine();
			endLine = null;
			
			if (split <= (numSplits - 1)) {
				seekDiff = (diff * (split + 1)) / numSplits;
				slr.seek(startOffset + seekDiff);
				slr.readLine();
				endLine = slr.readLine();
			} else {
				endLine = end;
			}
		} finally {
			if (slr != null) {
				slr.close();
			}
		}
		
		return new String[]{startLine, endLine};
	}
	
	class StepSeekingIterator implements CloseableIterator<String>
	{
		long startOffset;
		int numSplits;
		long endOffset;
		int currSplit;
		SeekableLineReader slr;
		
		public StepSeekingIterator(SeekableLineReader slr, long startOffset, long endOffset, int numSplits) throws IOException
		{
			this.slr = slr;
			this.currSplit = 0;
			this.startOffset = startOffset;
			this.numSplits = numSplits;
			this.endOffset = endOffset;
			
			slr.seek(startOffset);
		}

		public boolean hasNext() {
			return (currSplit < numSplits);
		}

		public String next() {
			
			String line = null;
			
			try {				
				if (startOffset + currSplit != 0) {
					slr.readLine();
				}
				
				line = slr.readLine();
				
				currSplit++;
				long seekDiff = ((endOffset - startOffset) * currSplit) / numSplits;
				
				slr.seek(startOffset + seekDiff);
				
			} catch (IOException io) {
				io.printStackTrace();
			}
			
			return line;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void close() throws IOException {
			slr.close();
		}
	}
	
	private long searchOffset(SeekableLineReader slr, 
			final String key, boolean lessThan) throws IOException {

		long offset = findOffset(slr, key);

	    slr.seek(offset);
	    
	    String line = null;
	    
	    if (offset > 0) {
	    	line = slr.readLine();
	    }
	    
	    String prev = null;
	    while(true) {
	    	if (line != null) {
	    		offset += line.getBytes().length + 1;
	    	}
	    	line = slr.readLine();
	    	if(line == null) break;
	    	if(line.compareTo(key) >= 0) break;
	    	prev = line;
	    }
	    
	    if (!lessThan) {
	    	prev = null;
	    } else {
	    	offset -= prev.getBytes().length + 1;
	 	}
	    
	    // To allow for skipping the line, in case we're not on the boundary
	    return (offset - 2);
	}
	
	private CloseableIterator<String> search(SeekableLineReader slr, 
			final String key, boolean lessThan) throws IOException {

		long min = findOffset(slr, key);

		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine(String.format("Aligning(%d)",min));
		}

	    slr.seek(min);
	    
	    String line;
	    
	    if(min > 0) line = slr.readLine();
	    String prev = null;
	    while(true) {
	    	line = slr.readLine();
	    	if(line == null) break;
	    	if(line.compareTo(key) >= 0) break;
	    	prev = line;
	    }
	    if (!lessThan) {
	    	prev = null;
	    }
	    
    
    	return new CachedStringIterator(slr, prev, line);
	}
	
	public class CachedStringIterator implements CloseableIterator<String> {
		private String first;
		private String second;
		private SeekableLineReader slr;
		private SeekableLineReaderIterator it;
		public CachedStringIterator(SeekableLineReader slr, String first, String second) {
			this.slr = slr;
			this.first = first;
			this.second = second;
			it = new SeekableLineReaderIterator(slr);
		}
		public boolean hasNext() {
			if(first != null) {
				return true;
			}
			if(second != null) {
				return true;
			}
			return it.hasNext();
		}

		public String next() {
			if(first != null) {
				String tmp = first;
				first = null;
				return tmp;
			}
			if(second != null) {
				String tmp = second;
				second = null;
				return tmp;
			}
			
			return it.next();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void close() throws IOException {
			slr.close();
		}
	}
	

}
