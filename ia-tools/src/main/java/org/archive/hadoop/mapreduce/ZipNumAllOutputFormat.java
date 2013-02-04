package org.archive.hadoop.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.archive.hadoop.util.PartitionName;

public class ZipNumAllOutputFormat extends FileOutputFormat<Text, Text> {
	private int count;
	private static final int DEFAULT_ZIP_NUM_LINES = 5000;
	private static final String ZIP_NUM_LINES_CONFIGURATION = "conf.zipnum.count";
	private static final String ZIP_NUM_OVERCRAWL_CONFIGURATION = "conf.zipnum.overcrawl.daycount";
	
	private static final String ZIP_NUM_PART_MOD = "conf.zipnum.partmod";
	private static final String DEFAULT_PART_MOD = "a-";
	private String partMod = "";

	public ZipNumAllOutputFormat() {
		this(DEFAULT_ZIP_NUM_LINES);
	}

	public ZipNumAllOutputFormat(int count) {
		this.count = count;
	}

	public static void setZipNumLineCount(Configuration conf, int count) {
		conf.setInt(ZIP_NUM_LINES_CONFIGURATION, count);
	}

	public static void setZipNumOvercrawlDayCount(Configuration conf, int count) {
		conf.setInt(ZIP_NUM_OVERCRAWL_CONFIGURATION, count);
	}

	@Override
	public RecordWriter<Text, Text> getRecordWriter(TaskAttemptContext context)
			throws IOException, InterruptedException {

		Configuration conf = context.getConfiguration();
		count = conf.getInt(ZIP_NUM_LINES_CONFIGURATION, DEFAULT_ZIP_NUM_LINES);
		int dayLimit = conf.getInt(ZIP_NUM_OVERCRAWL_CONFIGURATION, -1);
		
		partMod = conf.get(ZIP_NUM_PART_MOD, DEFAULT_PART_MOD);

		String partitionName = getPartitionName(context);
		Path mainFile = getWorkFile(context, partitionName + ".gz");
		Path summaryFile = getWorkFile(context, partitionName + "-idx");
		

		FileSystem mainFs = mainFile.getFileSystem(conf);
		FileSystem summaryFs = summaryFile.getFileSystem(conf);
		FSDataOutputStream mainOut = mainFs.create(mainFile, false);
		FSDataOutputStream summaryOut = summaryFs.create(summaryFile, false);
		if(dayLimit == -1) {
			// This (should be) a better implementation, but appears to have a 
			// bug - summary files are empty in some cases.. Should track it down
//			return new ZipNumRecordWriter(count, mainOut, summaryOut);
			return new ZipNumAllShardRecordWriter(count, mainOut, summaryOut, partitionName);
		} else {
			return new OvercrawlZipNumRecordWriter(count,dayLimit, mainOut, summaryOut);
		}
	}

	/**
	 * Get the path and filename for the output format.
	 * 
	 * @param context
	 *            the task context
	 * @param extension
	 *            an extension to add to the filename
	 * @return a full path $output/_temporary/$taskid/part-[mr]-$id
	 * @throws IOException
	 */
	public Path getWorkFile(TaskAttemptContext context, String partWithExt)
			throws IOException {
		FileOutputCommitter committer = (FileOutputCommitter) getOutputCommitter(context);
				return new Path(committer.getWorkPath(), partWithExt);
	}
	
	public String getPartitionName(TaskAttemptContext context)
	{
		TaskID taskId = context.getTaskAttemptID().getTaskID();  
		int partition = taskId.getId();
		String basename = 
			PartitionName
				.getPartitionOutputName(context.getConfiguration(), partition);
		if(basename == null) {
			// use default name:
			basename = String.format("part-%s%05d", partMod, partition);
		}
		
		return basename;
	}
}
