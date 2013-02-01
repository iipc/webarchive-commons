package org.archive.hadoop.pig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigTextInputFormat;
import org.apache.pig.builtin.PigStorage;
import org.apache.pig.bzip2r.Bzip2TextInputFormat;

public class LSRPigLoader extends PigStorage {
	
	protected String lsrRootLocation;
	
	protected String ext;
		
	public LSRPigLoader(String params)
	{
		super();
		initParams(params);
	}
	
	public LSRPigLoader(String params, String delimiter)
	{
		super(delimiter);
		initParams(params);
	}
	
	public LSRPigLoader(String params, String delimiter, String options)
	{
		super(delimiter, options);
		initParams(params);
	}
	
	protected void initParams(String paramString)
	{
		this.ext = paramString;
	}
		
	@SuppressWarnings("rawtypes")
	@Override
	public InputFormat getInputFormat() {
		if (lsrRootLocation.endsWith(".bz2") || lsrRootLocation.endsWith(".bz")) {
			return new Bzip2TextInputFormat()
			{
				@Override
				protected List<FileStatus> listStatus(JobContext job) throws IOException {
					return lsrFiltered(lsrRootLocation, job);
				}	
			};
		} else {
			return new PigTextInputFormat()
			{
				@Override
				protected List<FileStatus> listStatus(JobContext job) throws IOException {
					return lsrFiltered(lsrRootLocation, job);
				}	
			};
		}
	}
	
	@Override
	public void setLocation(String location, Job job) throws IOException {
		super.setLocation(location, job);
		lsrRootLocation = location;
	}
	
	public List<FileStatus> lsrFiltered(String location, JobContext job) throws IOException
	{
		return lsrFiltered(location, job, FileInputFormat.getInputPathFilter(job));
	}
		
	public List<FileStatus> lsrFiltered(String location, JobContext job, PathFilter customFilter) throws IOException
	{		
		ArrayList<PathFilter> filters = new ArrayList<PathFilter>();
		
		if (customFilter != null) {
			filters.add(customFilter);
		}
		
		final FileSystem fs = FileSystem.get(job.getConfiguration());
		List<FileStatus> files = new ArrayList<FileStatus>();
		
		// Extension Filter
		if ((ext != null) && !ext.isEmpty()) {
			PathFilter extFilter = new PathFilter()
			{
				public boolean accept(Path path) {
					if (path.getName().endsWith(ext)) {
						return true;
					}
					
					try {
						return fs.getFileStatus(path).isDir();
					} catch (IOException e) {
						e.printStackTrace();
						return false;
					}
				}
			};
			
			filters.add(extFilter);
		}
		
		filters.add(hiddenFileFilter);
		
		MultiPathFilter filter = new MultiPathFilter(filters);
		
		LinkedList<Path> dirQueue = new LinkedList<Path>();
		dirQueue.add(new Path(location));
		
		while (!dirQueue.isEmpty()) {
			
			Path root = dirQueue.removeFirst();
			
			FileStatus[] dirStatus = fs.listStatus(root, filter);
			
			for (FileStatus file : dirStatus)
			{				
				if (file.isDir()) {
					dirQueue.addLast(file.getPath());
				} else {
					files.add(file);		
				}
			}
		}
		
		if (files.isEmpty()) {
			String msg = "No Input Files Matched: " + (ext != null ? ext.toString() : "");
			if (customFilter != null) {
				msg += " Custom Filter: " + customFilter.toString();
			}
			throw new IOException(msg);
		}
		
		return files;
	}
	
	// Copied from FileInputFormat
	
	  static final PathFilter hiddenFileFilter = new PathFilter(){
	      public boolean accept(Path p){
	        String name = p.getName(); 
	        return !name.startsWith("_") && !name.startsWith("."); 
	      }
	    }; 
	
	  static class MultiPathFilter implements PathFilter {
	    private List<PathFilter> filters;

	    public MultiPathFilter(List<PathFilter> filters) {
	      this.filters = filters;
	    }

	    public boolean accept(Path path) {
	      for (PathFilter filter : filters) {
	        if (!filter.accept(path)) {
	          return false;
	        }
	      }
	      return true;
	    }
	  }
}
