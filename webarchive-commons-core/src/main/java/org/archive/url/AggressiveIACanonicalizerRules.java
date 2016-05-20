package org.archive.url;

public class AggressiveIACanonicalizerRules extends CanonicalizeRules {
	
	public AggressiveIACanonicalizerRules()
	{
		this(true);
	}
	
	public AggressiveIACanonicalizerRules(boolean stripSlash) {
		
		setRule(SCHEME_SETTINGS, SCHEME_LOWERCASE);
		setRule(HOST_SETTINGS,
				HOST_LOWERCASE|HOST_MASSAGE);
		
		setRule(PORT_SETTINGS,
				PORT_STRIP_DEFAULT);
		
		int pathSettings = PATH_LOWERCASE|PATH_STRIP_SESSION_ID;
		
		if (stripSlash) {
			pathSettings |= PATH_STRIP_TRAILING_SLASH_UNLESS_EMPTY;
		}
		
		setRule(PATH_SETTINGS, pathSettings);

		setRule(QUERY_SETTINGS,
				QUERY_LOWERCASE|QUERY_STRIP_SESSION_ID|QUERY_STRIP_EMPTY|
				QUERY_ALPHA_REORDER);
		
		setRule(HASH_SETTINGS,HASH_STRIP);
		
		setRule(AUTH_SETTINGS, AUTH_STRIP_PASS|AUTH_STRIP_USER);
	}

}
