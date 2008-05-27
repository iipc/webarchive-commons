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
