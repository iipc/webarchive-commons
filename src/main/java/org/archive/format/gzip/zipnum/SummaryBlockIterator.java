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
		
	protected CloseableIterator<String> summaryIterator;
	
	protected ZipNumIndex zipnumIndex;
	
	//protected SeekableLineReader currReader = null;
	
	protected SummaryLine nextLine, currLine;
	
	protected boolean isFirst = true;
	
	//protected String currPartId = null;
	
	protected int totalBlocks = 0;
	
	protected final ZipNumParams params;	

	public SummaryBlockIterator(CloseableIterator<String> summaryIterator, ZipNumIndex zipnumIndex, ZipNumParams params)
	{
		this.zipnumIndex = zipnumIndex;
		
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
				nextLine = new SummaryLine(summaryIterator.next());
			}
			isFirst = false;
		}
		
		if (nextLine == null) {
			return null;
		}
		
		if ((params.getMaxBlocks() > 0) && (totalBlocks >= params.getMaxBlocks())) {
			return null;
		}
		
		SeekableLineReader currReader = null;
		
		try {
			
			int numBlocks = 0;
			int maxAggregateBlocks = params.getMaxAggregateBlocks();
			
			long startOffset = nextLine.offset;
			String currPartId = nextLine.partId;
			
			int totalLength = 0;
		
			do {					
				currLine = nextLine;
				
				if (currLine == null) {
					return null;
				}
				
				if (summaryIterator.hasNext()) {
					nextLine = new SummaryLine(summaryIterator.next());
				} else {
					nextLine = null;
				}
			
				if (currLine.getNumFields() < 3) {
					LOGGER.severe("Bad line(" + currLine.toString() +") ");
					return null;
				}
				
//				if (currLine.sameTimestamp(nextLine)) {
//					if (numBlocks == 0) {
//						continue;
//					} else {
//						break;
//					}
//				}
	
//				if ((currPartId == null) || !currPartId.equals(currLine.partId) || (numBlocks == 0)) {
//					startOffset = currLine.offset;
//					totalLength = 0;
//					currPartId = currLine.partId;
//				}
				
				totalLength += currLine.length;
				numBlocks++;
				
			} while (((maxAggregateBlocks <= 0) || (numBlocks < maxAggregateBlocks)) && 
					  ((params.getMaxBlocks() <= 0) || (totalBlocks + numBlocks) < params.getMaxBlocks()) 
					  && currLine.isContinuous(nextLine));
			
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("Loading " + numBlocks + " blocks - " + startOffset + ":" + totalLength + " from " + currPartId);
			}
			
			//currReader = initReader(currPartId);
			currReader = zipnumIndex.createReader(currPartId);
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
		
//	protected SeekableLineReader initReader(String partId) throws IOException
//	{
//		//if ((currReader == null) || (currPartId == null) || !currPartId.equals(partId)) {
//			
////		if (currReader != null) {
////			currReader.close();
////			currReader = null;
////		}
//		
//		SeekableLineReader currReader = null;
//		
//		if (cluster.locationUpdater != null) {
//			currReader = initLocationReader(partId);
//		}
//		
//		if (currReader == null) {
//			String partUrl = cluster.getClusterPart(partId);
//			currReader = cluster.blockLoader.createBlockReader(partUrl);
//		}
//		
//		return currReader;
//	}
//	
//	protected SeekableLineReader initLocationReader(String partId)
//	{
//		String[] locations = cluster.locationUpdater.getLocations(partId);
//		
//		if (locations == null) {
//			LOGGER.severe("No locations for block(" + partId +")");
//		} else if (locations != null && locations.length > 0) {
//			for (String location : locations) {
//				try {
//					return cluster.blockLoader.createBlockReader(location);
//				} catch (IOException io) {
//					continue;
//				}
//			}
//		}
//		
//		return null;
//	}
	
	@Override
	public void close() throws IOException
	{
		if (summaryIterator != null) {
			summaryIterator.close();
			summaryIterator = null;
		}
		
//		if (currReader != null) {
//			currReader.close();
//			currReader = null;
//		}
	}
}