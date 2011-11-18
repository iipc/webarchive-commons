package org.archive.hadoop.jobs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import org.archive.hadoop.io.MergeClusterRangesInputFormat;
import org.archive.hadoop.mapreduce.SimpleTextMapper;
import org.archive.hadoop.mapreduce.ZipNumOutputFormat;

public class MergeClusterRangesJob implements Tool {
	public final static String TOOL_NAME = "merge-clusters";
	public static final String TOOL_DESCRIPTION = 
		"A tool for merging and re-partitioning CDX clusters";

	Configuration conf = null;
	/**
	 * As hard-coded into the Text RecordWriter
	 */
	public static String TEXT_OUTPUT_DELIM_CONFIG = 
		"mapred.textoutputformat.separator";
	
	static int printUsage() {
		System.out.println("merge-clusters [OPTIONS] SPLIT_PATH OUTPUT_DIRECTORY CLUSTER_PATH ...");
		System.out.println();
		System.out.println("Merge and possibly re-partition multiple clusters into a new cluster at OUTPUT_DIRECTORY.");
		System.out.println("SPLIT_PATH is the HDFS URL containing lines of the form:");
		System.out.println("\t\tNAME<tab>START<tab>END");
		System.out.println("");
		System.out.println("The output cluster will contain one .gz file for each line, ");
		System.out.println("containing records between START(inclusive) and END(exclusive)");
		System.out.println("");
		System.out.println("CLUSTER_PATH is a series of directories containing existing clusters, which must have an ALL.summary file.");
		System.out.println("\tOPTIONS can be:");
		System.out.println("\t\t--max-map-attempts NUM - retry map(merge) tasks up to NUM times(default likely 3..)");
		System.out.println("\t\t--zip-num-output NUM - compress output files with ZipNum, into blocks wit NUM lines, default is 3000");
//		System.out.println("\t\t--day-limit NUM - only allow NUM captures in a given day - discard extras");
//		System.out.println("\t\t--delimiter DELIM - assume DELIM delimter for input and output, instead of default <SPACE>");
//		System.out.println("\t\t--global-cdx - perform special filtering for the global Wayback CDX:");
//		System.out.println("\t\t\t. if lines have 10 columns, assume column 8 is HTML meta info - omit those with 'A'");
//		System.out.println("\t\t\t. reduce digest field 6 to 3-digits");
//		System.out.println("\t\t\t. omit records with non numeric HTTP response code field 5");
//		System.out.println("\t\t\t. omit records with non numeric file offset field -2");
//		System.out.println("\t\t\t. omit records which are 502/504 live web ARCs");
//		System.out.println("\t\t\t. only all 111 records per url-day");
		
//		System.out.println("\t\t--map-global - use the GLOBAL CDX map function, which implies:");
//		System.out.println("\t\t\t. extra trailing field indicating HTML meta NOARCHIVE data, which should be omitted, result lines do not include the last field");
//		System.out.println("\t\t\t. truncating digest field to 3 digits");
//		System.out.println("\t\t\t. column 0 is original URL (identity CDX files)");
		System.out.println();
//		ToolRunner.printGenericCommandUsage(System.out);
		return -1;
	}

	/**
	 * The main driver for sort program. Invoke this method to submit the
	 * map/reduce job.
	 * 
	 * @throws IOException
	 *             When there is communication problems with the job tracker.
	 */
	public int run(String[] args) throws Exception {
		
		int zipNumLines = -1;

		List<String> otherArgs = new ArrayList<String>();
		Job job = new Job(getConf(), "merge-clusters");
		Configuration conf = job.getConfiguration();
		for (int i = 0; i < args.length; ++i) {
			try {
				if ("--zip-num-output".equals(args[i])) {
					zipNumLines = Integer.parseInt(args[++i]);
				} else if ("--max-map-attempts".equals(args[i])) {
					conf.setInt("mapred.map.max.attempts",Integer.parseInt(args[++i]));
				} else {
					otherArgs.add(args[i]);
				}
			} catch (NumberFormatException except) {
				System.out.println("ERROR: Integer expected instead of "
						+ args[i]);
				return printUsage();
			} catch (ArrayIndexOutOfBoundsException except) {
				System.out.println("ERROR: Required parameter missing from "
						+ args[i - 1]);
				return printUsage(); // exits
			}
		}

		// Make sure there are at least 3 parameters left: split target input input ...
		if (otherArgs.size() < 3) {
			System.out.println("ERROR: Wrong number of parameters: "
					+ otherArgs.size() + " instead of 2.");
			return printUsage();
		}

		MergeClusterRangesInputFormat.setSplitPath(conf, otherArgs.get(0));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs.get(1)));
		String[] clusters = new String[otherArgs.size() - 2];
		for(int i = 2; i < otherArgs.size(); i++) {
			clusters[i-2] = otherArgs.get(i);
		}
		MergeClusterRangesInputFormat.setClusterPaths(conf, clusters);
		
		job.setInputFormatClass(MergeClusterRangesInputFormat.class);
		job.setJarByClass(MergeClusterRangesJob.class);

		job.setMapperClass(SimpleTextMapper.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);		
		
		if(zipNumLines != -1) { 
			System.err.format("INFO: zipnum count: %d\n",zipNumLines);
			ZipNumOutputFormat.setZipNumLineCount(conf, zipNumLines);
		}
		job.setOutputFormatClass(ZipNumOutputFormat.class);

		job.setNumReduceTasks(0);

		return (job.waitForCompletion(true) ? 0 : 1);
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new MergeClusterRangesJob(), args);
		System.exit(res);
	}

	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}



}
