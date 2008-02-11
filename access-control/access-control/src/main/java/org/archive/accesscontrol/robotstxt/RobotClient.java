package org.archive.accesscontrol.robotstxt;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.httpclient.URIException;
import org.archive.net.LaxURI;

/**
 * A client for checking whether a robot is allowed by a robots.txt file.
 * 
 * @author aosborne
 * 
 */
public abstract class RobotClient {
    /**
     * Returns true if a robot with the given user-agent is allowed to access
     * the given url.
     * 
     * @param url
     * @param userAgent
     * @return
     * @throws IOException
     */
    public boolean isRobotPermitted(String url, String userAgent)
            throws IOException {
        RobotRules rules = getRulesForUrl(url, userAgent);
        return !rules.blocksPathForUA(new LaxURI(url, false).getPath(),
                userAgent);
    }

    /**
     * Fetch the applicable ruleset for the given url and robot.
     * 
     * @param url
     * @param userAgent
     * @return
     * @throws IOException
     */
    public abstract RobotRules getRulesForUrl(String url, String userAgent)
            throws IOException;

    public static String robotsUrlForUrl(String url) throws URIException {
        LaxURI uri = new LaxURI(url, false);
        uri.setPath("/robots.txt");
        uri.setQuery(null);
        uri.setFragment(null);
        return uri.toString();
    }
    
    /**
     * Prepare the cache to lookup info for a given set of urls. The fetches
     * happen in parallel so this also makes a good option for speeding up bulk lookups.
     * 
     * This may be a no-op.
     */
    public abstract void prepare(Collection<String> urls, String userAgent);
    
    /**
     * Use a proxy server when fetching robots.txt data.
     * @param host
     * @param port
     */
    public abstract void setRobotProxy(String host, int port);
}
