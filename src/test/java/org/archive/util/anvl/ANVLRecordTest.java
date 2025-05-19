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

package org.archive.util.anvl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ANVLRecordTest {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Test
    public void testAdd() {
        ANVLRecord am = new ANVLRecord();
        am.add(new Element(new Label("entry")));
        am.add(new Element(new Label("who"),
            new Value("Gilbert, W.S. | Sullivan, Arthur")));
        am.add(new Element(new Label("what"),
                new Value("\rThe Yeoman of \rthe guard")));
        am.add(new Element(new Label("what"),
            new Value("The Yeoman of\r\n  the guard")));
        am.add(new Element(new Label("what"),
                new Value("The Yeoman of \n\tthe guard")));
        am.add(new Element(new Label("what"),
                new Value("The Yeoman of \r        the guard")));
        am.add(new Element(new Label("when/created"),
            new Value("1888")));
        logger.fine(am.toString());
        Map<String,String> m = am.asMap();
        logger.fine(m.toString());
    }

    @Test
    public void testEmptyRecord() throws Exception {
    	byte [] b = ANVLRecord.EMPTY_ANVL_RECORD.getUTF8Bytes();
    	assertEquals(2, b.length);
    	assertEquals('\r', b[0]);
    	assertEquals('\n', b[1]);
    }

    @Test
    public void testFolding() throws Exception {
        ANVLRecord am = new ANVLRecord();
        Exception e = null;
        try {
            am.addLabel("Label with \n in it");
        } catch (IllegalArgumentException iae) {
            e = iae;
        }
        assertInstanceOf(IllegalArgumentException.class, e);
        am.addLabelValue("label", "value with \n in it");
    }

    @Test
    public void testParse() throws UnsupportedEncodingException, IOException {
        String record = "   a: b\r\n#c#\r\nc:d\r\n \t\t\r\t\n\te" +
                "\r\nx:\r\n  # z\r\n\r\n";
        ANVLRecord r = ANVLRecord.load(new ByteArrayInputStream(
                record.getBytes(StandardCharsets.ISO_8859_1)));
        logger.fine(r.toString());
        assertEquals("a: b", r.get(0).toString());
        record = "   a: b\r\n\r\nsdfsdsdfds";
        r = ANVLRecord.load(new ByteArrayInputStream(
            record.getBytes(StandardCharsets.ISO_8859_1)));
        logger.fine(r.toString());
        record = "x:\r\n  # z\r\ny:\r\n\r\n";
        r = ANVLRecord.load(new ByteArrayInputStream(
            record.getBytes(StandardCharsets.ISO_8859_1)));
        logger.fine(r.toString());
        assertEquals("x:", r.get(0).toString());
    }

    @Test
    public void testExampleParse()
    throws IOException {
    	final String sample = "entry:\t\t\r\n# first ###draft\r\n" +
    		"who:\tGilbert, W.S. | Sullivan, Arthur\r\n" +
    		"what:\tThe Yeoman of\r\n" +
    		"\t\tthe Guard\r\n" +
    		"when/created:\t 1888\r\n\r\n";
        ANVLRecord r = ANVLRecord.load(new ByteArrayInputStream(
        		sample.getBytes(StandardCharsets.ISO_8859_1)));
        logger.fine(r.toString());
    }

    @Test
    public void testPoundLabel()
    throws IOException {
    	final String sample = "ent#ry:\t\t\r\n# first ###draft\r\n" +
    		"who:\tGilbert, W.S. | Sullivan, Arthur\r\n" +
    		"what:\tThe Yeoman of\r\n" +
    		"\t\tthe Guard\r\n" +
    		"when/created:\t 1888\r\n\r\n";
        ANVLRecord r = ANVLRecord.load(sample);
        logger.fine(r.toString());
    }

    @Test
    public void testNewlineLabel()
    throws IOException {
    	final String sample = "ent\nry:\t\t\r\n# first ###draft\r\n" +
    		"who:\tGilbert, W.S. | Sullivan, Arthur\r\n" +
    		"what:\tThe Yeoman of\r\n" +
    		"\t\tthe Guard\r\n" +
    		"when/created:\t 1888\r\n\r\n";
    	IllegalArgumentException iae = null;
    	try {
    		ANVLRecord.load(sample);
    	} catch(IllegalArgumentException e) {
    		iae = e;
    	}
        assertNotNull(iae);
    }
}
