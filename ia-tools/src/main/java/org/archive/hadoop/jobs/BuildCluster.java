package org.archive.hadoop.jobs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.hadoop.mapreduce.AlphaPartitioner;
import org.archive.hadoop.mapreduce.CDXMapper;
import org.archive.hadoop.mapreduce.GZIPMembersLineInputFormat;
import org.archive.hadoop.mapreduce.GZIPRangeLineDereferencingRecordReader;
import org.archive.hadoop.mapreduce.IdentityTextReducer;
import org.archive.hadoop.mapreduce.LineDereferencingInputFormat;
import org.archive.hadoop.mapreduce.LineDereferencingRecordReader;
import org.archive.hadoop.mapreduce.SimpleTextMapper;
import org.archive.hadoop.mapreduce.ZipNumOutputFormat;

public class BuildCluster implements Tool {
	
	public static final String TOOL_NAME = "cluster-build";
	public static final String TOOL_DESCRIPTION = "A map/reduce program that creates a CDX cluster, reading from several input formats";

	Configuration conf = null;
	/**
	 * As hard-coded into the Text RecordWriter
	 */
	public static String TEXT_OUTPUT_DELIM_CONFIG = 
		"mapred.textoutputformat.separator";
	
	
	
	static int printUsage() {
		System.out.println(TOOL_NAME + " [OPTIONS] <split> <input> <output>");
		System.out.println("\tOPTIONS can be:");
		System.out.println("\t\t-m NUM - try to run with approximately NUM map tasks");
		System.out.println("\t\t--compressed-input - assume input is compressed, even without .gz suffix");
		System.out.println("\t\t--skip-bad - If an input block is found to be corrupted, skip it, log it, and continue");
		System.out.println("\t\t--gzip-range - assume input lines are PATH START LENGTH such that a");
		System.out.println("\t\t\t valid gzip record exists in PATH between START and START+LENGTH");
		System.out.println("\t\t\t that contains the records to process");
		System.out.println("\t\t--surt-output - produce new-style SURT CDX");
		System.out.println("\t\t--compress-output - compress output files with GZip");
		System.out.println("\t\t--zip-num-output NUM - compress output files with ZipNum, into blocks wit NUM lines");
		System.out.println("\t\t--delimiter DELIM - assume DELIM delimter for input and output, instead of default <SPACE>");
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
		
		long desiredMaps = 10;
		int zipNumLines = -1;
		
		boolean compressOutput = false;
		boolean compressedInput = false;
		boolean gzipRange = false;
		boolean SURTOutput = false;
		Job job = new Job(getConf(), "alpha-sort");
		Configuration conf = job.getConfiguration();
		List<String> otherArgs = new ArrayList<String>();
//		int mapMode = CDXCanonicalizingMapper.MODE_FULL;
		for (int i = 0; i < args.length; ++i) {
			try {
				if ("-m".equals(args[i])) {
					desiredMaps = Integer.parseInt(args[++i]);
				} else if ("--compress-output".equals(args[i])) {
					compressOutput = true;
				} else if ("--surt-output".equals(args[i])) {
					SURTOutput = true;
				} else if ("--zip-num-output".equals(args[i])) {
					zipNumLines = Integer.parseInt(args[++i]);
				} else if ("--compressed-input".equals(args[i])) {
					compressedInput = true;
				} else if ("--skip-bad".equals(args[i])) {
					GZIPRangeLineDereferencingRecordReader.setSkipBadGZIPRanges(conf, true);
				} else if ("--max-map-attempts".equals(args[i])) {
					conf.setInt("mapred.map.max.attempts",Integer.parseInt(args[++i]));
				} else if ("--gzip-range".equals(args[i])) {
					gzipRange = true;
				} else if ("--delimiter".equals(args[i])) {
					delim = args[++i];
				} else if ("--child-opts".equals(args[i])) {
					String opts = args[++i];
					conf.set("mapred.child.java.opts", opts);
					System.err.format("Set 'mapred.child.java.opts' to '%s'\n",opts);

//				} else if ("--map-full".equals(args[i])) {
//					mapMode = CDXCanonicalizingMapper.MODE_FULL;
//				} else if ("--map-global".equals(args[i])) {
//					mapMode = CDXCanonicalizingMapper.MODE_GLOBAL;
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

		// Make sure there are exactly 3 parameters left: split input output
		if (otherArgs.size() != 3) {
			System.out.println("ERROR: Wrong number of parameters: "
					+ otherArgs.size() + " instead of 3.");
			return printUsage();
		}

		String splitPathString = otherArgs.get(0);
		String inputPathString = otherArgs.get(1);
		String outputPathString = otherArgs.get(2);

		Path splitPath = new Path(splitPathString);
		Path inputPath = new Path(inputPathString);
		Path outputPath = new Path(outputPathString);


		job.setJarByClass(BuildCluster.class);
		
		if(SURTOutput) {
			job.setMapperClass(CDXMapper.class);
		} else {
			job.setMapperClass(SimpleTextMapper.class);
		}
		
		job.setReducerClass(IdentityTextReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

//		// configure the "map mode"
//		CDXCanonicalizingMapper.setMapMode(conf, mapMode);
		
		// set up the delimter:
		conf.set(TEXT_OUTPUT_DELIM_CONFIG, delim);
		
		if(zipNumLines != -1) { 
			System.err.format("INFO: zipnum count: %d\n",zipNumLines);
			ZipNumOutputFormat.setZipNumLineCount(conf, zipNumLines);
			job.setOutputFormatClass(ZipNumOutputFormat.class);
			
		} else if (compressOutput) {
			FileOutputFormat.setCompressOutput(job, true);
			FileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);
		}

		// set up the Partitioner, including number of reduce tasks:
		FileSystem fs = inputPath.getFileSystem(conf);


		AlphaPartitioner.setPartitionPath(conf, splitPathString);
		job.setPartitionerClass(AlphaPartitioner.class);

		// calculate the byte size to get the correct number of map tasks:
		FileStatus inputStatus = fs.getFileStatus(inputPath);
		long inputLen = inputStatus.getLen();
		long bytesPerMap = (int) inputLen / desiredMaps;

		FileInputFormat.addInputPath(job, inputPath);
		FileInputFormat.setMaxInputSplitSize(job, bytesPerMap);
		if(gzipRange) { 
			
//			job.setInputFormatClass(GZIPRangeLineDereferencingInputFormat.class);			
			job.setInputFormatClass(GZIPMembersLineInputFormat.class);			
		} else {
			job.setInputFormatClass(LineDereferencingInputFormat.class);
			if(compressedInput) {
				LineDereferencingRecordReader.forceCompressed(conf);
			}
		}
		FileOutputFormat.setOutputPath(job, outputPath);

		int splitCount = AlphaPartitioner.countLinesInPath(splitPath, conf);
		System.err.format("INFO: split/partition count: %d\n",splitCount);

		System.err.format("INFO: RAW -PRE: %d\n",job.getConfiguration().getInt("mapred.reduce.tasks", -999));
		job.setNumReduceTasks(splitCount);
		System.err.format("INFO: RAW -POST: %d\n",job.getConfiguration().getInt("mapred.reduce.tasks", -999));

		System.err.format("INFO: split/partition count: %d\n",splitCount);
		System.err.format("INFO configured reducerCount: %d\n", job.getNumReduceTasks());
		return (job.waitForCompletion(true) ? 0 : 1);
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new BuildCluster(), args);
		System.exit(res);
	}

	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

}
