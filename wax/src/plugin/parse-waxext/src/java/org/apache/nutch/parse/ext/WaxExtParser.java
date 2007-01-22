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

package org.apache.nutch.parse.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.OutlinkExtractor;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseImpl;
import org.apache.nutch.parse.ParseStatus;
import org.apache.nutch.parse.Parser;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.PluginRepository;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.CommandRunner;
import org.archive.access.nutch.NutchwaxConfiguration;

/**
 * A wrapper that invokes external command to do real parsing job.
 * 
 * Modified by St.Ack so can customize to work better with xpdf. Copied local
 * rather than include ExtParser jar both in resultant plugin and in build path.
 * 04182006.
 * 
 * @author John Xing
 */

public class WaxExtParser implements Parser {
    public final Log LOG = LogFactory.getLog(this.getClass().getName());
    private static final Pattern TITLE =
        Pattern.compile("^Title:(.*)$", Pattern.MULTILINE);

    static final int BUFFER_SIZE = 4096;

    static final int TIMEOUT_DEFAULT = 30; // in seconds

    // handy map from String contentType to String[] {command, timeoutString}
    Hashtable TYPE_PARAMS_MAP = new Hashtable();

    private Configuration conf;

    public WaxExtParser() {
        super();
    }

    public Parse getParse(Content content) {

        String contentType = content.getContentType();

        String[] params = (String[]) TYPE_PARAMS_MAP.get(contentType);
        if (params == null)
            return new ParseStatus(ParseStatus.FAILED,
                    "No external command defined for contentType: "
                            + contentType).getEmptyParse(getConf());

        String command = params[0];
        int timeout = Integer.parseInt(params[1]);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Use " + command + " with timeout=" + timeout + "secs");
        }

        String text = null;
        String title = null;

        try {
            byte[] raw = content.getContent();
            String contentLength = content.getMetadata().
                get("contentLength");
            if (contentLength != null && raw.length != Integer.parseInt(contentLength)) {
                return new ParseStatus(ParseStatus.FAILED,
                    ParseStatus.FAILED_TRUNCATED,
                    "Content truncated at " + raw.length +
                    " bytes (Original was " + contentLength +
                    ". Parser can't handle incomplete " + contentType +
                    " file.").getEmptyParse(getConf());
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream(BUFFER_SIZE);
            ByteArrayOutputStream es = new ByteArrayOutputStream(
                    BUFFER_SIZE / 4);

            CommandRunner cr = new CommandRunner();

            cr.setCommand(command + " " + System.getProperty("java.io.tmpdir")
                    + " " + contentType);
            cr.setInputStream(new ByteArrayInputStream(raw));
            cr.setStdOutputStream(os);
            cr.setStdErrorStream(es);

            cr.setTimeout(timeout);

            cr.evaluate();

            if (cr.getExitValue() != 0)
                return new ParseStatus(ParseStatus.FAILED, "External command "
                        + command + " failed with error: " + es.toString()
                        + ", contentLength " + contentLength + ", raw length "
                        + Integer.toString(raw.length))
                        .getEmptyParse(getConf());
            text = os.toString();
            
            CharSequence cs =
                text.subSequence(0, Math.min(512, text.length()));
            Matcher m = TITLE.matcher(cs);
            if (m.find()) {
                if (m.group(1) != null) {
                    title = m.group(1).trim();
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("PDFInfo: " + cs.toString());
                }
            }

        } catch (Exception e) { // run time exception
            return new ParseStatus(e).getEmptyParse(getConf());
        }
        if (title == null) {
            title = "";
        }
        if (text == null) {
            text = "";
        }

        // collect outlink
        Outlink[] outlinks = OutlinkExtractor.getOutlinks(text, getConf());

        ParseData parseData = new ParseData(ParseStatus.STATUS_SUCCESS, title,
                outlinks, content.getMetadata());
        parseData.setConf(this.conf);
        return new ParseImpl(text, parseData);
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
        Extension[] extensions = PluginRepository.get(conf).getExtensionPoint(
                "org.apache.nutch.parse.Parser").getExtensions();

        String contentType, command, timeoutString;

        for (int i = 0; i < extensions.length; i++) {
            Extension extension = extensions[i];

            // only look for extensions defined by plugin parse-ext
            // Changed the id from parse-ext to parse-waxext. St.Ack.
            if (!extension.getDescriptor().getPluginId().equals("parse-waxext"))
                continue;

            contentType = extension.getAttribute("contentType");
            if (contentType == null || contentType.equals(""))
                continue;

            timeoutString = extension.getAttribute("timeout");
            if (timeoutString == null || timeoutString.equals(""))
                timeoutString = "" + TIMEOUT_DEFAULT;

            command = extension.getAttribute("command");
            if (command == null || command.equals(""))
                continue;

            TYPE_PARAMS_MAP.put(contentType, new String[] { command,
                    timeoutString });
        }
    }

    public Configuration getConf() {
        return this.conf;
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: WaxExtParser PDF_FILE...");
            System.exit(1);
        }
        Configuration conf = NutchwaxConfiguration.getConfiguration();
        WaxExtParser parser = new WaxExtParser();
        parser.setConf(conf);
        for (int i = 0; i < args.length; i++) {
            String name = args[i];
            String url = "file:" + name;
            File file = new File(name);
            byte[] bytes = new byte[(int) file.length()];
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            try {
                in.readFully(bytes);
                Parse parse = parser.getParse(new Content(url, url, bytes,
                        "application/pdf", new Metadata(), conf));
                System.out.println(parse.getData().getTitle());
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
    }
}
