package org.archive.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.google.common.io.ByteStreams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtils {
	public static void dumpMatch(String context, List<List<String>> res) {

		System.out.format("Context(%s) Found (%d) matches\n", context, res.size());
		for(List<String> r : res) {
			System.out.format("Match(%s)\n", StringParse.join(r));
		}
		
	}
	public static void assertLoLMatches(String[][] want, List<List<String>> got) {
		assertEquals(want.length,got.size());
		for(int i = 0; i < want.length; i++) {
			String [] wantSub = want[i];
			List<String> gotSub = got.get(i);
			assertEquals(wantSub.length,gotSub.size());
			for(int j = 0; j < wantSub.length; j++) {
				assertEquals(wantSub[j],gotSub.get(j));
			}
		}
	}
	public static void assertStreamEquals(InputStream is, byte[] b) throws IOException {
		byte[] got = ByteStreams.toByteArray(is);
		assertEquals(got.length,b.length);
		assertTrue(ByteOp.cmp(got,b));
	}
}
