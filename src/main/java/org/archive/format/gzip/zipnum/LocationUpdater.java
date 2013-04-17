package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.util.ArchiveUtils;
import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.SeekableLineReaderIterator;

public class LocationUpdater implements Runnable {
	
	final static Logger LOGGER = Logger.getLogger(LocationUpdater.class.getName());
	
	protected HashMap<String, String[]> locMap = null;
	protected SeekableLineReaderFactory locReaderFactory = null;
	protected String locUri;
	
	protected long lastModTime = 0;
	
	protected int checkInterval = 30000;
	
	protected Thread updaterThread;
	
	
	public final static String EARLIEST_TIMESTAMP = "_EARLIEST";
	public final static String LATEST_TIMESTAMP = "_LATEST";	
	public final static String OFF = "OFF";
	
	protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected Date startDate, endDate;
	
	protected ZipNumBlockLoader blockLoader = null;
	
	protected Date newStartDate, newEndDate;
	protected boolean newIsDisabled = false;
	protected boolean isDisabled = false;
	
	public LocationUpdater(String locUri, ZipNumBlockLoader blockLoader)
	{
		this.locUri = locUri;
		this.blockLoader = blockLoader;
		
		locMap = new HashMap<String, String[]>();
		
		try {
			locReaderFactory = GeneralURIStreamFactory.createSeekableStreamFactory(locUri, false);
			lastModTime = locReaderFactory.getModTime();
		
			loadPartLocations(locMap);
		} catch (IOException io) {
			LOGGER.warning("Exception on Load -- Disabling Cluster! " + io.toString());
			isDisabled = true;
			return;
		}
		
		isDisabled = newIsDisabled;
		startDate = newStartDate;
		endDate = newEndDate;
		
		if (checkInterval > 0) {
			updaterThread = new Thread(this, "LocationUpdaterThread");
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
		
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("*** Location Update: " + locUri);
		}
		
		ArrayList<String[]> filesToClose = new ArrayList<String[]>();
		
		synchronized (this) {
			for (Entry<String, String[]> files : destMap.entrySet()) {
				String[] existingFiles = locMap.get(files.getKey());
				if ((existingFiles != null) && !Arrays.equals(existingFiles, files.getValue())) {					
					filesToClose.add(existingFiles);
				}
				locMap.put(files.getKey(), files.getValue());
			}
			
			//locMap.putAll(destMap);
			
			startDate = newStartDate;
			endDate = newEndDate;
			isDisabled = newIsDisabled;
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
		if (isDisabled) {
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
					String msg = "Bad line(" + line + ") in (" + locUri + ")";
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

	@Override
	public void run() {
		try {
			while (true) {
				long currModTime = locReaderFactory.getModTime();
				
				if (currModTime != lastModTime) {
					syncLoad(currModTime);
				}
				
				Thread.sleep(checkInterval);
			}
		} catch (InterruptedException ie) {
			
		}
	}

	public int getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(int checkInterval) {
		this.checkInterval = checkInterval;
	}
}
