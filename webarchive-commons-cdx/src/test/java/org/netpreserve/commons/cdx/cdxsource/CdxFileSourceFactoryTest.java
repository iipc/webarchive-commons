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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.netpreserve.commons.cdx.CdxSource;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriConfigs;

import static org.assertj.core.api.Assertions.*;

/**
 * Test methods for CdxFileSourceFactory.
 */
public class CdxFileSourceFactoryTest {

    /**
     * Test of createCdxSource method, of class CdxFileSourceFactory.
     */
    @Test
    public void testCreateCdxSource() {
        CdxFileSourceFactory instance = new CdxFileSourceFactory();

        Uri sourceIdentifier;
        CdxSource cdxSource;

        sourceIdentifier = UriConfigs.STRICT.buildUri("cdxfile:src/test/*/*.cdx");
        cdxSource = instance.createCdxSource(sourceIdentifier);
        assertThat(cdxSource).isNotNull().isInstanceOf(MultiCdxSource.class);

        sourceIdentifier = UriConfigs.STRICT.buildUri("cdxfile:src/test/resources/cdxfile1.cdx");
        cdxSource = instance.createCdxSource(sourceIdentifier);
        assertThat(cdxSource).isNotNull().isInstanceOf(BlockCdxSource.class);

        sourceIdentifier = UriConfigs.STRICT.buildUri("cdxfile:src/test/resources/foo.cdxj");
        cdxSource = instance.createCdxSource(sourceIdentifier);
        assertThat(cdxSource).isNull();

        sourceIdentifier = UriConfigs.STRICT.buildUri("cdxfile:src/test/resources/notcdx.cdx");
        cdxSource = instance.createCdxSource(sourceIdentifier);
        assertThat(cdxSource).isNull();
    }

    /**
     * Test resolution of files including paths with wildcards.
     */
    @Test
    public void testresolveFiles() {
        CdxFileSourceFactory instance = new CdxFileSourceFactory();

        Path sourcePath;
        List<Path> files;

        sourcePath = Paths.get("src/test/*/*.cdx");
        files = instance.resolveFiles(sourcePath);
        assertThat(files).contains(
                Paths.get("src/test/resources/cdxfile1.cdx"),
                Paths.get("src/test/resources/cdxfile2.cdx"),
                Paths.get("src/test/resources/cdxfile3.cdx"),
                Paths.get("src/test/resources/cdxfile4.cdx"));

        sourcePath = Paths.get("src/test/*/*4.cdx");
        files = instance.resolveFiles(sourcePath);
        assertThat(files).contains(
                Paths.get("src/test/resources/cdxfile4.cdx"));

        sourcePath = Paths.get("src/test/resources/*.cdx");
        files = instance.resolveFiles(sourcePath);
        assertThat(files).contains(
                Paths.get("src/test/resources/cdxfile1.cdx"),
                Paths.get("src/test/resources/cdxfile2.cdx"),
                Paths.get("src/test/resources/cdxfile3.cdx"),
                Paths.get("src/test/resources/cdxfile4.cdx"));

        sourcePath = Paths.get("src/test/resources/cdxfile?.cdx");
        files = instance.resolveFiles(sourcePath);
        assertThat(files).contains(
                Paths.get("src/test/resources/cdxfile1.cdx"),
                Paths.get("src/test/resources/cdxfile2.cdx"),
                Paths.get("src/test/resources/cdxfile3.cdx"),
                Paths.get("src/test/resources/cdxfile4.cdx"));

        sourcePath = Paths.get("src/test/resources/cdxfile1.*");
        files = instance.resolveFiles(sourcePath);
        assertThat(files).contains(Paths.get("src/test/resources/cdxfile1.cdx"));

        sourcePath = Paths.get("src/test/resources/cdxfile1.cdx");
        files = instance.resolveFiles(sourcePath);
        assertThat(files).contains(Paths.get("src/test/resources/cdxfile1.cdx"));

        sourcePath = Paths.get("src/test/resources");
        files = instance.resolveFiles(sourcePath);
        assertThat(files).contains(
                Paths.get("src/test/resources"));

        sourcePath = Paths.get("src/test/res*");
        files = instance.resolveFiles(sourcePath);
        assertThat(files).contains(
                Paths.get("src/test/resources"));

        sourcePath = Paths.get("src/test/resources/foo.cdxj");
        files = instance.resolveFiles(sourcePath);
        assertThat(files).isEmpty();
    }

    /**
     * Test of getSupportedScheme method, of class CdxFileSourceFactory.
     */
    @Test
    public void testGetSupportedScheme() {
        assertThat(new CdxFileSourceFactory().getSupportedScheme()).isEqualTo("cdxfile");
    }

}
