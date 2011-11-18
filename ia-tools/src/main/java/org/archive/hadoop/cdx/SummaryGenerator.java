package org.archive.hadoop.cdx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class SummaryGenerator implements Tool {
	
	private static final Logger LOGGER =
		Logger.getLogger(SummaryGenerator.class.getName());
	
	public static final String SUMMARY_SUFFIX = ".summary";
	public static final String GZ_SUFFIX = ".gz";
	public static final String ALL_SUMMARY_PREFIX = "ALL";

	public final static String TOOL_NAME = "summary-generator";
	public static final String TOOL_DESCRIPTION = 
		"A tool for generating a meta-index summary from a set of shard partition summaries in a CDX HDFS installation";
	
	private Configuration conf;
	public  Configuration getConf()                   { return conf;      }
	public  void          setConf(Configuration conf) { this.conf = conf; }

	public static void USAGE(int code) {
		System.err.println("USAGE: " + TOOL_NAME + " HDFS_URL");
		System.exit(code);
	}
	
	public void createSummary(FileSystem fs, Path clusterPath, PrintWriter pw) 
	throws IOException {
		Charset UTF8 = Charset.forName("utf-8");
		HashMap<String, Path> summaries = new HashMap<String, Path>();
		HashMap<String, Path> parts = new HashMap<String, Path>();
		HashMap<String,Long> partsLength = new HashMap<String, Long>();
		FileStatus entries[] = fs.listStatus(clusterPath);
		int sumLen = SUMMARY_SUFFIX.length();
		int gzLen = GZ_SUFFIX.length();
		for(FileStatus entry : entries) {
			Path entryPath = entry.getPath();
			String pathStr = entryPath.toUri().toASCIIString();
			String name = entryPath.getName();
			if(entry.isDir()) {
				LOGGER.info("Ignoring Directory entry " + pathStr);
			} else if(name.equals(ALL_SUMMARY_PREFIX + SUMMARY_SUFFIX)) {
				// just skip - this is our target..
			} else if(name.endsWith(SUMMARY_SUFFIX)) {
				String prefix = name.substring(0,name.length() - sumLen);
				summaries.put(prefix, entryPath);
			} else if(name.endsWith(GZ_SUFFIX)) {
				String prefix = name.substring(0,name.length() - gzLen);
				parts.put(prefix, entryPath);
				partsLength.put(prefix, entry.getLen());
			} else {
				LOGGER.info("Ignoring entry " + pathStr);
			}
		}
		// just for sanities sake - lets make sure all summaries have a part:
		for(String name : summaries.keySet()) {
			if(!parts.containsKey(name)) {
				throw new IOException("Missing part for summary:" + name);
			}
		}
		// now dump all summaries - make sure we do it in sorted order:
		ArrayList<String> summaryNames = new ArrayList<String>(summaries.keySet());
		String tmp[] = new String[0];
		String tmp2[] = summaryNames.toArray(tmp);
		Arrays.sort(tmp2);
		for(String part : tmp2) {
//			long length = partsLength.get(part);
			FSDataInputStream fsdis = fs.open(summaries.get(part));
			InputStreamReader isr = new InputStreamReader(fsdis,UTF8);
			BufferedReader br = new BufferedReader(isr);
			String line;
//			String prevUrl = null;
//			long prevOffset = 0;
			while(true) {
				line = br.readLine();
				if(line == null) {
					break;
				}
				String fields[] = line.split("\\s");
				if(fields.length < 3) {
					throw new IOException("Bad line in " + part + ":" + line);
				}
				long offset = Long.parseLong(fields[0]);
				long length = Long.parseLong(fields[1]);
				String url = fields[2];
				pw.format("%s\t%s\t%d\t%d\n", 
						url, part, offset, length);

//				if(prevUrl != null) {
//					pw.format("%s\t%s\t%d\t%d\n", 
//							prevUrl, part, prevOffset, offset - prevOffset);
//				}
//				prevUrl = url;
//				prevOffset = offset;
			}
//			if(prevUrl != null) {
//				pw.format("%s\t%s\t%d\t%d\n", 
//						prevUrl, part, prevOffset, length - prevOffset);
//			}
			br.close();
		}
		pw.flush();
	}
	public void createSummaryOld(FileSystem fs, Path clusterPath, PrintWriter pw) 
	throws IOException {
		Charset UTF8 = Charset.forName("utf-8");
		HashMap<String, Path> summaries = new HashMap<String, Path>();
		HashMap<String, Path> parts = new HashMap<String, Path>();
		HashMap<String,Long> partsLength = new HashMap<String, Long>();
		FileStatus entries[] = fs.listStatus(clusterPath);
		int sumLen = SUMMARY_SUFFIX.length();
		int gzLen = GZ_SUFFIX.length();
		for(FileStatus entry : entries) {
			Path entryPath = entry.getPath();
			String pathStr = entryPath.toUri().toASCIIString();
			String name = entryPath.getName();
			if(entry.isDir()) {
				LOGGER.info("Ignoring Directory entry " + pathStr);
			} else if(name.endsWith(SUMMARY_SUFFIX)) {
				String prefix = name.substring(0,name.length() - sumLen);
				summaries.put(prefix, entryPath);
			} else if(name.endsWith(GZ_SUFFIX)) {
				String prefix = name.substring(0,name.length() - gzLen);
				parts.put(prefix, entryPath);
				partsLength.put(prefix, entry.getLen());
			} else {
				LOGGER.info("Ignoring entry " + pathStr);
			}
		}
		// just for sanities sake - lets make sure all summaries have a part:
		for(String name : summaries.keySet()) {
			if(!parts.containsKey(name)) {
				throw new IOException("Missing part for summary:" + name);
			}
		}
		// now dump all summaries - make sure we do it in sorted order:
		ArrayList<String> summaryNames = new ArrayList<String>(summaries.keySet());
		String tmp[] = new String[0];
		String tmp2[] = summaryNames.toArray(tmp);
		Arrays.sort(tmp2);
		for(String part : tmp2) {
			long length = partsLength.get(part);
			FSDataInputStream fsdis = fs.open(summaries.get(part));
			InputStreamReader isr = new InputStreamReader(fsdis,UTF8);
			BufferedReader br = new BufferedReader(isr);
			String line;
			String prevUrl = null;
			long prevOffset = 0;
			while(true) {
				line = br.readLine();
				if(line == null) {
					break;
				}
				String fields[] = line.split("\\s");
				if(fields.length < 3) {
					throw new IOException("Bad line in " + part + ":" + line);
				}
				long offset = Long.parseLong(fields[0]);
				String url = fields[1];

				if(prevUrl != null) {
					pw.format("%s\t%s\t%d\t%d\n", 
							prevUrl, part, prevOffset, offset - prevOffset);
				}
				prevUrl = url;
				prevOffset = offset;
			}
			if(prevUrl != null) {
				pw.format("%s\t%s\t%d\t%d\n", 
						prevUrl, part, prevOffset, length - prevOffset);
			}
			br.close();
		}
		pw.flush();
	}

	public int run(String[] args) throws Exception {
		if(args.length < 1) {
			USAGE(1);
		}
		if(args.length > 2) {
			USAGE(1);
		}
		
		String hdfsUrl = args[0];
		boolean isOld = false;
		if(args.length == 2) {
			hdfsUrl = args[1];
			isOld = true;
		}
		URI uri = new URI(hdfsUrl);
		FileSystem fs = FileSystem.get(uri,getConf());
		Path path = new Path(hdfsUrl);
		Path target = new Path(path,ALL_SUMMARY_PREFIX + SUMMARY_SUFFIX);
		if(fs.exists(target)) {
			System.err.format("Error-exists: " + target.toUri().toASCIIString());
			return 1;
		}
		
		Charset UTF8 = Charset.forName("utf-8");
		FSDataOutputStream os = fs.create(target);
			OutputStreamWriter osw = new OutputStreamWriter(os, UTF8);
		PrintWriter pw = new PrintWriter(osw);
		if(isOld) {
			createSummaryOld(fs, path, pw);
		} else {
			createSummary(fs, path, pw);
		}
		osw.flush();
		osw.close();
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new SummaryGenerator(), args);
		System.exit(res);
	}
}
