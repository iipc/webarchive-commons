package org.archive.hadoop.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.pig.PigException;
import org.apache.pig.StoreFunc;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.archive.hadoop.mapreduce.AlphaPartitioner;
import org.archive.hadoop.mapreduce.ZipNumOutputFormat;
import org.archive.hadoop.mapreduce.ZipNumRecordWriter;

public class ZipNumStorage extends StoreFunc {

	ZipNumRecordWriter writer;
	private static int DEFAULT_COUNT = 5000;
	private int count;
	public ZipNumStorage() {
		this(DEFAULT_COUNT);
	}
	public ZipNumStorage(String count) {
		try {
			this.count = Integer.parseInt(count);
		} catch(NumberFormatException e) {
			this.count = DEFAULT_COUNT;
		}
	}
	public ZipNumStorage(int count) {
		this.count = count;
	}
	@SuppressWarnings("rawtypes")
	@Override
	public OutputFormat getOutputFormat() throws IOException {
		return new ZipNumOutputFormat(count);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void prepareToWrite(RecordWriter writer) throws IOException {
		this.writer = (ZipNumRecordWriter) writer;
	}

	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static int BUFFER_SIZE = 1024 * 1024 * 5;
	private byte fieldDel = '\t';
	ByteArrayOutputStream mOut = new ByteArrayOutputStream(BUFFER_SIZE);

	@Override
	public void putNext(Tuple tuple) throws IOException {
		int sz = tuple.size();
		for (int i = 0; i < sz; i++) {
			Object field;
			try {
				field = tuple.get(i);
			} catch (ExecException ee) {
				throw ee;
			}

			putField(field);

			if (i != sz - 1) {
				mOut.write(fieldDel);
			}
		}
		writer.write(mOut.toByteArray());
		mOut.reset();
	}

	@SuppressWarnings("unchecked")
	private void putField(Object field) throws IOException {
		// string constants for each delimiter
		String tupleBeginDelim = "(";
		String tupleEndDelim = ")";
		String bagBeginDelim = "{";
		String bagEndDelim = "}";
		String mapBeginDelim = "[";
		String mapEndDelim = "]";
		String fieldDelim = ",";
		String mapKeyValueDelim = "#";

		switch (DataType.findType(field)) {
		case DataType.NULL:
			break; // just leave it empty

		case DataType.BOOLEAN:
			mOut.write(((Boolean) field).toString().getBytes(UTF8));
			break;

		case DataType.INTEGER:
			mOut.write(((Integer) field).toString().getBytes(UTF8));
			break;

		case DataType.LONG:
			mOut.write(((Long) field).toString().getBytes(UTF8));
			break;

		case DataType.FLOAT:
			mOut.write(((Float) field).toString().getBytes(UTF8));
			break;

		case DataType.DOUBLE:
			mOut.write(((Double) field).toString().getBytes(UTF8));
			break;

		case DataType.BYTEARRAY: {
			byte[] b = ((DataByteArray) field).get();
			mOut.write(b, 0, b.length);
			break;
		}

		case DataType.CHARARRAY:
			// oddly enough, writeBytes writes a string
			mOut.write(((String) field).getBytes(UTF8));
			break;

		case DataType.MAP:
			boolean mapHasNext = false;
			Map<String, Object> m = (Map<String, Object>) field;
			mOut.write(mapBeginDelim.getBytes(UTF8));
			for (Map.Entry<String, Object> e : m.entrySet()) {
				if (mapHasNext) {
					mOut.write(fieldDelim.getBytes(UTF8));
				} else {
					mapHasNext = true;
				}
				putField(e.getKey());
				mOut.write(mapKeyValueDelim.getBytes(UTF8));
				putField(e.getValue());
			}
			mOut.write(mapEndDelim.getBytes(UTF8));
			break;

		case DataType.TUPLE:
			boolean tupleHasNext = false;
			Tuple t = (Tuple) field;
			mOut.write(tupleBeginDelim.getBytes(UTF8));
			for (int i = 0; i < t.size(); ++i) {
				if (tupleHasNext) {
					mOut.write(fieldDelim.getBytes(UTF8));
				} else {
					tupleHasNext = true;
				}
				try {
					putField(t.get(i));
				} catch (ExecException ee) {
					throw ee;
				}
			}
			mOut.write(tupleEndDelim.getBytes(UTF8));
			break;

		case DataType.BAG:
			boolean bagHasNext = false;
			mOut.write(bagBeginDelim.getBytes(UTF8));
			Iterator<Tuple> tupleIter = ((DataBag) field).iterator();
			while (tupleIter.hasNext()) {
				if (bagHasNext) {
					mOut.write(fieldDelim.getBytes(UTF8));
				} else {
					bagHasNext = true;
				}
				putField((Object) tupleIter.next());
			}
			mOut.write(bagEndDelim.getBytes(UTF8));
			break;

		default: {
			int errCode = 2108;
			String msg = "Could not determine data type of field: " + field;
			throw new ExecException(msg, errCode, PigException.BUG);
		}

		}
	}

	@Override
	public void setStoreLocation(String path, Job job) throws IOException {
		System.err.format("Set partitioner class\n");
		job.getConfiguration().set("mapreduce.partitioner.class", AlphaPartitioner.class.getCanonicalName());
//		job.getConfiguration().set("mapreduce.partitioner.class", org.archive.hadoop.AlphaPartitioner.class.getCanonicalName());
//		job.setPartitionerClass(AlphaPartitioner.class);
        job.getConfiguration().set("mapred.textoutputformat.separator", "");
        FileOutputFormat.setOutputPath(job, new Path(path));
	}

}
