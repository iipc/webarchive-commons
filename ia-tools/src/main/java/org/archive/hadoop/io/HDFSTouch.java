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
	
	protected String DEFAULT_NAME_NODE_URI = "hdfs://ia400005.us.archive.org:6000";
	
	protected SimpleDateFormat hadoopStatFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
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
		System.err.println("Usage: " + TOOL_NAME + " HDFS_URL <yyyy-MM-dd HH:mm:ss>");
		System.err.println("Updated the mtime and atime on the specified hdfs path to current time, or optional timestamp");
		return code;
	}

	public int run(String[] args) throws Exception {
		
		if (args.length < 1) {
			USAGE(1);
		}
		
		String filePath = args[0];
		
		if (!filePath.startsWith("hdfs://")) {
			filePath = DEFAULT_NAME_NODE_URI + filePath;
		}
		
		Path path = new Path(filePath);
		FileSystem fs = path.getFileSystem(conf);
		
		if (fs.getFileStatus(path).isDir()) {
			System.err.println("Can't touch directories in this version: " + path);
			return 1;
		}
		
		long mtime = System.currentTimeMillis();
		
//		if (args.length >= 3) {
//			try {
//				mtime = hadoopStatFormat.parse(args[2]).getTime();
//			} catch (ParseException exc) {
//				
//			}
//		}
		
		if (args.length >= 2) {
			if (args[1].equals("-d")) {
				String tempFile = ".tmp_touch_latest";
				
				if (args.length >= 3) {
					tempFile = args[2];
				}
				
				Path tempFilePath = new Path(path.getParent(), tempFile);
				
				// Create empty output stream
				FSDataOutputStream out = fs.create(tempFilePath);
				out.close();
				fs.delete(tempFilePath, false);
			}
		}
		
		long atime = mtime;		
		fs.setTimes(path, mtime, atime);
						
		return 0;
	}
}
