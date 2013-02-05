package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.CloseableIterator;

public class SummaryBlockIterator extends AbstractPeekableIterator<SeekableLineReader>
{
	final static Logger LOGGER = 
		Logger.getLogger(SummaryBlockIterator.class.getName());
		
	class SplitLine
	{
		String line;
		String partId;
		String[] parts;
		
		String timestamp;
		
		long offset;
		int length;
		
		SplitLine(String line)
		{
			this.line = line;
			if (this.line == null) {
				return;
			}
			parts = this.line.split("\t");
			partId = parts[1];
			if (parts.length < 3) {
				return;
			}
			offset = Long.parseLong(parts[2]);
			length = Integer.parseInt(parts[3]);
			timestamp = makeTimestamp(parts[0]);
		}
		
		String makeTimestamp(String key)
		{
			if (params.getTimestampDedupLength() <= 0) {
				return null;
			}
			
			int space = key.indexOf(' ');
			if (space >= 0) {
				return key.substring(0, space + 1 + params.getTimestampDedupLength());
			} else {
				return null;
			}
		}
		
		boolean isContinuous(SplitLine next)
		{
			if (next == null || next.line == null) {
				return false;
			}
			
			// Must be same part
			if (!partId.equals(next.partId)) {
				return false;
			}
			
			if ((offset + length) != next.offset) {
				return false;
			}
			
			return true;
		}
		
		boolean sameTimestamp(SplitLine next)
		{
			if (next == null || next.timestamp == null) {
				return false;
			}
			
			if (timestamp == null) {
				return false;
			}
			
			return timestamp.equals(next.timestamp);
		}
	}
	
	protected CloseableIterator<String> summaryIterator;
	
	protected ZipNumCluster cluster;
	
	protected SeekableLineReader currReader = null;
	
	protected SplitLine nextLine, currLine;
	
	protected boolean isFirst = true;
	
	protected String currPartId = null;
	
	protected int totalBlocks = 0;
	
	protected final ZipNumParams params;	

	public SummaryBlockIterator(CloseableIterator<String> summaryIterator, ZipNumCluster cluster, ZipNumParams params)
	{
		this.cluster = cluster;
		
		this.summaryIterator = summaryIterator;
		this.isFirst = true;
		
		if (params != null) {
			this.params = params;
		} else {
			this.params = new ZipNumParams();
		}
	}
	
	@Override
	public SeekableLineReader getNextInner() {
					
		if (isFirst) {
			if (summaryIterator.hasNext()) {
				nextLine = new SplitLine(summaryIterator.next());
			}
			isFirst = false;
		}
		
		if ((params.getMaxBlocks() > 0) && (totalBlocks >= params.getMaxBlocks())) {
			return null;
		}
		
		try {
			
			int numBlocks = 0;
			int maxAggregateBlocks = params.getMaxAggregateBlocks();
			
			long startOffset = 0;
			int totalLength = 0;
		
			do {					
				currLine = nextLine;
				
				if (currLine == null) {
					isFirst = false;
					return null;
				}
				
				if (summaryIterator.hasNext()) {
					nextLine = new SplitLine(summaryIterator.next());
				} else {
					nextLine = null;
				}
			
				if (currLine.parts.length < 3) {
					LOGGER.severe("Bad line(" + currLine.line +") ");
					return null;
				}
				
				if (currLine.sameTimestamp(nextLine)) {
					if (numBlocks == 0) {
						continue;
					} else {
						break;
					}
				}
	
				if (initReader(currLine.partId) || (numBlocks == 0)) {
					startOffset = currLine.offset;
					totalLength = 0;					
				}
				
				totalLength += currLine.length;
				numBlocks++;
				
			} while (((maxAggregateBlocks <= 0) || (numBlocks < maxAggregateBlocks)) && 
					  ((params.getMaxBlocks() <= 0) || (totalBlocks + numBlocks) < params.getMaxBlocks()) 
					  && currLine.isContinuous(nextLine));
			
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Loading " + numBlocks + " blocks - " + startOffset + ":" + totalLength + " from " + currPartId);
			}
			
			currReader.seekWithMaxRead(startOffset, true, totalLength);
			
			totalBlocks += numBlocks;
				
		} catch (IOException io) {
			LOGGER.severe(io.toString());
			if (currReader != null) {
				try {
					currReader.close();
				} catch (IOException e) {
	
				}
				currReader = null;
			}
		}
		
		return currReader;
	}
		
	protected boolean initReader(String partId) throws IOException
	{
		if ((currReader == null) || (currPartId == null) || !currPartId.equals(partId)) {
			
			if (currReader != null) {
				currReader.close();
				currReader = null;
			}
			
			if (cluster.locationUpdater != null) {
				initLocationReader(partId);
			}
			
			if (currReader == null) {
				String partUrl = cluster.getClusterPart(partId);
				currReader = cluster.blockLoader.createBlockReader(partUrl);	
			}
			
			currPartId = partId;			
			return true;
		}
		
		return false;
	}
	
	protected void initLocationReader(String partId)
	{
		String[] locations = cluster.locationUpdater.getLocations(partId);
		
		if (locations == null) {
			LOGGER.severe("No locations for block(" + partId +")");
		} else if (locations != null && locations.length > 0) {
			for (String location : locations) {
				try {
					currReader = cluster.blockLoader.createBlockReader(location);
					break;
				} catch (IOException io) {
					continue;
				}
			}
		}
	}
	
	@Override
	public void close() throws IOException
	{
		if (summaryIterator != null) {
			summaryIterator.close();
			summaryIterator = null;
		}
		
		if (currReader != null) {
			currReader.close();
			currReader = null;
		}
	}
}