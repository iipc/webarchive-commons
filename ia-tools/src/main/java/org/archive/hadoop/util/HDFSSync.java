package org.archive.hadoop.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.archive.util.StreamCopy;

public class HDFSSync implements Tool {
	public final static String TOOL_NAME = "hdfs-sync";
	public static final String TOOL_DESCRIPTION = 
		"A tool for copying files into and out of HDFS, in a semi-restartable fashion";

	private Configuration conf;

	public void setConf(Configuration conf) {
		this.conf = conf;
	}
	public Configuration getConf() {
		return conf;
	}
	private static int USAGE(int code) {
		System.err.println("USAGE");
		System.err.println(TOOL_NAME);
		System.err.println("\t\tread SRC TGT tuples from STDIN, where one of SRC or TGT");
		System.err.println("\t\tis an hdfs:// URL, and the other is a local path");
		System.err.println("\t\tWill only perform the copy if TGT does not already exist");
		System.err.println("\t\tThis is NOT rsync - no comparison, checksumming, or even file length checking happens.");
		
		return code;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new HDFSSync(), args);
		System.exit(res);
	}

	public int run(String[] args) throws IOException {
		if(args.length != 0) {
		   return USAGE(1);
		}
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		while(true) {
			String line = br.readLine();
			if(line == null) {
				break;
			}
			String parts[] = line.split("\\s");
			if(parts.length != 2) {
				System.err.println("Bad input line(" + line + ")");
				return 1;
			}
			String src = parts[0];
			String tgt = parts[1];
		    if(src.startsWith("hdfs://")) {
				if(syncFromHDFS(src,tgt)) {
					System.out.format("Copied\t%s\t%s\n", src,tgt);
				}
		    } else if(tgt.startsWith("hdfs://")) {
				if(syncToHDFS(src,tgt)) {
					System.out.format("Copied\t%s\t%s\n", src,tgt);
				}
		    } else {
				System.err.println("Bad input tgt not '/' (" + line + ")");
				return 1;		    	
		    }
		}
		return 0;
	}
	private static boolean syncToHDFS(String src, String tgt) throws IOException {
	    if(!tgt.startsWith("hdfs://")) {
			System.err.println("Bad input tgt not 'hdfs://' (" + src + ","
					+ tgt + ")");
			System.exit(1);
	    }
	    if(!src.startsWith("/")) {
			System.err.println("Bad input src not '/' (" + src + "," 
					+ tgt + ")");
			System.exit(1);		    	
	    }
    	File srcFile = new File(src);
    	Path fsPath = new Path(tgt);
    	Path fsPathTmp = new Path(tgt+".TMP");

    	FileSystem fs = fsPath.getFileSystem(new Configuration());
    	if(fs.isFile(fsPath)) {
    		System.err.format("Target-Exists\t%s\t%s\n", src,tgt);
    		return false;    		
    	}

    	if(!srcFile.isFile()) {
			System.err.println("Bad input src not a File (" + src + "," 
					+ tgt + ")");
			System.exit(1);
    	}
    	FileInputStream fis = new FileInputStream(srcFile);
    	FSDataOutputStream fsOut = fs.create(fsPathTmp, true);
    	StreamCopy.copy(fis, fsOut);
    	fis.close();
    	fsOut.close();
    	if(fs.rename(fsPathTmp, fsPath)) {
    		return true;
    	}
    	System.err.format("FAILED to mv(%s) to (%s)\n",
    		fsPathTmp.toUri().toASCIIString(),
    		fsPath.toUri().toASCIIString());
    	return false;
	}
	
	private static boolean syncFromHDFS(String src, String tgt) throws IOException {
	    if(!src.startsWith("hdfs://")) {
			System.err.println("Bad input src not 'hdfs://' (" + src + ","
					+ tgt + ")");
			System.exit(1);
	    }
	    if(!tgt.startsWith("/")) {
			System.err.println("Bad input tgt not '/' (" + src + "," 
					+ tgt + ")");
			System.exit(1);		    	
	    }
    	File tgtFile = new File(tgt);
    	File tmpTgtFile = new File(tgt+".TMP");
    	if(tgtFile.exists()) {
    		System.err.format("Target-Exists\t%s\t%s\n", src,tgt);
    		return false;
    	}    	
    	if(tmpTgtFile.exists()) {
    		if(!tmpTgtFile.delete()) {
    			throw new IOException("FAILED to unlink(" + tmpTgtFile.getAbsolutePath()+")");
    		}
    	}
    	Path fsPath = new Path(src);
    	FileSystem fs = fsPath.getFileSystem(new Configuration());
    	
    	FSDataInputStream fsdis = null;
    	try {
    		fsdis = fs.open(fsPath);
    	} catch (IOException e) {
    		System.err.format("FAILD open(%s): (%s)\n",src,e.getMessage());
    		return false;
    	}
    	FileOutputStream fos = new FileOutputStream(tmpTgtFile);
    	StreamCopy.copy(fsdis, fos);
    	fos.close();
    	fsdis.close();
    	if(tmpTgtFile.renameTo(tgtFile)) {
    		return true;
    	}
    	System.err.println("FAILED to mv("+tmpTgtFile.getAbsolutePath()+") to ("+tgtFile.getAbsolutePath()+")");
    	return false;
	}
}
