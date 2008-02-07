/* SURTTokenizerTest
 *
 * $Id: SURTTokenizerTest.java 5056 2007-04-11 17:15:33Z paul_jack $
 *
 * Created on 3:40:18 PM May 11, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.surt;

import junit.framework.TestCase;

import org.apache.commons.httpclient.URIException;
import org.archive.surt.SURTTokenizer2;

/**
 *
 *
 * @author brad
 * @version $Date: 2007-04-11 10:15:33 -0700 (Wed, 11 Apr 2007) $, $Revision: 5056 $
 */
public class SURTTokenizer2Test extends TestCase {

        SURTTokenizer2 tok;
        /**
         * Test method for 'org.archive.wayback.accesscontrol.SURTTokenizer.nextSearch()'
         */
        public void testSimple() {
                tok = toSurtT("http://www.archive.org/foo");
                assertEquals("(org,archive,www,)/foo\t",tok.nextSearch());
                assertEquals("(org,archive,www,)/foo",tok.nextSearch());
                assertEquals("(org,archive,www,",tok.nextSearch());
                assertEquals("(org,archive,www",tok.nextSearch());
                assertEquals("(org,archive",tok.nextSearch());
                assertEquals("(org",tok.nextSearch());
                assertNull(tok.nextSearch());
        }
        /** test */
        public void testSimpleSurt() {
            tok = SURTTokenizer2.newFromSURT("http://(org,archive,www,)/foo");
            assertEquals("(org,archive,www,)/foo\t",tok.nextSearch());
            assertEquals("(org,archive,www,)/foo",tok.nextSearch());
            assertEquals("(org,archive,www,",tok.nextSearch());
            assertEquals("(org,archive,www",tok.nextSearch());
            assertEquals("(org,archive",tok.nextSearch());
            assertEquals("(org",tok.nextSearch());
            assertNull(tok.nextSearch());
        }
        /** test */
        public void testSlashPath() {
                tok = toSurtT("http://www.archive.org/");
                assertEquals("(org,archive,www,)/\t",tok.nextSearch());
                assertEquals("(org,archive,www,)/",tok.nextSearch());
                assertEquals("(org,archive,www,",tok.nextSearch());
                assertEquals("(org,archive,www",tok.nextSearch());
                assertEquals("(org,archive",tok.nextSearch());
                assertEquals("(org",tok.nextSearch());
                assertNull(tok.nextSearch());           
        }

        /** test */
        public void testEmptyPath() {
                tok = toSurtT("http://www.archive.org");
                assertEquals("(org,archive,www,)/\t",tok.nextSearch());
                assertEquals("(org,archive,www,)/",tok.nextSearch());
                assertEquals("(org,archive,www,",tok.nextSearch());
                assertEquals("(org,archive,www",tok.nextSearch());
                assertEquals("(org,archive",tok.nextSearch());
                assertEquals("(org",tok.nextSearch());
                assertNull(tok.nextSearch());           
        }


        /** test */
        public void testEmptyPathMore() {
                tok = toSurtT("http://brad.www.archive.org");
                assertEquals("(org,archive,www,brad,)/\t",tok.nextSearch());
                assertEquals("(org,archive,www,brad,)/",tok.nextSearch());
                assertEquals("(org,archive,www,brad,",tok.nextSearch());
                assertEquals("(org,archive,www,brad",tok.nextSearch());
                assertEquals("(org,archive,www",tok.nextSearch());
                assertEquals("(org,archive",tok.nextSearch());
                assertEquals("(org",tok.nextSearch());
                assertNull(tok.nextSearch());           
        }       
        /** test */
        public void testLongPathMore() {
                tok = toSurtT("http://brad.www.archive.org/one/two");
                assertEquals("(org,archive,www,brad,)/one/two\t",tok.nextSearch());
                assertEquals("(org,archive,www,brad,)/one/two",tok.nextSearch());
                assertEquals("(org,archive,www,brad,)/one",tok.nextSearch());
                assertEquals("(org,archive,www,brad,",tok.nextSearch());
                assertEquals("(org,archive,www,brad",tok.nextSearch());
                assertEquals("(org,archive,www",tok.nextSearch());
                assertEquals("(org,archive",tok.nextSearch());
                assertEquals("(org",tok.nextSearch());
                assertNull(tok.nextSearch());           
        }       
        /** test */
        public void testShortPathHash() {
                tok = toSurtT("http://www.archive.org/one/two#hash");
                assertEquals("(org,archive,www,)/one/two\t",tok.nextSearch());
                assertEquals("(org,archive,www,)/one/two",tok.nextSearch());
                assertEquals("(org,archive,www,)/one",tok.nextSearch());
                assertEquals("(org,archive,www,",tok.nextSearch());
                assertEquals("(org,archive,www",tok.nextSearch());
                assertEquals("(org,archive",tok.nextSearch());
                assertEquals("(org",tok.nextSearch());
                assertNull(tok.nextSearch());           
        }
        /** test */
        public void testCGI1() {
                tok = toSurtT("http://www.archive.org/cgi?foobar");
                assertEquals("(org,archive,www,)/cgi?foobar\t",tok.nextSearch());
                assertEquals("(org,archive,www,)/cgi?foobar",tok.nextSearch());
                assertEquals("(org,archive,www,)/cgi",tok.nextSearch());
                assertEquals("(org,archive,www,",tok.nextSearch());
                assertEquals("(org,archive,www",tok.nextSearch());
                assertEquals("(org,archive",tok.nextSearch());
                assertEquals("(org",tok.nextSearch());
                assertNull(tok.nextSearch());           
        }
        /** test */
        public void testPort() {
                tok = toSurtT("http://www.archive.org:8080/cgi?foobar");
                assertEquals("(org,archive,www,:8080)/cgi?foobar\t",tok.nextSearch());
                assertEquals("(org,archive,www,:8080)/cgi?foobar",tok.nextSearch());
                assertEquals("(org,archive,www,:8080)/cgi",tok.nextSearch());
                assertEquals("(org,archive,www,:8080",tok.nextSearch());
                assertEquals("(org,archive,www",tok.nextSearch());
                assertEquals("(org,archive",tok.nextSearch());
                assertEquals("(org",tok.nextSearch());
                assertNull(tok.nextSearch());
        }
        /** test */
        public void testLogin() {
                tok = toSurtT("http://brad@www.archive.org/cgi?foobar");
                assertEquals("(org,archive,www,@brad)/cgi?foobar\t",tok.nextSearch());
                assertEquals("(org,archive,www,@brad)/cgi?foobar",tok.nextSearch());
                assertEquals("(org,archive,www,@brad)/cgi",tok.nextSearch());
                assertEquals("(org,archive,www,@brad",tok.nextSearch());
                assertEquals("(org,archive,www",tok.nextSearch());
                assertEquals("(org,archive",tok.nextSearch());
                assertEquals("(org",tok.nextSearch());
                assertNull(tok.nextSearch());           
        }
        /** test */
        public void testLoginPass() {
                tok = toSurtT("http://brad:pass@www.archive.org/cgi?foobar");
                assertEquals("(org,archive,www,@brad:pass)/cgi?foobar\t",tok.nextSearch());
                assertEquals("(org,archive,www,@brad:pass)/cgi?foobar",tok.nextSearch());
                assertEquals("(org,archive,www,@brad:pass)/cgi",tok.nextSearch());
                assertEquals("(org,archive,www,@brad:pass",tok.nextSearch());
                assertEquals("(org,archive,www",tok.nextSearch());
                assertEquals("(org,archive",tok.nextSearch());
                assertEquals("(org",tok.nextSearch());
                assertNull(tok.nextSearch());           
        }
//      /** test */
        // leave this guy out for now: was a bug in Heritrix thus archive-commons
        // wait for new jar...
//      public void testLoginPassPort() {
//              tok = toSurtT("http://brad:pass@www.archive.org:8080/cgi?foobar");
//              assertEquals("(org,archive,www,:8080@brad:pass)/cgi?foobar\t",tok.nextSearch());
//              assertEquals("(org,archive,www,:8080@brad:pass)/cgi?foobar",tok.nextSearch());
//              assertEquals("(org,archive,www,:8080@brad:pass)/cgi",tok.nextSearch());
//              assertEquals("(org,archive,www,:8080@brad:pass",tok.nextSearch());
//              assertEquals("(org,archive,www,:8080",tok.nextSearch());
//              assertEquals("(org,archive,www",tok.nextSearch());
//              assertEquals("(org,archive",tok.nextSearch());
//              assertEquals("(org",tok.nextSearch());
//              assertNull(tok.nextSearch());
//      }
//      
        
        private SURTTokenizer2 toSurtT(final String u) {
                SURTTokenizer2 tok = null;
                try {
                        tok = new SURTTokenizer2(u);
                } catch (URIException e) {
                        e.printStackTrace();
                        assertFalse("URL Exception " + e.getLocalizedMessage(),true);
                }
                return tok;
        }
        
}
