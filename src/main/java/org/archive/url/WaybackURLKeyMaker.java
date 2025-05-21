package org.archive.url;

import java.net.URISyntaxException;
import java.util.List;

public class WaybackURLKeyMaker implements URLKeyMaker {
//	URLCanonicalizer canonicalizer = new NonMassagingIAURLCanonicalizer();
	URLCanonicalizer canonicalizer = new AggressiveIAURLCanonicalizer();
	
	public URLCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(URLCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	private boolean surtMode = true;
	
	protected List<RewriteRule> customRules;
	
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
		StringBuilder sb = new StringBuilder(urlkey);
		
		for (RewriteRule rule : customRules) {
			rule.rewrite(sb);
		}
		
		return sb.toString();
	}
}
