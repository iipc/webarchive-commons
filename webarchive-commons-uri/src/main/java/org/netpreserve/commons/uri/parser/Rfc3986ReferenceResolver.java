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

import org.netpreserve.commons.uri.ReferenceResolver;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriBuilderConfig;
import org.netpreserve.commons.uri.UriException;

/**
 *
 */
public class Rfc3986ReferenceResolver implements ReferenceResolver {

    @Override
    public void resolve(UriBuilder base, UriBuilder reference) throws UriException {
        UriBuilderConfig config = base.config;

        preResolve(base, reference);

        if (reference.scheme() != null) {
            base.scheme(reference.scheme());
            base.user(reference.user());
            base.password(reference.password());
            base.host(reference.host());
            base.port(reference.port());
            if (config.isPathSegmentNormalization()) {
                base.rawPath(removeDotSegments(reference.path()));
            } else {
                base.rawPath(reference.path());
            }
            base.rawQuery(reference.query());
            base.isAbsPath(reference.isAbsPath());
            base.copyHostFlags(reference);
        } else {
            if (reference.isAuthority()) {
                base.user(reference.user());
                base.password(reference.password());
                base.host(reference.host());
                base.port(reference.port());
                if (config.isPathSegmentNormalization()) {
                    base.rawPath(removeDotSegments(reference.path()));
                } else {
                    base.rawPath(reference.path());
                }
                base.rawQuery(reference.query());
                base.isAbsPath(reference.isAbsPath());
                base.copyHostFlags(reference);
            } else {
                if (reference.path().isEmpty()) {
                    if (reference.query() != null) {
                        base.rawQuery(reference.query());
                    }
                } else {
                    if (isAbsolutePath(base, reference)) {
                        if (config.isPathSegmentNormalization()) {
                            base.rawPath(removeDotSegments(reference.path()));
                        } else {
                            base.rawPath(reference.path());
                        }
                    } else {
                        mergePath(base, reference);
                        if (config.isPathSegmentNormalization()) {
                            base.rawPath(removeDotSegments(base.path()));
                        }
                        base.rawPath(removeDotSegments(base.path()));
                    }
                    base.rawQuery(reference.query());
                }
            }
        }
        base.rawFragment(reference.fragment());
    }

    public void preResolve(UriBuilder base, UriBuilder reference) throws UriException {
        UriBuilderConfig config = base.config;

        if (!config.isStrictReferenceResolution() && base.scheme().equals(reference.scheme())) {
            reference.scheme(null);
        }
    }

    boolean isAbsolutePath(UriBuilder base, UriBuilder reference) {
        return reference.path().startsWith("/");
    }

    /**
     * Resolve the base and relative path.
     * <p>
     * @param base a UriBuilder for the basePath.
     * @param reference a UriBuilder for the reference's path
     */
    public void mergePath(UriBuilder base, UriBuilder reference) {
        if (base.isAuthority() && base.path().isEmpty()) {
            base.rawPath("/" + reference.path());
            return;
        }

        if (reference.path().isEmpty()) {
            return;
        } else if (reference.path().charAt(0) == '/') {
            base.rawPath(reference.path());
        } else {
            int at = base.path().lastIndexOf('/');
            if (at != -1) {
                base.rawPath(base.path().substring(0, at + 1));
            }
            StringBuilder buff = new StringBuilder(base.path().length() + reference.path().length());
            buff.append((at != -1) ? base.path().substring(0, at + 1) : "/");
            buff.append(reference.path());
            base.rawPath(buff.toString());
        }
    }

    /**
     * Normalize the given hier path part.
     * <p>
     * <p>
     * Algorithm taken from URI reference parser at
     * http://www.apache.org/~fielding/uri/rev-2002/issues.html.
     * <p>
     * @param path the path to removeDotSegments
     * @return the normalized path
     * @throws UriException no more higher path level to be normalized
     */
    public String removeDotSegments(String path) throws UriException {

        if (path == null) {
            return null;
        }

        String normalized = path;

        // If the buffer begins with "./" or "../", the "." or ".." is removed.
        if (normalized.startsWith("./")) {
            normalized = normalized.substring(1);
        } else if (normalized.startsWith("../")) {
            normalized = normalized.substring(2);
        } else if (normalized.startsWith("..")) {
            normalized = normalized.substring(2);
        }

        // All occurrences of "/./" in the buffer are replaced with "/"
        int index = -1;
        while ((index = normalized.indexOf("/./")) != -1) {
            normalized = normalized.substring(0, index) + normalized.substring(index + 2);
        }

        // If the buffer ends with "/.", the "." is removed.
        if (normalized.endsWith("/.")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        int startIndex = 0;

        // All occurrences of "/<segment>/../" in the buffer, where ".."
        // and <segment> are complete path segments, are iteratively replaced
        // with "/" in order from left to right until no matching pattern remains.
        // If the buffer ends with "/<segment>/..", that is also replaced
        // with "/".  Note that <segment> may be empty.
        while ((index = normalized.indexOf("/../", startIndex)) != -1) {
            int slashIndex = normalized.lastIndexOf('/', index - 1);
            if (slashIndex >= 0) {
                normalized = normalized.substring(0, slashIndex) + normalized.substring(index + 3);
            } else {
                startIndex = index + 3;
            }
        }
        if (normalized.endsWith("/..")) {
            int slashIndex = normalized.lastIndexOf('/', normalized.length() - 4);
            if (slashIndex >= 0) {
                normalized = normalized.substring(0, slashIndex + 1);
            }
        }

        // All prefixes of "<segment>/../" in the buffer, where ".."
        // and <segment> are complete path segments, are iteratively replaced
        // with "/" in order from left to right until no matching pattern remains.
        // If the buffer ends with "<segment>/..", that is also replaced
        // with "/".  Note that <segment> may be empty.
        while ((index = normalized.indexOf("/../")) != -1) {
            int slashIndex = normalized.lastIndexOf('/', index - 1);
            if (slashIndex >= 0) {
                break;
            } else {
                normalized = normalized.substring(index + 3);
            }
        }
        if (normalized.endsWith("/..")) {
            int slashIndex = normalized.lastIndexOf('/', normalized.length() - 4);
            if (slashIndex < 0) {
                normalized = "/";
            }
        }

        return normalized;
    }

}
