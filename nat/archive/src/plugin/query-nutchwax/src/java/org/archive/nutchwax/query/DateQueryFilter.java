
package org.archive.nutchwax.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;

import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.Query.Clause;
import org.apache.nutch.searcher.QueryException;
import org.apache.nutch.searcher.QueryFilter;

/** 
 * 
 */
public class DateQueryFilter implements QueryFilter
{
  // public static final Log LOG = LogFactory.getLog( DateQueryFilter.class );
  
  private static final String  FIELD = "date";
  private static final Pattern queryPattern = Pattern.compile("^(\\d{4,14})$|^(\\d{4,14})(?:-(\\d{4,14}))$");

  private Configuration conf;
  
  public void setConf( Configuration conf )
  {
    this.conf = conf;
  }
  
  public Configuration getConf()
  {
    return this.conf;
  }

  public BooleanQuery filter( Query input, BooleanQuery output )
    throws QueryException
  {
    Clause [] clauses = input.getClauses();
    
    for ( Clause clause : clauses )
      {
        // Skip non-date clauses
        if ( ! FIELD.equals( clause.getField() ) )
          {
            continue ;
          }

        Matcher matcher = queryPattern.matcher( clause.getTerm().toString() );

        if ( matcher == null || !matcher.matches() )
          {
            // TODO: Emit message
            throw new QueryException( "" );
          }

        String date = matcher.group( 1 );

        if ( date != null )
          {
            doDateQuery( output, clause, date );

            continue ;
          }

        String lower = matcher.group( 2 );
        String upper = matcher.group( 3 );
        
        if ( lower != null && upper != null )
          {
            doRangeQuery( output, clause, lower, upper );

            continue ;
          }

        // No matching groups -- weird since the matcher.matches()
        // check succeeded above.
        // TODO: Emit message
        throw new QueryException( "" );
      }

    return output;
  }

  private void doDateQuery( BooleanQuery output, Clause clause, String date )
  {
    // If the date has less than the 14-digit precision, make it into a range
    // query for the precision given.  E.g., if the date is "20080510", then
    // make it into a "[20080510000000-20080510999999]"
    if ( date.length() < 14 )
      {
        String lower = this.padDate( date, '0' );
        String upper = this.padDate( date, '9' );

        doRangeQuery( output, clause, lower, upper );

        return ;
      }

    TermQuery term = new TermQuery( new Term( FIELD, date ) );
    
    // Set boost on term?
    // term.setBoolst( boost );

    output.add( term, 
                ( clause.isProhibited()
                  ? BooleanClause.Occur.MUST_NOT
                  : ( clause.isRequired()
                      ? BooleanClause.Occur.MUST
                      : BooleanClause.Occur.SHOULD ) ) );
  }

  private void doRangeQuery( BooleanQuery output, Clause clause, String lower, String upper )
  {
    lower = padDate( lower );
    upper = padDate( upper );

    RangeQuery range = new RangeQuery( new Term( FIELD, lower ),
                                       new Term( FIELD, upper ),
                                       true );
    
    // Set boost on range query?
    // range.setBoolst( boost );

    output.add( range,
                ( clause.isProhibited()
                  ? BooleanClause.Occur.MUST_NOT
                  : ( clause.isRequired()
                      ? BooleanClause.Occur.MUST
                      : BooleanClause.Occur.SHOULD ) ) );
  }


  private String padDate( final String date, final char pad )
  {
    StringBuilder buf = new StringBuilder( date );
    int len = 14 - date.length( );
    for ( int i = 0; i < len ; i++ )
      {
        buf.append( pad );
      }

    return buf.toString( );
  }

  private String padDate( String date )
  {
    return padDate( date, '0' );
  }

}
