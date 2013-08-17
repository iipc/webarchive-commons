package org.archive.util.binsearch;

import java.io.IOException;

import org.archive.util.iterator.AbstractPeekableIterator;

public class SeekableLineReaderIterator extends AbstractPeekableIterator<String> {
	protected SeekableLineReader slr;
	
	public SeekableLineReaderIterator(SeekableLineReader slr) {
		this.slr = slr;
	}
	
	@Override
	public String getNextInner() {
		String next = null;
		if (slr != null) {
			try {
				next = slr.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return next;
	}
	@Override
	public void close() throws IOException {
		slr.close();
	}
}