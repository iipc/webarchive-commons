package org.archive.url;

import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WaybackURLKeyMaker implements URLKeyMaker {
//	URLCanonicalizer canonicalizer = new NonMassagingIAURLCanonicalizer();
	URLCanonicalizer canonicalizer = new DefaultIAURLCanonicalizer();
	
	public URLCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(URLCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	private boolean surtMode = true;
	
	protected List<RewriteRule> customRules;
	
	public static class RewriteRule
	{
		String startsWith;
		String regex;
		String replace;
		Pattern regexPattern;
		
		public String getStartsWith() {
			return startsWith;
		}
		public void setStartsWith(String startsWith) {
			this.startsWith = startsWith;
		}
		public String getRegex() {
			return regex;
		}
		public void setRegex(String regex) {
			regexPattern = Pattern.compile(regex);
			this.regex = regex;
		}
		public String getReplace() {
			return replace;
		}
		public void setReplace(String replace) {
			this.replace = replace;
		}
	}
	
	public WaybackURLKeyMaker()
	{

	}
	
	public boolean isSurtMode()
	{
		return surtMode;
	}
	
	public WaybackURLKeyMaker(boolean surtMode)
	{
		this.surtMode = surtMode;
	}

	public String makeKey(String url) throws URISyntaxException {
		if(url == null) {
			return "-";
		}
		if(url.length() == 0) {
			return "-";
		}
		if(url.startsWith("filedesc")) {
			return url;
		}
		if(url.startsWith("warcinfo")) {
			return url;
		}
		if(url.startsWith("dns:")) {
			String authority = url.substring(4);
			if (!surtMode) {
				return authority;
			}
			String surt = URLRegexTransformer.hostToSURT(authority);
			return surt + ")";
		}
		HandyURL hURL;

		hURL = URLParser.parse(url);
		canonicalizer.canonicalize(hURL);
		String key = hURL.getURLString(surtMode, surtMode, false);
		if (!surtMode) {
			return key;
		}
		int parenIdx = key.indexOf('(');
		if(parenIdx == -1) {
			// something very wrong..
			return url;
		}
		key = key.substring(parenIdx+1);
		
		if (customRules != null) {
			key = applyCustomRules(key);
		}
		
		return key;
	}

	public List<RewriteRule> getCustomRules() {
		return customRules;
	}

	public void setCustomRules(List<RewriteRule> customRules) {
		this.customRules = customRules;
	}
	
	protected String applyCustomRules(String urlkey)
	{
		for (RewriteRule rule : customRules) {
			if ((rule.startsWith != null) && !urlkey.startsWith(rule.startsWith)) {
				continue;
			}
			
			if (rule.regexPattern == null || rule.replace == null) {
				continue;
			}
			
			Matcher match = rule.regexPattern.matcher(urlkey);
			
			if (match.matches()) {
				urlkey = match.replaceAll(rule.replace);
			}
		}
		
		return urlkey;
	}
}
