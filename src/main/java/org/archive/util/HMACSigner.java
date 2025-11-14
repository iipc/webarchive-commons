package org.archive.util;

import java.nio.charset.StandardCharsets;

/**
 * Generate an HMAC key given a secret sig, key name and optional id and an expiration time
 * 
 * getHMacCookieStr() creates an HMAC digest from an expiration <timestamp> (seconds from now) and an optional <id> (which may be omitted/null)
 *
 * with id:
 * <name>-<id>=<timestamp>-<hmac> where <hmac> is HMAC digest of <id>-<timestamp>
 * 
 * without id:
 * <name>=<timestamp>-<hmac> where <hmac> is HMAC digest of <timestamp>
 * 
 */

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMACSigner {
	
	protected String name;
	protected String sig;
	protected String algo = "HmacMD5";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSig() {
		return sig;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}
	
	public String getAlgo() {
		return algo;
	}

	public void setAlgo(String algo) {
		this.algo = algo;
	}

	public HMACSigner()
	{
		
	}
	
	public HMACSigner(String sig, String name)
	{
		this.sig = sig;
		this.name = name;
	}

	//HMAC function courtesy of: 
	//http://www.supermind.org/blog/1102/generating-hmac-md5-sha1-sha256-etc-in-java
	public static String hmacDigest(String msg, String keyString, String algo) {
		String digest = null;
		try {
			SecretKeySpec key = new SecretKeySpec(
			        (keyString).getBytes(StandardCharsets.UTF_8), algo);
			Mac mac = Mac.getInstance(algo);
			mac.init(key);

			byte[] bytes = mac.doFinal(msg.getBytes(StandardCharsets.US_ASCII));

			StringBuilder hash = new StringBuilder();
			
			for (int i = 0; i < bytes.length; i++) {
				String hex = Integer.toHexString(0xFF & bytes[i]);
				if (hex.length() == 1) {
					hash.append('0');
				}
				hash.append(hex);
			}
			digest = hash.toString();

		} catch (Exception e) {
			return null;
		}

		return digest;
	}
	
	public String getHMacCookieStr(long durationSecs)
	{
		return getHMacCookieStr(null, durationSecs);
	}
	
	public String getHMacCookieStr(String id, long durationSecs)
	{
		boolean includeId = (id != null && !id.isEmpty());
		
		long expire = System.currentTimeMillis() / 1000 + durationSecs;
		
	    StringBuilder cookieStr = new StringBuilder(name);
	    
	    if (includeId) {
	    	cookieStr.append('-');
	    	cookieStr.append(id);
	    }
	    
	    cookieStr.append('=');
	    cookieStr.append(expire);
	    cookieStr.append('-');
	    
		StringBuilder msg = new StringBuilder();
		
		if (includeId) {
			msg.append(id);
			msg.append('-');
		}
		
		msg.append(expire);
	    
		String digest = hmacDigest(msg.toString(), sig, algo);
	    cookieStr.append(digest);
	    
	    return cookieStr.toString();
	}
}
