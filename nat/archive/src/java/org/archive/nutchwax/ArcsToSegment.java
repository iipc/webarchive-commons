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
package org.archive.nutchwax;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map.Entry;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.NutchWritable;
import org.apache.nutch.crawl.SignatureFactory;
import org.apache.nutch.fetcher.FetcherOutputFormat;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLFilterException;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseImpl;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.parse.ParseStatus;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.protocol.ProtocolStatus;
import org.apache.nutch.scoring.ScoringFilters;
import org.apache.nutch.util.LogUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.apache.nutch.util.StringUtil;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;


/**
 * Convert Archive files (.arc/.warc) files to a Nutch segment.  This
 * is sometimes called "importing" other times "converting", the terms
 * are equivalent.
 *
 * <code>ArcsToSegment</code> is coded as a Hadoop job and is intended
 * to be run within the Hadoop framework, or at least started by the
 * Hadoop launcher incorporated into Nutch.  Although there is a
 * <code>main</code> driver, the Nutch launcher script is strongly
 * recommended.
 *
 * This class was initially adapted from the Nutch
 * <code>Fetcher</code> class.  The premise is since the Nutch
 * fetching process acquires external content and places it in a Nutch
 * segment, we can perform a similar activity by taking content from
 * the ARC files and place that content in a Nutch segment in a
 * similar fashion.  Ideally, once the <code>ArcsToSegment</code> is
 * used to import a set of ARCs into a Nutch segment, the resulting
 * segment should be more-or-less the same as one created by Nutch's
 * own Fetcher.
 * 
 * Since we are mimicing the Nutch Fetcher, we have to be careful
 * about some implementation details that might not seem relevant
 * to the importing of ARC files.  I've noted those details with
 * comments prefaced with "?:".
 */
public class ArcsToSegment extends Configured implements Tool, Mapper
{

  public static final Log LOG = LogFactory.getLog( ArcsToSegment.class );

  private JobConf        jobConf;
  private URLFilters     urlFilters;
  private ScoringFilters scfilters;
  private ParseUtil      parseUtil;
  private URLNormalizers normalizers;
  private int            interval;

  private long           numSkipped;
  private long           numImported;
  private long           bytesSkipped;
  private long           bytesImported;

  /**
   * ?: Is this necessary?
   */
  public ArcsToSegment()
  {
    
  }

  /**
   * <p>Constructor that sets the job configuration.</p>
   * 
   * @param conf
   */
  public ArcsToSegment( Configuration conf )
  {
    setConf( conf );
  }

  /**
   * <p>Configures the job.  Sets the url filters, scoring filters, url normalizers
   * and other relevant data.</p>
   * 
   * @param job The job configuration.
   */
  public void configure( JobConf job )
  {
    // set the url filters, scoring filters the parse util and the url
    // normalizers
    this.jobConf     = job;
    this.urlFilters  = new URLFilters    ( jobConf );
    this.scfilters   = new ScoringFilters( jobConf );
    this.parseUtil   = new ParseUtil     ( jobConf );
    this.normalizers = new URLNormalizers( jobConf, URLNormalizers.SCOPE_FETCHER );
    this.interval    = jobConf.getInt( "db.fetch.interval.default", 2592000      );
  }

  /**
   * In Mapper interface.
   * @inherit
   */
  public void close()
  {
    
  }

  /**
   * <p>Runs the Map job to translate an arc file into output for Nutch 
   * segments.</p>
   * 
   * @param key Line number in manifest corresponding to the <code>value</code>
   * @param value A line from the manifest
   * @param output The output collecter.
   * @param reporter The progress reporter.
   */
  public void map( WritableComparable key, 
                   Writable           value, 
                   OutputCollector    output, 
                   Reporter           reporter )
    throws IOException
  {
    String arcUrl      = "";
    String collection  = "";
    String segmentName = getConf().get( Nutch.SEGMENT_NAME_KEY );
    
    // Each line of the manifest is "<url> <collection>" where <collection> is optional
    String[] line = value.toString().split( " " );
    arcUrl = line[0];

    if ( line.length > 1 )
      {
        collection = line[1];
      }

    if ( LOG.isInfoEnabled() ) LOG.info( "Importing ARC: " + arcUrl );

    ArchiveReader r = ArchiveReaderFactory.get( arcUrl );

    ArcReader reader = new ArcReader( r );

    try
      {
        for ( ARCRecord record : reader )
          {
            // When reading WARC files, records of type other than
            // "response" are returned as 'null' by the Iterator, so
            // we skip them.
            if ( record == null ) continue ;

            importRecord( record, segmentName, collection, output );

            // FIXME: What does this do exactly?
            reporter.progress();
          }
      }
    finally
      {
        r.close();

        if ( LOG.isInfoEnabled() ) 
          {
            LOG.info( "Completed ARC: "  + arcUrl );
            LOG.info( "URLs skipped : " + this.numSkipped  );
            LOG.info( "URLs imported: " + this.numImported );
            LOG.info( "URLs total   : " + ( this.numSkipped + this.numImported ) );
          }
      }
    
  }

  /**
   * Import an ARCRecord.
   *
   * @param record
   * @param segmentName 
   * @param collectionName
   * @param output
   * @return whether record was imported or not (i.e. filtered out due to URL filtering rules, etc.)
   */
  private boolean importRecord( ARCRecord record, String segmentName, String collectionName, OutputCollector output )
  {
    ARCRecordMetaData meta = record.getMetaData();
    
    if ( LOG.isInfoEnabled() ) LOG.info( "Consider URL: " + meta.getUrl() + " (" + meta.getMimetype() + ")" );

    /* ?: On second thought, DON'T do this.  Even if we don't have a
       parser registered for a content-type, we still want to index
       its URL and possibly other meta-data.
    */
    /*
    // First, check to see if we have a parser registered for the
    // URL's Content-Type, so we don't read in some huge video file
    // only to discover we don't have a parser for it.
    if ( ! this.hasRegisteredParser( meta.getMimetype() ) )
      {
        if ( LOG.isInfoEnabled() ) LOG.info( "No parser registered for: "  + meta.getMimetype() );
        
        this.numSkipped++;
        this.bytesSkipped += meta.getLength();
        
        return false ;
      }
    */

    // ?: Arguably, we shouldn't be normalizing nor filtering based
    // on the URL.  If the document made it into the (W)ARC file, then
    // it should be indexed.  But then again, the normalizers and
    // filters can be disabled in the Nutch configuration files.
    String url = this.normalizeAndFilterUrl( meta.getUrl() );
    
    if ( url == null )
      {
        if ( LOG.isInfoEnabled() ) LOG.info( "Skip     URL: "  + meta.getUrl() );
        
        this.numSkipped++;
        this.bytesSkipped += meta.getLength();
        
        return false;
      }
    
    // URL is good, let's import the content.
    if ( LOG.isInfoEnabled() ) LOG.info( "Import   URL: " + meta.getUrl() );
    this.numImported++;
    this.bytesImported += meta.getLength();
    
    try
      {
        // Skip the HTTP headers in the response body, so that the
        // parsers are parsing the reponse body and not the HTTP
        // headers.
        record.skipHttpHeader();

        // Read the bytes of the HTTP response
        byte[] bytes = new byte[(int) meta.getLength()];
        record.read( bytes );

        Metadata contentMetadata = new Metadata( );
        // Set the segment name, just as is done by standard Nutch fetching.
        // Then, add the NutchWAX-specific metadata fields.
        contentMetadata.set( Nutch   .SEGMENT_NAME_KEY, segmentName );

        contentMetadata.set( NutchWax.CONTENT_TYPE_KEY, meta.getMimetype()          );
        contentMetadata.set( NutchWax.ARCNAME_KEY,      meta.getArcFile().getName() );
        contentMetadata.set( NutchWax.COLLECTION_KEY,   collectionName              );
        contentMetadata.set( NutchWax.DATE_KEY,         meta.getDate()              );
        
        Content content = new Content( url, url, bytes, meta.getMimetype(), contentMetadata, getConf() );

        output( output, new Text( url ), content );

        return true;
      }
    catch ( Throwable t )
      {
        LOG.error( "Import fail : " + meta.getUrl( ), t );
      }

    return false;
  }

  /**
   * Normalize and filter ther URL.  If the URL is malformed or
   * filtered (according to registered Nutch URL filtering plugins),
   * return <code>null</code>.  Otherwise return the normalized URL.
   *
   * @param  candidateUrl to be normalized and filtered
   * @return normalized URL, <code>null</code> if malformed or filtered out
   */
  private String normalizeAndFilterUrl( String candidateUrl )
  {
    String url = null;
    try
      {
        url = normalizers.normalize( candidateUrl, URLNormalizers.SCOPE_FETCHER );

        url = urlFilters.filter( url );

        return url;
      }
    catch ( MalformedURLException mue )
      {
        if ( LOG.isInfoEnabled() ) LOG.info( "MalformedURL: "  + candidateUrl );
      }
    catch ( URLFilterException ufe )
      {
        if ( LOG.isInfoEnabled() ) LOG.info( "URL filtered: "  + candidateUrl );
      }

    return null;
  }

  /**
   * TODO: Add check for registered parser for URL's Content-Type.
   * The idea is to see if there is a registered parser *before*
   * reading all the bytes of the content.  This way, if we have a
   * 100MB .mp4 movie file, but no parser registered for it, we don't
   * bother reading in the 100MB body.
   *    
   * Right now, the ParseUtil doesn't have a hasParser(ContentType)
   * method, so we have to read in the entire content body then try to
   * parse it just to discover if it is parsable or not.
   *
   * Another option is to create a fake Content object with the same
   * Content-Type as the real content and then try parsing the fake
   * Cotnent object to see if a parser was found for it or not.  But
   * that seems pretty hokey.
   */
  private boolean hasRegisteredParser( String contentType )
  {
    /* The following would be nice if such a method existed... 

       return this.parseUtil.hasParser( contentType );
     */
    return true;
  }

  /**
   *
   */
  private void output( OutputCollector output,
                       Text            key,
                       Content         content )
  {
    // Create the datum
    CrawlDatum datum = new CrawlDatum( CrawlDatum.STATUS_FETCH_SUCCESS, this.interval, 1.0f );

    // ?: I have no idea why we need to store the ProtocolStatus in
    // the datum's metadata, but the Nutch Fetcher class does it and
    // it seems important.  Since we're not really fetching here, we
    // assume ProtocolStatus.STATUS_SUCCESS is the right thing to do.
    datum.getMetaData().put( Nutch.WRITABLE_PROTO_STATUS_KEY, ProtocolStatus.STATUS_SUCCESS );

    // ?: Since we store the ARCRecord's archival date in the Content object, we follow the
    // logic in Nutch's Fetcher and store the current import time/date in the Datum.  I have
    // no idea if it makes a difference, other than this value is stored in the "tstamp"
    // field in the Lucene index whereas the ARCRecord date is stored in the "date" field
    // we added above.
    datum.setFetchTime( System.currentTimeMillis() );

    // ?: It doesn't seem to me that we need/use the scoring stuff
    // one way or another, but we might as well leave it in.
    try
      {
        scfilters.passScoreBeforeParsing( key, datum, content );
      }
    catch ( Exception e )
      {
        if ( LOG.isWarnEnabled() ) LOG.warn( "Couldn't pass score before parsing for: " + key, e );
      }

    // ?: This is kind of interesting.  In the Nutch Fetcher class, if the parsing fails,
    // the Content is not added to the output.  But in ArcsToSegment, we still add it, even
    // if the parsing fails.  Why?
    //
    // One benefit is that even if the parsing fails, having the Content in the index still
    // allows us to find the document by URL, date, etc.
    //
    // However, I don't know what will happen when a summary is computed...if the Content isn't there, will
    // it fail or just return an empty summary?
    ParseResult parseResult = null;
    try
      {
        parseResult = this.parseUtil.parse( content );
      }
    catch ( Exception e )
      {
        LOG.warn( "Error parsing: " + key, e );
      }
    
    // ?: This is taken from Nutch Fetcher.  I believe the signatures are used in the Fetcher
    // to ensure that URL contents are not stored multiple times if the signature doesn't change.
    // Makes sense.  But, in our case, we're relying on the (W)ARC production tools to eliminate
    // duplicate data (or are we?), so how important is the signature for our purposes?
    // I'll go ahead and leave it in, in case it's needed by Nutch for unknown purposes.
    //
    // Also, since we still import documents even if the parsing fails, we compute a signature
    // using an "empty" Parse object in the case of parse failure.  I don't know why we create
    // an empty Parse object rather than just use 'null', but I'm copying the way the Fetcher
    // does it.
    //
    // One odd thing is that we add the signature to the datum here, then "collect" the datum 
    // just below, but then after collecting the datum, we update the signature when processing
    // the ParseResults.  I guess "collecting" doesn't write out the datum, but "collects" it for
    // later output, thus we can update it after collection (I guess).
    if ( parseResult == null )
      {
        byte[] signature = SignatureFactory.getSignature( getConf() ).calculate( content, new ParseStatus().getEmptyParse( getConf() ) );
        datum.setSignature( signature );
      }
    
    try
      {
        output.collect( key, new NutchWritable( datum   ) );
        output.collect( key, new NutchWritable( content ) );
        
        if ( parseResult != null )
          {
            for ( Entry<Text, Parse> entry : parseResult )
              {
                Text url    = entry.getKey();
                Parse parse = entry.getValue();
                ParseStatus parseStatus = parse.getData().getStatus();
                
                if ( !parseStatus.isSuccess() )
                  {
                    LOG.warn( "Error parsing: " + key + ": " + parseStatus );
                    parse = parseStatus.getEmptyParse(getConf());
                  }
                
                byte[] signature = SignatureFactory.getSignature(getConf()).calculate(content, parse);
                
                // ?: Why bother setting this one again?  According to ParseData Javadoc,
                // the getContentMeta() returns the original Content metadata object, so
                // why are we setting the segment name on it to the same value again?
                // Let's leave it out.
                // parse.getData().getContentMeta().set( Nutch.SEGMENT_NAME_KEY, segmentName );

                // ?: These two are copied from Nutch's Fetcher implementation.  
                parse.getData().getContentMeta().set( Nutch.SIGNATURE_KEY,    StringUtil.toHexString(signature) );
                parse.getData().getContentMeta().set( Nutch.FETCH_TIME_KEY,   Long.toString(datum.getFetchTime() ) );
                
                if ( url.equals( key ) )
                  {
                    datum.setSignature( signature );
                  }

                // ?: As above, we'll leave the scoring hooks in place.
                try
                  {
                    scfilters.passScoreAfterParsing( url, content, parse );
                  }
                catch ( Exception e )
                  {
                    if ( LOG.isWarnEnabled() ) LOG.warn( "Couldn't pass score, url = " + key, e );
                  }

                output.collect( url, new NutchWritable( new ParseImpl( new ParseText( parse.getText() ), parse.getData(), parse.isCanonical() ) ) );
              }
          }
      }
    catch ( Exception e )
      {
        LOG.error( "Error outputting Nutch record for: " + key, e );
      }
  }

  /**
   * 
   */
  public int run( String[] args ) throws Exception
  {
    String usage = "Usage: ArcsToSegment <manifestPath> <segmentPath>";

    if ( args.length < 2 )
      {
        System.err.println( usage );
        return -1;
      }

    Path manifestPath = new Path( args[0] );
    Path segmentPath  = new Path( args[1] );

    JobConf job = new NutchJob( getConf() );

    try
      {
        job.setJobName( "ArcsToSegment " + manifestPath );
        job.set( Nutch.SEGMENT_NAME_KEY, segmentPath.getName() );

        job.setInputPath  ( manifestPath);
        job.setInputFormat( TextInputFormat.class );

        job.setMapperClass( ArcsToSegment.class   );

        job.setOutputPath      ( segmentPath               );
        job.setOutputFormat    ( FetcherOutputFormat.class );
        job.setOutputKeyClass  ( Text.class                );
        job.setOutputValueClass( NutchWritable.class       );

        JobClient.runJob( job );
      }
    catch ( Exception e )
      {
        LOG.fatal( "ArcsToSegment: ", e );
        return -1;
      }
    
    return 0;
  }

  /**
   *
   */
  public static void main(String args[]) throws Exception
  {
    int result = ToolRunner.run( NutchConfiguration.create(), new ArcsToSegment(), args );

    System.exit( result );
  }

}
