package org.archive.hadoop.jobs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.hadoop.mapreduce.HTTPImportMapper;

public class HTTPImportJob extends Configured implements Tool {
	Configuration conf = null;
	
	public final static String TOOL_NAME = "httpimport";
	public final static String TOOL_DESCRIPTION = "A map/reduce program that imports a bunch of HTTP URLs into HDFS.";
	
	public final static String HTTP_IMPORT_TARGET = "http-import.target";
	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
		
	}

	static int printUsage() {
		System.out.println("http-import [OPTIONS] <input> <outputdir> <importTarget>");
		System.out.println("\tOPTIONS can be:");
		System.out.println("\t\t-m NUM - try to run with approximately NUM map tasks");
		System.out.println("\t\t--soft-fails - skip URLs that cannot be fetched. Default is to fail task & abort job.");
		System.out.println("\tThe input file contains lines of the form:");
		System.out.println("\t\t\tURL");
		System.out.println("\tOR");
		System.out.println("\t\t\tBASENAME<SPACE>URL");
		System.out.println("\tif only URL is specified, then the target will be <importTarget>/<BASENAME of URL>");
		System.out.println("\totherwise the target will be <importTarget>/<BASENAME>");
		System.out.println();
		return -1;
	}
	
	public int run(String[] args) throws Exception {
		Job job = new Job(getConf(), "http-import");
		Configuration conf = job.getConfiguration();
		job.setJarByClass(HTTPImportJob.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapperClass(HTTPImportMapper.class);

		int i = 0;
		int numMaps = 10;
		while(i < args.length -1) {
			if(args[i].equals("-m")) {
				i++;
				numMaps = Integer.parseInt(args[i]);
				i++;
			} else if(args[i].equals("--soft-fails")) {
				HTTPImportMapper.setSoftFails(conf, true);
				i++;
			} else {
				break;
			}
		}
		if(args.length - 3 != i) {
			printUsage();
//			throw new IllegalArgumentException("wrong number of args...");
		}
		Path inputPath = new Path(args[i]);
		Path outputPath = new Path(args[i+1]);
		Path targetPath = new Path(args[i+2]);

		TextInputFormat.addInputPath(job, inputPath);
		FileOutputFormat.setOutputPath(job, outputPath);
		HTTPImportMapper.setTargetDir(conf, targetPath.toString());

		conf.setBoolean("mapred.map.tasks.speculative.execution", false);
		
		FileSystem fs = inputPath.getFileSystem(conf);
		FileStatus inputStatus = fs.getFileStatus(inputPath);
		long inputLen = inputStatus.getLen();
		long bytesPerMap = (int) inputLen / numMaps;

		FileInputFormat.setMaxInputSplitSize(job, bytesPerMap);

		
		return (job.waitForCompletion(true) ? 0 : 1);
	}
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new HTTPImportJob(), args);
		System.exit(res);
	}

}
