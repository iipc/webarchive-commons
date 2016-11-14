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

import org.netpreserve.commons.uri.Scheme;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.uri.UriException;

/**
 *
 */
public class MimicBrowserReferenceResolver extends Rfc3986ReferenceResolver {

    @Override
    public void preResolve(UriBuilder base, UriBuilder reference) throws UriException {
        UriBuilderConfig config = base.config;

        if (!config.isStrictReferenceResolution() && base.scheme().equals(reference.scheme())) {
            reference.scheme(null);
        }

        // Check combinations of base and reference that can not be resolved
        if (!reference.schemeType().isSpecial()
                && !base.isAuthority()
                && !base.isAbsPath()
                && (!reference.isIPv4address() && !reference.isIPv6reference() && !reference.isRegName())
                && !reference.isAbsPath()
                && (reference.schemeType() == Scheme.UNDEFINED || reference.schemeType().isSpecial())
                && reference.fragment() == null
                || (reference.schemeType() == Scheme.UNDEFINED && base.schemeType() == Scheme.UNKNOWN && reference
                .fragment() == null && !base.isAbsPath())
                || (reference.schemeType().isSpecial() && !reference.isAuthority() && reference.path().isEmpty()
                && reference.query() == null && reference.fragment() == null)) {

            throw new UriException("Cannot resolve '" + reference.build() + "' against '" + base.build() + "'");

        }

        // Special handling of some combination of base and reference schemes when reference has no authority
        if (reference.scheme() != base.scheme() && !reference.isAuthority() && reference.schemeType().isSpecial()) {
            if (reference.schemeType() == Scheme.FILE) {
                reference.host("");
                if (!reference.path().startsWith("/")) {
                    reference.rawPath("/" + reference.path());
                }
            } else {
                int leadingSlashCount = 0;
                String path = reference.path();
                while (path.length() > leadingSlashCount && path.charAt(leadingSlashCount) == '/') {
                    leadingSlashCount++;
                }
                path = path.substring(leadingSlashCount);
                int slash = path.indexOf('/');
                if (slash > 0) {
                    reference.config.getParser().decomposeAuthority(reference, path.substring(0, slash));
                    reference.rawPath(path.substring(slash));
                } else {
                    reference.config.getParser().decomposeAuthority(reference, path);
                    reference.rawPath("/");
                }
            }
        }

        // Normalize backslashes only if base has one of the special schema types
        if (base.schemeType().isSpecial()) {
            reference.rawPath(reference.path().replace('\\', '/'));
        }

    }

    @Override
    boolean isAbsolutePath(UriBuilder base, UriBuilder reference) {
        String path = reference.path();
        int leadingSlashCount = 0;
        while (leadingSlashCount < path.length() && path.charAt(leadingSlashCount) == '/') {
            leadingSlashCount++;
        }

        if (isWindowsDriveLetter(path, leadingSlashCount)) {
            if (leadingSlashCount == 0) {
                reference.rawPath("/" + path);
                leadingSlashCount++;
            }
            base.clearAuthority();
            base.host("");
        }

        return leadingSlashCount > 0;
    }

    boolean isWindowsDriveLetter(String path, int offset) {
        return path.length() > offset + 1
                && Rfc3986Parser.ALPHA.get(path.charAt(offset))
                && (path.charAt(offset + 1) == ':' || (path.charAt(offset + 1) == '|'));
    }

}
