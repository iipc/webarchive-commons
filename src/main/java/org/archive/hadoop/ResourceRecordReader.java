package org.archive.hadoop;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.archive.extract.ExtractingResourceFactoryMapper;
import org.archive.extract.ExtractingResourceProducer;
import org.archive.extract.ResourceFactoryMapper;
import org.archive.format.gzip.GZIPMemberSeries;
import org.archive.resource.MetaData;
import org.archive.resource.Resource;
import org.archive.resource.ResourceParseException;
import org.archive.resource.ResourceProducer;
import org.archive.resource.TransformingResourceProducer;
import org.archive.resource.arc.ARCResourceFactory;
import org.archive.resource.gzip.GZIPResourceContainer;
import org.archive.resource.warc.WARCResourceFactory;
import org.archive.streamcontext.HDFSStream;
import org.archive.streamcontext.Stream;
import org.archive.util.StreamCopy;

public class ResourceRecordReader extends RecordReader<ResourceContext, MetaData>{
	private final static Logger LOG =
		Logger.getLogger(ResourceRecordReader.class.getName());

	WARCResourceFactory wf = new WARCResourceFactory();
	ARCResourceFactory af = new ARCResourceFactory();
	Stream stream;
	GZIPMemberSeries series;
	private ResourceProducer producer;
//	private ResourceExtractor extractor;
	private String name;
	private long startOffset;
	private long length;
	
	private ResourceContext cachedK;
	private MetaData cachedV;
	
	@Override
	public void close() throws IOException {
		producer.close();
	}

	@Override
	public ResourceContext getCurrentKey() throws IOException, InterruptedException {
		return cachedK;
	}

	@Override
	public MetaData getCurrentValue() throws IOException, InterruptedException {
		return cachedV;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		if(length == 0) {
			return 0;
		}
		long curOffset = stream.getOffset();
		float amtDone = curOffset - startOffset;
		float flen = (float) length;
		return amtDone / flen;
	}

	@Override
	public void initialize(InputSplit inputSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		if(inputSplit instanceof FileSplit) {
			FileSplit fs = (FileSplit) inputSplit;
			Path fsPath = fs.getPath();
	    	FileSystem fSys = fsPath.getFileSystem(context.getConfiguration());
	    	FSDataInputStream fsdis = fSys.open(fsPath);
	    	String path = fsPath.getName();
	    	name = fsPath.getName();
	    	stream = new HDFSStream(fsdis);
	    	startOffset = fs.getStart();
			length = fs.getLength();
			long endOffset = startOffset + length;
			stream.setOffset(startOffset);	    	
	    	series = new GZIPMemberSeries(stream, name, startOffset);
			GZIPResourceContainer prod = 
				new GZIPResourceContainer(series,endOffset);
			ResourceProducer envelope;
	    	if(path.endsWith(".warc.gz") || path.endsWith(".wat.gz")) {
	    		envelope = new TransformingResourceProducer(prod,wf);
			} else if(path.endsWith(".arc.gz")) {
				envelope = new TransformingResourceProducer(prod,af);
			} else {
				throw new IOException("arguments must be arc.gz or warc.gz");
			}
	    	ResourceFactoryMapper mapper = new ExtractingResourceFactoryMapper();
	    	producer = new ExtractingResourceProducer(envelope, mapper);

		} else {
			throw new IOException("Need FileSplit input...");
		}
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		// TODO: loop while getting resourceparseexceptions:
		try {
			Resource r = producer.getNext();
			if(r != null) {

				StreamCopy.readToEOF(r.getInputStream());
				LOG.info(String.format(Locale.ROOT, "Extracted offset %d\n",
						series.getCurrentMemberStartOffset()));
				cachedK = new ResourceContext(name, 
						series.getCurrentMemberStartOffset());
				cachedV = r.getMetaData().getTopMetaData();
				return true;
			}
		} catch (ResourceParseException e) {
			e.printStackTrace();
			throw new IOException(
					String.format(Locale.ROOT, "ResourceParseException at(%s)(%d)",
							name,series.getCurrentMemberStartOffset()),
					e);
		}
		return false;
	}

}
