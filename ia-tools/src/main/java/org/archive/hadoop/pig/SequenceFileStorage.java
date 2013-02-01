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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.ByteWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.pig.StoreFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;

/**
 * Pig StoreFunc which stores Tuples in a Hadoop SequenceFile.  Hadoop
 * SequenceFiles are made up of unordered (key,value) pairs.  So, you
 * can add (key,value) pairs in any order, the inputs do not need to
 * be sorted.
 *
 * In a Hadoop SequenceFile, the key and the value are each typed
 * according to the Hadoop type system.  The type of the key and the
 * value are specified when the SequenceFile is created.  Thus, when
 * this StoreFunc is initialized, the key and value types must be
 * given.  By default, if the types are not specified, they are
 * assumed to be Text (i.e. Strings).
 *
 * For example, in a Pig script:
 *
 *   STORE foo INTO 'foo' USING SequenceFileStorage();  -- Default (Text,Text)
 *
 * or
 *
 *   STORE foo INTO 'foo' USING SequenceFileStorage( 'org.apache.hadoop.io.LongWritable',
 *                                                   'org.apache.hadoop.io.BytesWritable' );
 * 
 */
public class SequenceFileStorage extends StoreFunc
{
  /*
   * It's possible that we can actually get the Class objects in the
   * constructor; however, I'm not sure that the whole environment is
   * setup when this class is instantiated, so I think it's safer to
   * defer getting the Class objects until they are actually needed in
   * the RecordWriter.
   */
  String keyType   = "org.apache.hadoop.io.Text";
  String valueType = "org.apache.hadoop.io.Text";

  /*
   * We create a null object for the key and value types.  If we need
   * to write a null to the sequence file, then we just use these
   * instances.
   */
  Writable nullKey;
  Writable nullValue;
  
  Text emptyTextKey = new Text("");
  
  RecordWriter writer;

  public SequenceFileStorage()
  {
  }
  
  public SequenceFileStorage( String valueType )
  {
    this.valueType = valueType;
  }

  public SequenceFileStorage( String keyType,
                              String valueType )
  {
    this.keyType   = keyType;
    this.valueType = valueType;
  }

  /**
   * Most of this method is cut/pasted from the Hadoop
   * SequenceFileOutputFormat.  The big difference is that we use the
   * key and value types given to this Pig storage class rather than
   * using the ones set by the job configuration.
   */
  public OutputFormat getOutputFormat() throws IOException
  {
    return new SequenceFileOutputFormat()
      {
        public RecordWriter getRecordWriter( TaskAttemptContext context )
          throws IOException, InterruptedException 
        {
          Configuration conf = context.getConfiguration();
          
          Class keyClass, valueClass;
          try
            {
              keyClass   = conf.getClassByName( keyType   );
              valueClass = conf.getClassByName( valueType );
            }
          catch ( ClassNotFoundException cnfe ) { throw new IOException( cnfe ); }

          // Instantiate null objects for the key and value types.
          // See getWritable() for their use.
          try
            {
              nullKey   = (Writable) keyClass.newInstance();
              nullValue = (Writable) valueClass.newInstance();
            }
          catch ( Exception roe ) { throw new IOException( roe ); }

          CompressionCodec codec = null;
          CompressionType compressionType = CompressionType.NONE;
          if (getCompressOutput(context)) {
            // find the kind of compression to do
            compressionType = getOutputCompressionType(context);
            
            // find the right codec
            Class<?> codecClass = getOutputCompressorClass(context, DefaultCodec.class);
            codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, conf);
          }
          // get the path of the temporary output file 
          Path file = getDefaultWorkFile(context, "");
          FileSystem fs = file.getFileSystem(conf);
          final SequenceFile.Writer out = 
            SequenceFile.createWriter(fs, conf, file,
                                      keyClass,
                                      valueClass,
                                      compressionType,
                                      codec,
                                      context);
          
          return new RecordWriter() {
            
            public void write( Object key, Object value)
              throws IOException {
              
              out.append(key, value);
            }
            
            public void close(TaskAttemptContext context) throws IOException { 
              out.close();
            }
          };
        }
    };
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
   * Tuples must have two entries, the first for the SequenceFile
   * 'key' and the second for the 'value'.
   */
  public void putNext( Tuple tuple ) throws IOException
  {
    try
      {
    	Writable key, value;
    	
        int size = tuple.size();
        
        if ( size == 2 )
        {
          key   = getWritable( tuple.get(0), this.nullKey   );
          value = getWritable( tuple.get(1), this.nullValue );
        } else if ( size == 1 )
        {
          key   = this.nullKey;
          value = getWritable( tuple.get(0), this.nullValue );          
        } else
        {
           throw new IOException( "Invalid tuple size, must be 1 or 2: " + size );
        }
        
        this.writer.write( key, value );
      }
    catch ( InterruptedException ie )
      {
        throw new IOException( ie );
      }
  }

  /**
   * Convert the Pig tupleValue to the corresponding Hadoop object.
   */
  public Writable getWritable( Object tupleValue, Writable nullWritable ) throws IOException
  {
    switch ( DataType.findType( tupleValue ) )
      {
      case DataType.BOOLEAN:
        return new BooleanWritable( (Boolean) tupleValue );

      case DataType.BYTE:
        return new ByteWritable( (Byte) tupleValue );

      case DataType.CHARARRAY:
        return new Text( (String) tupleValue );

      case DataType.INTEGER:
        return new IntWritable( (Integer) tupleValue );

      case DataType.LONG:
        return new LongWritable( (Long) tupleValue );

      case DataType.DOUBLE:
        return new DoubleWritable( (Double) tupleValue );

      case DataType.FLOAT:
        return new FloatWritable( (Float) tupleValue );

      case DataType.BYTEARRAY:
        return new BytesWritable( (byte[]) tupleValue );

        // If we get a 'null' from Pig, just pass through the
        // already-instantiated Hadoop nullWritable.
      case DataType.NULL:
        return nullWritable;

        // Don't know what to do with these complex data types.
      case DataType.BAG:
      case DataType.ERROR:
      case DataType.MAP:
      case DataType.TUPLE:
      case DataType.UNKNOWN:
      default:
        throw new IOException( "Cannot write values of type: " + DataType.findTypeName( tupleValue ) );
      }
  }

}
