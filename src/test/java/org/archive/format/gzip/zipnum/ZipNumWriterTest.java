package org.archive.format.gzip.zipnum;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Locale;

import org.archive.format.gzip.GZIPMemberSeries;
import org.archive.format.gzip.GZIPSeriesMember;
import org.archive.streamcontext.SimpleStream;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZipNumWriterTest {

	@Test
	public void testAddRecord() throws IOException {
        File main = File.createTempFile("test-znw",".main");
		File summ = File.createTempFile("test-znw",".summ");
		main.deleteOnExit();
		summ.deleteOnExit();
		System.out.format(Locale.ROOT, "Summ: %s\n", summ.getAbsolutePath());
		int limit = 10;
		ZipNumWriter znw = new ZipNumWriter(new FileOutputStream(main,false), 
				new FileOutputStream(summ,false), limit);
		for(int i = 0; i < 1000; i++) {
			znw.addRecord(String.format(Locale.ROOT,"%06d\n",i).getBytes(UTF_8));
		}
		znw.close();
		InputStreamReader isr =
			new InputStreamReader(new FileInputStream(summ), UTF_8);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		int count = 0;
		while(true) {
			line = br.readLine();
			if(line == null) {
				break;
			}
			String parts[] = line.split("\t");
			FileChannel fc = new RandomAccessFile(main, "r").getChannel();
			long offset = Long.parseLong(parts[0]);
			int len = Integer.parseInt(parts[1]);
			byte[] gz = new byte[len];
			ByteBuffer bb = ByteBuffer.wrap(gz);
			int amt = fc.read(bb, offset);
			assertEquals(amt,len);
			ByteArrayInputStream bais = new ByteArrayInputStream(gz);
			GZIPMemberSeries gzms = new GZIPMemberSeries(new SimpleStream(bais));
			GZIPSeriesMember m = gzms.getNextMember();
			m.skipMember();
			gzms.close();
			count++;
		}
		assertEquals(count,100);
		br.close();
		
	}
	
}
