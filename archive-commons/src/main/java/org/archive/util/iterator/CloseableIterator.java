package org.archive.util.iterator;

import java.io.IOException;
import java.util.Iterator;

public interface CloseableIterator<E> extends Iterator<E> {
	public void close() throws IOException;
}
