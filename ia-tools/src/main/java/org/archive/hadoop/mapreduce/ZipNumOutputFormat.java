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

public class ZipNumOutputFormat extends FileOutputFormat<Text, Text> {
	private int count;
	private static final int DEFAULT_ZIP_NUM_LINES = 5000;
	private static final String ZIP_NUM_LINES_CONFIGURATION = "conf.zipnum.count";
	private static final String ZIP_NUM_OVERCRAWL_CONFIGURATION = "conf.zipnum.overcrawl.daycount";

	public ZipNumOutputFormat() {
		this(DEFAULT_ZIP_NUM_LINES);
	}

	public ZipNumOutputFormat(int count) {
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

		Path mainFile = getWorkFile(context, ".gz");
		Path summaryFile = getWorkFile(context, ".summary");
		FileSystem mainFs = mainFile.getFileSystem(conf);
		FileSystem summaryFs = summaryFile.getFileSystem(conf);
		FSDataOutputStream mainOut = mainFs.create(mainFile, false);
		FSDataOutputStream summaryOut = summaryFs.create(summaryFile, false);
		if(dayLimit == -1) {
			// This (should be) a better implementation, but appears to have a 
			// bug - summary files are empty in some cases.. Should track it down
//			return new ZipNumRecordWriter(count, mainOut, summaryOut);
			return new ZipNumRecordWriterOld(count, mainOut, summaryOut);
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
	public Path getWorkFile(TaskAttemptContext context, String extension)
			throws IOException {
		FileOutputCommitter committer = (FileOutputCommitter) getOutputCommitter(context);

		TaskID taskId = context.getTaskAttemptID().getTaskID();  
		int partition = taskId.getId();
		String basename = 
			PartitionName
				.getPartitionOutputName(context.getConfiguration(), partition);
		if(basename == null) {
			// use default name:
			basename = String.format("part-%05d", partition);
		}
		basename = basename + extension;
		return new Path(committer.getWorkPath(), basename);
	}
}
