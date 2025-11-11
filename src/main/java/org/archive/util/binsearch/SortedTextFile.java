package org.archive.util.binsearch;

import java.io.IOException;
import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.iterator.CloseableIterator;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SortedTextFile {
	
	public static class NumericComparator implements Comparator<String>
	{
		@Override
		public int compare(String arg0, String arg1) {
			long val0 = Long.parseLong(arg0);
			long val1 = Long.parseLong(arg1);
			
			if (val0 < val1) {
				return -1;
			} else if (val0 == val1) {
				return 0;
			} else {
				return 1;
			}
		}
	};
	
	public static class DefaultComparator implements Comparator<String>
	{
		@Override
		public int compare(String arg0, String arg1) {
			return arg0.compareTo(arg1);
		}
	};
	
	public final static Comparator<String> numericComparator = new NumericComparator();
	public final static Comparator<String> defaultComparator = new DefaultComparator();
	
	private final static Logger LOGGER =
		Logger.getLogger(SortedTextFile.class.getName());
	
	protected SeekableLineReaderFactory factory;
	protected int binsearchBlockSize = SeekableLineReaderFactory.BINSEARCH_BLOCK_SIZE;
	
	public SortedTextFile(SeekableLineReaderFactory factory) {
		setFactory(factory);
	}
	
	public SortedTextFile(String filename) throws IOException
	{
		this(filename, true);
	}
	
	public SortedTextFile(String filename, boolean useNio) throws IOException
	{
		this.factory = GeneralURIStreamFactory.createSeekableStreamFactory(filename, useNio);
	}
	
	protected SortedTextFile()
	{
		this.factory = null;
	}
	
	protected void setFactory(SeekableLineReaderFactory factory)
	{
		this.factory = factory;
	}
	
	public void reloadFactory()
	{
		try {
	        this.factory.reload();
        } catch (IOException e) {
        	LOGGER.warning(e.toString());
        }
	}

	public int getBinsearchBlockSize() {
        return binsearchBlockSize;
    }

    public void setBinsearchBlockSize(int binsearchBlockSize) {
        this.binsearchBlockSize = binsearchBlockSize;
    }

    public CloseableIterator<String> getRecordIteratorLT(final String prefix) 
	throws IOException {
		return getRecordIterator(prefix, true);
	}

	public CloseableIterator<String> getRecordIterator(final String prefix) 
	throws IOException {
		return getRecordIterator(prefix, false);
	}
	
	public SeekableLineReader getSLR() throws IOException
	{
		return factory.get();
	}
	
	public CloseableIterator<String> getRecordIterator(final long offset) throws IOException
	{
		SeekableLineReader slr = factory.get();
		slr.seek(offset);
		return new SeekableLineReaderIterator(slr);
	}

	public CloseableIterator<String> getRecordIterator(final String prefix, 
			boolean lessThan) throws IOException {
		
		SeekableLineReader slr = factory.get();
		
		try {
			return search(slr, prefix, lessThan, defaultComparator);
		} catch (IOException io) {
			if (slr != null) {
				slr.close();
			}
			throw io;
		}
	}
	
	public long binaryFindOffset(SeekableLineReader slr, final String key, Comparator<String> comparator) throws IOException
	{
		int blockSize = binsearchBlockSize;
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
	    	if(mid > 0) slr.skipLine(); // probably a partial line
	    	line = slr.readLine();
	    	
	    	if (comparator.compare(key, line) > 0) {

	    		if(LOGGER.isLoggable(Level.FINE)) {
	    			LOGGER.fine(String.format(Locale.ROOT, "Search(%d) (%s)/(%s) : After",
	    					mid * blockSize, key,line));
	    		}
	    		min = mid;
	    	} else {

	    		if(LOGGER.isLoggable(Level.FINE)) {
	    			LOGGER.fine(String.format(Locale.ROOT, "Search(%d) (%s)/(%s) : Before",
					mid * blockSize, key,line));
	    		}
	    		max = mid;
	    	}
	    }
	    // find the right line
	    min = min * blockSize;
	    return min;
	}
	
	public long[] getStartEndOffsets(SeekableLineReader slr, String start, String end) throws IOException
	{
		long endOffset = 0;
		
		if ((end != null) && !end.isEmpty()) {
			//endOffset = this.findOffset(slr, end);
			endOffset = this.searchOffset(slr, end, false, defaultComparator);
		} else {
			endOffset = slr.getSize();
		}
		
		long startOffset = 0;
		
		if ((start != null) && !start.isEmpty()) {
			startOffset = this.searchOffset(slr, start, true, defaultComparator);
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
	
	public String[] getRange(String start, String end) throws IOException
	{
		SeekableLineReader slr = null;
		String startLine = null;
		String endLine = null;		
		
		try {
			slr = factory.get();
			
			if (start.isEmpty()) {
				slr.seek(0);
				startLine = slr.readLine();
			} else {
				startLine = search(slr, start, true, defaultComparator).next();
			}
			
			if (end.isEmpty()) {
				endLine = getLastLine(slr);
			} else {
				endLine = search(slr, end, true, defaultComparator).next();
			}
			
		} finally {
			if (slr != null) {
				slr.close();
			}
		}
		
		return new String[]{startLine, endLine};		
	}
	
	// end exclusive
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
				slr.skipLine();
			}
			
			startLine = slr.readLine();
			endLine = null;
			
			if (split <= (numSplits - 1)) {
				seekDiff = (diff * (split + 1)) / numSplits;
				slr.seek(startOffset + seekDiff);
				slr.skipLine();
				endLine = slr.readLine();
			} else {
				endLine = end;
			}

			// Last line
			if (endLine == null) {
				endLine = getLastLine(slr);
			}
			
		} finally {
			if (slr != null) {
				slr.close();
			}
		}
		
		return new String[]{startLine, endLine};
	}
	
	public String getLastLine(SeekableLineReader slr) throws IOException
	{
		int lastLineLenTest = 0;
		int lastLineLenInc = 400;
		String endLine = null;
		
		do {
			
			lastLineLenTest += lastLineLenInc;
			
			slr.seek(slr.getSize() - lastLineLenTest); // TODO: assume larger buffer
			slr.readLine(); // skip partial line
			String nextLine = null;
			endLine = null;
			
			while ((nextLine = slr.readLine()) != null) {
				endLine = nextLine;
			}
			
		} while (endLine == null);
		
		return endLine;
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
					slr.skipLine();
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
			final String key, boolean lessThan, Comparator<String> comparator) throws IOException {

		long offset = binaryFindOffset(slr, key, comparator);

	    slr.seek(offset);
	    
	    String line = null;
	    
	    if (offset > 0) {
	    	slr.skipLine();
	    }
	    
	    String prev = null;
	    while(true) {
	    	if (line != null) {
	    		offset += line.getBytes(UTF_8).length + 1;
	    	}
	    	line = slr.readLine();
	    	if(line == null) break;
	    	if(comparator.compare(line, key) >= 0) break;
	    	prev = line;
	    }
	    
	    if (lessThan && prev != null) {
	    	offset -= prev.getBytes(UTF_8).length + 1;
	    }
	    
	    return offset;
	}
	
	private CloseableIterator<String> search(SeekableLineReader slr, 
			final String key, boolean lessThan, Comparator<String> comparator) throws IOException {

		long min = binaryFindOffset(slr, key, comparator);

		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine(String.format(Locale.ROOT, "Aligning(%d)",min));
		}

	    slr.seek(min);
	    
	    String line;
	    
	    if (min > 0) {
	    	slr.skipLine();
	    }
	    String prev = null;
	    while(true) {
	    	line = slr.readLine();
	    	if (line == null) break;
	    	if (comparator.compare(line, key) >= 0) break;
	    	prev = line;
	    }
	    if (!lessThan) {
	    	prev = null;
	    }
	    
    
    	return new CachedStringIterator(slr, prev, line);
	}
	
	public static class CachedStringIterator implements CloseableIterator<String> {
		private String first;
		private String second;
		private SeekableLineReader slr;
		private SeekableLineReaderIterator it;
		
		public CachedStringIterator(String first, String second) {
			this.slr = null;
			this.first = first;
			this.second = second;
		}
		
		public CachedStringIterator(SeekableLineReader slr, String first, String second) {
			this.slr = slr;
			this.first = first;
			this.second = second;
			if (slr != null) {
				it = new SeekableLineReaderIterator(slr);
			}
		}
		
		public boolean hasNext() {
			if(first != null) {
				return true;
			}
			if(second != null) {
				return true;
			}
			if (it == null) {
				return false;
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
			if (it == null) {
				return null;
			}			
			return it.next();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void close() throws IOException {
			if (slr != null) {
				slr.close();
			}
		}
	}
	

}
