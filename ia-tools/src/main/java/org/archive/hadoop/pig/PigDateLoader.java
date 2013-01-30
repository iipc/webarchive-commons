package org.archive.hadoop.pig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.pig.builtin.PigStorage;
import org.archive.util.ArchiveUtils;

public class PigDateLoader extends PigStorage {
	
	protected String paramString;
	
	protected PathFilter filter;
	
	final static String FILE_COMPARE_PARAM = "FileNewerThanFilter.compareParam";
	
	public static class FileNewerThanFilter extends Configured implements PathFilter
	{
		protected Date date;
		
		protected String ext;
		protected FileSystem fs;
		
		protected CompareOp op = CompareOp.GT;
		
		static enum CompareOp
		{
			EQ,
			LT,
			GT,
			GTEQ,
			LTEQ,
		}
		
		private boolean compare(long A, long B) {
			switch (op) {
			case EQ:
				return (A == B);
			case LT:
				return (A < B);
			case GT:
				return (A > B);
			case GTEQ:
				return (A >= B);
			case LTEQ:
				return (A <= B);
			default:
				return (A > B);
			}
		}
		
		protected CompareOp parseOp(String op)
		{
			if (op.equals("=")) {
				return CompareOp.EQ;
			}
			
			if (op.equals("<")) {
				return CompareOp.LT;
			}
			
			if (op.equals(">")) {
				return CompareOp.GT;
			}
			
			if (op.equals(">=")) {
				return CompareOp.GTEQ;
			}
			
			if (op.equals(">=")) {
				return CompareOp.LTEQ;
			}
			
			return CompareOp.GT;
		}
		
		@Override
		public void setConf(Configuration conf) {
			
			if (conf == null) {
				return;
			}			
			
			String paramString = conf.get(FILE_COMPARE_PARAM);
			String[] params = paramString.split("\\s+");
			
			String dateStr = null;
			
			if (params.length == 1) {
				dateStr = params[0];
			} else if (params.length == 2) {
				op = parseOp(params[0]);
				dateStr = params[1];
			} else if (params.length >= 3) {
				ext = params[0];
				op = parseOp(params[1]);
				dateStr = params[2];
			}
						
			try {
				this.fs = FileSystem.get(conf);
				
				// Get date of file rather than exact date
				if (dateStr.startsWith("/")) {
					Path datePath = new Path(dateStr);
					FileStatus status = fs.getFileStatus(datePath);
					this.date = new Date(status.getModificationTime());
				} else {
					this.date = ArchiveUtils.getDate(dateStr);	
				}
				
				System.out.println("Inited Date: " + date.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public boolean accept(Path path) {
			
			try {
				FileStatus status = fs.getFileStatus(path);
								
				if (!compare(status.getModificationTime(), date.getTime())) {
					return false;
				}
				
				// Check extension for non-directories
				if ((ext != null) && !status.isDir() && !path.getName().endsWith(ext)) {
					return false;
				}
				
				System.out.println("ACCEPTED: " + path.toString());
				return true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return false;
		}
	}
	
	public PigDateLoader(String params)
	{
		super();
		initParams(params);
	}
	
	public PigDateLoader(String params, String delimiter)
	{
		super(delimiter);
		initParams(params);
	}
	
	public PigDateLoader(String params, String delimiter, String options)
	{
		super(delimiter, options);
		initParams(params);
	}
	
	protected void initParams(String paramString)
	{
		this.paramString = paramString;
	}
	
		
	@Override
	public void setLocation(String location, Job job) throws IOException {
		
		Configuration conf = job.getConfiguration();
		conf.set(FILE_COMPARE_PARAM, paramString);
			
		String manual = conf.get("filterManually", "0");
		
		if (manual.equals("0")) {
			super.setLocation(location, job);
			FileInputFormat.setInputPathFilter(job, FileNewerThanFilter.class);	
			
		} else if (manual.equals("1")) {
			
			String inputPath = conf.get("mapred.input.dir", "");
						
			if (!inputPath.isEmpty()) {
				
				super.setLocation(location, job);
				conf.set("mapred.input.dir", inputPath);
				
				System.out.println("INPUT PATH:" + inputPath);
				return;
			}
			
			Path[] paths = filterManually(location, conf);
			
			super.setLocation(location, job);
			FileInputFormat.setInputPaths(job, paths);
		}
	}
	
	protected Path[] filterManually(String location, Configuration conf) throws IOException
	{
		FileNewerThanFilter filter = new FileNewerThanFilter();
		filter.setConf(conf);
		
		List<Path> paths = new ArrayList<Path>();
		
		LinkedList<Path> dirQueue = new LinkedList<Path>();
		dirQueue.add(new Path(location));
		
		while (!dirQueue.isEmpty()) {
			
			Path root = dirQueue.removeFirst();
			
			FileStatus[] dirStatus = filter.fs.listStatus(root, filter);
			
			for (FileStatus status : dirStatus)
			{
				Path currPath = status.getPath();
				
				if (status.isDir()) {
					dirQueue.addLast(currPath);
				} else {
					paths.add(currPath);						
				}
			}
		}
		
		Path[] pathArray = new Path[paths.size()];		
		return paths.toArray(pathArray);
	}
	
}
