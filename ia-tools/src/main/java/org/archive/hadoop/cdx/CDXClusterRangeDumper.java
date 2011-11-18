package org.archive.hadoop.cdx;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.util.iterator.SortedCompositeIterator;

public class CDXClusterRangeDumper implements Tool {
	Charset UTF8 = Charset.forName("utf-8");
	public final static String TOOL_NAME = "cluster-range";
	public static final String TOOL_DESCRIPTION = 
		"A tool for dumping ranges of a CDX cluster to STDOUT";
	
	private Configuration conf;
	

	public void setConf(Configuration conf) {
		this.conf = conf;
	}
	public Configuration getConf() {
		return conf;
	}

	public static int USAGE(int code) {
		System.err.println("Usage: " + TOOL_NAME + " START END CLUSTER_HDFS_URL ...");
		System.err.println("\tDump all CDX records within the cluster at CLUSTER_HDFS_URL");
		System.err.println("\tstarting at START(inclusive), ending at END(exclusive)");
		System.err.println("\tto STDOUT. If multiple clusters URLs are specified");
		System.err.println("\ttheir results will be merged into a single sorted stream.");
		return code;
	}
	public int run(String[] args) throws Exception {
		if(args.length < 3) {
			return USAGE(1);
		}
		String start = args[0];
		String end = args[1];
		Iterator<String> itr;
		if(args.length == 3) {
			Path clusterPath = new Path(args[2]);
			CDXCluster c = new CDXCluster(getConf(), clusterPath);
			itr = c.getRange(start,end);
		} else {
			Comparator<String> comparator = new Comparator<String>() {
				public int compare(String s1, String s2) {
					return s1.compareTo(s2);
				}
			};
			SortedCompositeIterator<String> scitr = 
				new SortedCompositeIterator<String>(comparator);
			for(int i = 2; i < args.length; i++) {
				Path clusterPath = new Path(args[i]);
				CDXCluster c = new CDXCluster(getConf(), clusterPath);
				scitr.addIterator(c.getRange(start,end));
			}
			itr = scitr;
		}
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out, UTF8));
		
		while(itr.hasNext()) {
			pw.println(itr.next());
		}
		pw.flush();
		pw.close();

		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new CDXClusterRangeDumper(), args);
		System.exit(res);
	}

}
