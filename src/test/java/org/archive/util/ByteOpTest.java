package org.archive.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.google.common.io.LittleEndianDataOutputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteOpTest {

	@Test
	public void testReadShort() throws IOException {
		byte a[] = new byte[]{0,1,2,3};
		ByteArrayInputStream bais = new ByteArrayInputStream(a);
		int bos = ByteOp.readShort(bais);
		System.out.format("BO.Read short(%d)\n", bos);
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(a));
		int disv = dis.readUnsignedShort();
		System.out.format("DI.Read short(%d)\n", disv);
		for(int i = 0; i < 256 * 256; i++) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(2);
			LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(baos);
			dos.writeShort(i);
			ByteArrayInputStream bais2 = new ByteArrayInputStream(baos.toByteArray());
			int gotI = ByteOp.readShort(bais2);
			assertEquals(i, gotI);
		}
	}

	@Test
	public void testAppend() {
		byte a[] = new byte[]{1};
		byte b[] = new byte[]{2};
		byte n[] = ByteOp.append(a,b);
		assertEquals(2,n.length);
		assertEquals(1,n[0]);
		assertEquals(2,n[1]);

		byte a2[] = new byte[]{1,2,3,4};
		byte b2[] = new byte[]{5,6,7,8};
		byte n2[] = ByteOp.append(a2,b2);
		assertEquals(8,n2.length);
		assertEquals(1,n2[0]);
		assertEquals(2,n2[1]);
		assertEquals(5,n2[4]);
		
	}
}
