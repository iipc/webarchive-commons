package org.archive.util;

import java.util.List;
import java.util.regex.Pattern;

public class StringParse {
	private final static Pattern IP_PATTERN =
        Pattern.compile("b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).)"
                              + "{3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)b");
	public static boolean isIP(final String ip) {
		// TODO:
		return ip.length() > 0;
//		return IP_PATTERN.matcher(ip).matches();
	}
	public static boolean isIPBad(final String ip) {
		return IP_PATTERN.matcher(ip).matches();
	}
	public static String join(List<String> p) {
		return join(p,",");
	}
	public static String join(List<String> p, String delim) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(String part : p) {
			if(first) {
				first = false;
			} else {
				sb.append(delim);
			}
			sb.append(part);
		}
		return sb.toString();
	}
}
