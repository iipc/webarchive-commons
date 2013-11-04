package org.archive.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractRule
{
	protected String startsWith;
	protected String regex;
	
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
	
	public Matcher extract(String url)
	{		
		if ((startsWith != null) && !url.startsWith(startsWith)) {
			return null;
		}
		
		if (regexPattern == null) {
			return null;
		}
		
		Matcher match = regexPattern.matcher(url);
		
		if (!match.find()) {
			return null;
		}
		
		return match;
	}
}