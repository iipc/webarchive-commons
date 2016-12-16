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
package org.netpreserve.commons.uri.parser;

import org.junit.Test;
import org.netpreserve.commons.uri.UriConfigs;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriFormat;

import static org.assertj.core.api.Assertions.*;

/**
 *
 */
public class StrictSurtEncoderTest {

    /**
     * Test of encode method, of class StrictSurtEncoder.
     */
    @Test
    public void testEncode() {
        Uri uri = UriConfigs.STRICT.buildUri("http://www.øks.com/path");
        StrictSurtEncoder instance = new StrictSurtEncoder();

        StringBuilder sb = new StringBuilder();
        UriFormat uriFormat = UriConfigs.SURT_KEY_FORMAT;
        instance.encode(sb, uri, uriFormat);
        assertThat(sb.toString()).isEqualTo("(com,øks,www,)");

        sb = new StringBuilder();
        uriFormat = uriFormat.decodeHost(false);
        instance.encode(sb, uri, uriFormat);
        assertThat(sb.toString()).isEqualTo("(com,xn--ks-kka,www,)");
    }

    @Test
    public void testWithDefaultConfig() {
        Uri uri = UriConfigs.SURT_KEY.buildUri("http://www.øks.Com/pAth%2dår?jsessionid=foo&q=r");
        assertThat(uri).hasToString("(com,øks,)/pAth-%C3%A5r?q=r");
    }

}
