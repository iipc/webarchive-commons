package org.archive.url;

import java.net.IDN;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.google.common.net.InetAddresses;

public class GoogleURLCanonicalizer implements URLCanonicalizer {
//	Pattern OCTAL_IP = Pattern.compile("^(0[0-7]*)(\\.[0-7]+)*$");
//	Pattern DECIMAL_IP = Pattern.compile("^([0-9]+)(\\.[0-9]+)*$");
	Pattern OCTAL_IP = Pattern.compile("^(0[0-7]*)(\\.[0-7]+)?(\\.[0-7]+)?(\\.[0-7]+)?$");
	Pattern DECIMAL_IP = Pattern.compile("^([1-9][0-9]*)(\\.[0-9]+)?(\\.[0-9]+)?(\\.[0-9]+)?$");

	public void canonicalize(HandyURL url) {

		url.setHash(null);
		url.setAuthUser(minimalEscape(url.getAuthUser()));
		url.setAuthPass(minimalEscape(url.getAuthPass()));
		
		url.setQuery(minimalEscape(url.getQuery()));
		String hostE = unescapeRepeatedly(url.getHost());
		String host = null;
		try {
			host = IDN.toASCII(hostE);
		} catch(IllegalArgumentException e) {
			if(!e.getMessage().contains("A prohibited code point was found")) {
				// TODO: What to do???
//				throw e;
			}
			host = hostE;
			
		}
		host = host.replaceAll("^\\.+", "").
					replaceAll("\\.\\.+", ".").
					replaceAll("\\.$", "");
		String ip = null;
//			try {
				ip = attemptIPFormats(host);
//			} catch (URIException e) {
//				e.printStackTrace();
//			}
		if(ip != null) {
			host = ip;
		} else {
			host = escapeOnce(host.toLowerCase());
		}
		url.setHost(host);
		// now the path:

		String path = unescapeRepeatedly(url.getPath());
		
		url.setPath(escapeOnce(normalizePath(path)));
	}
	
	private static final Pattern SINGLE_FORWARDSLASH_PATTERN = 
		Pattern.compile("/");
	
	public String normalizePath(String path) {
		if(path == null) {
			path = "/";
		} else {
			// -1 gives an empty trailing element if path ends with '/':
			String[] paths = SINGLE_FORWARDSLASH_PATTERN.split(path,-1);
			ArrayList<String> keptPaths = new ArrayList<String>();
			boolean first = true;
			for(String p : paths) {
				if(first) {
					first = false;
					continue;
				} else if(p.compareTo(".") == 0) {
					// skip
					continue;
				} else if(p.compareTo("..") == 0) {
					// pop the last path, if present:
					if(keptPaths.size() > 0) {
						keptPaths.remove(keptPaths.size()-1);
					} else {
						// TODO: leave it? let's do for now...
						keptPaths.add(p);
					}
				} else {
					keptPaths.add(p);
				}
			}
			int numKept = keptPaths.size();
			if(numKept == 0) {
				path = "/";
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("/");
				for(int i = 0; i < numKept - 1; i++) {
					String p = keptPaths.get(i);
					if(p.length() > 0) {
						// this will omit multiple slashes:
						sb.append(p).append("/");
					}
				}
				sb.append(keptPaths.get(numKept-1));
				path = sb.toString();
			}
		}
		return path;
	}
	

	public String attemptIPFormats(String host) { //throws URIException {
		if(host == null) {
			return null;
		}
		if(host.matches("^\\d+$")) {
			try {
				Long l = Long.parseLong(host);
				return InetAddresses.fromInteger(l.intValue()).getHostAddress();
			} catch(NumberFormatException e) {
			}
		} else {
			// check for octal:
			Matcher m = OCTAL_IP.matcher(host);
			if(m.matches()) {
				int parts = m.groupCount();
				if(parts > 4) {
					// WHAT TO DO?
					return null;
//					throw new URIException("Bad Host("+host+")");
				}
				int[] ip = new int[]{0,0,0,0};
				for(int i=0; i < parts; i++) {
					int octet = Integer.parseInt(m.group(i+1).substring((i==0)?0:1),8);
					if((octet < 0) || (octet > 255)) {
						return null;
//						throw new URIException("Bad Host("+host+")");
					}
					ip[i] = octet;
				}
				return String.format("%d.%d.%d.%d",ip[0],ip[1],ip[2],ip[3]);
			} else {
				Matcher m2 = DECIMAL_IP.matcher(host);
				if(m2.matches()) {
					int parts = m2.groupCount();
					if(parts > 4) {
						// WHAT TO DO?
						return null;
//						throw new URIException("Bad Host("+host+")");
					}
					int[] ip = new int[]{0,0,0,0};
					for(int i=0; i < parts; i++) {
						
						String m2Group = m2.group(i+1);
						if(m2Group == null)
							return null;
						//int octet = Integer.parseInt(m2.group(i+1).substring((i==0)?0:1));
						int octet = Integer.parseInt(m2Group.substring((i==0)?0:1));
						
						if((octet < 0) || (octet > 255)) {
							return null;
//							throw new URIException("Bad Host("+host+")");
						}
						ip[i] = octet;
					}
					return String.format("%d.%d.%d.%d",ip[0],ip[1],ip[2],ip[3]);
					
				}
			}
		}
		return null;
	}
	
	public String minimalEscape(String input) {
		return escapeOnce(unescapeRepeatedly(input));
	}
	
	public String escapeOnce(String input) {
		if(input == null) {
			return null;
		}
		StringBuilder sb = null;
		int len = input.length();
		boolean ok = false;;
		for(int i = 0; i < len; i++) {
			char c = input.charAt(i);
			ok = false;
			if(c > 32) {
				if(c < 128) {
					if(c != '#') {
						ok = (c != '%');
					}
				}
			}
			if(ok) {
				if(sb != null) {
					sb.append(c);
				}
			} else {
				if(sb == null) {
					sb = new StringBuilder(input.substring(0,i));
//				} else {
//					// BUGBUG: What about chars > 255?!
//					sb.append('%').append(Integer.toHexString(c).toUpperCase());
//				}
				}
				// BUGBUG: What about chars > 255?!
				sb.append("%");
				String hex = Integer.toHexString(c).toUpperCase();
				if(hex.length() == 1) {
					sb.append('0');
				}
				sb.append(hex);
			}
		}
		if(sb == null) {
			return input;
		}
		return sb.toString();
	}
	
	public String unescapeRepeatedly(String input) {
		if(input == null) {
			return null;
		}
		while(true) {
			String un = decode(input);
			if(un.compareTo(input) == 0) {
				return input;
			}
			input = un;
		}
	}
	
	public String decode(String input) {
		int len = input.length();
		int i = 0;
		StringBuilder sb = null;
		boolean foundHex = false;
		while(i < len-2) {
			char c = input.charAt(i);
			foundHex = false;
			if(c == '%') {
				// are next two hex chars?
				int h1 = getHex(input.charAt(i+1));
				if(h1 > -1) {
					int h2 = getHex(input.charAt(i+2));
					if(h2 > -1) {
						if(sb == null) {
							sb = new StringBuilder(len);
							if(i > 0) {
								sb.append(input.substring(0,i));
							}
						}
						foundHex = true;
						i += 2;
						char f = (char) ((h1 << 4) + h2);
						sb.append(f);
					}
				}
			}
			if(!foundHex) {
				if(sb != null) {
					sb.append(c);
				}
			}
			i++;
		}
		if(sb == null) {
			return input;
		}
		// append the last chars if missed:
		for(int i2 = i; i2 < len; i2++) {
			sb.append(input.charAt(i2));
		}
		return sb.toString();
	}

	public int getHex(final char c) {
		if(c < '0') {
			return -1;
		}
		if(c <= '9') {
			return c - '0';
		}
		if(c < 'A') {
			return -1;
		}
		if(c <= 'F') {
			return 10 + (c - 'A');
		}
		if(c < 'a') {
			return -1;
		}
		if(c <= 'f') {
			return 10 + (c - 'a');
		}
		return -1;
	}
	
}
