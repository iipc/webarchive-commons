package org.archive.accesscontrol.robotstxt;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * HttpRobotClient allows fetching of robots.txt rules over HTTP.
 * 
 * @author aosborne
 *
 */
public class HttpRobotClient extends RobotClient {
    protected HttpClient http = new HttpClient(
            new MultiThreadedHttpConnectionManager());
    
    public HttpClient getHttpClient() {
        return http;
    }

    public RobotRules getRulesForUrl(String url, String userAgent) throws IOException {
        String robotsUrl = robotsUrlForUrl(url);
        HttpMethod method = new GetMethod(robotsUrl);
        method.addRequestHeader("User-Agent", userAgent);
        http.executeMethod(method);
        RobotRules rules = new RobotRules();
        rules.parse(method.getResponseBodyAsStream());
        return rules;
    }

    @Override
    public void prepare(Collection<String> urls, String userAgent) {
        // no-op
    }

    @Override
    public void setRobotProxy(String host, int port) {
        http.getHostConfiguration().setProxy(host, port);
    }
}
