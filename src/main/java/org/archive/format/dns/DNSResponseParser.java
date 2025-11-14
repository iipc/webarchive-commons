package org.archive.format.dns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class DNSResponseParser {

	private boolean isBlank(String line) {
		return line.matches("\\S");
	}
	private boolean isDate(String dateLine) {
		return !isBlank(dateLine);
	}
	public void parse(InputStream is, DNSResponse response) throws IOException, DNSParseException {
		/*
		20110328212258
		www.google.com.		86399	IN	CNAME	www.l.google.com.
		www.l.google.com.	299	IN	A	74.125.71.105
		www.l.google.com.	299	IN	A	74.125.71.103
		www.l.google.com.	299	IN	A	74.125.71.99
		www.l.google.com.	299	IN	A	74.125.71.147
		www.l.google.com.	299	IN	A	74.125.71.104
		www.l.google.com.	299	IN	A	74.125.71.106
		*/
		try {
			// TODO: should we wrap in a CountingInputStream and indicate 
			//        observed octet-length?
			BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			String date = br.readLine().trim();
			if(isDate(date)) {
				response.setDate(date);
			}
			while(true) {
				String line = br.readLine();
				if(line == null) {
					break;
				}
				if(!isBlank(line)) {
					response.add(DNSRecord.parse(line));
				}
			}
		} catch (UnsupportedEncodingException e) {
			// really really should not happen..
			e.printStackTrace();
		}
	}
}
