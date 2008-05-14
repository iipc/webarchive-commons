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
package org.archive.nutchwax.query;

import java.util.List;
import java.util.ArrayList;

import org.apache.lucene.search.BooleanQuery;

import org.apache.nutch.searcher.QueryFilter;
import org.apache.nutch.searcher.QueryException;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.FieldQueryFilter;
import org.apache.nutch.searcher.RawFieldQueryFilter;
import org.apache.hadoop.conf.Configuration;

/** 
 * 
 */
public class ConfigurableQueryFilter implements QueryFilter
{
  private List<QueryFilter> filters;
  private Configuration     conf;

  public ConfigurableQueryFilter( )
  {
    this.filters = new ArrayList<QueryFilter>( );
  }

  public void setConf( Configuration conf )
  {
    this.conf = conf;
    
    this.constructFilters( );
  }


  public Configuration getConf( )
  {
    return this.conf;
  }


  public BooleanQuery filter( Query input, BooleanQuery output )
    throws QueryException 
  {
    for ( QueryFilter filter : this.filters )
      {
        output = filter.filter(input, output);
      }
    
    return output;
  }

  private void constructFilters( )
  {
    String filterSpecs = conf.get( "nutchwax.filter.query" );
    
    if ( null == filterSpecs )
      {
        return ;
      }

    filterSpecs = filterSpecs.trim( );

    for ( String filterSpec : filterSpecs.split("\\s+") )
      {
        String spec[] = filterSpec.split("[:]");
        
        if ( spec.length < 2 )
          {
            // TODO: Warning
            continue;
          }

        if ( "field".equals( spec[0] ) )
          {
            String name  = spec[1];
            float  boost = 1.0f;
            if ( spec.length > 2 )
              {
                try
                  {
                    boost = Float.parseFloat( spec[2] );
                  }
                catch ( NumberFormatException nfe )
                  {
                    // TODO: Warning, but ignore it.
                  }
              }
            QueryFilter filter = new FieldQueryFilterImpl( name, boost );

            this.filters.add( filter );
          }
        else if ( "raw".equals( spec[0] ) )
          {
            String  name      = spec[1];
            boolean lowerCase = true;
            float   boost     = 1.0f;
            if ( spec.length > 2 )
              {
                lowerCase = Boolean.parseBoolean( spec[2] );
              }
            if ( spec.length > 3 )
              {
                try
                  {
                    boost = Float.parseFloat( spec[2] );
                  }
                catch ( NumberFormatException nfe )
                  {
                    // TODO: Warning, but ignore it.
                  }
              }
            QueryFilter filter = new RawFieldQueryFilterImpl( name, lowerCase, boost );

            this.filters.add( filter );
          }
        else
          {
            // TODO: Warning uknown filter type
          }
      }
  }

  public class RawFieldQueryFilterImpl extends RawFieldQueryFilter
  {
    private Configuration conf;

    public RawFieldQueryFilterImpl( String field, boolean lowerCase, float boost )
    {
      super( field, lowerCase, boost );

      // Use the same conf as the owning instance.
      this.setConf( ConfigurableQueryFilter.this.conf );
    }

    public Configuration getConf( )
    {
      return this.conf;
    }
    
    public void setConf( Configuration conf )
    {
      this.conf = conf;
    }
  }

  public class FieldQueryFilterImpl extends FieldQueryFilter
  {
    public FieldQueryFilterImpl( String field, float boost )
    {
      super( field, boost );

      // Use the same conf as the owning instance.
      this.setConf( ConfigurableQueryFilter.this.conf );
    }
  }

}
