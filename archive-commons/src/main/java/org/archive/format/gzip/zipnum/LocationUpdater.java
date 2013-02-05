package org.archive.format.gzip.zipnum;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.util.GeneralURIStreamFactory;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.SeekableLineReaderIterator;

public class LocationUpdater implements Runnable {
	
	final static Logger LOGGER = Logger.getLogger(LocationUpdater.class.getName());
	
	protected HashMap<String, String[]> locMap = null;
	protected SeekableLineReaderFactory locReaderFactory = null;
	protected String locUri;
	
	protected long lastModTime = 0;
	
	protected int checkInterval = 5000;
	
	protected Thread updaterThread;
	
	public LocationUpdater(String locUri) throws IOException
	{
		this.locUri = locUri;
		locMap = new HashMap<String, String[]>();
		locReaderFactory = GeneralURIStreamFactory.createSeekableStreamFactory(locUri, false);
		lastModTime = locReaderFactory.getModTime();
		loadPartLocations(locMap);
		
		updaterThread = new Thread(this, "LocationUpdaterThread");
		updaterThread.start();
	}
	
	protected void syncLoad(long newModTime) throws IOException
	{
		HashMap<String, String[]> destMap = new HashMap<String, String[]>();
		loadPartLocations(destMap);
		
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("*** Location Update: " + locUri);
		}
		
		synchronized (this) {
			locMap.putAll(destMap);
		}
		
		lastModTime = newModTime;
	}
	
	public synchronized String[] getLocations(String key)
	{
		return locMap.get(key);
	}
	
	protected void loadPartLocations(HashMap<String, String[]> destMap) throws IOException
	{
		SeekableLineReaderIterator lines = null;
		
		try {
			
			lines = new SeekableLineReaderIterator(locReaderFactory.get());
			
			while (lines.hasNext()) {
				String line = lines.next();
				String[] parts = line.split("\\s");
				if (parts.length < 2) {
					String msg = "Bad line(" + line +") in (" + locUri + ")";
					throw new IOException(msg);
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
					try {
						syncLoad(currModTime);
					} catch (IOException e) {
						LOGGER.warning(e.toString());
					}
				}
				
				Thread.sleep(checkInterval);
			}
		} catch (InterruptedException ie) {
			
		}
	}
}
