package org.archive.util.binsearch;

import java.io.File;
import java.net.URI;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.util.binsearch.impl.HDFSSeekableLineReaderFactory;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;
import org.archive.util.binsearch.impl.RandomAccessFileSeekableLineReaderFactory;
import org.archive.util.iterator.SortedCompositeIterator;

public class FileSearchTool implements Tool {
	private final static Logger LOGGER =
		Logger.getLogger(FileSearchTool.class.getName());
	
	
	private Configuration conf;
	public final static String TOOL_NAME = "bin-search";
	public static final String TOOL_DESCRIPTION = 
		"A tool for performing binary searches through sorted text files, either local, or in HDFS";

	
	private static int USAGE(int code) {
		System.err.println("USAGE: " + TOOL_NAME + " [OPTIONS] PREFIX PATH ...");
		System.err.println("\tOPTIONS can be one of:");
		System.err.println("\t\t--less-than  return records starting at the largest record smaller than PREFIX");
		System.err.println();
		System.err.println("PREFIX is the key to search for");
		System.err.println("PATH can be a path to a local file, or a path in an HDFS filesystem");
		System.err.println("if multiple PATHs are presented, they are all searched, and the matching records are merged together.");
		
		return code;
	}
		
	public int run(String[] args) throws Exception {
		if(args.length < 2) {
			return USAGE(1);
		}
		int arg = 0;
		boolean lessThan = false;
		while(arg < args.length){
			if(args[arg].equals("--less-than")) {
				lessThan = true;
				arg++;
			} else if(args[arg].equals("-v")) {
				Logger.getLogger(SortedTextFile.class.getName()).setLevel(Level.FINE);
				arg++;
			} else {
				break;
			}
		}
		String key = args[arg++];
		if(arg == args.length){
			return USAGE(1);
		}

		Comparator<String> comparator = new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		};
		SortedCompositeIterator<String> mergeItr = 
			new SortedCompositeIterator<String>(comparator);

		for(int i = arg; i < args.length; i++) {
			String spec = args[i];
			SeekableLineReaderFactory factory;
			if(spec.startsWith("hdfs://")) {
				Configuration conf = new Configuration();
				FileSystem fs = FileSystem.get(new URI(spec),conf);
				Path path = new Path(spec);
				factory = new HDFSSeekableLineReaderFactory(fs, path);
				LOGGER.warning("Added HDFS file: " + spec);
			} else if(spec.startsWith("http://")) {
				factory = new HTTPSeekableLineReaderFactory(spec);
				LOGGER.warning("Added HTTP file: " + spec);
			} else {
				File file = new File(spec);
				factory = new RandomAccessFileSeekableLineReaderFactory(file);
				LOGGER.warning("Added file: " + spec);
			}
			SortedTextFile stf = new SortedTextFile(factory);
			mergeItr.addIterator(stf.getRecordIterator(key, lessThan));
		}
		
		while(mergeItr.hasNext()) {
			System.out.println(mergeItr.next());
		}
		System.out.flush();
		mergeItr.close();

		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new FileSearchTool(), args);
		System.exit(res);
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return conf;
	}
}
