package org.archive.hadoop.cdx;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class HDFSLSR implements Tool {
	
	Charset UTF8 = Charset.forName("utf-8");
	public final static String TOOL_NAME = "hdfs-lsr";
	public static final String TOOL_DESCRIPTION = 
		"A tool for producing lsr type output from HDFS to STDOUT";
	
	private Configuration conf;
	
	
	public void listPath(FileStatus status, FileSystem fs, PrintWriter target) throws IOException {
		if(status.isDir()) {
			//System.err.format("Recursing into %s\n", status.getPath().toUri().toASCIIString());
			FileStatus entries[] = fs.listStatus(status.getPath());
			for(FileStatus entry : entries) {
				listPath(entry,fs,target);
			}
		} else {
			Path path = status.getPath();
			target.format("%s\t%s\n", 
					path.getName(), path.toUri().toASCIIString());
		}
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}
	public Configuration getConf() {
		return conf;
	}
	public static int USAGE(int code) {
		System.err.println("Usage: " + TOOL_NAME + " HDFS_URL");
		System.err.println("\tRecursively descend into HDFS_URL, producing one line");
		System.err.println("\tto STDOUT for each FILE found. Lines are of the format:");
		System.err.println("\t\tBASENAME<tab>PATH");
		return code;
	}
	public int run(String[] args) throws Exception {
		if(args.length != 1) {
			return USAGE(1);
		}
		Path path = new Path(args[0]);
		FileSystem fs = path.getFileSystem(getConf());
		FileStatus status = fs.getFileStatus(path);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out, UTF8));
		listPath(status, fs, pw);
		pw.flush();
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new HDFSLSR(), args);
		System.exit(res);
	}


}
