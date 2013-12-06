package org.archive.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RewriteRule
{
	protected String startsWith;
	protected String regex;
	protected String replace;
	
	protected Pattern regexPattern;
	
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
	
	public boolean rewrite(StringBuilder sb)
	{
		String urlkey = sb.toString();
		
		if ((startsWith != null) && !urlkey.startsWith(startsWith)) {
			return false;
		}
		
		if (regexPattern == null || replace == null) {
			return false;
		}
		
		Matcher match = regexPattern.matcher(urlkey);
		
		if (match.matches()) {
			sb.replace(0, sb.length(), match.replaceAll(replace));
			return true;
		}
		
		return false;
	}
}