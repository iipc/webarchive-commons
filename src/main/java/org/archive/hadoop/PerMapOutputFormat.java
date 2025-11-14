/**
 * Copyright 2012 Internet Archive
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.archive.hadoop;

import java.io.*;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.InvalidJobConfException;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * OutputFormat that directs the output to a file named according to
 * the input file.  For instance, if the input file is "foo", then the
 * output file is also named "foo".  A suffix can be easily added, or
 * a regex+replace applied to the input filename to produce an output
 * filename.
 *
 * This class can be used in conjunction with FilenameInputFormat in a
 * map-reduce job that only does a map() function, no reduce.  By
 * combining these input and output formats, it's easy to read from a
 * large set of input files, process each one in a separate map task,
 * and write the output to a file with a name based on the input.
 *
 * For example, suppose you had 1000 WARC files and your map() task
 * just reads a single WARC file and outputs the number of records in
 * it.  Use the FilenameInputFormat and the PerMapOutputFormat,
 * setting the "permap.suffix" property to ".count" and for each WARC
 * input file (e.g. "foo.warc.gz") you'll get a corresponding ".count"
 * file in the output (.e.g. "foo.warc.gz.count").
 *
 * The nice thing about using this class as the OutputFormat is that
 * Hadoop will manage the temporary file for you.  This means that if
 * the map task fails (suppose the task node kernel panics), Hadoop
 * will automatically delete the temp file from the failed task and
 * re-schedule it.
 *
 * This class assumes the actual OutputFormat is a SequenceFile.  If
 * not -- suppose you want to output a MapFile or plain text -- then
 * specify the output format in the "permap.output.format.class"
 * property.
 *
 * This class was insired by Hadoop's
 * <pre>org.apache.hadoop.mapred.lib.MultipleOutputFormat</pre>
 */
public class PerMapOutputFormat<K,V> extends FileOutputFormat<K,V>
{
  private String getOutputFilename( JobConf job )
    throws IOException
  {
    String regex   = job.get( "permap.regex"  , null );
    String replace = job.get( "permap.replace", null );
    String suffix  = job.get( "permap.suffix" , null );

    String inputFilename = job.get("map.input.file");

    if ( inputFilename == null ) throw new IOException( "map.input.file is null, not running in map task?" );

    String outputFilename = (new Path(inputFilename)).getName();

    if ( regex != null && replace != null )
      {
        outputFilename = outputFilename.replaceAll( regex, replace );
      }
    else if ( suffix != null )
      {
        outputFilename += suffix;
      }

    if ( outputFilename == null ) throw new IOException( "outputFilename is null" );

    return outputFilename;
  }

  private OutputFormat<K,V> getOutputFormat( JobConf job )
  {
    return ReflectionUtils.newInstance( job.getClass( "permap.output.format.class",
                                                       SequenceFileOutputFormat.class,
                                                       OutputFormat.class ),
                                        job );
  }
  

  public RecordWriter<K, V> getRecordWriter( FileSystem fs, JobConf job, String name, Progressable progress )
    throws IOException
  {
    String outputFilename = getOutputFilename( job );

    OutputFormat<K,V> of = getOutputFormat( job );

    return of.getRecordWriter( fs, job, outputFilename, progress );
    
  }

  /**
   * Over-ride the default FileOutputFormat's checkOutputSpecs() to
   * allow for the target directory to already exist.
   */
  public void checkOutputSpecs( FileSystem ignored, JobConf job )
    throws FileAlreadyExistsException, InvalidJobConfException, IOException 
  {
  }

}
