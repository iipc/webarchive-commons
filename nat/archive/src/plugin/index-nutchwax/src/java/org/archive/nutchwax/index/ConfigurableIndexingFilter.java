/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
