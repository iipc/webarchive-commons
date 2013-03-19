package org.archive.util.iterator;

import java.io.IOException;
import java.util.Iterator;

public class CloseableIteratorUtil {
	public static <E> void attemptClose(Iterator<E> i) throws IOException {
		if(i instanceof CloseableIterator) {
			((CloseableIterator<E>) i).close();
		}
	}
}
