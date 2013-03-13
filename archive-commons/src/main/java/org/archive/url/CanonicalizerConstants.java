package org.archive.url;

public interface CanonicalizerConstants {
	public static final int HOST_SETTINGS = 0;
	
	public static final int HOST_ORIGINAL = 0;
	public static final int HOST_LOWERCASE = 1;
	public static final int HOST_MASSAGE = 2;

	
	public static final int PORT_SETTINGS = 1;
	
	public static final int PORT_ORIGINAL = 0;
	public static final int PORT_STRIP_DEFAULT = 1;

	
	public static final int PATH_SETTINGS = 2;

	public static final int PATH_ORIGINAL = 0;
	public static final int PATH_LOWERCASE = 1;
	public static final int PATH_STRIP_SESSION_ID = 2;
	public static final int PATH_STRIP_EMPTY = 4;
	public static final int PATH_STRIP_TRAILING_SLASH_UNLESS_EMPTY = 8;

	
	public static final int QUERY_SETTINGS = 3;

	public static final int QUERY_ORIGINAL = 0;
	public static final int QUERY_LOWERCASE = 1;
	public static final int QUERY_STRIP_SESSION_ID = 2;
	public static final int QUERY_STRIP_EMPTY = 4;
	public static final int QUERY_ALPHA_REORDER = 8;
	// TODO: Need a setting to remove empty query ARGs..

	public static final int HASH_SETTINGS = 4;

	public static final int HASH_ORIGINAL = 0;
	public static final int HASH_STRIP = 1;


	public static final int AUTH_SETTINGS = 5;

	public static final int AUTH_ORIGINAL = 0;
	public static final int AUTH_STRIP_USER = 1;
	public static final int AUTH_STRIP_PASS = 2;
	
	public static final int SCHEME_SETTINGS = 6;

	public static final int SCHEME_ORIGINAL = 0;
	public static final int SCHEME_LOWERCASE = 1;
	
	
	public static final int NUM_SETTINGS = 7;

}
