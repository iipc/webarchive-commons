package org.archive.util.binsearch;

import java.io.IOException;

import org.archive.util.io.RuntimeIOException;
import org.archive.util.iterator.AbstractPeekableIterator;

public class SeekableLineReaderIterator extends AbstractPeekableIterator<String> {
	protected SeekableLineReader slr;
	protected boolean propagateException;
	
	public SeekableLineReaderIterator(SeekableLineReader slr) {
		this(slr, true);
	}
	
	public SeekableLineReaderIterator(SeekableLineReader slr, boolean propagateException) {
		this.slr = slr;
		this.propagateException = propagateException;
	}
	
	@Override
	public String getNextInner() {
		String next = null;
		if (slr != null) {
			try {
				next = slr.readLine();
			} catch (IOException e) {
				if (propagateException) {
					throw new RuntimeIOException(e.toString());
				}
			}
		}
		return next;
	}
	@Override
	public void close() throws IOException {
		if (slr != null) {
			slr.close();
		}
	}
}