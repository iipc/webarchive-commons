package org.archive.url;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class HandyURL {
	public final static int DEFAULT_PORT = -1;

	private String scheme;
	private String authUser;
	private String authPass;
	private String host;
	private int port;
	private String path;
	private String query;
	private String hash;
	private String opaque;

	// cached values:
	private String cachedPubSuffix;
	private String cachedPubPrefix;

	public HandyURL() {
		this.scheme = null;
		this.authUser = null;
		this.authPass = null;
		this.host = null;
		this.port = DEFAULT_PORT;
		this.path = null;
		this.query = null;
		this.hash = null;
		this.opaque = null;
	}

	public HandyURL(String scheme,
			String authUser,
			String authPass,
			String host,
			int port,
			String path,
			String query,
			String hash) {
		this.scheme = scheme;
		this.authUser = authUser;
		this.authPass = authPass;
		this.host = host;
		this.port = port;
		this.path = path;
		this.query = query;
		this.hash = hash;
	}

	public String getSURTString(boolean includeScheme) {
		return getURLString(true, includeScheme, false);
	}

	public String getURLString(boolean surt, boolean includeScheme, boolean publicSuffix) {
		if(opaque != null) {
			return opaque;
		}
		StringBuilder sb = new StringBuilder();

		if(includeScheme) {
			sb.append(scheme).append("://");
			if (surt) {
				sb.append("(");
			}
		}
		if(authUser != null) {
			sb.append(authUser);
			if(authPass != null) {
				sb.append(":").append(authPass);
			}
			sb.append("@");
		}
		String hostSrc = host;
		if(publicSuffix) {
			hostSrc = getPublicSuffix();
		}
		if(surt) {
			hostSrc = URLRegexTransformer.hostToSURT(hostSrc);
		}
		sb.append(hostSrc);
		if(port != DEFAULT_PORT) {
			sb.append(":").append(port);
		}
		if(surt) {
			sb.append(")");
		}
		boolean hasPath = (path != null) && (path.length() > 0);
		if(hasPath) {
			sb.append(path);
		} else {
			if((query != null) || (hash != null)) {
				// must have '/' with query or hash: (XXX says what?)
				sb.append("/");
			}
		}
		if(query != null) {
			sb.append('?').append(query);
		}
		if(hash != null) {
			sb.append('#').append(hash);
		}
		return sb.toString();

	}

	public String getURLString() {
		return getURLString(false, true, false);
	}

	public String getPathQuery() {
		StringBuilder sb = new StringBuilder();
		boolean hasPath = (path != null) && (path.length() > 0);
		if(hasPath) {
			sb.append(path);
		} else {
			if((query != null) || (hash != null)) {
				// must have '/' with query or hash: (XXX says what?)
				sb.append("/");
			}
		}
		if(query != null) {
			sb.append('?').append(query);
		}
		return sb.toString();
	}

	public URL toURL() throws MalformedURLException {
		return new URL(getURLString());
	}

	public String getPublicSuffix() {
		if(cachedPubSuffix != null) {
			return cachedPubSuffix;
		}
		if(host == null) {
			return null;
		}
		cachedPubSuffix = URLRegexTransformer.hostToPublicSuffix(host);
		return cachedPubSuffix;
	}

	public String getPublicPrefix() {
		if(cachedPubPrefix != null) {
			return cachedPubPrefix;
		}
		if(host == null) {
			return null;
		}
		String pubS = getPublicSuffix();
		if(pubS == null) {
			return null;
		}
		int hostLen = host.length();
		hostLen -= pubS.length();
		if(hostLen > 1) {
			cachedPubPrefix = host.substring(0,(host.length() - pubS.length())-1);
		} else {
			cachedPubPrefix = "";
		}
		return cachedPubPrefix;
	}

	/**
	 * @return the scheme
	 */
	public String getScheme() {
		return scheme;
	}
	/**
	 * @param scheme the scheme to set
	 */
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	/**
	 * @return the authUser
	 */
	public String getAuthUser() {
		return authUser;
	}
	/**
	 * @param authUser the authUser to set
	 */
	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}
	/**
	 * @return the authPass
	 */
	public String getAuthPass() {
		return authPass;
	}
	/**
	 * @param authPass the authPass to set
	 */
	public void setAuthPass(String authPass) {
		this.authPass = authPass;
	}
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
		cachedPubPrefix = null;
		cachedPubSuffix = null;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}
	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}
	/**
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}
	/**
	 * @param hash the hash to set
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * @return the opaque
	 */
	public String getOpaque() {
		return opaque;
	}

	/**
	 * @param opaque the opaque to set
	 */
	public void setOpaque(String opaque) {
		this.opaque = opaque;
	}

	public String toDebugString() {
		return String.format(Locale.ROOT, "Scheme(%s) UserName(%s) UserPass(%s) Host(%s) port(%d) Path(%s) Query(%s) Frag(%s)",
				scheme, authUser, authPass, host, port, path, query, hash);
	}

}
