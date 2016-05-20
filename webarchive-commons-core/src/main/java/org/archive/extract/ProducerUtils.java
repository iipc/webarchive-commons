package org.archive.extract;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.archive.resource.ResourceProducer;
import org.archive.resource.producer.ARCFile;
import org.archive.resource.producer.EnvelopedResourceFile;
import org.archive.resource.producer.WARCFile;

public class ProducerUtils {
	public static boolean STRICT_GZ = false;

	public static ResourceProducer getProducer(String path) throws IOException {
		return getProducer(path,0);
	}
	public static ResourceProducer getProducer(String path, long offset) throws IOException {
	    ResourceProducer producer = null;
	    EnvelopedResourceFile ef = new EnvelopedResourceFile(null);
	    ef.setStrict(STRICT_GZ);
	    ARCFile af = new ARCFile();
	    af.setStrict(STRICT_GZ);
	    WARCFile wf = new WARCFile();
	    wf.setStrict(STRICT_GZ);
		File file = new File(path);

	    if(path.startsWith("hdfs://")) {
	    	String name = file.getName();
	    	Path fsPath = new Path(path);
	    	FileSystem fs = fsPath.getFileSystem(new Configuration());
	    	FSDataInputStream fsdis = fs.open(fsPath);
	   
	    	if(path.endsWith(".warc.gz") || path.endsWith(".wat.gz")) {
				producer = wf.getGZResourceProducer(fsdis,name,offset);
			} else if(path.endsWith(".arc.gz")) {
				producer = af.getGZResourceProducer(fsdis,name,offset);
			} else if(path.endsWith(".arc")) {
				producer = af.getResourceProducer(fsdis,name,offset);
			} else if(path.endsWith(".warc") || path.endsWith(".wat")) {
				producer = wf.getResourceProducer(fsdis,name,offset);
			} else if(path.endsWith(".gz")) {
				producer = ef.getGZResourceProducer(fsdis,name,offset);
			}

	    } else if(path.startsWith("http://")) {
	    	String name = file.getName();
	    	URL url = new URL(path);

	    	if(path.endsWith(".warc.gz") || path.endsWith(".wat.gz")) {
				producer = wf.getGZResourceProducer(url,name,offset);
			} else if(path.endsWith(".arc.gz")) {
				producer = af.getGZResourceProducer(url,name,offset);
			} else if(path.endsWith(".arc")) {
				producer = af.getResourceProducer(url,name,offset);
			} else if(path.endsWith(".warc") || path.endsWith(".wat")) {
				producer = wf.getResourceProducer(url,name,offset);
			} else if(path.endsWith(".gz")) {
				producer = ef.getGZResourceProducer(url,name,offset);
			}

	    } else {

	    	if(!(file.exists() && file.canRead())) {
				System.err.println(path + " is not a readable file.");
				return null;
			}
			if(path.endsWith(".warc.gz") || path.endsWith(".wat.gz")) {
				producer = wf.getGZResourceProducer(file,offset);
			} else if(path.endsWith(".arc.gz")) {
				producer = af.getGZResourceProducer(file,offset);
			} else if(path.endsWith(".arc")) {
				producer = af.getResourceProducer(file,offset);
			} else if(path.endsWith(".warc") || path.endsWith(".wat")) {
				producer = wf.getResourceProducer(file,offset);
			} else if(path.endsWith(".gz")) {
				producer = ef.getGZResourceProducer(file,offset);
			}
	    }
	    return producer;
	}
}
