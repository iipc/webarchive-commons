package org.archive.url;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.net.InternetDomainName;

public class URLRegexTransformer {
	// TODO: Provide example URLs for each...
	
    private static final OptimizedPattern PATH_OPTS[] = {
    	new OptimizedPattern("(?i)^.*/(\\((?:[a-z]\\([0-9a-z]{24}\\))+\\)/)[^\\?]+\\.aspx.*$", ".aspx", 1, 1),
    	new OptimizedPattern("(?i)^.*/(\\([0-9a-z]{24}\\)/)(?:[^\\?]+\\.aspx.*)$", ".aspx", 1, 1),
    };    
    
    
    private static final OptimizedPattern QUERY_OPTS[] = {

    	new OptimizedPattern("(?i)^(.*)(?:jsessionid=[0-9a-zA-Z]{32})(?:&(.*))?$", "jsessionid=", 1, 2),
    	new OptimizedPattern("(?i)^(.*)(?:phpsessid=[0-9a-zA-Z]{32})(?:&(.*))?$", "phpsessid=", 1, 2),
    	new OptimizedPattern("(?i)^(.*)(?:sid=[0-9a-zA-Z]{32})(?:&(.*))?$", "sid=", 1, 2),
    	new OptimizedPattern("(?i)^(.*)(?:ASPSESSIONID[a-zA-Z]{8}=[a-zA-Z]{24})(?:&(.*))?$", "aspsessionid", 1, 2),
    	new OptimizedPattern("(?i)^(.*)(?:cfid=[^&]+&cftoken=[^&]+)(?:&(.*))?$", "cftoken=", 1, 2),
    };




    public static String stripOpts(String orig, OptimizedPattern op[]) {
    	String origLC = orig.toLowerCase(Locale.ROOT);
        StringBuilder sb = null;
        int i = 0;
        int max = op.length;
        while(i < max) {
        	if(origLC.indexOf(op[i].match) != -1) {
        		sb = new StringBuilder(orig);
        		break;
        	}
        	i++;
        }
        if(sb == null) {
        	return orig;
        }
        while(i < max) {
        	if(origLC.indexOf(op[i].match) != -1) {
        		Matcher m = op[i].pattern.matcher(sb);
        		if(m != null && m.matches()) {
//        			dumpMatcher(m);
        			if(op[i].start == op[i].end) {
            			sb.delete(m.start(op[i].start), m.end(op[i].end));        				
        			} else {
        				if(m.group(op[i].end) == null) {
        					sb.setLength(m.end(op[i].start));
        				} else {
        					sb = sb.delete(m.end(op[i].start), m.start(op[i].end));
        				}
        			}
        		}
        	}
        	i++;
        }
        return sb.toString();
    }

    public static String stripPathSessionID(String path) {
    	return stripOpts(path, PATH_OPTS);
    }
    
    public static String stripQuerySessionID(String query) {
    	return stripOpts(query, QUERY_OPTS);
    }

//    private static void dumpMatcher(Matcher m) {
//    	System.err.format("Matcher\n");
//    	System.err.format("groupCount(%d)\n", m.groupCount());
//    	for(int i = 0; i <= m.groupCount(); i++) {
//    		System.err.format("\tgroup(%d): %s\n", i,m.group(i));
//    	}
//    }

    public static class OptimizedPattern {
    	Pattern pattern;
    	String match;
    	int start;
    	int end;
    	public OptimizedPattern(String regex, String match, int start, int end) {
    		this.pattern = Pattern.compile(regex);
    		this.match = match;
    		this.start = start;
    		this.end = end;
    	}
    	public OptimizedPattern(Pattern pattern, String match, int start, int end) {
    		this.pattern = pattern;
    		this.match = match;
    		this.start = start;
    		this.end = end;
    	}
    }
    
	public static String hostToPublicSuffix(String host) {
		InternetDomainName idn;
	
		try {
			idn = InternetDomainName.from(host);
		} catch(IllegalArgumentException e) {
			return host;
		}
		InternetDomainName tmp = idn.publicSuffix();
		if(tmp == null) {
			return host;
		}
		String pubSuff = tmp.toString();
		int idx = host.lastIndexOf(".", host.length() - (pubSuff.length()+2));
		if(idx == -1) {
			return host;
		}
		return host.substring(idx+1);
	}
	
	public static String hostToSURT(String host) {
		// TODO: ensure we DONT reverse IP addresses!
		String parts[] = host.split("\\.",-1);
		if(parts.length == 1) {
			// strip enclosing "[" and "]" from IPv6 hosts
			if (host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
				return host.substring(1, host.length() - 1);
			}
			return host;
		}
		StringBuilder sb = new StringBuilder(host.length());
		for(int i = parts.length - 1; i > 0; i--) {
			sb.append(parts[i]).append(",");
		}
		sb.append(parts[0]);
		return sb.toString();
	}
	
	public static String hostToProperSURT(String host) {
		return "(" + hostToSURT(host) + ")";
	}
}
