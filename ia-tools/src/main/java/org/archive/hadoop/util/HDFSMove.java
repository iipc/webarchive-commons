package org.archive.hadoop.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class HDFSMove implements Tool {
	public final static String TOOL_NAME = "hdfs-mv";
	public static final String TOOL_DESCRIPTION = 
		"A tool for moving lots of files around within HDFS";

	private Configuration conf;

	public void setConf(Configuration conf) {
		this.conf = conf;
	}
	public Configuration getConf() {
		return conf;
	}
	private static int USAGE(int code) {
		System.err.println("USAGE");
		System.err.println(TOOL_NAME + " TARGET");
		System.err.println("read paths from STDIN, for each path, move to directory TARGET, keeping original filename");
		return code;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new HDFSMove(), args);
		System.exit(res);
	}

	public int run(String[] args) throws IOException {
		if(args.length != 1) {
		   return USAGE(1);
		}
		String targetPath = args[0];
		Path targetDir = new Path(targetPath);
		
    	FileSystem fs = targetDir.getFileSystem(new Configuration());
    	
		
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		while(true) {
			String line = br.readLine();
			if(line == null) {
				break;
			}
			Path from = new Path(line);
			Path to = new Path(targetDir,from.getName());
			if(fs.rename(from, to)) {
				System.out.format("Moved\t%s\t%s\n",
						from.toUri().toASCIIString(),
						to.toUri().toASCIIString());
			} else {
				System.err.format("FAILED-MOVE\t%s\t%s\n",
						from.toUri().toASCIIString(),
						to.toUri().toASCIIString());
			}
		}
		return 0;
	}

}
