package org.archive.hadoop.io;

import java.text.SimpleDateFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class HDFSTouch implements Tool {
	
	protected Configuration conf;
	
	public final static String TOOL_NAME = "hdfs-touch";
	
	public final String DEFAULT_NAME_NODE_URI = "hdfs://ia400005.us.archive.org:6000";
	
	public final static String TMP_FILENAME = ".tmp_touch_latest";
	
	public final static String FORMAT_STR = "yyyy-MM-dd HH:mm:ss";
	
	protected SimpleDateFormat hadoopStatFormat = new SimpleDateFormat(FORMAT_STR);
	
	
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new HDFSTouch(), args);
		System.exit(res);
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}
	
	public Configuration getConf() {
		return conf;
	}
	
	public static int USAGE(int code) {
		System.err.println("Usage: " + TOOL_NAME + "[ -d ] HDFS_URL <" + FORMAT_STR + ">");
		System.err.println("Updated the mtime and atime on the specified hdfs path to current time, or optional timestamp");
		return code;
	}

	public int run(String[] args) throws Exception {
		
		if (args.length < 1) {
			USAGE(1);
		}
		
		String filePath = null;
		boolean updateDir = false;
		
		if (args.length == 1) {
			filePath = args[0];
		} else {
			updateDir = (args[0].equals("-d"));
			filePath = args[1];
		}
		
		if (!filePath.startsWith("hdfs://")) {
			filePath = DEFAULT_NAME_NODE_URI + filePath;
		}
		
		Path path = new Path(filePath);
		FileSystem fs = path.getFileSystem(conf);
		
		if (fs.getFileStatus(path).isDir()) {
			System.err.println("Can't touch directories in this version\nThis is a directory: " + path);
			return 1;
		}
		
		long mtime = System.currentTimeMillis();
		
		if (args.length >= 3) {
			try {
				mtime = hadoopStatFormat.parse(args[2]).getTime();
			} catch (Exception exc) {
				System.err.println("Error parsing timestamp: " + args[2] + "\nExpected format: " + FORMAT_STR);
			}
		}
		
		long atime = mtime;		
		fs.setTimes(path, mtime, atime);
		
		if (updateDir) {			
			Path tempFilePath = new Path(path.getParent(), TMP_FILENAME);
			
			// Create empty output stream
			FSDataOutputStream out = fs.create(tempFilePath);
			out.close();
			// Delete tmp file
			fs.delete(tempFilePath, false);
		}
						
		return 0;
	}
}
