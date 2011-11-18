package org.archive.hadoop.storage;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.hadoop.io.Text;
import org.archive.hadoop.mapreduce.ZipNumRecordWriter;
import org.archive.util.ByteOp;
import org.archive.util.IAUtils;

import junit.framework.TestCase;

public class ZipnumRecordWriterTest extends TestCase {
	public void testCreate() throws IOException, InterruptedException {
		File m = new File("/tmp/main.gz");
		File s = new File("/tmp/summ.txt");
		DataOutputStream outM = new DataOutputStream(new FileOutputStream(m));
		DataOutputStream outS = new DataOutputStream(new FileOutputStream(s));
		
		ZipNumRecordWriter w = new ZipNumRecordWriter(20, outM, outS);
		Text key = new Text();
		Text val = new Text();
		for(int i = 0; i < 200; i++) {
			key.set(String.format("Line number %06d",i).getBytes(IAUtils.UTF8));
			val.set(String.format("Value %06d",i).getBytes(IAUtils.UTF8));
			w.write(key, val);
		}
		w.close(null);
	}
}
