/* NutchwaxConfiguration.java
 *
 * $Id$
 *
 * Created Feb 14, 2006
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Heritrix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.access.nutch;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.util.NutchConfiguration;
import javax.servlet.ServletContext;
import java.util.Enumeration;


/**
 * Configuration that adds NutchWAX configuration to base nutch and hadoop
 * config.
 */
public class NutchwaxConfiguration {
    private static final String KEY = NutchwaxConfiguration.class.getName();
    private static final Configuration configuration;
    static {
        Configuration c = NutchConfiguration.create();
        c.addDefaultResource("wax-default.xml");
        configuration = c;
    }
    
    private NutchwaxConfiguration() {
        super();
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static Configuration getConfiguration(ServletContext application) {
        Configuration nutchConf = (Configuration)application.getAttribute(KEY);
        if (nutchConf != null) {
            return nutchConf;
        }
        nutchConf = NutchwaxConfiguration.getConfiguration();
        // Copied from NutchConfiguration.
        Enumeration e = application.getInitParameterNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            nutchConf.set(name, application.getInitParameter(name));
        }
        application.setAttribute(KEY, nutchConf);
        return nutchConf;
    }
}
