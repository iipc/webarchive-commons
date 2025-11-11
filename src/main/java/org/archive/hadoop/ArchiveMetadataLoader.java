package org.archive.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.pig.FileInputLoadFunc;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.archive.resource.MetaData;

/**
 * Pig Storage Loader for Archive meta data.
 * 
 * @author brad
 *
 */
public class ArchiveMetadataLoader extends FileInputLoadFunc {
	private final static Logger LOG = 
		Logger.getLogger(ArchiveMetadataLoader.class.getName());
	
	ResourceRecordReader reader;
	protected TupleFactory mTupleFactory = TupleFactory.getInstance();
	private ArrayList<Object> mProtoTuple = null;
	private ResourceContext key;
	private MetaData value;
	public ArchiveMetadataLoader() {
		mProtoTuple = new ArrayList<Object>(3);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public InputFormat getInputFormat() throws IOException {
		return new ResourceInputFormat();
	}

	@Override
	public Tuple getNext() throws IOException {
		boolean next = false;
		try {
			next = reader.nextKeyValue();
		} catch (InterruptedException e) {
			// is this needed and the right way?
			throw new IOException(e);
		}

		if (!next)
			return null;

		try {
			key = reader.getCurrentKey();
			LOG.info(String.format(Locale.ROOT, "Loaded key-offset %d\n", key.offset));
			value = reader.getCurrentValue();
		} catch (InterruptedException e) {
			// is this needed and the right way?
			throw new IOException(e);
		}
		mProtoTuple.add(key.name);
		mProtoTuple.add(key.offset);
		mProtoTuple.add(value.getTopMetaData().toString());
		Tuple t = mTupleFactory.newTuple(mProtoTuple);
		mProtoTuple.clear();
		return t;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void prepareToRead(RecordReader reader, PigSplit arg1)
			throws IOException {
		this.reader = (ResourceRecordReader) reader;
//		FileSplit fSplit = (FileSplit) arg1.getWrappedSplit();
//		System.err.format("Prepare to read(%s) (%d-%d)\n",
//				fSplit.getPath().toUri().toASCIIString(),
//				fSplit.getStart(),fSplit.getLength());
	}

	@Override
	public void setLocation(String location, Job job) throws IOException {
	    FileInputFormat.setInputPaths(job, location);
	}
}
