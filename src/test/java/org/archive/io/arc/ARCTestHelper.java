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

import org.archive.io.SubInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lists the URLs from an ARC file.
 */
public class ARCTestHelper {

    // Extracts the header URLs from an ARC file
    public static List<String> getURLs(File arc) throws IOException {
        List<String> urls = new ArrayList<String>();
        if (!arc.exists()) {
            throw new IOException("The file '" + arc + "' does not exist");
        }
        InputStream fis = new FileInputStream(arc);
        SubInputStream in = new SubInputStream(fis);
        String line = in.readLine();
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
            //oldOffset = in.getOffset();
            line = in.readLine();
            //noinspection StatementWithEmptyBody
            //while ((line = in.readLine()) != null && line.isEmpty());
        }
        in.close();
        fis.close();
        return urls;
    }

    // Checks that the two content lengths (ARC and server-issued) for each record matches
    public static void testARCContentLength(File arc) throws IOException {
        if (!arc.exists()) {
            throw new IOException("The file '" + arc + "' does not exist");
        }
        InputStream fis = new FileInputStream(arc);
        SubInputStream out = new SubInputStream(fis);

        final Pattern CONTENT_LENGTH = Pattern.compile("Content-Length: ([0-9]+)[^0-9]*");
        String outline;
        while ((outline = out.readLine()) != null) {
            if (outline.isEmpty()) {
                throw new IllegalStateException("Got unexpected empty line. Next line is\n" + out.readLine());

            }
            final long delta = getDelta(outline);
            SubInputStream sub = new SubInputStream(out, delta, out.getPosition());
            long contentLength = -1;
            String inline;
            while ((inline = sub.readLine()) != null) {
                Matcher clMatcher = CONTENT_LENGTH.matcher(inline);
                if (clMatcher.matches()) {
                    contentLength = Long.parseLong(clMatcher.group(1));
                }
                if (inline.isEmpty() || "\r".equals(inline)) {
                    break;
                }
            }
            if (contentLength != -1 && contentLength != sub.available()) {
                throw new IllegalStateException(String.format(
                        "sub_pos=%6d, sub_length=%6d, sub_available=%6d, Content-Length=%6d, header=%s",
                        sub.getPosition(), sub.getLength(), sub.available(), contentLength, outline));
            }
            sub.close();

            // Newline delimiter
            if (out.read() == -1) {
                break;
            }
        }
        fis.close();
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
