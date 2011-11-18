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
import java.nio.charset.Charset;

/**
 * Miscellaneous useful methods.
 *
 * @author gojomo & others
 */
public class IAUtils {
    public final static Charset UTF8 = Charset.forName("utf-8");

    final public static String COMMONS_VERSION = loadCommonsVersion();

    public static String loadCommonsVersion() {
        InputStream input = IAUtils.class.getResourceAsStream(
        	"/org/archive/archive-commons-version.txt");
		if (input == null) {
		    return "UNKNOWN";
		}
		BufferedReader br = null;
		String version;
		try {
		    br = new BufferedReader(new InputStreamReader(input));
		    version = br.readLine();
		    br.readLine();
		} catch (IOException e) {
		    return e.getMessage();
		} finally {
		    closeQuietly(br);
		}
		
		return version.trim();
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

