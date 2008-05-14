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
package org.archive.nutchwax.tools;

import java.io.File;
import java.util.Iterator;

import org.apache.lucene.index.IndexReader;

public class DumpIndex
{
  public static void main(String[] args) throws Exception
  {
    String option   = "";
    String indexDir = "";

    if (args.length == 1)
    {
      indexDir = args[0];
    }
    else if (args.length == 2)
    {
      option   = args[0];
      indexDir = args[1];
    }

    if (! (new File(indexDir)).exists())
    {
      usageAndExit();
    }
    
    if (option.equals("-f"))
    {
      listFields(indexDir);
    }
    else
    {
      dumpIndex(indexDir);
    }
  }
  
  private static void dumpIndex(String indexDir) throws Exception
  {
    IndexReader reader = IndexReader.open(indexDir);
    
    Object[] fieldNames = reader.getFieldNames(IndexReader.FieldOption.ALL).toArray();

    for (int i = 0; i < fieldNames.length; i++)
    {
      System.out.print(fieldNames[i] + "\t");
    }

    System.out.println();

    int numDocs = reader.numDocs();
    
    for (int i = 0; i < numDocs; i++)
    {
      for (int j = 0; j < fieldNames.length; j++)
      {
        System.out.print(reader.document(i).get((String) fieldNames[j]) + "\t");
      }
      
      System.out.println();
    }
  }
  
  private static void listFields(String indexDir) throws Exception
  {
    IndexReader reader = IndexReader.open(indexDir);

    Iterator it = reader.getFieldNames(IndexReader.FieldOption.ALL).iterator();
    
    while (it.hasNext())
    {
      System.out.println(it.next());
    }
    
    reader.close();
  }
  
  private static void usageAndExit()
  {
    System.out.println("Usage: DumpIndex [-f] index");
    System.exit(1);
  }
}
