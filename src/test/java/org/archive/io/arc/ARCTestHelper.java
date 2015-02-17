/*
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
 *
 */
package org.archive.io.arc;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lists the URLs from an ARC file.
 */
public class ARCTestHelper {

    public static List<String> getURLs(File arc) throws IOException {
        List<String> urls = new ArrayList<String>();
        if (!arc.exists()) {
            throw new IOException("The file '" + arc + "' does not exist");
        }
        LineInputStream in = new LineInputStream(arc);

        String line;
        long oldOffset = 0;

        // Skip the ARC header
        majorheader:
        while ((line = in.readLine()) != null) {
            if (!line.contains("</arcmetadata>")) {
                continue;
            }
            while ((line = in.readLine()) != null) {
                if (!line.isEmpty()) {
                    break majorheader;
                }
            }
        }
        if (line == null) {
            // No recognized records
            return urls;
        }

        final Pattern URL_EXTRACT = Pattern.compile("^(.+) [0-9]{14} .*");
        // Iterate the records
        while (line != null) {
            //System.out.println(line + " (absolute offset: " + oldOffset + ")");
            Matcher matcher = URL_EXTRACT.matcher(line);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Unable to extract URL from '" + line + "'");
            }
            urls.add(matcher.group());
            final long delta = getDelta(line);
            if (in.skip(delta) != delta) {
                System.err.println("Could not skip " + delta + " bytes");
            }
             // Skip the newline after content
            if (in.read() == -1) {
                break;
            }
            oldOffset = in.getOffset();
            line = in.readLine();
            //noinspection StatementWithEmptyBody
            //while ((line = in.readLine()) != null && line.isEmpty());
        }
        in.close();
        return urls;
    }

    public static class LineInputStream extends FileInputStream {
        private long offset = 0;
        public LineInputStream(File file) throws FileNotFoundException {
            super(file);
        }
        public String readLine() throws IOException {
            ByteArrayOutputStream by = new ByteArrayOutputStream();
            int b;
            while ((b = read()) != '\n' && b != -1) {
                by.write(b);
            }
            return by.size() == 0 && b == -1 ? null : by.toString("utf-8");
        }
        public long getOffset() {
            return offset;
        }

        @Override
        public int read() throws IOException {
            offset++;
            return super.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            int read = super.read(b);
            offset += read;
            return read;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = super.read(b, off, len);
            offset += read;
            return read;
        }

        @Override
        public long skip(long n) throws IOException {
            long read = super.skip(n);
            offset += read;
            return read;
        }
    }

    /// http://www.example.com/somepath 192.168.10.12 20111129020924 text/html 79022
    private static long getDelta(String line) {
        String tokens[] = line.split(" ");
        try {
            return Long.parseLong(tokens[tokens.length-1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to extract delta from line\n" + line);
        }
    }
}
