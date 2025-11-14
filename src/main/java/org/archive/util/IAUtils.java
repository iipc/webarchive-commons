/*
 *  This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.archive.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Miscellaneous useful methods.
 *
 * @author gojomo &amp; others
 */
public class IAUtils {
    public final static Charset UTF8 = UTF_8;

    final public static String COMMONS_VERSION = loadCommonsVersion();
    final public static String PUBLISHER = loadCommons("publisher");
    final public static String OPERATOR = loadCommons("operator");
    final public static String WAT_WARCINFO_DESCRIPTION = loadCommons("wat.warcinfo.description");
    final public static String WARC_FORMAT = loadCommons("warc.format");
    final public static String WARC_FORMAT_CONFORMS_TO = loadCommons("warc.format.conforms.to");

    public static String loadCommonsVersion() {
        InputStream input = IAUtils.class.getResourceAsStream(
        	"/org/archive/ia-web-commons-version.txt");
		if (input == null) {
		    return "UNKNOWN";
		}
		BufferedReader br = null;
		String version;
		try {
		    br = new BufferedReader(new InputStreamReader(input, UTF_8));
		    version = br.readLine();
		    br.readLine();
		} catch (IOException e) {
		    return e.getMessage();
		} finally {
		    closeQuietly(br);
		}
		
		return version.trim();
    }
    
    public static String loadCommons(String id) {
        InputStream input = IAUtils.class.getResourceAsStream("/org/archive/commons.properties");
        Reader reader = null;
        if (input == null) {
            return "UNKNOWN";
        }
        reader = new InputStreamReader(input, UTF_8);
        Properties prop = new Properties();
        try {
            prop.load(reader);
        } catch (IOException e1) {
            return "UNKNOWN";
        }
        if (prop.getProperty(id) != null) {
            return prop.getProperty(id);
        } else {
            return "UNKNOWN";
        }
        
    }
    
    public static void closeQuietly(Object input) {
        if(input == null || ! (input instanceof Closeable)) {
            return;
        }
        try {
            ((Closeable)input).close();
        } catch (IOException ioe) {
            // ignore
        }
    }
}

