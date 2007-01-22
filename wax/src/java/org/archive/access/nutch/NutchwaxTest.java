/* $Id$
 *
 * Created Sep 5, 2006
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Heritrix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.access.nutch;

import java.io.IOException;

import org.apache.hadoop.io.Text;

import junit.framework.TestCase;

/**
 * @author stack
 * @version $Date$ $Version$
 */
public class NutchwaxTest extends TestCase {
	public void testGetCollectionFromWaxKey() throws IOException {
		String key = " c=nla2005,u=http://www.funkmymobile.com.au/en/" +
		    "download.php?f=c_14&a=khamega&type=14&id=5247&son_nom=" +
		    "??%20Janta%20??%20Ja&son_wav=jantaja.mp3&lang=EN&c=GB\n";
		String collection = "nla2005";
		assertEquals(Nutchwax.getCollectionFromWaxKey(new Text(key)),
			collection);
		key = "c=nla2005,u=http://www.mobilewallpapers.com.au/en/download." +
			"php?f=c_14&a=mowall&type=14&id=5247&son_nom=??%20Janta%20??%20" +
			"Ja&son_wav=jantaja.mp3&lang=EN&c=GB";
		assertEquals(Nutchwax.getCollectionFromWaxKey(new Text(key)),
			collection);
		key = "c=5b5c430260d421a0ac8fdd461142e867,u=http://history." +
			"sacentral.sa.gov.au/site/page.cfm?u=47&listMode=" +
			"listLinks&path=\n 4873,4884,4894";
		collection = "5b5c430260d421a0ac8fdd461142e867";
		assertEquals(Nutchwax.getCollectionFromWaxKey(new Text(key)),
				collection);
		String url = "http://history." +
			"sacentral.sa.gov.au/site/page.cfm?u=47&listMode=" +
			"listLinks&path=\n 4873,4884,4894";
		assertEquals(Nutchwax.getUrlFromWaxKey(new Text(key)),
				url);
	}
}