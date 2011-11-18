package org.archive.hadoop.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class HDFSeeko implements Tool {
	public static final String TOOL_NAME = "hdfs-fseeko";
	public static final String TOOL_DESCRIPTION = "tool which outputs ranges of files in HDFS";
	private Configuration conf;
	private static int maxRead = 1024 * 4;
	byte buffer[];
	public HDFSeeko() {
		buffer = new byte[maxRead];
	}
	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return conf;
	}
	private static int USAGE(int code) {
		System.err.println("USAGE");
		System.err.println(TOOL_NAME + " HDFS_URL [OFFSET] [LENGTH]");
		System.err.println("\tdump to STDOUT the contents of HDFS_URL");
		System.err.println("\tif additional arguments are provided the first is the offset where dumping begins");
		System.err.println("\tif a third option is specified, output up to LENGTH bytes, otherwise dump to EOF");
		System.err.println("");
		System.err.println("\tif no arguments are given, lines are read from STDIN.");
		System.err.println("\teach line is 1-3 SPACE separated fields, with semantics for the fields");
		System.err.println("\tidentical to the command line arguments. Inputs lines may contain different numbers of fields.");

		return code;
	}
	public int run(String[] args) throws Exception {
		OutputStream out = System.out;
		if(args.length == 0) {
			Charset UTF8 = Charset.forName("UTF-8");
			InputStreamReader isr = new InputStreamReader(System.in,UTF8);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while(true) {
				line = br.readLine();
				if(line == null) {
					break;
				}
				String parts[] = line.split(" ");
				try {
					dump(out,parts);
				} catch(NumberFormatException e) {
					throw new IOException("Bad input line:" + line,e);
				} catch(IllegalArgumentException e) {
					throw new IOException("Bad input line:" + line,e);
				}
			}
		} else {
			try {
				dump(out,args);
			} catch(NumberFormatException e) {
				return USAGE(1);
			} catch(IllegalArgumentException e) {
				return USAGE(1);
			}
		}
		return 0;
	}

	private void dump(OutputStream out, String args[]) throws IOException, NumberFormatException, IllegalArgumentException, URISyntaxException {
		String url = args[0];
		long offset = 0;
		long length = -1;
			if(args.length > 1) {
				offset = Long.parseLong(args[1]);
				if(args.length > 2) {
					length = Long.parseLong(args[2]);
					if(args.length > 3) {
						throw new IllegalArgumentException();
					}
				}
			}
		dump(out,url,offset,length);
	}
	
	private void dump(OutputStream out, String url, long offset, long length) 
	throws URISyntaxException, IOException {

		URI uri = new URI(url);		

		FileSystem fs = FileSystem.get(uri, getConf());
		Path path = new Path(url);
		FSDataInputStream fsdis = fs.open(path);
		fsdis.seek(offset);
		if(length == -1) {
			// dump till EOF:
			while(true) {
				int amt = fsdis.read(buffer);
				if(amt == -1) {
					break;
				}
				out.write(buffer,0,amt);
			}
		} else {
			long totalRead = 0;
			while(length > 0) {
				int amtToRead = (int) Math.min(maxRead, length);
				int amtRead = fsdis.read(buffer,0,amtToRead);
				if(amtRead == -1) {
					throw new IOException(String.format("Got EOF after (%d) bytes. (%d) left\n",
							totalRead,length));
				}
				length -= amtRead;
				totalRead += amtRead;
				out.write(buffer,0,amtRead);
			}
		}		
	}
	
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new HDFSeeko(), args);
		System.exit(res);
	}
}
