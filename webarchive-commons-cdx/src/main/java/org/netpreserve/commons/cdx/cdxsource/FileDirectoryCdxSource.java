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
package org.netpreserve.commons.cdx.cdxsource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;

import org.netpreserve.commons.cdx.CdxSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A CdxSource serving all files in a directory.
 * <p>
 * This CdxSource might also watch for changes to the contents of the directory.
 */
public class FileDirectoryCdxSource extends MultiCdxSource {

    private static final Logger LOG = LoggerFactory.getLogger(FileDirectoryCdxSource.class);

    private static final long WAITTIME_FOR_MODIFICATIONS_MS = 100;

    private final Path directory;

    private final boolean watch;

    private final Map<Path, CdxSource> files = new HashMap<>();

    static final WatcherService WATCHER = new WatcherService();

    static {
        CdxSourceExecutorService.getInstance().submit(WATCHER);
    }

    /**
     * Create a new FileDirectoryCdxSource.
     * <p>
     * The FileDirectoryCdxSource might watch the directory for changes to it's contents. A {@link WatchService} is used
     * to accomplish this and all conditions related to platform dependencies for that service applies for this class as
     * well.
     * <p>
     * @param directory the directory to serve
     * @param watch true if changes in directory contents should be watched
     */
    public FileDirectoryCdxSource(final Path directory, final boolean watch) {
        this.directory = directory.toAbsolutePath();
        this.watch = watch;
        init();
    }

    /**
     * Initialize this CdxSource with all the cdx files in the directory.
     */
    private void init() {
        if (Files.isDirectory(directory)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
                LOG.info("Adding all files in '{}' as cdx sources", directory);
                for (Path file : dirStream) {
                    add(file);
                }

                if (watch) {
                    LOG.info("Watches files in '{}' for modifications", directory);
                    WATCHER.register(this);
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else {
            throw new IllegalArgumentException("Path '" + directory + "' is not a directory");
        }
    }

    /**
     * Add a file to this directory.
     * <p>
     * @param file the file to add
     */
    public void add(Path file) {
        try {
            // Sanity checks
            if (!Files.isRegularFile(file)) {
                LOG.info("File '{}' is not a regular file and was ignored", file);
                return;
            }
            if (!Files.isSameFile(directory, file.getParent())) {
                LOG.info("File '{}' is not part of the directory '{}' and was ignored", file, directory);
                return;
            }

            // Create a CdxSource for the file and add it.
            file = file.toAbsolutePath();
            CdxSource cdxSource = new BlockCdxSource(new CdxFileDescriptor(file));
            addSource(cdxSource);
            files.put(file, cdxSource);
            LOG.info("Added file '{}' as a cdx source", file);
        } catch (Exception ex) {
            LOG.warn("Could not create CDX Source from '{}'. Cause: {}:{}", file,
                    ex.getClass().getName(), ex.getLocalizedMessage());
        }
    }

    /**
     * Remove a file from this directory.
     * <p>
     * @param file the file to remove
     */
    public void remove(Path file) {
        file = file.toAbsolutePath();
        CdxSource src = files.get(file);
        if (src != null) {
            try {
                removeSource(src);
                LOG.info("Removed file '{}' as a cdx source", file);
            } catch (Exception ex) {
                LOG.warn("Could not remove CDX Source for file '{}'. Cause: {}:{}", file,
                        ex.getClass().getName(), ex.getLocalizedMessage());
            }
        } else {
            LOG.info("File '{}' is not known as a cdx source in this directory '{}' and was ignored", file, directory);
        }
    }

    /**
     * The Service watching for changes in the directory.
     * <p>
     * This Service is a single instance for all instances of FileDirectoryCdxSource.
     */
    private static class WatcherService implements Callable<Void> {

        WatchService watchService;

        Map<WatchKey, FileDirectoryCdxSource> watched = new HashMap<>();

        Map<Path, DelayedModification> tasks = new HashMap<>();

        /**
         * Default constructor.
         */
        WatcherService() {
            try {
                watchService = FileSystems.getDefault().newWatchService();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        /**
         * Register a FileDirectoryCdxSource to be watched.
         * <p>
         * @param directoryCdxSource the FileDirectoryCdxSource which underlying directory should be watched
         * @return the watch key representing the registration of the directory with the underlying {@link WatchService}
         */
        public WatchKey register(FileDirectoryCdxSource directoryCdxSource) {
            try {
                Path dir = directoryCdxSource.directory;

                WatchKey key = dir.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                watched.put(key, directoryCdxSource);
                return key;
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        /**
         * Notify about a modified file.
         * <p>
         * If a taks exists for the file, it's execution time is updated. Otherwise a task is created and the execution
         * time is set {@link #WAITTIME_FOR_MODIFICATIONS_MS} milliseconds in the future.
         * <p>
         * @param cdxSource the source this modification applies to
         * @param file the modified file
         * @param kind the kind of modification
         */
        private void notifyDelayed(FileDirectoryCdxSource cdxSource, Path file, WatchEvent.Kind<?> kind) {
            DelayedModification task = tasks.get(file);
            if (task == null) {
                task = new DelayedModification(cdxSource, file, kind);
                tasks.put(file, task);
                CdxSourceExecutorService.getInstance().submit(task);
            } else {
                task.notReady();
            }
        }

        @Override
        public Void call() throws Exception {

            for (;;) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        LOG.error("File events overflowed");
                        continue;
                    }

                    Path file = ((Path) key.watchable()).resolve((Path) event.context());
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        FileDirectoryCdxSource cdxSource = watched.get(key);
                        if (cdxSource != null) {
                            notifyDelayed(cdxSource, file, event.kind());
                        }
                    } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        LOG.debug("File '{}' was removed", file);
                        FileDirectoryCdxSource cdxSource = watched.get(key);
                        if (cdxSource != null) {
                            cdxSource.remove(file);
                        }
                    } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        FileDirectoryCdxSource cdxSource = watched.get(key);
                        if (cdxSource != null) {
                            notifyDelayed(cdxSource, file, event.kind());
                        }
                    }
                }

                // reset the key
                key.reset();
            }
        }

        /**
         * A class that delay the execution of tasks caused by file system modifications.
         * <p>
         * It start with adding a constant ({@link #WAITTIME_FOR_MODIFICATIONS_MS}) to the current time. Another thread
         * might force this calculation to be updated by calling {@link #notReady()}. This is done when the file system
         * watcher receives {@link StandardWatchEventKinds#ENTRY_MODIFY} events. When the calculated time has passed,
         * the modification will be executed.
         */
        private final class DelayedModification implements Callable<Void> {

            long runAt;

            final FileDirectoryCdxSource cdxSource;

            final Path file;

            final WatchEvent.Kind<?> kind;

            /**
             * Construct a new DelayedModification task.
             * <p>
             * @param cdxSource the source this modification applies to
             * @param file the modified file
             * @param kind the kind of modification
             */
            DelayedModification(FileDirectoryCdxSource cdxSource, Path file, WatchEvent.Kind<?> kind) {
                this.cdxSource = cdxSource;
                this.file = file;
                this.kind = kind;
            }

            /**
             * Signal that it should still wait before applying the modification.
             */
            public void notReady() {
                runAt = System.currentTimeMillis() + WAITTIME_FOR_MODIFICATIONS_MS;
            }

            @Override
            public Void call() throws Exception {
                notReady();

                while ((runAt - System.currentTimeMillis()) > 0) {
                    LockSupport.parkUntil(this, runAt);
                }

                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    LOG.debug("Found new file '{}'", file);
                    cdxSource.add(file);
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    LOG.debug("File '{}' was removed", file);
                    cdxSource.remove(file);
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    LOG.info("File '{}' was modified, reloading", file);
                    cdxSource.remove(file);
                    cdxSource.add(file);
                }
                tasks.remove(file);
                return null;
            }

        }

    }
}
