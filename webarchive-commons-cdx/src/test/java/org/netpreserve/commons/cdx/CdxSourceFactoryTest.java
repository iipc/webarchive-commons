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
package org.netpreserve.commons.cdx;


import org.junit.Test;
import org.netpreserve.commons.cdx.cdxsource.BlockCdxSource;
import org.netpreserve.commons.cdx.cdxsource.MultiCdxSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Test methods for CdxSourceFactory.
 */
public class CdxSourceFactoryTest {

    /**
     * Test of getDescriptor method, of class CdxSourceFactory.
     */
    @Test
    public void testGetDescriptor() {
        CdxSource cdxSource;

        cdxSource = CdxSourceFactory.getCdxSource("nonexistent:");
        assertThat(cdxSource).isNull();

        cdxSource = CdxSourceFactory.getCdxSource("cdxfile:src/test/*/*.cdx");
        assertThat(cdxSource).isNotNull().isInstanceOf(MultiCdxSource.class);

        cdxSource = CdxSourceFactory.getCdxSource("cdxfile:src/test/resources/cdxfile1.cdx");
        assertThat(cdxSource).isNotNull().isInstanceOf(BlockCdxSource.class);

        cdxSource = CdxSourceFactory.getCdxSource("cdxfile:src/test/resources/foo.cdxj");
        assertThat(cdxSource).isNull();
    }

}
