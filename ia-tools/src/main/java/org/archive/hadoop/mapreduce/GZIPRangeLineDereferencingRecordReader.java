package org.archive.hadoop.mapreduce;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.archive.hadoop.cdx.HDFSBlockLoader;

public class GZIPRangeLineDereferencingRecordReader extends LineDereferencingRecordReader{
	
	private static final String SKIP_BAD_GZ_RANGES = "gzip.range.skipbad";
	
	public static void setSkipBadGZIPRanges(Configuration conf, boolean skip) {
		conf.setBoolean(SKIP_BAD_GZ_RANGES, skip);
	}
	public static boolean getSkipBadGZIPRanges(Configuration conf) {
		return conf.getBoolean(SKIP_BAD_GZ_RANGES,false);
	}

	String curInputLine = null;
	FSDataInputStream fsdis = null;
	long curStart = 0;
	byte[] buffer = null;
	private boolean skipBad = false;
	HDFSBlockLoader loader = null;
	
	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
	throws IOException, InterruptedException {
		
		Configuration conf = context.getConfiguration();
		FileSplit fileSplit = (FileSplit) split;
		loader = new HDFSBlockLoader(fileSplit.getPath().getFileSystem(conf));
		skipBad = getSkipBadGZIPRanges(conf);
		super.initialize(split, context);
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if(key == null) {
			key = new Text();
		}
		if(value == null) {
			value = new Text();
		}
		while(true) {
			if(curReader == null) {
				// are there more?
				if(internal.nextKeyValue()) {
					progress = internal.getProgress();
					curInputLine = internal.getCurrentValue().toString();
					String[] parts = curInputLine.split("\\s");
					if(parts.length != 3) {
						throw new IOException("Bad format line(" + curInputLine +")");
					}
					String newFile = parts[0];
					if(fsdis != null) {
						if(!newFile.equals(curFile)) {
							// close old and open new, otherwise we can just
							// do another read on the current one:
							fsdis.close();
							curFile = newFile;
							Path path = new Path(curFile);
							fsdis = fileSystem.open(path);
						}
					} else {
						curFile = newFile;
						Path path = new Path(curFile);
						fsdis = fileSystem.open(path);						
					}
					curFile = parts[0];
					curStart = Long.parseLong(parts[1]);
					int length = Integer.parseInt(parts[2]);
					if(buffer == null) {
						buffer = new byte[length];
					} else if (buffer.length < length) {
						buffer = new byte[length];
					}
					InputStream is = null;
					// the whole chunk is now in buffer:
					try {
						fsdis.readFully(curStart,buffer,0,length);
						is = new GZIPInputStream(new ByteArrayInputStream(buffer,0,length));
					} catch (IOException e) {
						if(skipBad) {
							System.err.format("GZIP-BLOCK-ERROR\t%s\t%d\t%s\t%s\n",
									curFile,curStart,e.getClass().toString(),
									e.getMessage());
							curReader = null;

							continue; // while(true) loop
							
						} else {
							throw new IOException(String.format("%s:%d - (%s) %s",
								curFile,curStart,
								e.getClass().toString(),e.getMessage()));
						}
						
					}
					curReader = new BufferedReader(new InputStreamReader(is,UTF8));
					curLine = 0;

				} else {
					// all done:
					return false;
				}
			}
			// try to read another line:
			String nextLine = null;
			try {
				nextLine = curReader.readLine();
			} catch(IOException e) {
				if(skipBad) {
					System.err.format("GZIP-BLOCK-ERROR\t%s\t%d\t%s\t%s\n",
							curFile,curStart,e.getClass().toString(),
							e.getMessage());
					nextLine = null;
					
				} else {
					throw new IOException(String.format("%s:%d - (%s) %s",
						curFile,curStart,
						e.getClass().toString(),e.getMessage()));
				}
			}
			if(nextLine != null) {
				key.set(curFile+":"+curStart+":"+curLine);
				value.set(nextLine);
				curLine++;
				return true;
			}
			curReader.close();
			curReader = null;
		}
	}

}
