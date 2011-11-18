package org.archive.hadoop.cdx;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.archive.util.iterator.AbstractPeekableIterator;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.zip.OpenJDK7GZIPInputStream;

public class ZipNumBlockIterator {
	private byte[] compressed;
	public ZipNumBlockIterator(byte[] compressed) {
		this.compressed = compressed;
	}
	public CloseableIterator<String> iterator() throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
		OpenJDK7GZIPInputStream gzis = new OpenJDK7GZIPInputStream(bais);
		InputStreamReader isr = new InputStreamReader(gzis);
		BufferedReader br = new BufferedReader(isr);
		return AbstractPeekableIterator.wrapReader(br);
	}
}
