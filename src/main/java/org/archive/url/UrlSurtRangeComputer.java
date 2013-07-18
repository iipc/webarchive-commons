package org.archive.url;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.archive.util.ArchiveUtils;

public class UrlSurtRangeComputer {
    
    public static final BasicURLCanonicalizer basicCanon = new BasicURLCanonicalizer();
    public static final IAURLCanonicalizer iaNoSlashCanon = new IAURLCanonicalizer(new AggressiveIACanonicalizerRules(true));
    public static final IAURLCanonicalizer iaKeepSlashCanon = new IAURLCanonicalizer(new AggressiveIACanonicalizerRules(false));
    
    public static enum MatchType
    {
        exact,
        prefix,
        host,
        domain,
    };
    
    public final boolean returnSurt;
    
    public UrlSurtRangeComputer(boolean returnSurt)
    {
        this.returnSurt = returnSurt;
    }
		
	public String[] determineRange(String url, MatchType match, String from, String to) throws UnsupportedEncodingException, URISyntaxException
	{
		String startKey = null;
		String endKey = null;
		
		if (url.indexOf('.') == 0) {
			url = url.substring(1);
		}		
		
		HandyURL hURL = URLParser.parse(url);
		
		basicCanon.canonicalize(hURL);
		
		if (match == MatchType.prefix) {
			iaKeepSlashCanon.canonicalize(hURL);	
		} else {
			iaNoSlashCanon.canonicalize(hURL);
		}
				
		String host = hURL.getHost();
			
		if (hURL.getPath().isEmpty()) {
			hURL.setPath("/");
		}
		
		if ((match == MatchType.prefix) && hURL.getPath().equals("/")) {
			match = MatchType.host;
		}
		
		switch (match) {
		case exact:
			startKey = hURL.getURLString(returnSurt, false, false);
			
			if (!to.isEmpty()) {
				to = ArchiveUtils.dateToTimestamp(to);
				endKey = startKey + " " + to;
			} else {			
				endKey = startKey + "!";
			}
			
			if (!from.isEmpty()) {
				from = ArchiveUtils.dateToTimestamp(from);
				startKey += " " + from;
			}
			break;

		case prefix:
			startKey = hURL.getURLString(returnSurt, false, false);
			endKey = incLastChar(startKey);
			break;
			
		case host:
		    if (returnSurt) {
		        String hostSURT = URLRegexTransformer.hostToSURT(host);	
		        startKey = hostSURT + ")/";
		        endKey = hostSURT + "*";
		    } else {
		        startKey = host + "/";
		        endKey = host + "0";
		    }
			break;
			
		case domain:
		    if (returnSurt) {
                String hostSURT = URLRegexTransformer.hostToSURT(host);    
                startKey = hostSURT + ")/";
                endKey = hostSURT + "-";
		    } else {
		        // Unsupported in non-surt mode!
		        return null;
		    }
			break;
		}
		
		return new String[]{startKey, endKey, host};
	}
	
	protected String incLastChar(String input)
	{
        StringBuilder sb = new StringBuilder(input);
        sb.setCharAt(sb.length() - 1, (char)(sb.charAt(sb.length() - 1) + 1));
        return sb.toString();
	}
}
