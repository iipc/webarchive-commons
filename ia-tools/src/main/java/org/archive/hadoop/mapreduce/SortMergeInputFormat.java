package org.archive.hadoop.mapreduce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.archive.hadoop.util.PartitionName;
import org.archive.url.SURT;
import org.archive.util.io.BytesReadObserver;
import org.archive.util.io.MultiMemberOpenJDKGZIPInputStream;
import org.archive.util.io.NotifyingInputStream;
import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.CachingStringFilter;
import org.archive.util.iterator.FilterStringIterator;
import org.archive.util.iterator.SortedCompositeIterator;
import org.archive.util.iterator.StringFilter;
import org.archive.util.iterator.StringTransformer;
import org.archive.util.iterator.TransformingPrefixStringFilter;

public class SortMergeInputFormat extends InputFormat<Long, Text> {

	private final static Logger LOG = 
		Logger.getLogger(SortMergeInputFormat.class.getName());
	private final static Charset UTF8 = Charset.forName("UTF-8");
	
	private static final String SORT_MERGE_INPUT_PATH_CONFIG = 
		"sort.merge.input.path";
	private static final String SORT_MERGE_INPUT_COMPRESSED_CONFIG = 
		"sort.merge.input.compressed";

	private static final String SORT_MERGE_INPUT_FILTER_FIELD = 
		"sort.merge.input.filter.field";
	private static final String SORT_MERGE_INPUT_FILTER_PATH = 
		"sort.merge.input.filter.path";
	
	// TODO: Make this configurable by class:
	private static final String SORT_MERGE_INPUT_FILTER_SURT = 
		"sort.merge.input.filter.surt";

	public static void setInputPath(Configuration conf, String path) throws IOException {
		conf.set(SORT_MERGE_INPUT_PATH_CONFIG, path);
		addPartitionOutputNames(conf, new Path(path));
	}

	public static Path getInputPath(Configuration conf) {
		return new Path(conf.get(SORT_MERGE_INPUT_PATH_CONFIG));
	}
	
	

	// crazy hack - the Configuration doesn't get serialized after getSplits().
	public static void addPartitionOutputNames(Configuration conf, Path path) 
	throws IOException {
		
		Path inputPath = getInputPath(conf);
		FileSystem fs = FileSystem.get(inputPath.toUri(), conf);
		FSDataInputStream fsdis = fs.open(inputPath);
		InputStreamReader isr = new InputStreamReader(fsdis, Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);
		int i = 0;
		while(true) {
			String line = br.readLine();
			if(line == null) {
				break;
			}
			String parts[] = line.split("\\s");
			PartitionName.setPartitionOutputName(conf, i, parts[0]);
			i++;
		}
		br.close();
	}

	public static void setCompressedInput(Configuration conf,
			boolean compressed) {
		conf.setBoolean(SORT_MERGE_INPUT_COMPRESSED_CONFIG, compressed);
	}
	public static boolean getCompressedInput(Configuration conf) {
		return conf.getBoolean(SORT_MERGE_INPUT_COMPRESSED_CONFIG,false);
	}
	
	public static void setFilterField(Configuration conf, int field) {
		conf.setInt(SORT_MERGE_INPUT_FILTER_FIELD, field);
	}
	public static int getFilterField(Configuration conf) {
		return conf.getInt(SORT_MERGE_INPUT_FILTER_FIELD,-1);
	}
	public static void setFilterPath(Configuration conf, String path) {
		conf.set(SORT_MERGE_INPUT_FILTER_PATH, path);
	}
	public static Path getFilterPath(Configuration conf) {
		return new Path(conf.get(SORT_MERGE_INPUT_FILTER_PATH));
	}
	public static void setFilterSURT(Configuration conf, boolean isSURT) {
		conf.setBoolean(SORT_MERGE_INPUT_FILTER_SURT, isSURT);
	}
	public static boolean getFilterSURT(Configuration conf) {
		return conf.getBoolean(SORT_MERGE_INPUT_FILTER_SURT,false);
	}

	@Override
	public RecordReader<Long, Text> createRecordReader(InputSplit arg0,
			TaskAttemptContext arg1) throws IOException, InterruptedException {
		return new SortMergeRecordReader();
	}
	
	@Override
	public List<InputSplit> getSplits(JobContext context) throws IOException,
			InterruptedException {
		
		Configuration conf = context.getConfiguration();
		Path inputPath = getInputPath(conf);
		FileSystem fs = FileSystem.get(inputPath.toUri(), conf);
		FSDataInputStream fsdis = fs.open(inputPath);
		InputStreamReader isr = new InputStreamReader(fsdis, Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);
		ArrayList<InputSplit> splits = new ArrayList<InputSplit>();
		long pos = Integer.MAX_VALUE - 1;
		int i = 0;
		while(true) {
			String line = br.readLine();
			if(line == null) {
				break;
			}
			SortMergeInputSplit s = parseInputSplit(fs,pos,line);
			PartitionName.setPartitionOutputName(conf, i, s.getOutputName());
			i++;
			pos--;
			splits.add(s);
		}
		br.close();
		return splits;
	}

	private SortMergeInputSplit parseInputSplit(FileSystem fs, long pos, String line) throws IOException {
		String parts[] = line.split("\\s");
		FileStatus biggest = null;
		ArrayList<Path> inputPaths = new ArrayList<Path>();
		String outputPath = null;
		
		for(String part : parts) {
			if(outputPath == null) {
				outputPath = part;
				continue;
			}
			Path path = new Path(part);
			FileStatus status = fs.getFileStatus(path);
			if(status.isDir()) {
				throw new IOException(String.format("Part(%s) is a Directory!",
							part));
			}
			if(biggest == null) {
				biggest = status;
			} else {
				if(status.getLen() > biggest.getLen()) {
					biggest = status;
				}
			}
			inputPaths.add(path);
		}
		BlockLocation[] blockLocations = 
			fs.getFileBlockLocations(biggest, 0, biggest.getBlockSize());
		
		LOG.warning(String.format("Created InputSplit(%d) Hosts(%s) paths(%s)",
				biggest.getLen(),
				StringUtils.join(blockLocations[0].getHosts(), ","),
				StringUtils.join(inputPaths, ",")));
		String[] inp = new String[inputPaths.size()];
		for(int i = 0; i< inputPaths.size(); i++) {
			inp[i] = inputPaths.get(i).toString();
		}
		
		return new SortMergeInputSplit(pos, outputPath,
				blockLocations[0].getHosts(), inp);
	}

	public class SortMergeRecordReader extends RecordReader<Long, Text> implements BytesReadObserver {
		float progress = 0.0f;
		long totalBytes = 0;
		long bytesRead = 0;
		Long key = null;
		Text value = null;
		ArrayList<BufferedReader> readers;
		Iterator<String> itr;

		public void notifyBytesRead(int read) {
			if(read > 0) {
				bytesRead += read;
				progress = (float) bytesRead / (float) totalBytes;
			}
		}
		
		@Override
		public void close() throws IOException {
			for(BufferedReader reader : readers) {
				reader.close();
			}
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
			return progress;
		}

		@Override
		public void initialize(InputSplit split, TaskAttemptContext context)
				throws IOException, InterruptedException {

			SortMergeInputSplit smSplit;
			if(split instanceof SortMergeInputSplit) {
				smSplit = (SortMergeInputSplit) split;
			} else {
				throw new IOException("Split not right class!?");
			}
			Configuration conf = context.getConfiguration();
			readers = new ArrayList<BufferedReader>();

			SortedCompositeIterator<String> itrS = 
				new SortedCompositeIterator<String>(new Comparator<String>() {
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});

			LOG.warning(String.format("Split - components(%s)\n",
					StringUtils.join(smSplit.getPaths(), ',')));

			for(String pathStr : smSplit.getPaths()) {
				Path path = new Path(pathStr);
				FileSystem fs = path.getFileSystem(conf);
				FSDataInputStream fsdis = fs.open(path);
				FileStatus status = fs.getFileStatus(path);
				totalBytes += status.getLen();
				InputStream is = new NotifyingInputStream(fsdis,this);

				if(path.toString().endsWith(".gz") || 
						getCompressedInput(conf)) {
					is = new MultiMemberOpenJDKGZIPInputStream(is);
					
					LOG.warning(String.format("Opened(%s) as GZ",pathStr));
				} else {
					LOG.warning(String.format("Opened(%s) as RAW",pathStr));
				}
				InputStreamReader isr = new InputStreamReader(is, UTF8);
				BufferedReader br = new BufferedReader(isr);
				readers.add(br);
				itrS.addIterator(AbstractPeekableIterator.wrapReader(br));
			}
			int filterField = getFilterField(conf);
			if(filterField >= 0) {
				Path filterPath = getFilterPath(conf);
				boolean isSURT = getFilterSURT(conf);
				StringTransformer stringTransformer = null;
				if(isSURT) {
					stringTransformer =	new StringTransformer() {
						public String transform(String input) {
							return SURT.toSURT(input);
						}
					};
				}
				FileSystem fs = filterPath.getFileSystem(conf);
				FSDataInputStream fsdis = fs.open(filterPath);
				InputStreamReader isr = new InputStreamReader(fsdis, UTF8);
				BufferedReader br = new BufferedReader(isr);
				Iterator<String> i = AbstractPeekableIterator.wrapReader(br);
				ArrayList<String> al = new ArrayList<String>();
				while(i.hasNext()) {
					al.add(i.next());
				}
				StringFilter stringFilter = 
					new TransformingPrefixStringFilter(al, stringTransformer);
				
				// TODO: make this, and the number of cached items configurable:
				CachingStringFilter cachingFilter = 
					new CachingStringFilter(stringFilter, 100);
				FilterStringIterator itrF = 
					new FilterStringIterator(itrS, cachingFilter);
				itrF.setField(filterField);
				itr = itrF;
			} else {
				itr = itrS;
			}
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
