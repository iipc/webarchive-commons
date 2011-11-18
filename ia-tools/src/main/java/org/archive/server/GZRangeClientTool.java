package org.archive.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.util.DateUtils;

import com.google.common.io.ByteStreams;

public class GZRangeClientTool implements Tool {
	public final static String TOOL_NAME = "gzrange-client";
	public static final String TOOL_DESCRIPTION = 
		"Command line tool for repackages records from remote ARC/WARC files into new ARC/WARC files.";
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final static String USAGE_HEADER = "Repackages a series of W/ARC records into new W/ARC files.\n\n"
		+ "Reads lines from MANIFEST, which are of the format:\n\n"
		+ "\tOFFSET URL1 URL2 ... URLN\n\n"
		+ "where:\n\n"
		+ "\tOFFSET is the start offset of a W/ARC record\n"
		+ "\tURLX are HTTP URLs pointing to the W/ARCs\n\n"
		+ "A new series of W/ARC files are written in TGT_DIR, where each is prefixed with PREFIX\n"
		+ "\n";	
	
	private Configuration conf;

	public void setConf(Configuration conf) {
		this.conf = conf;
	}
	public Configuration getConf() {
		return conf;
	}
	
	public static int USAGE(Options opts) {
		HelpFormatter formatter = new HelpFormatter();
		System.err.println();
		PrintWriter pw = new PrintWriter(System.err);
		formatter.printHelp(pw,80,TOOL_NAME + " [OPTIONS] TGT_DIR PREFIX MANIFEST",USAGE_HEADER,opts,4,5,"");
		pw.flush();
		return 1;
	}


	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new GZRangeClientTool(), args);
		System.exit(res);
	}

	private static Options buildOptions() {
		Options options = new Options();
        
		Option arcSize = new Option("as","arc-size",true,
		 "stop writing records to ARCs after they grow beyond SIZE bytes");
		arcSize.setArgName("SIZE");

		Option warcSize = new Option("ws","warc-size",true,
				 "stop writing records to WARCs after they grow beyond SIZE bytes");
		warcSize.setArgName("SIZE");
		
		Option warcHeaderFields = new Option("wf","warc-header-fields",true,
				"Read default WARC header fields from file PATH");
		warcHeaderFields.setArgName("PATH");

		Option timestamp = new Option("t","timestamp",true,
				"Use TIMESTAMP14 as the timestamp for W/ARC names, and for W/ARC header records.");
		timestamp.setArgName("TIMESTAMP14");

		Option errOnExit = new Option("e", "exit-on-error", false, 
		"if declared, a failure to get a single record causes a failure in the tool");
		
		options.addOption(arcSize);
		options.addOption(warcSize);
		options.addOption(warcHeaderFields);
		options.addOption(timestamp);
		options.addOption(errOnExit);

		
		return options;
	}
	
	public int run(String[] args) throws Exception {
		Options options = buildOptions();
		CommandLineParser parser = new PosixParser();
		CommandLine line = null;
		try {
			line = parser.parse( options, args );
		} catch (ParseException e) {
			System.err.format("Problem parsing options (%s)\n", e.getMessage());
			return USAGE(options);
		}
	    String[] extra = line.getArgs();
	    if(extra.length != 3) {
	    	return USAGE(options);
	    }
    	
	    File targetDir = new File(extra[0]);
	    String prefix = extra[1];
	    File manifest = new File(extra[2]);
	    
	    if(!targetDir.isDirectory()) {
	    	System.err.println("Target directory(" + extra[0] + ") is not a directory");
    		return 1;
	    }
	    if(!targetDir.canWrite()) {
	    	System.err.println("Target directory(" + extra[0] + ") is not writable");
    		return 1;
	    }
	    if(!manifest.isFile()) {
	    	System.err.println("Manifest file(" + extra[2] + ") is not a file");
    		return 1;
	    }
	    if(!manifest.canRead()) {
	    	System.err.println("Manifest file(" + extra[2] + ") is not readable");
    		return 1;
	    }
	    
	    String timestamp14 = DateUtils.get14DigitDate(System.currentTimeMillis());
	    if(line.hasOption("timestamp")) {
	    	timestamp14 = DateUtils.get14DigitDate(DateUtils.parse14DigitDate(line.getOptionValue("timestamp")));
	    }
	    GZRangeClient cli = new GZRangeClient(targetDir, prefix, timestamp14);
	    if(line.hasOption("arc-size")) {
	    	cli.setMaxArcSize(Long.parseLong(line.getOptionValue("arc-size")));
	    }
	    if(line.hasOption("warc-size")) {
	    	cli.setMaxWarcSize(Long.parseLong(line.getOptionValue("warc-size")));
	    }
	    if(line.hasOption("warc-header-fields")) {
	    	String path = line.getOptionValue("warc-header-fields");
	    	File f = new File(path);
	    	FileInputStream fis = new FileInputStream(f);
	    	int len = (int) f.length();
	    	byte[] whf = new byte[len];
	    	ByteStreams.readFully(fis, whf);
	    	cli.setWarcHeaderContents(whf);
	    }
	    if(line.hasOption("e")) {
	    	System.err.println("Exit on error mode");
	    	cli.setExitOnError(true);
	    }
	    
	    FileInputStream manIS = new FileInputStream(manifest);
	    InputStreamReader manR = new InputStreamReader(manIS,UTF8);
	    BufferedReader manBufR = new BufferedReader(manR);
	    while(true) {
	    	String manLine = manBufR.readLine();
	    	if(manLine == null) {
	    		break;
	    	}
	    	String[] parts = manLine.split("\\s");
	    	if(parts.length < 2) {
	    		System.err.format("Line(%s) has < 2 fields\n",manLine);
	    		return 1;
	    	}
	    	long offset = 0;
	    	try {
	    		offset = Long.parseLong(parts[0]);
	    	} catch(NumberFormatException e) {
	    		System.err.format("Line(%s) has non numeric column 1\n",manLine);
	    		return 1;
	    	}
	    	ArrayList<String> urls = new ArrayList<String>();
	    	for(int i = 1; i < parts.length; i++) {
	    		if(!parts[i].startsWith("http://")) {
		    		System.err.format("URL in Line(%s) does not start with http://\n",manLine);
		    		System.exit(1);	    			
	    		}
	    		urls.add(parts[i]);
	    	}
	    	cli.append(offset, urls);
	    }
	    cli.finish();
	    return 0;
	}
}
