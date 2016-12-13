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

import java.util.Set;

import org.netpreserve.commons.uri.PostParseNormalizer;
import org.netpreserve.commons.uri.Scheme;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.normalization.report.Description;
import org.netpreserve.commons.uri.normalization.report.Example;

import static org.netpreserve.commons.uri.Scheme.DNS;
import static org.netpreserve.commons.uri.normalization.SchemeBasedNormalizer.immutableSetOf;

public class OptimisticDnsScheme extends SchemeBasedNormalizer implements PostParseNormalizer {

    private static final Set<Scheme> SUPPORTED_SCHEMES = immutableSetOf(DNS);

    @Override
    @Description(name = "Optimistic DNS scheme",
                 description = "If dns host is found in the authority, then it is moved to the path. "
                 + "If dns host is found in the path, then it is trimmed for leading and trailing slashes.")
    @Example(uri = "dns:www.example.com", normalizedUri = "dns:www.example.com")
    @Example(uri = "dns://www.example.com/one.html", normalizedUri = "dns:www.example.com")
    @Example(uri = "dns:///www.example.com/", normalizedUri = "dns:www.example.com")
    public void normalize(UriBuilder builder) {
        if (builder.host() != null && !builder.host().isEmpty()) {
            builder.path(builder.host());
            builder.clearAuthority();
        } else if (!builder.path().isEmpty() && builder.isAbsPath()) {
            builder.clearAuthority();
            builder.path(builder.path().replaceFirst("^/*(.*?)/*$", "$1"));
        }
    }

    @Override
    public Set<Scheme> getSupportedSchemes() {
        return SUPPORTED_SCHEMES;
    }

}
