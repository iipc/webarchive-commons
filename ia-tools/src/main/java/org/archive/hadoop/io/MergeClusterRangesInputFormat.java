package org.archive.hadoop.io;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.archive.hadoop.cdx.CDXCluster;
import org.archive.hadoop.cdx.SplitFile;
import org.archive.hadoop.util.PartitionName;
import org.archive.util.iterator.CloseableIteratorUtil;
import org.archive.util.iterator.SortedCompositeIterator;

public class MergeClusterRangesInputFormat extends InputFormat<Long, Text> {
	
	private static final Logger LOG =
		Logger.getLogger(MergeClusterRangesInputFormat.class.getName());

	private static final String SPLIT_CONFIG_KEY = "merge.cluster.split.path";
	private static final String MERGE_CLUSTER_PATH_CONFIG_KEY = "merge.cluster.paths";

	public static void setSplitPath(Configuration conf, String path) throws IOException {
		conf.set(SPLIT_CONFIG_KEY, path);
		LOG.warning(String.format("Setting Split path: %s",path));
		SplitFile splitFile = new SplitFile();
		Reader r = getSplitReader(conf);
		splitFile.read(r);
		for(int i = 0; i < splitFile.size(); i++) {
			PartitionName.setPartitionOutputName(conf, i, splitFile.getName(i));
		}
		r.close();
	}

	private static Reader getSplitReader(Configuration conf) throws IOException {
		String pathString = getSplitPath(conf);
		LOG.warning(String.format("Got Split path: %s",pathString));
		Path splitPath = new Path(pathString);
		FileSystem fs = FileSystem.get(splitPath.toUri(), conf);
		FSDataInputStream fsdis = fs.open(splitPath);
		return new InputStreamReader(fsdis, Charset.forName("UTF-8"));
		
	}
	private static String getSplitPath(Configuration conf) {
		return conf.get(SPLIT_CONFIG_KEY);
	}

	public static void setClusterPaths(Configuration conf, String[] paths) {
		conf.setStrings(MERGE_CLUSTER_PATH_CONFIG_KEY, paths);
	}
	private static String[] getClusterPaths(Configuration conf) {
		return conf.getStrings(MERGE_CLUSTER_PATH_CONFIG_KEY);
	}

	@Override
	public RecordReader<Long, Text> createRecordReader(InputSplit split,
			TaskAttemptContext arg1) throws IOException, InterruptedException {
		// TODO!!
		return new MergeClusterRangesRecordReader();
	}

	@Override
	public List<InputSplit> getSplits(JobContext context) throws IOException,
			InterruptedException {
		
		Configuration conf = context.getConfiguration();
		Reader r = getSplitReader(conf);
		SplitFile splitFile = new SplitFile();
		splitFile.read(r);
		r.close();

		ArrayList<InputSplit> splits = new ArrayList<InputSplit>();
		for(int i = 0; i < splitFile.size(); i++) {
			MergeClusterRangesInputSplit split = 
				new MergeClusterRangesInputSplit(splitFile.size() - i, 
						splitFile.getStart(i), 
						splitFile.getEnd(i),
						getClusterPaths(conf));
			splits.add(split);
			LOG.warning(String.format("Added split(%d) (%s)-(%s)",
					splitFile.size() - i,splitFile.getStart(i),splitFile.getEnd(i)));
		}

		return splits;
		
	}

	public class MergeClusterRangesRecordReader extends RecordReader<Long, Text> {
		Iterator<String> itr;
		Long key = null;
		Text value = null;

		@Override
		public void close() throws IOException {
			CloseableIteratorUtil.attemptClose(itr);
		}

		@Override
		public Long getCurrentKey() throws IOException, InterruptedException {
			return key;
		}

		@Override
		public Text getCurrentValue() throws IOException, InterruptedException {
			return value;
		}

		@Override
		public float getProgress() throws IOException, InterruptedException {
			// TODO ...
			return 0;
		}

		@Override
		public void initialize(InputSplit split, TaskAttemptContext context)
				throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();

			MergeClusterRangesInputSplit mSplit = 
				(MergeClusterRangesInputSplit) split;

			SortedCompositeIterator<String> itrS = 
				new SortedCompositeIterator<String>(new Comparator<String>() {
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});
			String start = mSplit.getStart();
			String end = mSplit.getEnd();

			for(String clusterPath : mSplit.getClusterPaths()) {
				LOG.warning(String.format("Added range(%d) (%s)-(%s): %s",
						context.getTaskAttemptID().getId(),start,end,clusterPath));
				CDXCluster cluster = new CDXCluster(conf, new Path(clusterPath));
				itrS.addIterator(cluster.getRange(start, end));
			}
			// TODO: filtering on SURTs:

			itr = itrS;
		}

		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			if(key == null) {
				key = new Long(0);
			}
			if(value == null) {
				value = new Text();
			}
			if(itr.hasNext()) {
				key = new Long(key.longValue() + 1);
				value.set(itr.next());
				return true;
			}
			return false;
		}
		
	}
}
