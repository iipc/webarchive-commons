/*
 * Copyright 2016 The International Internet Preservation Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netpreserve.commons.uri;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the Mimic browser configuration.
 * <p>
 * The test cases are from
 * <a href="https://github.com/w3c/web-platform-tests/blob/master/url/urltestdata.json">https://github.com/w3c/web-platform-tests/blob/master/url/urltestdata.json</a>
 */
public class MimicBrowserTest {

    @Test
    public void testParsing() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonReader jsonReader = gson.newJsonReader(new FileReader("src/test/resources/urltestdata.json"));
        jsonReader.beginArray();

        int i = 1;
        List<String> errors = new ArrayList<>();

        while (jsonReader.hasNext()) {
            if (jsonReader.peek() == JsonToken.STRING) {
                jsonReader.nextString();
                continue;
            }

            TestData td = gson.fromJson(jsonReader, TestData.class);

            try {
                try {
                    Uri uri = UriBuilder.mimicBrowserUriBuilder().uri(td.base).resolve(td.input).build();

                    if (td.failure) {
                        fail("UriException was expected for test #" + i
                                + ". Base '" + td.base + "', input '" + td.input + "'. Result was '" + uri + "'");
                    }

                    assertThat(uri.toString())
                            .as("Wrong href for test #%d. Base '%s', input '%s'", i, td.base, td.input)
                            .isEqualTo(td.href);

                    assertThat(uri.getScheme() + ":")
                            .as("Wrong scheme for test #%d. Base '%s', input '%s'", i, td.base, td.input)
                            .isEqualTo(td.protocol);

                    assertThat(uri.user == null ? "" : uri.user)
                            .as("Wrong username for test #%d. Base '%s', input '%s'", i, td.base, td.input)
                            .isEqualTo(td.username);

                    assertThat(uri.password == null ? "" : uri.password)
                            .as("Wrong password for test #%d. Base '%s', input '%s'", i, td.base, td.input)
                            .isEqualTo(td.password);

                    String hostName = uri.getHost();
                    if (hostName == null) {
                        hostName = "";
                    } else if (uri.isIPv6reference) {
                        hostName = "[" + hostName + "]";
                    }
                    assertThat(hostName)
                            .as("Wrong hostname for test #%d. Base '%s', input '%s'", i, td.base, td.input)
                            .isEqualTo(td.hostname);

                    String port = uri.getPort() == Uri.DEFAULT_PORT_MARKER ? "" : String.valueOf(uri.getPort());
                    assertThat(port)
                            .as("Wrong port for test #%d. Base '%s', input '%s'", i, td.base, td.input)
                            .isEqualTo(td.port);

                    assertThat(uri.getPath())
                            .as("Wrong path for test #%d. Base '%s', input '%s'", i, td.base, td.input)
                            .isEqualTo(td.pathname);

                    assertThat(uri.getQuery() == null || uri.getQuery().isEmpty() ? "" : "?" + uri.getQuery())
                            .as("Wrong search for test #%d. Base '%s', input '%s'", i, td.base, td.input)
                            .isEqualTo(td.search);

                    assertThat(uri.getFragment() == null || uri.getFragment().isEmpty() ? "" : "#" + uri.getFragment())
                            .as("Wrong fragment for test #%d. Base '%s', input '%s'", i, td.base, td.input)
                            .isEqualTo(td.hash);
                } catch (UriException e) {
                    assertThat(td.failure)
                            .as("Exception '%s' thrown for test #%d was not expected. Base '%s', input '%s'",
                                    e, i, td.base, td.input)
                            .isTrue();
                }
            } catch (AssertionError | Exception e) {
                errors.add(e.getMessage());
            }
            i++;
        }
        if (!errors.isEmpty()) {
            fail("Tests: " + i + ". Errors: " + errors.size() + "\n"
                    + errors.stream().collect(Collectors.joining("\n")));
        }
    }

    class TestData {

        String input;

        String base;

        String href;

        String origin;

        String protocol;

        String username;

        String password;

        String host;

        String hostname;

        String port;

        String pathname;

        private String search;

        String hash;

        boolean failure;

    }
}
