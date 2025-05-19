/*
 *  This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.uid;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * @author stack
 * @version $Revision$ $Date$
 */
public class UUIDGeneratorTest {
	@Test
	public void testQualifyRecordID() throws URISyntaxException {
		RecordIDGenerator g = new UUIDGenerator();
		URI uri = g.getRecordID();
		Map<String, String> qualifiers = new HashMap<String, String>();
		qualifiers.put("a", "b");
		URI nuURI = g.qualifyRecordID(uri, qualifiers);
		assertNotSame(uri, nuURI);
		qualifiers.put("c", "d");
		nuURI = g.qualifyRecordID(nuURI, qualifiers);
		assertNotSame(uri, nuURI);
	}
}
