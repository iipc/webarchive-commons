package org.archive.hadoop.mapreduce;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.archive.util.StringFieldExtractor;
import org.archive.util.StringFieldExtractor.StringTuple;

public class GlobalWaybackCDXReducer extends Reducer<Text, Text, Text, Text> 
implements Configurable {

	private static final Logger LOGGER =
		Logger.getLogger(GlobalWaybackCDXReducer.class.getName());
	private Configuration conf;
	private static final int DEFAULT_DAY_LIMIT = 111;
	private static final String DAY_LIMIT_CONFIG = "cdx.daily.limit";
	private static final char DELIMITER = ' ';
	private static final int DATE_FIELD = 2;
	private int dayLimit;
	private StringFieldExtractor sfe;
	private String lastDayUrl = null;
	private String lastDay = null;
	private int lastDayCount = 0;

	public static void setDailyLimit(Configuration conf, int limit) {
		conf.setInt(DAY_LIMIT_CONFIG, limit);
	}
	
	private static String dayPart(final String timestamp) {
		if(timestamp == null) {
			return null;
		}
		return timestamp.substring(0,Math.min(timestamp.length(), 8));
	}
	
	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		if(dayLimit != 0) {
			String ks = key.toString();
			StringTuple st = sfe.split(ks);
			String activeDay = dayPart(st.second);
			String url = st.first;
			if(lastDayUrl == null) {
				lastDayUrl = url;
				lastDay = activeDay;
				lastDayCount = 0;
			} else {
				if(lastDayUrl.equals(url)) {
					// on the same url, is it the same day?
					if(lastDay.equals(activeDay)) {

						// leave counters alone:

						// TODO: uniqueness checking - now we are just throwing away
						//       anything - would be nice to omit dupes, first.
					} else {
						// a new day:
						lastDay = activeDay;
						lastDayCount = 0;
					}
				} else {
					// a new URL:
					lastDayUrl = url;
					lastDay = activeDay;
					lastDayCount = 0;
				}
			}
		}			
		
		for (Text value : values) {

			if(lastDayCount > dayLimit) {
				// to many for this day..
				break;
			}
			String vs = value.toString();
			
			String parts[] = vs.split(" ");
			if((parts.length < 7) ||(parts.length > 8)){
				LOGGER.warning("Bad input(column count): " + key.toString() + " " + vs);
				continue;
			}
//			String urlKey = parts[0];
//			String timestamp = parts[1];
			int idx = 0;
			String origUrl = parts[idx++];
			String mime = parts[idx++];
			String responseCode = parts[idx++];
			String digest = parts[idx++];
			String redirect = parts[idx++];
			String robotFlags = null;
			if(parts.length == 8) {
				robotFlags = parts[idx++];
			}
			String offset = parts[idx++];
			String filename = parts[idx];

			// now - do we output?
			if(robotFlags != null) {
				if(robotFlags.contains("A")) {
					continue;
				}
			}
			try {
				int code = Integer.parseInt(responseCode);
				// erk.. let's filter out live web stuff that's 502/504:
				if((code == 502) || (code == 504)) {
					if(filename.startsWith("live-20") 
							&& filename.endsWith(".arc.gz")) {
						// discard:
						continue;
					}
				}
			} catch(NumberFormatException e) {
				LOGGER.warning("Bad input(response code): " + key.toString() + " " + vs);
				continue;
			}

			try {
				Long.parseLong(offset);
			} catch(NumberFormatException e) {
				LOGGER.warning("Bad input(offset): " + key.toString() + " " + vs);
				continue;
			}

			if(digest.length() > 3) {
				digest = digest.substring(0,3);
			}

			lastDayCount++;
			value.set(String.format("%s %s %s %s %s %s %s",
					origUrl,mime,responseCode,digest,redirect,offset,filename));
			context.write(key, value);
		}
	}



	public void setConf(Configuration conf) {
		this.conf = conf;
		dayLimit = conf.getInt(DAY_LIMIT_CONFIG, DEFAULT_DAY_LIMIT);
		sfe = new StringFieldExtractor(DELIMITER, DATE_FIELD);
	}



	public Configuration getConf() {
		return conf;
	}
}
