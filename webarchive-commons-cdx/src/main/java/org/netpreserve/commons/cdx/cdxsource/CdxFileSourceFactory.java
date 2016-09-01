/*
 * Copyright 2015 IIPC.
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
package org.netpreserve.commons.cdx.cdxsource;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.netpreserve.commons.cdx.CdxSourceFactory;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.netpreserve.commons.cdx.CdxSource;
import org.netpreserve.commons.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating CDX sources from OS files.
 * <p>
 * This factory supports cdx identifiers of the form {@code cdxfile:<path>}.
 * <p>
 * The {@code <path>} could be file or a directory. If it is a directory, all the files in the directory will be added.
 * It supports the simple wildcards {@code *} (any number of characters) and {@code ?} (one character).
 * <p>
 * Examples:
 * <ul>
 * <li>{@code /dir1/dir2/*.cdxj} - any file in {@code /dir1/dir2/} ending with {@code .cdxj}
 * <li>{@code /dir1/dir?/} - any file in any subdirectory of {@code /dir1/} which contains 4 characters starting with
 * {@code dir}
 * </ul>
 */
public class CdxFileSourceFactory extends CdxSourceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CdxFileSourceFactory.class);

    @Override
    public CdxSource createCdxSource(Uri identifier) {
        Path sourcePath = Paths.get(identifier.getPath());

        List<Path> files = resolveFiles(sourcePath);

        if (files.isEmpty()) {
            return null;
        }

        try {
            CdxSource cdxSource;
            switch (files.size()) {
                case 0:
                    cdxSource = null;
                    break;
                case 1:
                    cdxSource = new BlockCdxSource(new CdxFileDescriptor(files.get(0)));
                    break;
                default:
                    cdxSource = new MultiCdxSource();
                    for (Path file : files) {
                        ((MultiCdxSource) cdxSource).addSource(new BlockCdxSource(new CdxFileDescriptor(file)));
                    }
                    break;
            }
            return cdxSource;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Resolve files from a path potentially containing wildcards.
     * <p>
     * @param sourcePath the path
     * @return a List of discovered files. Might be empty, but never null.
     */
    List<Path> resolveFiles(Path sourcePath) {
        List<Path> files = new ArrayList<>();
        return innerResolveFiles(sourcePath, 1, files);
    }

    /**
     * The recursive traversal of files and directories.
     * <p>
     * @param sourcePath the potentially modified source path (if original contains wildcard)
     * @param nameIdx the index of the path element to be evaluated for wildcards
     * @param files the list of discovered files to add to
     * @return the list of discovered files is returned as a convenience
     */
    private List<Path> innerResolveFiles(Path sourcePath, int nameIdx, List<Path> files) {
        // The sourcePath is a regular file, add it and return.
        if (Files.isRegularFile(sourcePath)) {
            files.add(sourcePath);
            LOG.info("Adding file '{}' as a cdx source", sourcePath);
            return files;
        }

        // The sourcePath is a directory, add all files in directory and return.
        if (Files.isDirectory(sourcePath)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(sourcePath)) {
                LOG.info("Adding all files in '{}' as cdx sources", sourcePath.toAbsolutePath());
                for (Path file : dirStream) {
                    if (Files.isRegularFile(file)) {
                        files.add(file);
                        LOG.info("Adding file '{}' as a cdx source", file);
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return files;
        }

        // The source path could not be found, see if there are wildcards in the path and process.
        Path rootPath = sourcePath.subpath(0, nameIdx);

        if (Files.isDirectory(rootPath)) {
            innerResolveFiles(sourcePath, nameIdx + 1, files);
            return files;
        }

        String currentName = rootPath.getFileName().toString();

        if (currentName.contains("*") || currentName.contains("?")) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(rootPath.getParent(), currentName)) {
                for (Path file : dirStream) {
                    if (Files.isRegularFile(file)) {
                        files.add(file);
                        LOG.info("Adding file '{}' as a cdx source", file);
                    } else {
                        if (nameIdx < sourcePath.getNameCount()) {
                            Path resolvedPath = file.resolve(sourcePath.subpath(nameIdx, sourcePath.getNameCount()));

                            innerResolveFiles(resolvedPath, nameIdx + 1, files);
                        } else {
                            innerResolveFiles(file, nameIdx + 1, files);
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else {
            LOG.warn("Could not find file: {}", sourcePath.toAbsolutePath());
            return files;
        }
        return files;
    }

    @Override
    public final String getSupportedScheme() {
        return "cdxfile";
    }

}
