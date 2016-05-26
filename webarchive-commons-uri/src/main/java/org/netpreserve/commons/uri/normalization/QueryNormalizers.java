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
package org.netpreserve.commons.uri.normalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.netpreserve.commons.uri.PostParseNormalizer;
import org.netpreserve.commons.uri.UriBuilder;

import static org.netpreserve.commons.uri.UriBuilder.SCHEME_HTTP;
import static org.netpreserve.commons.uri.UriBuilder.SCHEME_HTTPS;
import static org.netpreserve.commons.uri.normalization.SchemeBasedNormalizer.immutableSetOf;

/**
 *
 */
public class QueryNormalizers extends SchemeBasedNormalizer implements PostParseNormalizer {

    private static final Set<String> SUPPORTED_SCHEMES = immutableSetOf(SCHEME_HTTP, SCHEME_HTTPS);

    public interface QueryNormalizer {

        void normalize(SortedMap<String, List<String>> queries);

    }

    List<QueryNormalizer> normalizers = new ArrayList<>();

    public QueryNormalizers() {
        normalizers.add(new StripSessionId());
    }

    @Override
    public void normalize(UriBuilder builder) {
        if (!normalizers.isEmpty() && builder.query() != null) {
            SortedMap<String, List<String>> queries = new TreeMap<>();

            for (String queryElement : builder.query().split("&")) {
                String[] keyValue = queryElement.split("=", 2);
                List<String> values = queries.get(keyValue[0]);
                if (values == null) {
                    values = new ArrayList<>();
                    queries.put(keyValue[0], values);
                }
                if (keyValue.length > 1) {
                    values.add(keyValue[1]);
                }
            }

            for (QueryNormalizer normalizer : normalizers) {
                normalizer.normalize(queries);
            }

            if (queries.isEmpty()) {
                builder.query(null);
            } else {
                StringBuilder query = new StringBuilder();
                for (Map.Entry<String, List<String>> entry : queries.entrySet()) {
                    if (entry.getValue().isEmpty()) {
                        if (query.length() > 0) {
                            query.append('&');
                        }
                        query.append(entry.getKey());
                    } else {
                        Collections.sort(entry.getValue());
                        for (String value : entry.getValue()) {
                            if (query.length() > 0) {
                                query.append('&');
                            }
                            query.append(entry.getKey()).append('=').append(value);
                        }
                    }
                }
                builder.query(query.toString());
            }
        }
    }

    @Override
    public Set<String> getSupportedSchemes() {
        return SUPPORTED_SCHEMES;
    }

}
