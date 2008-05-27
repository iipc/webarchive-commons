/*
 * Copyright (C) 2008 Internet Archive.
 * 
 * This file is part of the archive-access tools project
 * (http://sourceforge.net/projects/archive-access).
 * 
 * The archive-access tools are free software; you can redistribute them and/or
 * modify them under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or any
 * later version.
 * 
 * The archive-access tools are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * the archive-access tools; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.archive.nutchwax.index;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.Parse;

/** 
 * Adds web archive specific fields to a document.
 */
public class ConfigurableIndexingFilter implements IndexingFilter
{
  public static final Log LOG = LogFactory.getLog( ConfigurableIndexingFilter.class );

  private Configuration conf;
  private List<FieldSpecification> fieldSpecs;

  public void setConf( Configuration conf )
  {
    this.conf = conf;
    
    String filterSpecs = conf.get( "nutchwax.filter.index" );
    
    if ( null == filterSpecs )
      {
        return ;
      }

    this.fieldSpecs = new ArrayList<FieldSpecification>( );

    filterSpecs = filterSpecs.trim( );
    
    for ( String filterSpec : filterSpecs.split("\\s+") )
      {
        String spec[] = filterSpec.split("[:]");

        String  srcKey    = spec[0];
        boolean lowerCase = true;
        boolean store     = true;
        boolean tokenize  = false;
        String  destKey   = srcKey;
        switch ( spec.length )
          {
          case 5:
            destKey = spec[4];
          case 4:
            tokenize = Boolean.parseBoolean( spec[3] );
          case 3:
            store = Boolean.parseBoolean( spec[2] );
          case 2:
            lowerCase = Boolean.parseBoolean( spec[1] );
          }

        LOG.info( "Add field specification: " + srcKey + ":" + lowerCase + ":" + store + ":" + tokenize + ":" + destKey );

        this.fieldSpecs.add( new FieldSpecification( srcKey, lowerCase, store, tokenize, destKey ) );
      }
  }

  private static class FieldSpecification
  {
    String  srcKey;
    boolean lowerCase;
    boolean store;
    boolean tokenize;
    String  destKey;

    public FieldSpecification( String srcKey, boolean lowerCase, boolean store, boolean tokenize, String destKey )
    {
      this.srcKey    = srcKey;
      this.lowerCase = lowerCase;
      this.store     = store;
      this.tokenize  = tokenize;
      this.destKey   = destKey;
    }
  }

  public Configuration getConf()
  {
    return this.conf;
  }

  /**
   * Transfer NutchWAX field values stored in the parsed content to
   * the Lucene document.
   */
  public Document filter( Document doc, Parse parse, Text url, CrawlDatum datum, Inlinks inlinks )
    throws IndexingException
  {
    Metadata meta = parse.getData().getContentMeta();

    for ( FieldSpecification spec : this.fieldSpecs )
      {
        String value = meta.get( spec.srcKey );
        
        if ( value == null ) continue;

        if ( spec.lowerCase )
          {
            value = value.toLowerCase( );
          }
        
        doc.add( new Field( spec.destKey, 
                            value, 
                            spec.store    ? Field.Store.YES : Field.Store.NO, 
                            spec.tokenize ? Field.Index.TOKENIZED : Field.Index.UN_TOKENIZED ) );
      }

    return doc;
  }
  
}
