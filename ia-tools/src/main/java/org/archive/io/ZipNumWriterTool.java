package org.archive.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.format.gzip.zipnum.ZipNumWriter;

public class ZipNumWriterTool implements Tool {
	public final static String TOOL_NAME = "zipnum-writer";
	public final static String TOOL_DESCRIPTION = "A command line tool for producing ZipNum output files";
	
	private static final Charset UTF8 = Charset.forName("utf-8");
	private Configuration conf;
	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return conf;
	}

	private static int USAGE(int code, String msg) {
		if(msg != null) {
			System.err.println(msg);
		}
		System.err.println("USAGE " + TOOL_NAME + " GZ SUMM LIMIT");
		System.err.println("USAGE " + TOOL_NAME + " GZ SUM LIMIT INPUT");
		System.err.println("Write ZipNum at GZ, Summary at SUM, with LIMIT lines per record");
		System.err.println("If INPUT is specified, read lines from INPUT, otherwise from STDIN");

		return code;
	}

	public int run(String args[]) throws IOException {
		if((args.length < 3) || (args.length > 4)) {
			return USAGE(1,"Wrong number of arguments");
		}
		InputStream in = System.in;
		int arg = 0;
		if(args.length == 4) {
			in = new FileInputStream(new File(args[arg++]));
		}
		File gz = new File(args[arg++]);
		File summ = new File(args[arg++]);
		int limit = Integer.valueOf(args[arg++]);
		ZipNumWriter znw = new ZipNumWriter(new FileOutputStream(gz,false), 
				new FileOutputStream(summ,false), limit);

		InputStreamReader isr = new InputStreamReader(in,UTF8);
		BufferedReader br = new BufferedReader(isr);
		while(true) {
			String line = br.readLine();
			if(line == null) {
				znw.close();
				break;
			}
			line = line + "\n";
			znw.addRecord(line.getBytes(UTF8));
		}
		znw.close();
		return 0;
	}
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new ZipNumWriterTool(), args);
		System.exit(res);
	}


}
