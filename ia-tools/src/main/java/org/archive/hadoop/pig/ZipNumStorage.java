/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.archive.hadoop.pig;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.pig.StoreFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;

import org.archive.hadoop.mapreduce.ZipNumOutputFormat;

/**
 * Very simple/minimal StoreFunc to write {key,value} pairs into IA's
 * "zip num" format, using the ZipNumOutputFormat class.
 */
public class ZipNumStorage extends StoreFunc
{
  RecordWriter writer;

  Text key   = new Text();
  Text value = new Text();
  
  public OutputFormat getOutputFormat() throws IOException
  {
    return new ZipNumOutputFormat();
  }
  
  public void setStoreLocation( String location, Job job ) throws IOException
  {
    FileOutputFormat.setOutputPath( job, new Path(location) );
  }

  public void prepareToWrite( RecordWriter writer) throws IOException  
  {
    this.writer = writer;
  }

  /**
   * Tuples can have either one or two chararray values.
   *
   * If there is only 1, then we split it on the first space.
   * If there are 2, then we just accept them as-is.
   *
   * The underlying ZipNumOutputFormat requires that the data
   * be split into a (key,value) pair, with both the key and
   * value being Strings (Pig chararray).
   */
  public void putNext( Tuple tuple ) throws IOException
  {
    try
      {
        int size = tuple.size();

        if ( size != 1 && size != 2 )
          {
            throw new IOException( "Invalid tuple size, must be 1 or 2: " + size );
          }
        
        if ( DataType.findType( tuple.get(0) ) != DataType.CHARARRAY )
          {
            throw new IOException( "Invalid type for tuple 0, not CHARARRAY: " + DataType.findTypeName( DataType.findType( tuple.get(0) ) ) + ":" + tuple.get(0) + ":" + tuple.get(1) );
          }
        
        if ( size == 2 )
          {
            if ( DataType.findType( tuple.get(1) ) != DataType.CHARARRAY )
              {
                throw new IOException( "Invalid type for tuple 1, not CHARARRAY: " + DataType.findTypeName( DataType.findType( tuple.get(1) ) ) + ":" + tuple.get(0) + ":" + tuple.get(1) );
              }
            
            this.key  .set( (String) tuple.get(0) );
            this.value.set( (String) tuple.get(1) );
          }
        else
          {
            String s[] = ((String)tuple.get(0)).split( " ", 2 );
            
            this.key  .set( s[0] );
            this.value.set( s[1] );
          }
        
        this.writer.write( this.key, this.value );
      }
    catch ( InterruptedException ie )
      {
        throw new IOException( ie );
      }
  }
}
