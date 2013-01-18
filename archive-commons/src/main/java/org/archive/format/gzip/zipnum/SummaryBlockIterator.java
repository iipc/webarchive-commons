package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.logging.Logger;

import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.CloseableIterator;

public class SummaryBlockIterator extends AbstractPeekableIterator<SeekableLineReader>
{
	final static Logger LOGGER = 
		Logger.getLogger(SummaryBlockIterator.class.getName());
	
	protected CloseableIterator<String> summaryIterator;
	
	protected ZipNumCluster cluster;
	
	protected SeekableLineReader currReader = null;
	
	protected String currLine;
	protected String nextLine;
	protected boolean isFirst = true;
	
	protected String currPartId = null;
					
	public SummaryBlockIterator(CloseableIterator<String> summaryIterator, ZipNumCluster cluster)
	{
		this.cluster = cluster;
		
		this.summaryIterator = summaryIterator;
		this.isFirst = true;
	}

	@Override
	public SeekableLineReader getNextInner() {
					
		if (isFirst) {
			if (summaryIterator.hasNext()) {
				nextLine = summaryIterator.next();
			}
		}
				
		currLine = nextLine;
		
		if (summaryIterator.hasNext()) {
			nextLine = summaryIterator.next();
		} else {
			nextLine = null;
		}
		
		String blockDescriptor = currLine;
		
		String parts[] = blockDescriptor.split("\t");
	
		if ((parts.length < 3) || (parts.length > 4)) {
			ZipNumCluster.LOGGER.severe("Bad line(" + blockDescriptor +") ");
			//throw new RecoverableRecordFormatException("Bad line(" + blockDescriptor + ")");
			isFirst = false;
			return null;
		}
		
		String partId = parts[1];

		String locations[] = null;
		
		if (cluster.locMap != null) {
			locations = cluster.locMap.get(partId);
			
			if (locations == null) {
				ZipNumCluster.LOGGER.severe("No locations for block(" + partId +")");
			}
			
		} else {
			partId = cluster.clusterUri + "/" + partId + ".gz";
		}
		
		long offset = Long.parseLong(parts[2]);
		int length = Integer.parseInt(parts[3]);
		
		try {
			if ((currReader == null) || (currPartId == null) || !currPartId.equals(partId)) {
				
				if (currReader != null) {
					currReader.close();
					currReader = null;
				}
				
				if (locations != null && locations.length > 0) {
					for (String location : locations) {
						try {
							currReader = cluster.blockLoader.createBlockReader(location);
							break;
						} catch (IOException io) {
							continue;
						}
					}
				}
				
				if (currReader == null) {
					currReader = cluster.blockLoader.createBlockReader(partId);	
				}
								
				currPartId = partId;
				isFirst = false;
			}
			
			currReader.seekWithMaxRead(offset, true, length);
			
		} catch (IOException io) {
			LOGGER.severe(io.toString());
			if (currReader != null) {
				try {
					currReader.close();
				} catch (IOException e) {

				}
			}
			currReader = null;
		}
		
		isFirst = false;			
		return currReader;
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