package org.archive.url;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.logging.Logger;

import org.archive.util.iterator.AbstractPeekableIterator;

public class SURT {
	private static final Logger LOG = 
		Logger.getLogger(SURT.class.getCanonicalName());
	public static String toSURT(String input) {
		if(input.startsWith("(")) {
			return input;
		}
		try {
//			String tmp = input;
//			if(tmp == null) {
//				throw new URIException();
//			}
			String tmp = SURTTokenizer.prefixKey(input);
			if(tmp.contains("/")) {
				return tmp;
			}
			return tmp + ",";
		} catch (URIException e) {
			LOG.warning("URI Exception for(" + input + "):" + e.getLocalizedMessage());
//			e.printStackTrace();
			return input;
		}
	}
	public static void main(String[] args) {
		String line;
		InputStreamReader isr = new InputStreamReader(System.in, StandardCharsets.UTF_8);
		BufferedReader br = new BufferedReader(isr);
		Iterator<String> i = AbstractPeekableIterator.wrapReader(br);
		while(i.hasNext()) {
			line = i.next();
			System.out.println(toSURT(line));
		}
	}
}
