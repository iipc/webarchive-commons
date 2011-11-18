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
import org.archive.hadoop.mapreduce.GlobalWaybackMergeMapper;
import org.archive.hadoop.mapreduce.SimpleTextMapper;
import org.archive.hadoop.mapreduce.SortMergeInputFormat;
import org.archive.hadoop.mapreduce.ZipNumOutputFormat;

public class MergeClusters implements Tool {
	
	public static final String TOOL_NAME = "cluster-merge";
	public static final String TOOL_DESCRIPTION = "A map/reduce program that merges a set of clusters with a common split into a new cluster.";
	
	Configuration conf = null;
	/**
	 * As hard-coded into the Text RecordWriter
	 */
	public static String TEXT_OUTPUT_DELIM_CONFIG = 
		"mapred.textoutputformat.separator";
	
	static int printUsage() {
		System.out.println(TOOL_NAME + " [OPTIONS] <input> <output>");
		System.out.println("\tOPTIONS can be:");
		System.out.println("\t\t-m NUM - try to run with approximately NUM map tasks");
		System.out.println("\t\t--max-map-attempts NUM - retry map(merge) tasks up to NUM times(default likely 3..)");
		System.out.println("\t\t--compressed-input - assume input is compressed, even without .gz suffix");
		System.out.println("\t\t--gzip-range - assume input lines are PATH START LENGTH such that a");
		System.out.println("\t\t\t valid gzip record exists in PATH between START and START+LENGTH");
		System.out.println("\t\t\t that contains the records to process");
		System.out.println("\t\t--compress-output - compress output files with GZip");
		System.out.println("\t\t--zip-num-output NUM - compress output files with ZipNum, into blocks wit NUM lines");
		System.out.println("\t\t--day-limit NUM - only allow NUM captures in a given day - discard extras");
		System.out.println("\t\t--delimiter DELIM - assume DELIM delimter for input and output, instead of default <SPACE>");
		System.out.println("\t\t--global-cdx - perform special filtering for the global Wayback CDX:");
		System.out.println("\t\t\t. if lines have 10 columns, assume column 8 is HTML meta info - omit those with 'A'");
		System.out.println("\t\t\t. reduce digest field 6 to 3-digits");
		System.out.println("\t\t\t. omit records with non numeric HTTP response code field 5");
		System.out.println("\t\t\t. omit records with non numeric file offset field -2");
		System.out.println("\t\t\t. omit records which are 502/504 live web ARCs");
		System.out.println("\t\t\t. only all 111 records per url-day");
		
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
		String delim = " ";
		
		int zipNumLines = -1;

		boolean globalCDX = false;
		List<String> otherArgs = new ArrayList<String>();
		Job job = new Job(getConf(), "sort-merge");
		Configuration conf = job.getConfiguration();

		for (int i = 0; i < args.length; ++i) {
			try {
				if ("--global-cdx".equals(args[i])) {
					globalCDX = true;
				} else if ("--zip-num-output".equals(args[i])) {
					zipNumLines = Integer.parseInt(args[++i]);
				} else if ("--day-limit".equals(args[i])) {
					ZipNumOutputFormat.setZipNumOvercrawlDayCount(conf,
							Integer.parseInt(args[++i]));
				} else if ("--max-map-attempts".equals(args[i])) {
					conf.setInt("mapred.map.max.attempts",Integer.parseInt(args[++i]));
				} else if ("--delimiter".equals(args[i])) {
					delim = args[++i];
				} else if("--filter-field".equals(args[i])) {
					SortMergeInputFormat.setFilterField(conf, 
							Integer.parseInt(args[++i]));

				} else if("--filter-path".equals(args[i])) {
					SortMergeInputFormat.setFilterPath(conf,args[++i]);

				} else if("--filter-surt".equals(args[i])) {
					SortMergeInputFormat.setFilterSURT(conf, true);

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

		// Make sure there are exactly 2 parameters left: inputPath output
		if (otherArgs.size() != 2) {
			System.out.println("ERROR: Wrong number of parameters: "
					+ otherArgs.size() + " instead of 2.");
			return printUsage();
		}
		

		String inputPathString = otherArgs.get(0);
		String outputPathString = otherArgs.get(1);

		Path outputPath = new Path(outputPathString);
		
		
		job.setInputFormatClass(SortMergeInputFormat.class);
		System.out.format("Setting input path(%s)\n", inputPathString);
		SortMergeInputFormat.setInputPath(conf, inputPathString);

		job.setJarByClass(MergeClusters.class);

		if(globalCDX) {
			job.setMapperClass(GlobalWaybackMergeMapper.class);
		} else {
			job.setMapperClass(SimpleTextMapper.class);
		}

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);		
		
		// set up the delimter:
		conf.set(TEXT_OUTPUT_DELIM_CONFIG, delim);
		
		if(zipNumLines != -1) { 
			System.err.format("INFO: zipnum count: %d\n",zipNumLines);
			ZipNumOutputFormat.setZipNumLineCount(conf, zipNumLines);
		}
		job.setOutputFormatClass(ZipNumOutputFormat.class);

		FileOutputFormat.setOutputPath(job, outputPath);

		job.setNumReduceTasks(0);

		return (job.waitForCompletion(true) ? 0 : 1);
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new MergeClusters(), args);
		System.exit(res);
	}

	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

}
