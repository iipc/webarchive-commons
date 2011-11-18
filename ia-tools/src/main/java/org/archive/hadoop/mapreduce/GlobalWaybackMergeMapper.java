package org.archive.hadoop.mapreduce;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class GlobalWaybackMergeMapper extends Mapper<Object, Text, Text, Text>
implements Configurable {
	private static final Logger LOGGER =
		Logger.getLogger(GlobalWaybackMergeMapper.class.getName());

	private Configuration conf;

	private Text outKey = new Text();
	private Text outValue = new Text();
	public static void setDailyLimit(Configuration conf, int limit) {
		conf.setInt(DAY_LIMIT_CONFIG, limit);
	}

	private static final int DEFAULT_DAY_LIMIT = 111;
	private static final String DAY_LIMIT_CONFIG = "cdx.daily.limit";
	private int dayLimit;
	private String lastDayUrl = null;
	private String lastDay = null;
	private int lastDayCount = 0;

	public Configuration getConf() {
		return conf;
	}
	public void setConf(Configuration conf) {
		this.conf = conf;		
		dayLimit = conf.getInt(DAY_LIMIT_CONFIG, DEFAULT_DAY_LIMIT);
	}
	public void map(Object y, Text value, Context context) throws IOException,
	InterruptedException {
		String vs = value.toString();
		
		String parts[] = vs.split(" ");
		if((parts.length < 9) || (parts.length > 10)){
			System.err.format("Bad input(%s)\n",vs);
			LOGGER.warning("Bad input(column count): " + vs);
			return;
		}
		int idx = 0;
		String urlKey = parts[idx++];
		String timestamp = parts[idx++];
		String activeDay = timestamp.substring(0,Math.min(timestamp.length(), 8));

		if(dayLimit != 0) {
			if(lastDayUrl == null) {
				lastDayUrl = urlKey;
				lastDay = activeDay;
				lastDayCount = 0;
			} else {
				if(lastDayUrl.equals(urlKey)) {
					// on the same url, is it the same day?
					if(lastDay.equals(activeDay)) {

						// TODO: uniqueness checking - now we are just throwing away
						//       anything - would be nice to omit dupes, first.
						if(lastDayCount > dayLimit) {
							// too many for this day..
							LOGGER.fine("Too many for one day:" + vs);
							return;
						}

					} else {
						// a new day:
						lastDay = activeDay;
						lastDayCount = 0;
					}
				} else {
					// a new URL:
					lastDayUrl = urlKey;
					lastDay = activeDay;
					lastDayCount = 0;
				}
			}
		}			

		
		
		String origUrl = parts[idx++];
		String mime = parts[idx++];
		String responseCode = parts[idx++];
		String digest = parts[idx++];
		String redirect = parts[idx++];
		String robotFlags = null;
		if(parts.length == 10) {
			robotFlags = parts[idx++];
		}
		String offset = parts[idx++];
		String filename = parts[idx];

		// now - do we output?
		if(robotFlags != null) {
			if(robotFlags.contains("A")) {
				LOGGER.fine("Skipping noArchive-record:" + vs);
				return;
			}
		}
		try {
			if(mime.contains("warc/")) {
				// let it ride -- no responseCode for many warc record types..
			} else {
				int code = Integer.parseInt(responseCode);
				// erk.. let's filter out live web stuff that's 502/504:
				if((code == 502) || (code == 504)) {
					if(filename.startsWith("live-20") 
							&& filename.endsWith(".arc.gz")) {
						LOGGER.finer("Skipping live web 50X:" + vs);
						return;
					}
				}
			}
		} catch(NumberFormatException e) {
			LOGGER.fine("Bad input(response code): " + vs);
			return;
		}

		try {
			Long.parseLong(offset);
		} catch(NumberFormatException e) {
			LOGGER.warning("Bad input(offset): " + vs);
			return;
		}

		if(digest.length() > 3) {
			digest = digest.substring(0,3);
		}

		lastDayCount++;
		outKey.set(String.format("%s %s",urlKey,timestamp));
		outValue.set(String.format("%s %s %s %s %s %s %s",
				origUrl,mime,responseCode,digest,redirect,offset,filename));
		context.write(outKey, outValue);

	}
	
}
