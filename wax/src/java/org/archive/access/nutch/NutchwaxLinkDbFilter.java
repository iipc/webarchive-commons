/* $Id$
 * 
 * Created on January 4, 2007
 *
 * Copyright (C) 2007 Internet Archive.
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
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.nutch.crawl.LinkDbFilter;

/**
 * Override so we can meddle with the key passed the superclass stripping
 * collection (then, when the super's mapper is done, put the collection back.
 * @author stack
 */
public class NutchwaxLinkDbFilter extends LinkDbFilter {
    @Override
    public void map(WritableComparable key, Writable value,
        final OutputCollector output, Reporter r)
    throws IOException {
        final String collection = Nutchwax.getCollectionFromWaxKey(key);
        final OutputCollector oo = new OutputCollector() {
            public void collect(WritableComparable k, Writable v)
            throws IOException {
                output.collect(Nutchwax.generateWaxKey(k, collection), v);
            }
        };
        super.map(new Text(Nutchwax.getUrlFromWaxKey(key)), value, oo, r);
    }
}