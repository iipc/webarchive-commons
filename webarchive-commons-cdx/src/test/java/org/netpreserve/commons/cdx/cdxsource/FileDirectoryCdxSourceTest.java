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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netpreserve.commons.cdx.CdxRecord;
import org.netpreserve.commons.cdx.FieldName;
import org.netpreserve.commons.cdx.SearchKeyTemplate;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for class FileDirectoryCdxSource.
 */
public class FileDirectoryCdxSourceTest {

    TestLogger logger = TestLoggerFactory.getTestLogger(FileDirectoryCdxSource.class);

    Path testDir = Paths.get("target/testdir/watchedcdx");

    /**
     * Clear the logs after each run.
     */
    @After
    public void clearLoggers() {
        TestLoggerFactory.clear();
    }

    /**
     * Make sure we are using a clean test directory.
     * <p>
     * @throws IOException is thrown if the test directory could not be created or cleaned.
     */
    @Before
    public void cleanupTestDir() throws IOException {
        if (Files.isDirectory(testDir)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(testDir)) {
                for (Path file : dirStream) {
                    Files.deleteIfExists(file);
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        Files.createDirectories(testDir);
    }

    /**
     * Test watching a live directory.
     * <p>
     * @throws Exception should never be thrown
     */
    @Test
    public void testWatchDirectory() throws Exception {
        Path nonCdx = testDir.resolve("foo.txt");
        Path source1 = Paths.get(ClassLoader.getSystemResource("cdxfile1.cdx").toURI());
        Path dest1 = testDir.resolve(source1.getFileName());
        Path source2 = Paths.get(ClassLoader.getSystemResource("cdxfile2.cdx").toURI());
        Path dest2 = testDir.resolve(source2.getFileName());

        FileDirectoryCdxSource cdxSource = new FileDirectoryCdxSource(testDir, true);

        // Create two files, one cdx and one empty
        assertThat(cdxSource.size()).isZero();
        Files.createFile(nonCdx);
        Files.copy(source1, dest1);
        // Should not be detected yet since we are waiting to see if file is done with modifications
        assertThat(cdxSource.size()).isZero();
        Thread.sleep(150);
        // The Cdx file should now be detected, the empty one ignored
        assertThat(cdxSource.size()).isEqualTo(1);

        // Check that the first record in 'dest1' can be found
        CdxRecord r = cdxSource.search(new SearchKeyTemplate(), null, false).iterator().next();
        assertThat(r.get(FieldName.ORIGINAL_URI)).hasToString("http://www.hotel.as/robots.txt");

        // Copy another file make sure it takes more than the 100ms grace period to verify that it is
        // not added before it is completely written.
        List<String> lines = Files.readAllLines(source2);
        Files.createFile(dest2);
        for (String line : lines) {
            Files.write(dest2, line.getBytes(), StandardOpenOption.APPEND);
            Files.write(dest2, new byte[]{'\n'}, StandardOpenOption.APPEND);
            assertThat(cdxSource.size()).isEqualTo(1);
            Thread.sleep(1);
        }
        assertThat(cdxSource.size()).isEqualTo(1);
        Thread.sleep(150);
        assertThat(cdxSource.size()).isEqualTo(2);

        // Check that the first record in 'dest2' can be found
        r = cdxSource.search(new SearchKeyTemplate(), null, false).iterator().next();
        assertThat(r.get(FieldName.ORIGINAL_URI)).hasToString("http://www.the-islander.org.ac/robots.txt");

        // Check that modified file gets reloaded
        Files.setLastModifiedTime(dest1, FileTime.fromMillis(System.currentTimeMillis()));
        assertThat(logger.getAllLoggingEvents()).doesNotContain(LoggingEvent
                .info("File '{}' was modified, reloading", dest1.toAbsolutePath()));
        Thread.sleep(150);
        assertThat(logger.getAllLoggingEvents()).contains(LoggingEvent.info("File '{}' was modified, reloading", dest1
                .toAbsolutePath()));
        assertThat(cdxSource.size()).isEqualTo(2);

        // Delete the empty file and the last added cdx file and check that only the first cdx file is used
        Files.delete(nonCdx);
        Files.delete(dest2);
        Thread.sleep(150);
        assertThat(cdxSource.size()).isEqualTo(1);
        r = cdxSource.search(new SearchKeyTemplate(), null, false).iterator().next();
        assertThat(r.get(FieldName.ORIGINAL_URI)).hasToString("http://www.hotel.as/robots.txt");
    }

}
