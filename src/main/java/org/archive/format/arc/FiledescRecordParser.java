package org.archive.format.arc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class FiledescRecordParser {
	public boolean strict = false;
	public FiledescRecord parse(InputStream is) throws IOException {
		FiledescRecord rec = new FiledescRecord();
		try {
			// TODO: count input bytes read...
			BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			String line = br.readLine();
			parseLine1(rec,line);
			line = br.readLine();
			if(line == null) {
				if(strict) {
					throw new IOException("No format line");
				}
				rec.setFormat("Unknown");
			} else {
				rec.setFormat(line);
			}
			// TODO: only parse metadata if format 1.1?
			line = br.readLine();
			while(line != null) {
				addMetaDataLine(rec,line);
				line = br.readLine();
			}
			
		} catch(UnsupportedEncodingException e) {
			// nope
		}
		return rec;
	}
	private void addMetaDataLine(FiledescRecord rec, String metaLine) {
		if(metaLine.startsWith("<")) {
			int endOfTag = metaLine.indexOf('>');
			if(endOfTag > 0) {
				String name = metaLine.substring(1,endOfTag);
				String tail = "</"+name+">";
				if(metaLine.endsWith(tail)) {
					// looks good:
					int nameLength = name.length();
					int wrapperLength = (nameLength * 2) + 5;
					int valueLength = metaLine.length() - wrapperLength;
					if(valueLength > 0) {
						String value = metaLine.substring(nameLength + 2, nameLength + valueLength + 2);
						rec.addMetaData(name,value);
					}
				}
			}
		}
	}
	private void parseLine1(FiledescRecord rec, String line1) throws IOException {
		if(line1 == null) {
			if(strict) {
				throw new IOException("Empty filedesc record");
			}
			rec.setMajorVersion(0);
			rec.setMinorVersion(0);
			rec.setOrganization("Unknown");
			return;
		}
		String parts[] = line1.split(" ");
		if(parts.length < 3) {
			//hrm...
			if(strict) {
				throw new IOException("Bad Filedesc line 1:"+line1);
			}
			rec.setOrganization(line1);
			if(parts.length < 2) {
				rec.setMinorVersion(0);
			} else {
				rec.setMinorVersion(parseIntOrVal(parts[1],0));
			}
			if(parts.length < 1) {
				rec.setMajorVersion(0);
			} else {
				rec.setMajorVersion(parseIntOrVal(parts[0],0));				
			}
		} else {
			if(parts.length == 3) {
				rec.setOrganization(parts[2]);
			} else {
				StringBuilder sb = new StringBuilder(line1.length());
				for(int i = 2; i < parts.length; i++) {
					if(i > 2) {
						sb.append(" ");
					}
					sb.append(parts[i]);
					rec.setOrganization(sb.toString());
				}
			}
			rec.setMajorVersion(parseIntOrVal(parts[0],0));
			rec.setMinorVersion(parseIntOrVal(parts[1],0));
			
		}
	}

	private int parseIntOrVal(String string, int val) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {}
		return val;
	}
}
