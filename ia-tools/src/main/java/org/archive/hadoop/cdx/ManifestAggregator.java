package org.archive.hadoop.cdx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.SortedCompositeIterator;

public class ManifestAggregator implements Tool {
	
	public final static String TOOL_NAME = "manifest-aggregator";
	public static final String MANIFEST_BASENAME = "manifest.txt";
	public static final String TOOL_DESCRIPTION = 
		"A tool for merging sorted manifest files in a CDX HDFS installation";
	
	private Configuration conf;
	
	public void aggregate(Path partsPath, OutputStream target) throws IOException {
		String dirString = partsPath.toUri().toASCIIString();
		
		FileSystem fs = partsPath.getFileSystem(getConf());
		FileStatus status = fs.getFileStatus(partsPath);
		if(!status.isDir()) {
			throw new IOException(dirString + " is not a directory!");
		}
		FileStatus entries[] = fs.listStatus(partsPath);
		ArrayList<Path> manifests = new ArrayList<Path>();
		for(FileStatus entry : entries) {
			Path entryPath = entry.getPath();
			if(!entry.isDir()) {
				throw new IOException(
						String.format("Non directory entry (%s) in %s",
								entryPath.getName(),dirString));
			}
			Path manifestPath = new Path(entryPath,MANIFEST_BASENAME);
			if(!fs.isFile(manifestPath)) {
				throw new IOException(
						String.format("No file at manifest path %s",
								manifestPath.toUri().toASCIIString()));
			}
			manifests.add(manifestPath);
		}
		Comparator<String> comparator = new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		};
		Charset UTF8 = Charset.forName("utf-8");
		SortedCompositeIterator<String> mergeItr = 
			new SortedCompositeIterator<String>(comparator);
		for(Path manifestPath : manifests) {
			FSDataInputStream fsdis = fs.open(manifestPath);
			InputStreamReader isr = new InputStreamReader(fsdis, UTF8);
			BufferedReader br = new BufferedReader(isr);
			mergeItr.addIterator(AbstractPeekableIterator.wrapReader(br));
		}
		OutputStreamWriter osw = new OutputStreamWriter(target, UTF8);
		PrintWriter pw = new PrintWriter(osw);
		while(mergeItr.hasNext()) {
			pw.println(mergeItr.next());
		}
		pw.flush();
		pw.close();
		mergeItr.close();
	}
	public void setConf(Configuration conf) {
		this.conf = conf;
	}
	public Configuration getConf() {
		return conf;
	}
	public static void USAGE(int code) {
		System.err.println("Usage: " + TOOL_NAME + " HDFS_PATH LOCAL_PATH");
		System.exit(code);
	}
	public int run(String[] args) throws Exception {
		if(args.length != 2) {
			USAGE(1);
		}
		Path inputDir = new Path(args[0]);
		File target = new File(args[1]);
		FileOutputStream fos = new FileOutputStream(target);
		aggregate(inputDir, fos);
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new ManifestAggregator(), args);
		System.exit(res);
	}

}
