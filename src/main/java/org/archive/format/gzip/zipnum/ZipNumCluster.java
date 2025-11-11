package org.archive.format.gzip.zipnum;
/**
 * ZipNumCluster
 * 
 * A ZipNumIndex representing multiple shards which can be loaded dynamically. The shard locations are loaded dynamically
 * from a specified file and can be reloaded at a specified internval.
 *   Files used
 *   - ALL.loc - a required file specifying <shard>\t<location uri>[\t<more location uris>]
 *   - ALL.lastblocks - a file specifying size of last blocks in each shard. This is optional and only used for size calculation.
 * 
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.util.ArchiveUtils;
import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.SeekableLineReaderIterator;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.iterator.CloseableIterator;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ZipNumCluster extends ZipNumIndex {
	
	final static Logger LOGGER = Logger.getLogger(ZipNumCluster.class.getName());
	
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
	
	private class LocationUpdater implements Runnable
	{
		@Override
		public void run() {
			try {
				while (true) {
					long currModTime = locReaderFactory.getModTime();
					
					if (currModTime != lastModTime) {
						syncLoad(currModTime);
						
						Thread.sleep(checkInterval);
						
						if (summary != null) {
							summary.reloadFactory();
						}
					}
					
					Thread.sleep(checkInterval);
				}
			} catch (InterruptedException ie) {
				
			}
		}
	}
	
	protected HashMap<String, String[]> locMap = null;
	protected SeekableLineReaderFactory locReaderFactory = null;
	protected String locFile;
	
	protected long lastModTime = 0;
	
	protected int checkInterval = 30000;
	
	protected Thread updaterThread;
	
	
	public final static String EARLIEST_TIMESTAMP = "_EARLIEST";
	public final static String LATEST_TIMESTAMP = "_LATEST";	
	public final static String OFF = "OFF";
	
	protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
	protected Date startDate, endDate;
	
	class BlockSize
	{
		String urltimestamp;
		long count;
	}
	
	protected BlockSize[] lastBlockSizes = new BlockSize[0];
	protected String blockSizesFile;
	
	protected String locRoot = null, newLocRoot = null;
	
	protected long totalAdjustment = 0;
	
	protected Date newStartDate, newEndDate;
	protected boolean newIsDisabled = false;
	protected boolean disabled = false;
	
	//final static int DEFAULT_LOC_CACHE_EXPIRE_MILLIS = 120000;
	
	protected ConcurrentHashMap<String, LocCacheEntry> locCacheMap;
	
	protected boolean cacheRemoteLoc = false;

	protected int locCacheExpireMillis = 120000;
	
	protected int locCacheMaxDuration = 1000;
	
	class LocCacheEntry
	{
		String loc;
		long expire;
		
		LocCacheEntry(String loc, long expire) 
		{ 
			this.loc = loc;
			this.expire = expire; 
		}
		
		public boolean equals(Object obj)
		{
			if (obj == null) {
				return false;
			}
			
			if (obj instanceof String) {
				return loc.equals(obj);
			}
			
			if (obj instanceof LocCacheEntry) {
				return loc.equals(((LocCacheEntry)obj).loc);
			}
			
			return false;
		}
	}	
	
	@Override
	public void init() throws IOException
	{
		super.init();
		
		this.blockSizesFile = locFile.replaceAll(".loc", ".lastblocks");
		
		locMap = new HashMap<String, String[]>();
		
		if (cacheRemoteLoc) {
			locCacheMap = new ConcurrentHashMap<String, LocCacheEntry>();
		}
		
		try {
			locReaderFactory = GeneralURIStreamFactory.createSeekableStreamFactory(locFile, false);
			lastModTime = locReaderFactory.getModTime();
		
			loadPartLocations(locMap);

		} catch (IOException io) {
			LOGGER.warning("Exception on Load -- Disabling Cluster! " + io.toString());
			disabled = true;
			return;
		}
		
		disabled = newIsDisabled;
		startDate = newStartDate;
		endDate = newEndDate;
		locRoot = newLocRoot;
		this.cdxLinesTotalCount = computeTotalLines();
		
		if (!disabled) {
			this.loadLastBlockSizes(blockSizesFile);
		}
		
		if (checkInterval > 0) {
			updaterThread = new Thread(new LocationUpdater(), "LocationUpdaterThread");
			updaterThread.start();
		}
	}
	
	protected void syncLoad(long newModTime)
	{
		HashMap<String, String[]> destMap = new HashMap<String, String[]>();
		
		try {
			loadPartLocations(destMap);
		} catch (IOException e) {
			LOGGER.warning(e.toString());
			return;
		}
		
		if (!disabled) {
			this.loadLastBlockSizes(blockSizesFile);
		}
		
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("*** Location Update: " + locFile);
		}
		
		ArrayList<String[]> filesToClose = new ArrayList<String[]>();
		
		synchronized (this) {
			for (Entry<String, String[]> files : destMap.entrySet()) {
				String[] existingFiles = locMap.get(files.getKey());
				
				String[] newFiles = files.getValue();
				
				if ((existingFiles != null) && !Arrays.equals(existingFiles, newFiles)) {					
					filesToClose.add(existingFiles);
				}
				
				locMap.put(files.getKey(), newFiles);
			}
			
			//locMap.putAll(destMap);
			
			startDate = newStartDate;
			endDate = newEndDate;
			disabled = newIsDisabled;
			locRoot = newLocRoot;
			
			this.cdxLinesTotalCount = computeTotalLines();
		}
		
		if (this.locCacheMap != null) {
			locCacheMap.clear();
		}
		
		closeExistingFiles(filesToClose);
		
		lastModTime = newModTime;
	}
	
	private void closeExistingFiles(ArrayList<String[]> filesToClose) {
		for (String[] files : filesToClose) {
			for (String file : files) {
				try {
					blockLoader.closeFileFactory(file);
				} catch (IOException e) {
					LOGGER.warning(e.toString());
				}
			}
		}
	}

	public synchronized String[] getLocations(String key)
	{
		return locMap.get(key);
	}
	
	public String getLocRoot()
	{
		return locRoot;
	}
	
	public String getLocFile()
	{
		return locFile;
	}
	
	public void setLocFile(String locFile)
	{
		this.locFile = locFile;
	}
	
	public int getLocCacheExpireMillis() {
		return locCacheExpireMillis;
	}

	public void setLocCacheExpireMillis(int locCacheExpireMillis) {
		this.locCacheExpireMillis = locCacheExpireMillis;
	}

	public int getLocCacheMaxDuration() {
		return locCacheMaxDuration;
	}

	public void setLocCacheMaxDuration(int locCacheMaxDuration) {
		this.locCacheMaxDuration = locCacheMaxDuration;
	}

	public boolean isCacheRemoteLoc() {
		return cacheRemoteLoc;
	}

	public void setCacheRemoteLoc(boolean cacheRemoteLoc) {
		this.cacheRemoteLoc = cacheRemoteLoc;
	}

	protected Date parseDate(String date)
	{
		try {
			return dateFormat.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}
	
	public boolean dateRangeCheck(String key)
	{
		// Allow a cluster to be "disabled" by specifying an empty ALL.loc
		if (disabled) {
			return false;
		}
		
		if ((startDate == null) && (endDate == null)) {
			return true;
		}
		
		int spaceIndex = key.indexOf(' ');
		if (spaceIndex < 0) {
			return true;
		}
		
		String dateStr = key.substring(spaceIndex + 1);
		Date reqDate = null;
		
		try {
			reqDate = ArchiveUtils.getDate(dateStr);
		} catch (ParseException e) {
			return true;
		}
		
		if ((startDate != null) && reqDate.before(startDate)) {
			return false;
		}
		
		if ((endDate != null) && reqDate.after(endDate)) {
			return false;
		}
		
		return true;
	}
	
	protected void loadLastBlockSizes(String filename)
	{
		BufferedReader reader = null;
		
		String line = null;
		
		List<BlockSize> list = new ArrayList<BlockSize>();
		totalAdjustment = 0;
		
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), UTF_8));
			
			while ((line = reader.readLine()) != null) {
				String[] splits = line.split("\t");
				
				BlockSize block = new BlockSize();
				block.count = Long.parseLong(splits[1]);
				block.urltimestamp = splits[2];
				list.add(block);
				totalAdjustment += block.count;
			}
		} catch (Exception e) {
			LOGGER.warning(e.toString());

		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LOGGER.warning(e.toString());
				}
			}
		}
		
		lastBlockSizes = list.toArray(new BlockSize[list.size()]);
	}
	
	protected void loadPartLocations(HashMap<String, String[]> destMap) throws IOException
	{
		SeekableLineReaderIterator lines = null;
		
		newStartDate = newEndDate = null;
		newIsDisabled = false;
		
		try {
			
			lines = new SeekableLineReaderIterator(locReaderFactory.get());
			
			while (lines.hasNext()) {
				String line = lines.next();
				
				if (line.isEmpty()) {
					continue;
				}
				
				String[] parts = line.split("\\t");
				
				if (parts[0].equals(OFF)) {
					newIsDisabled = true;
					break;
				}
				
				if (parts.length < 2) {
					String msg = "Bad line(" + line + ") in (" + locFile + ")";
					LOGGER.warning(msg);
					continue;
				}
				
				if (parts[0].equals(EARLIEST_TIMESTAMP)) {
					newStartDate = parseDate(parts[1]);
					continue;
				} else if (parts[0].equals(LATEST_TIMESTAMP)) {
					newEndDate = parseDate(parts[1]);
					continue;
				}
				
				String locations[] = new String[parts.length - 1];
				
				if (newLocRoot == null) {
					int lastSlash = parts[1].lastIndexOf('/');
					newLocRoot = parts[1].substring(0, lastSlash + 1);
				}
			
				for (int i = 1; i < parts.length; i++) {
					locations[i-1] = parts[i];
				}
				
				destMap.put(parts[0], locations);
			}
		} finally {
			if (lines != null) {
				lines.close();
			}
		}
	}

	public int getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(int checkInterval) {
		this.checkInterval = checkInterval;
	}

	public long getTotalAdjustment() {
		return totalAdjustment;
	}

	public int getNumBlocks() {
		return lastBlockSizes.length;
	}
	
	public long getLastBlockDiff(String startKey, int startPart, int endPart) {
		if (startPart >= lastBlockSizes.length || endPart >= lastBlockSizes.length) {
			return 0;
		}
		
		if (startKey.equals(lastBlockSizes[startPart].urltimestamp)) {
			startPart++;
		}
		
		long diff = 0;
		
		for (int i = startPart; i < endPart; i++) {
			diff += lastBlockSizes[i].count;
			diff -= this.getCdxLinesPerBlock();
		}
		
		return diff;
	}
	

	// Adjust from shorter blocks, if loaded
	public long computeTotalLines()
	{		
		long numLines = 0;
		
		try {
			numLines = this.getNumLines(summary.getRange("", ""));
		} catch (IOException e) {
			LOGGER.warning(e.toString());
			return 0;
		}
		
		long adjustment = getTotalAdjustment();
		numLines -= (getNumBlocks() - 1);
		numLines *= this.getCdxLinesPerBlock();
		numLines += adjustment;
		return numLines;
	}
	
	public CloseableIterator<String> getCDXIterator(String key, String start, String end, ZipNumParams params) throws IOException {
		
		if (!dateRangeCheck(key)) {
			return EMPTY_ITERATOR;
		}
		
		return super.getCDXIterator(key, start, end, params);
	}
	
	public CloseableIterator<String> getCDXIterator(String key, String prefix, boolean exact, ZipNumParams params) throws IOException {
		
		if (!dateRangeCheck(key)) {
			return EMPTY_ITERATOR;
		}
		
		return super.getCDXIterator(key, prefix, exact, params);
	}
	
	public boolean isDisabled() {
		return this.disabled;
	}
		
	@Override
	SeekableLineReader doBlockLoad(String partId, long startOffset, int totalLength) {
		
		SeekableLineReader reader = null;
		
		String[] locations = getLocations(partId);
		
		if (locations == null) {
			LOGGER.severe("No locations for block(" + partId +")");
			return null;
		}
		
		// Attempt cached load for http
		if (cacheRemoteLoc && (locCacheMap != null) && (locations.length > 0) && GeneralURIStreamFactory.isHttp(locations[0])) {
			reader = loadCachedBalancedReader(partId, locations, startOffset, totalLength);
		} else {
		// Standard block load path
			for (String location : locations) {
				reader = blockLoader.attemptLoadBlock(location, startOffset, totalLength, true, isRequired());
				if (reader != null) {
					return reader;
				}
			}
		}
		
		return reader;
	}
		
	protected String locCacheGet(String key)
	{
		LocCacheEntry entry = locCacheMap.get(key);
		
		if (entry == null) {
			return null;
		}
		
		if (System.currentTimeMillis() > entry.expire) {
			locCacheMap.remove(key);
			return null;
		}
		
		return entry.loc;
	}
	
	protected void locCachePut(String key, String loc)
	{
		locCacheMap.putIfAbsent(key, new LocCacheEntry(loc, System.currentTimeMillis() + locCacheExpireMillis));
	}
	
	SeekableLineReader loadCachedBalancedReader(String partId, String[] locations, long offset, int length)
	{
		SeekableLineReader reader = null;
		
		String cachedUrl = locCacheGet(partId);
		
		if (cachedUrl != null) {
			long start = System.currentTimeMillis();
			
			reader = blockLoader.attemptLoadBlock(cachedUrl, offset, length, true, false);
		
			long duration = System.currentTimeMillis() - start;
			
			if ((reader == null) || (duration > locCacheMaxDuration)) {
				locCacheMap.remove(partId, cachedUrl);
			}
			
			if (reader != null) {
				return reader;
			}
		}
		
		ArrayList<Integer> indexs = new ArrayList<Integer>();
		
		for (int i = 0; i < locations.length; i++) {
			indexs.add(i);
		}
		if (locations.length > 1) {
			Collections.shuffle(indexs);
		}
		
		final int lastIndex = locations.length - 1;
		
		for (int i = 0; i < indexs.size(); i++) {
			
			int index = indexs.get(i);
			
			// Skip failed cached url
			if ((cachedUrl != null) && locations[index].equals(cachedUrl)) {
				continue;
			}
			
			long start = System.currentTimeMillis();
			
			boolean required = (isRequired() && (i == lastIndex));
			
			reader = blockLoader.attemptLoadBlock(locations[index], offset, length, true, required);
			
			long duration = System.currentTimeMillis() - start;
			
			if (reader != null) {
				String connectedUrl = ((HTTPSeekableLineReader)reader).getConnectedUrl();
				
				if ((duration < locCacheMaxDuration) && (connectedUrl != null)) {
					locCachePut(partId, connectedUrl);
				}
				
				return reader;
			}
		}

		return reader;
	}
}
