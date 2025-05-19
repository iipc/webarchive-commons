package org.archive.format.dns;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DNSResponseParserTest {
	DNSResponseParser parser = new DNSResponseParser();
	@Test
	public void testParse() throws DNSParseException, IOException {
		verifyResults("20110328212258\nfarm6.static.flickr.a06.yahoodns.net.\t300\tIN\tA\t98.136.170.121\n",
				"20110328212258",new String[][] {{"farm6.static.flickr.a06.yahoodns.net.","300","IN","A","98.136.170.121"}});
		
		verifyResults("  20110328212258  \nfarm6.static.flickr.a06.yahoodns.net.\t300\tIN\tA\t98.136.170.121\n",
				"20110328212258",new String[][] {{"farm6.static.flickr.a06.yahoodns.net.","300","IN","A","98.136.170.121"}});
	
	}
	private void verifyResults(String res, String date, String d[][]) throws DNSParseException, IOException {
		ByteArrayInputStream is = 
			new ByteArrayInputStream(res.getBytes("UTF-8"));
		DNSResponse response = new DNSResponse();
		parser.parse(is, response);
		verifyResults(response,date,d);
	}
	private void verifyResults(DNSResponse response, String date, String d[][]) {
		assertEquals(date,response.getDate());
		assertEquals(d.length, response.size());
		for(int i = 0; i < d.length; i++) {
			String want[] = d[i];
			DNSRecord rec = response.get(i);
			assertEquals(want[0],rec.getName());
			assertEquals(want[1],String.valueOf(rec.getTtl()));
			assertEquals(want[2],rec.getNetClass());
			assertEquals(want[3],rec.getType());
			assertEquals(want[4],rec.getValue());
		}
	}
}
