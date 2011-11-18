package org.archive.hadoop.cdx;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.util.StreamCopy;

public class HDFSRangeDumper implements Tool {
	
	Charset UTF8 = Charset.forName("utf-8");
	public final static String TOOL_NAME = "range-dumper";
	public static final String TOOL_DESCRIPTION = 
		"A tool for dumping contents of files from HDFS to STDOUT";
	
	private Configuration conf;
	
	
	public void dumpPath(Path inputPath, OutputStream target) throws IOException {
		FileSystem fs = inputPath.getFileSystem(getConf());
		FSDataInputStream fsdis = fs.open(inputPath);
		StreamCopy.copy(fsdis, target);
		fsdis.close();
	}
	public void dumpPath(Path inputPath, OutputStream target, long start, long length) throws IOException {
		String inputPathString = inputPath.toUri().toASCIIString();
		FileSystem fs = inputPath.getFileSystem(getConf());
		FSDataInputStream fsdis = fs.open(inputPath);
		fsdis.seek(start);
		long amt = StreamCopy.copyLength(fsdis, target, length);
		if(amt != length) {
			throw new IOException(
					String.format("Short copy(%s)(%d)(%d): got(%d)\n",
					inputPathString,start,length,amt));
		}
		fsdis.close();
	}
	public void setConf(Configuration conf) {
		this.conf = conf;
	}
	public Configuration getConf() {
		return conf;
	}
	public static void USAGE(int code) {
		System.err.println("Usage: " + TOOL_NAME + " [INPUT]");
		System.err.println("\tReads lines from local path INPUT (or STDIN if omitted) of the format:");
		System.err.println("\t\tHDFS_URL");
		System.err.println("\tOR");
		System.err.println("\t\tHDFS_URL<tab>OFFSET<tab>LENGTH");
		System.err.println("\tIn the first form, dumps the entire contents of HDFS_URL");
		System.err.println("\tIn the second, dumps LENGTH octets from HDFS_URL beginning at offset OFFSET");
		System.exit(code);
	}
	public int run(String[] args) throws Exception {
		if(args.length > 1) {
			USAGE(1);
		}
		InputStreamReader isr = null;
		File input = null;
		if(args.length == 0) {
			isr = new InputStreamReader(System.in,UTF8);
		} else {
			input = new File(args[0]);
			isr = new InputStreamReader(new FileInputStream(input),UTF8);
		}
		BufferedReader br = new BufferedReader(isr);
		String line;
		OutputStream out = new BufferedOutputStream(System.out);
		while(true) {
			line = br.readLine();
			if(line == null) {
				break;
			}
			String parts[] = line.split("\t");
			if(parts.length == 1) {
				
				dumpPath(new Path(line), out);

			} else if(parts.length == 3) {
				
				long start = Long.parseLong(parts[1]);
				long length = Long.parseLong(parts[2]);

				dumpPath(new Path(parts[0]), System.out, start, length);
			
			} else {
				throw new IOException("Wrong number of fields in " + line);
			}
			System.err.format("Dumped\t%s\n",parts[0]);
		}
		if(input != null) {
			isr.close();
		}
		out.flush();
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new HDFSRangeDumper(), args);
		System.exit(res);
	}

}
