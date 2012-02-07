package org.archive.hadoop.mapreduce;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.archive.extract.ExtractingResourceFactoryMapper;
import org.archive.extract.ExtractingResourceProducer;
import org.archive.extract.ExtractorOutput;
import org.archive.extract.ProducerUtils;
import org.archive.extract.ResourceFactoryMapper;
import org.archive.extract.WATExtractorOutput;
import org.archive.hadoop.jobs.WATExtractorJob;
import org.archive.resource.Resource;
import org.archive.resource.ResourceProducer;
import org.archive.util.StringFieldExtractor;
import org.archive.util.StringFieldExtractor.StringTuple;

public class WATExtractorMapper extends
		Mapper<Object, Text, Text, Text> {
	
	
	public final static String WAT_EXTRACTOR_TARGET = "wat-extractor.target";
	public final static String WAT_EXTRACTOR_OVERRIDE = "wat-extractor.override";
	Path target = null;
	FileSystem filesystem = null;
	boolean overrideExistentFile = false;
	StringFieldExtractor sfe = new StringFieldExtractor(' ', 1);
	
	public static void setTargetDir(Configuration conf, String path) {
		conf.set(WAT_EXTRACTOR_TARGET, path);
	}
	
	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		Configuration conf = context.getConfiguration();
		String targetString = conf.get(WATExtractorJob.WAT_EXTRACT_TARGET);
	
		overrideExistentFile = conf.getBoolean(WAT_EXTRACTOR_OVERRIDE, false);
		target = new Path(targetString);
		filesystem = target.getFileSystem(conf);
	}
	
	public void map(Object y, Text value, Context context) 
			throws IOException,	InterruptedException {		
		
		//PArse the URL files	
		String valueS = value.toString();
		String name;
		String url = valueS;
		int idx = valueS.indexOf(' ');
		if(idx == -1) {
			URL tmpUrl = new URL(valueS);
			name = tmpUrl.getPath();
			if(name.contains("/")) {
				name = name.substring(name.lastIndexOf('/')+1);
			}
		} else {
			StringTuple t = sfe.split(valueS);
			if((t.first == null) || (t.second == null)) {
				throw new IOException("Bad input line:" + valueS);
			}
			name = t.first;
			url = t.second;
		}
			
		Path thisTarget = new Path(target,name);
		Path thisTargetTmp = new Path(target,name+".wat.gz");
		doExtract(url, thisTarget,thisTargetTmp);
	
    }
	
	private void doExtract(String url, Path target, Path targetTmp) throws IOException {
		// Check if the target exists (from previous map)
		long targetLen = getPathLength(target);
	
		int max = Integer.MAX_VALUE;

		if(targetLen > -1) {
			// there's a file in the filesystem already, 
			
			if(overrideExistentFile){
			
				if(!filesystem.delete(target, false)) {
					throw new IOException("Failed to delete old copy");
				}
			} else {
				return;
			}
		}
		
	    FSDataOutputStream fsdOut = filesystem.create(targetTmp, false); 
		ExtractorOutput out;
	   	out = new WATExtractorOutput(fsdOut);
	   	
	    ResourceProducer producer = ProducerUtils.getProducer(url);

	    ResourceFactoryMapper mapper = new ExtractingResourceFactoryMapper();
	    ExtractingResourceProducer exProducer = 
	    	new ExtractingResourceProducer(producer, mapper);

	    Logger.getLogger("org.archive").setLevel(Level.WARNING);

		int count = 0;
		int incr = 1;
		while(count < max) {
			try {
				Resource r = exProducer.getNext();
				if(r == null) {
					break;
				}
				count += incr;
				
				out.output(r);
			} catch(Exception e){
				e.printStackTrace();
				
			}
		}
	}
	
	
	private long getPathLength(Path path) throws IOException {
		FileStatus stat = null;
		try {
			stat = filesystem.getFileStatus(path);
			// present.. check by size:
		} catch (FileNotFoundException e) {
			return -1;
		}
		return stat.getLen();
	}

	public static void setOverride(Configuration conf, boolean isOverride) {
		conf.setBoolean(WAT_EXTRACTOR_OVERRIDE, isOverride);
		
	}
}
