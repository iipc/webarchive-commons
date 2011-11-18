package org.archive.hadoop.cdx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Comparator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.SortedCompositeIterator;

import junit.framework.TestCase;

public class CDXClusterTest extends TestCase {

	public void testGetRangeIterator() throws IOException {
//		File target = new File("/tmp/tofel.tmp");
//		OutputStream out = new FileOutputStream(target);
//		PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, Charset.forName("UTF-8")));
//		String start = "tofel.com/";
//		String end = "tofel.com2/";
//		assertTrue(start.compareTo(end) < 0);
//		Configuration conf = new Configuration();
//		Path clusterPath = new Path("hdfs://hadoop-name.us.archive.org:6100/user/brad/global-cdx-clusters/20110606");
//		CDXCluster c = new CDXCluster(conf, clusterPath);
//		CloseableIterator<String> ci = c.getRange("tofel.com/","tofel.com2/");
//		while(ci.hasNext()) {
//			pw.println(ci.next());
//		}
//		pw.flush();
//		pw.close();
	}
	public void testGetManyRangeIterator() throws IOException {
//		File target = new File("/tmp/tofel.all.tmp");
//		OutputStream out = new FileOutputStream(target);
//		PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, Charset.forName("UTF-8")));
//		String start = "tofel.com/";
//		String end = "tofel.com2/";
//
//		Configuration conf = new Configuration();
//		String[] clusters = new String[] {
//				"20110606","20110719-001",
//				"20110719-002-005","20110719-006-010",
//				"20110719-011-015","20110719-016-023",
//				"20111013-001-005"};
//		Comparator<String> comparator = new Comparator<String>() {
//			public int compare(String s1, String s2) {
//				return s1.compareTo(s2);
//			}
//		};
//		SortedCompositeIterator<String> itr = 
//			new SortedCompositeIterator<String>(comparator);
//		
//		for(String cluster : clusters) {
//			Path clusterPath = new Path("hdfs://hadoop-name.us.archive.org:6100/user/brad/global-cdx-clusters/"+cluster);
//			CDXCluster c = new CDXCluster(conf, clusterPath);
//			CloseableIterator<String> ci = c.getRange(start,end);
//			itr.addIterator(ci);
//		}
//		
//		while(itr.hasNext()) {
//			pw.println(itr.next());
//		}
//		pw.flush();
//		pw.close();
	}

}
