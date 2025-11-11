package org.archive.url;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.util.StringFieldExtractor;
import org.archive.util.StringFieldExtractor.StringTuple;

public class IAURLCanonicalizer implements URLCanonicalizer, CanonicalizerConstants {
	private CanonicalizeRules rules;
	public IAURLCanonicalizer(CanonicalizeRules rules) {
		this.rules = rules;
	}

	public void canonicalize(HandyURL url) {
		if(url.getOpaque() != null) {
			return;
		}
		if (rules.isSet(SCHEME_SETTINGS, SCHEME_LOWERCASE)) {
			if (url.getScheme() != null) {
				url.setScheme(url.getScheme().toLowerCase(Locale.ROOT));
			}
		}
		if(rules.isSet(HOST_SETTINGS, HOST_LOWERCASE)) {
			url.setHost(url.getHost().toLowerCase(Locale.ROOT));
		}
		if(rules.isSet(HOST_SETTINGS, HOST_MASSAGE)) {
			url.setHost(massageHost(url.getHost()));
		}
		if(rules.isSet(AUTH_SETTINGS,AUTH_STRIP_USER)) {
			url.setAuthUser(null);
			url.setAuthPass(null);
		} else if (rules.isSet(AUTH_SETTINGS,AUTH_STRIP_PASS)) {
			url.setAuthPass(null);
		}
		if(rules.isSet(PORT_SETTINGS, PORT_STRIP_DEFAULT)) {
			int defaultPort = getDefaultPort(url.getScheme());
			if(defaultPort == url.getPort()) {
				url.setPort(HandyURL.DEFAULT_PORT);
			}
		}
		String path = url.getPath();
		if(rules.isSet(PATH_SETTINGS, PATH_STRIP_EMPTY) && path.equals("/")) {
			url.setPath(null);
		} else {
			if(rules.isSet(PATH_SETTINGS, PATH_LOWERCASE)) {
				path = path.toLowerCase(Locale.ROOT);
			}
			if(rules.isSet(PATH_SETTINGS, PATH_STRIP_SESSION_ID)) {
				path = URLRegexTransformer.stripPathSessionID(path);
			}
			if(rules.isSet(PATH_SETTINGS, PATH_STRIP_EMPTY) && path.equals("/")) {
				url.setPath(null);
			} else if(rules.isSet(PATH_SETTINGS, PATH_STRIP_TRAILING_SLASH_UNLESS_EMPTY)) {
				if(path.endsWith("/") && (path.length() > 1)) {
					path = path.substring(0,path.length() - 1);
				}
			}
			url.setPath(path);
		}
		
		String query = url.getQuery();
		if(query != null) {
			// we have a query... what to do with it?
				
			// first remove uneeded:
			if(rules.isSet(QUERY_SETTINGS, QUERY_STRIP_SESSION_ID)) {
				query = URLRegexTransformer.stripQuerySessionID(query);
			}
			// lower-case:
			if(rules.isSet(QUERY_SETTINGS, QUERY_LOWERCASE)) {
				query = query.toLowerCase(Locale.ROOT);
			}
			// re-order?
			if(rules.isSet(QUERY_SETTINGS, QUERY_ALPHA_REORDER)) {
				query = alphaReorderQuery(query);
			}
			if(query.equals("")) {
			    if(rules.isSet(QUERY_SETTINGS, QUERY_STRIP_EMPTY)) {
			        query = null;
			    }
			}
			url.setQuery(query);
		}
	}
	
	public static String alphaReorderQuery(String orig) {
		if(orig == null) {
			return null;
		}
		if(orig.length() <= 1) {
			return orig;
		}
		String args[] = orig.split("&",-1);
		StringTuple qas[] = new StringTuple[args.length];
		StringFieldExtractor sfe = new StringFieldExtractor('=', 1);
		for(int i = 0; i < args.length; i++) {
			qas[i] = sfe.split(args[i]);
		}
		Arrays.sort(qas,new Comparator<StringTuple>() {

			public int compare(StringTuple o1, StringTuple o2) {
				int cmp = o1.first.compareTo(o2.first);
				if(cmp != 0) {
					return cmp;
				}
				if(o1.second == null) {
					if(o2.second == null) {
						// both null - same
						return 0;
					}
					// first null, second non-null, so first is smaller
					return -1;
				} else if(o2.second == null) {
					// first non-null, second null, second is smaller
					return 1;
				}
				// neither null, compare them:
				return o1.second.compareTo(o2.second);
			}
		});
		StringBuilder sb = new StringBuilder(orig.length());
		int max = qas.length - 1;
		for(int i = 0; i < max; i++) {
			if(qas[i].second == null) {
				sb.append(qas[i].first).append('&');
				
			} else {
				sb.append(qas[i].first).append('=').append(qas[i].second).append('&');
			}
		}
		if(qas[max].second == null) {
			sb.append(qas[max].first);
		
		} else {
			sb.append(qas[max].first).append('=').append(qas[max].second);
		}
		
		return sb.toString();
	}
	
	
	public static final Pattern WWWN_PATTERN = Pattern.compile("^www\\d*\\.");
	public static String massageHost(String host) {
		while(true) {
			Matcher m = WWWN_PATTERN.matcher(host);
			if(m.find()) {
				host = host.substring(m.group(0).length());
			} else {
				break;
			}
		}
		return host;
	}
	public static int getDefaultPort(String scheme) {
		String lcScheme = scheme.toLowerCase(Locale.ROOT);
		if(lcScheme.equals("http")) {
			return 80;
		} else if(lcScheme.equals("https")) {
			return 443;
		}
		return 0;
	}
}
