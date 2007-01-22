/**
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Changed package name. St.Ack
package org.archive.access.nutch;

// Added by St.Ack.
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.searcher.OpenSearchServlet;
import org.apache.nutch.util.NutchConfiguration;
 

/**
 * Subclass of OpenSearchServlet from nutch.
 * Adds in the Nutchwax plugins and does special encoding if
 * there is a 'exacturl' in the query string parameter.
 * @author stack
 */
public class NutchwaxOpenSearchServlet extends OpenSearchServlet {
	private static final long serialVersionUID = -6009645870609220838L;

	public void init(ServletConfig config) throws ServletException {
        // Put our configuration into place in the servlet context ahead
        // of the query done by OpenSearchServlet#init so it finds our
        // Configuration and doesn't create its own -- we're faking it out.
		// Do same for nutchbean.  Add our NutchwaxBean into place instead.
        Configuration conf = NutchwaxConfiguration.
        	getConfiguration(config.getServletContext());
        config.getServletContext().
        	setAttribute(NutchConfiguration.class.getName(), conf);
        try {
        	NutchwaxBean.get(config.getServletContext(), conf);
        } catch (IOException e) {
            throw new ServletException(e);
        }
        super.init(config);
    }

    public void doGet(final HttpServletRequest request,
            final HttpServletResponse response)
    throws ServletException, IOException {
        // Call super method passing an overridden version of
    	// HttpServletRequest so we can preprocess the query string whenever
    	// a call to #getParameter adding our exacturl encoding.  See
    	// NutchwaxQuery for why we have to do this.
        super.doGet(new HttpServletRequest() {
            public String getParameter(String s) {
                if (s == null || !s.equals("query")) {
                    return request.getParameter(s);
                }
                String queryString = request.getParameter(s);    
                if (queryString != null) {
                    queryString = NutchwaxQuery.encodeExacturl(queryString);
                }
                return queryString;
            }

            public String getAuthType() {
                return request.getAuthType();
            }

            public Cookie[] getCookies() {
                return request.getCookies();
            }

            public long getDateHeader(String s) {
                return request.getDateHeader(s);
            }

            public String getHeader(String s) {
                return request.getHeader(s);
            }

            public Enumeration getHeaders(String s) {
                return request.getHeaders(s);
            }

            public Enumeration getHeaderNames() {
                return request.getHeaderNames();
            }

            public int getIntHeader(String s) {
                return request.getIntHeader(s);
            }

            public String getMethod() {
                return request.getMethod();
            }

            public String getPathInfo() {
                return request.getPathInfo();
            }

            public String getPathTranslated() {
                return request.getPathTranslated();
            }

            public String getContextPath() {
                return request.getContextPath();
            }

            public String getQueryString() {
                return request.getQueryString();
            }

            public String getRemoteUser() {
                return request.getRemoteUser();
            }

            public boolean isUserInRole(String s) {
                return request.isUserInRole(s);
            }

            public Principal getUserPrincipal() {
                return request.getUserPrincipal();
            }

            public String getRequestedSessionId() {
                return request.getRequestedSessionId();
            }

            public String getRequestURI() {
                return request.getRequestURI();
            }

            public StringBuffer getRequestURL() {
                return request.getRequestURL();
            }

            public String getServletPath() {
                return request.getServletPath();
            }

            public HttpSession getSession(boolean s) {
                return request.getSession(s);
            }

            public HttpSession getSession() {
                return request.getSession();
            }

            public boolean isRequestedSessionIdValid() {
                return request.isRequestedSessionIdValid();
            }

            public boolean isRequestedSessionIdFromCookie() {
                return request.isRequestedSessionIdFromCookie();
            }

            public boolean isRequestedSessionIdFromURL() {
                return request.isRequestedSessionIdFromURL();
            }

            public boolean isRequestedSessionIdFromUrl() {
            	throw new RuntimeException("Unimplemented");
            }

            public Object getAttribute(String s) {
                return request.getAttribute(s);
            }

            public Enumeration getAttributeNames() {
                return request.getAttributeNames();
            }

            public String getCharacterEncoding() {
                return request.getCharacterEncoding();
            }

            public void setCharacterEncoding(String s)
                    throws UnsupportedEncodingException {
                request.setCharacterEncoding(s);
            }

            public int getContentLength() {
                return request.getContentLength();
            }

            public String getContentType() {
                return request.getContentType();
            }

            public ServletInputStream getInputStream() throws IOException {
                return request.getInputStream();
            }

            public Enumeration getParameterNames() {
                return request.getParameterNames();
            }

            public String[] getParameterValues(String s) {
                return request.getParameterValues(s);
            }

            public Map getParameterMap() {
                return request.getParameterMap();
            }

            public String getProtocol() {
                return request.getProtocol();
            }

            public String getScheme() {
                return request.getScheme();
            }

            public String getServerName() {
                return request.getServerName();
            }

            public int getServerPort() {
                return request.getServerPort();
            }

            public BufferedReader getReader() throws IOException {
                return request.getReader();
            }

            public String getRemoteAddr() {
                return request.getRemoteAddr();
            }

            public String getRemoteHost() {
                return request.getRemoteHost();
            }

            public void setAttribute(String a, Object b) {
                request.setAttribute(a, b);
            }

            public void removeAttribute(String a) {
                request.removeAttribute(a);
            }

            public Locale getLocale() {
                return request.getLocale();
            }

            public Enumeration getLocales() {
                return request.getLocales();
            }

            public boolean isSecure() {
                return request.isSecure();
            }

            public RequestDispatcher getRequestDispatcher(String a) {
                return request.getRequestDispatcher(a);
            }

            public String getRealPath(String a) {
                throw new RuntimeException("Unimplemented");
            }

            public int getRemotePort() {
                return request.getRemotePort();
            }

            public String getLocalName() {
                return request.getLocalName();
            }

            public String getLocalAddr() {
                return request.getLocalAddr();
            }

            public int getLocalPort() {
                return request.getLocalPort();
            }
        }, response);
    }
}