/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/LaxHttpParser.java,v 1.13 2005/01/11 13:57:06 oglueck Exp $
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
/*
 * 
 */

package org.archive.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.archive.format.http.HttpHeader;

/**
 * A Modified version of HttpParser which doesn't throw exceptions on bad header lines
 * 
 * A utility class for parsing http header values according to
 * RFC-2616 Section 4 and 19.3.
 * 
 * @author Michael Becke
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 2.0beta1
 */
public class LaxHttpParser {

    /** Log object for this class. */
    private static final Logger LOG = Logger.getLogger(LaxHttpParser.class.getName());
    
    /**
     * Constructor for LaxHttpParser.
     */
    protected LaxHttpParser() { }

    /**
     * Return byte array from an (unchunked) input stream.
     * Stop reading when <code>"\n"</code> terminator encountered
     * If the stream ends before the line terminator is found,
     * the last part of the string will still be returned. 
     * If no input data available, <code>null</code> is returned.
     *
     * @param inputStream the stream to read from
     *
     * @throws IOException if an I/O problem occurs
     * @return a byte array from the stream
     */
    public static byte[] readRawLine(InputStream inputStream) throws IOException {
        LOG.finest("enter LaxHttpParser.readRawLine()");

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int ch;
        while ((ch = inputStream.read()) >= 0) {
            buf.write(ch);
            if (ch == '\n') { // be tolerant (RFC-2616 Section 19.3)
                break;
            }
        }
        if (buf.size() == 0) {
            return null;
        }
        return buf.toByteArray();
    }

    /**
     * Read up to <code>"\n"</code> from an (unchunked) input stream.
     * If the stream ends before the line terminator is found,
     * the last part of the string will still be returned.
     * If no input data available, <code>null</code> is returned.
     *
     * @param inputStream the stream to read from
     * @param charset charset of HTTP protocol elements
     *
     * @throws IOException if an I/O problem occurs
     * @return a line from the stream
     * 
     * @since 3.0
     */
    public static String readLine(InputStream inputStream, String charset) throws IOException {
        LOG.finest("enter LaxHttpParser.readLine(InputStream, String)");
        byte[] rawdata = readRawLine(inputStream);
        if (rawdata == null) {
            return null;
        }
        // strip CR and LF from the end
        int len = rawdata.length;
        int offset = 0;
        if (len > 0) {
            if (rawdata[len - 1] == '\n') {
                offset++;
                if (len > 1) {
                    if (rawdata[len - 2] == '\r') {
                        offset++;
                    }
                }
            }
        }
        try {
            return new String(rawdata, 0, len - offset, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(rawdata, 0, len - offset, StandardCharsets.ISO_8859_1);
        }
    }

    /**
     * Read up to <code>"\n"</code> from an (unchunked) input stream.
     * If the stream ends before the line terminator is found,
     * the last part of the string will still be returned.
     * If no input data available, <code>null</code> is returned
     *
     * @param inputStream the stream to read from
     *
     * @throws IOException if an I/O problem occurs
     * @return a line from the stream
     * 
     * @deprecated use #readLine(InputStream, String)
     */

    public static String readLine(InputStream inputStream) throws IOException {
        LOG.finest("enter LaxHttpParser.readLine(InputStream)");
        return readLine(inputStream, StandardCharsets.US_ASCII.name());
    }
    
    /**
     * Parses headers from the given stream.  Headers with the same name are not
     * combined.
     * 
     * @param is the stream to read headers from
     * @param charset the charset to use for reading the data
     * 
     * @return an array of headers in the order in which they were parsed
     * 
     * @throws IOException if an IO error occurs while reading from the stream
     *
     * @since 3.0
     */
    public static HttpHeader[] parseHeaders(InputStream is, String charset) throws IOException {
        LOG.finest("enter HeaderParser.parseHeaders(InputStream, String)");

        ArrayList<HttpHeader> headers = new ArrayList<>();
        String name = null;
        StringBuffer value = null;
        for (; ;) {
            String line = LaxHttpParser.readLine(is, charset);
            if ((line == null) || (line.trim().length() < 1)) {
                break;
            }

            // Parse the header name and value
            // Check for folded headers first
            // Detect LWS-char see HTTP/1.0 or HTTP/1.1 Section 2.2
            // discussion on folded headers
            if ((line.charAt(0) == ' ') || (line.charAt(0) == '\t')) {
                // we have continuation folded header
                // so append value
                if (value != null) {
                    value.append(' ');
                    value.append(line.trim());
                }
            } else {
                // make sure we save the previous name,value pair if present
                if (name != null) {
                    headers.add(new HttpHeader(name, value.toString()));
                }

                // Otherwise we should have normal HTTP header line
                // Parse the header name and value
                int colon = line.indexOf(":");
                
                // START IA/HERITRIX change
                // Don't throw an exception if can't parse.  We want to keep
                // going even though header is bad. Rather, create
                // pseudo-header.
                if (colon < 0) {
                    // throw new ProtocolException("Unable to parse header: " +
                    //      line);
                    name = "HttpClient-Bad-Header-Line-Failed-Parse";
                    value = new StringBuffer(line);

                } else {
                name = line.substring(0, colon).trim();
                value = new StringBuffer(line.substring(colon + 1).trim());
                }
                // END IA/HERITRIX change
            }

        }

        // make sure we save the last name,value pair if present
        if (name != null) {
            headers.add(new HttpHeader(name, value.toString()));
        }
        
        return headers.toArray(new HttpHeader[0]);
    }

    /**
     * Parses headers from the given stream.  Headers with the same name are not
     * combined.
     * 
     * @param is the stream to read headers from
     * 
     * @return an array of headers in the order in which they were parsed
     * 
     * @throws IOException if an IO error occurs while reading from the stream
     *
     * @deprecated use #parseHeaders(InputStream, String)
     */
    public static HttpHeader[] parseHeaders(InputStream is) throws IOException {
        LOG.finest("enter HeaderParser.parseHeaders(InputStream, String)");
        return parseHeaders(is, StandardCharsets.US_ASCII.name());
    }
}
